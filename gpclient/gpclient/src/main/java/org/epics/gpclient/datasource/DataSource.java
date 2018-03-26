/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.gpclient.ReadCollector;
import static org.epics.util.concurrent.Executors.namedPool;
import org.epics.util.concurrent.ProcessingQueue;

/**
 * A source for data that is going to be processed by the PVManager.
 * PVManager can work with more than one source at a time. Support
 * for each different source can be added by external libraries.
 * <p>
 * To implement a datasource, one has to implement the {@link #createChannel(java.lang.String) }
 * method, and the requested will be forwarded to the channel accordingly.
 * The channels are automatically cached and reused. The name under which
 * the channels are looked up in the cache or registered in the cache is configurable.
 * <p>
 * Channel handlers can be implemented from scratch, or one can use the {@link MultiplexedChannelHandler}
 * for handlers that want to open a single connection which is going to be
 * shared by all readers and writers.
 *
 * @author carcassi
 */
public abstract class DataSource {

    private static final Logger log = Logger.getLogger(DataSource.class.getName());

    private final boolean writeable;

    /**
     * Returns true whether the channels of this data source can be
     * written to.
     * 
     * @return true if data source accept write operations
     */
    public boolean isWriteable() {
        return writeable;
    }
    
    /**
     * Creates a new data source.
     * 
     * @param writeable whether the data source implements write operations
     */
    public DataSource(boolean writeable) {
        this.writeable = writeable;
    }

    // Keeps track of the currently created channels
    private Map<String, ChannelHandler> usedChannels = new ConcurrentHashMap<String, ChannelHandler>();

    /**
     * Returns a channel from the given name, either cached or it
     * will create it.
     * 
     * @param channelName name of a channel
     * @return a new or cached handler
     */
    ChannelHandler channel(String channelName) {
        ChannelHandler channel = usedChannels.get(channelHandlerLookupName(channelName));
        if (channel == null) {
            channel = createChannel(channelName);
            if (channel == null)
                return null;
            usedChannels.put(channelHandlerRegisterName(channelName, channel), channel);
        }
        return channel;
    }
    
    /**
     * Returns the lookup name to use to find the channel handler in
     * the cache. By default, it returns the channel name itself.
     * If a datasource needs multiple different channel names to
     * be the same channel handler (e.g. parts of the channel name
     * are initialization parameters) then it can override this method
     * to change the lookup.
     * 
     * @param channelName the channel name
     * @return the channel handler to look up in the cache
     */
    protected String channelHandlerLookupName(String channelName) {
        return channelName;
    }
    
    /**
     * Returns the name the given handler should be registered as.
     * By default, it returns the lookup name, so that lookup and
     * registration in the cache are consistent. If a datasource
     * needs multiple different channel names to be the same 
     * channel handler (e.g. parts of the channel name are read/write
     * parameters) then it can override this method to change the
     * registration.
     * 
     * @param channelName the name under which the ChannelHandler was created
     * @param handler the handler to register
     * @return the name under which to register in the cache
     */
    protected String channelHandlerRegisterName(String channelName, ChannelHandler handler) {
        return channelHandlerLookupName(channelName);
    }

    /**
     * Creates a channel handler for the given name. In the simplest
     * case, this is the only method a data source needs to implement.
     * 
     * @param channelName the name for a new channel
     * @return a new handler
     */
    protected abstract ChannelHandler createChannel(String channelName);

    // The executor used by the data source to perform asynchronous operations,
    // such as connections and writes. We use one extra thread for each datasource,
    // mainly to be able to shut it down during cleanup
    private final ExecutorService exec = Executors.newSingleThreadExecutor(namedPool("PVMgr " + getClass().getSimpleName() + " Worker "));
    
    // Keeps track of the recipes that were opened with
    // this data source.
    private final Set<ReadSubscription> readRecipes = Collections.synchronizedSet(new HashSet<ReadSubscription>());
    private final Set<WriteSubscription> writeRecipes = Collections.synchronizedSet(new HashSet<WriteSubscription>());

    private final ProcessingQueue<ReadSubscription> connectReadQueue = new ProcessingQueue<>(exec, new Consumer<List<ReadSubscription>>() {
        @Override
        public void accept(List<ReadSubscription> list) {
            for (ReadSubscription channelReadRecipe : list) {
                try {
                    readRecipes.add(channelReadRecipe);
                    String channelName = channelReadRecipe.getChannelName();
                    ChannelHandler channelHandler = channel(channelName);
                    if (channelHandler == null) {
                        throw new RuntimeException("Channel named '" + channelName + "' not found");
                    }
                    channelHandler.addReader(channelReadRecipe.getCollector());
                } catch(Exception ex) {
                    // If an error happens while adding the read subscription,
                    // notify the appropriate handler
                    channelReadRecipe.getCollector().notifyError(ex);
                }
            }
        }
    });
    
    public void connectRead(final ReadSubscription readRecipe) {
        connectReadQueue.submit(readRecipe);
    }

    private final ProcessingQueue<ReadSubscription> disconnectReadQueue = new ProcessingQueue<>(exec, new Consumer<List<ReadSubscription>>() {
        @Override
        public void accept(List<ReadSubscription> list) {
            for (ReadSubscription channelReadRecipe : list) {
                try {
                    if (!readRecipes.remove(channelReadRecipe)) {
                        log.log(Level.WARNING, "ChannelReadRecipe {0} was disconnected but was never connected. Ignoring it.", channelReadRecipe);
                    } else {
                        String channelName = channelReadRecipe.getChannelName();
                        ChannelHandler channelHandler = channel(channelName);
                        // If the channel is not found, it means it was not found during
                        // connection and a proper notification was sent then. Silently
                        // ignore it.
                        if (channelHandler != null) {
                            channelHandler.removeReader(channelReadRecipe.getCollector());
                        }
                    }
                } catch(Exception ex) {
                    // If an error happens while adding the read subscription,
                    // notify the appropriate handler
                    channelReadRecipe.getCollector().notifyError(ex);
                }
            }
        }
    });
    
    public void disconnectRead(final ReadSubscription readRecipe) {
        disconnectReadQueue.submit(readRecipe);
    }

    private final ProcessingQueue<WriteSubscription> connectWriteQueue = new ProcessingQueue<>(exec, new Consumer<List<WriteSubscription>>() {
        @Override
        public void accept(List<WriteSubscription> list) {
            for (WriteSubscription channelWriteRecipe : list) {
                try {
                    writeRecipes.add(channelWriteRecipe);
                    String channelName = channelWriteRecipe.getChannelName();
                    ChannelHandler channelHandler = channel(channelName);
                    if (channelHandler == null) {
                        throw new RuntimeException("Channel named '" + channelName + "' not found");
                    }
                    channelHandler.addWriter(channelWriteRecipe.getCollector());
                } catch(Exception ex) {
                    // If an error happens while adding the read subscription,
                    // notify the appropriate handler
                    channelWriteRecipe.getCollector().notifyError(ex);
                }
            }
        }
    });
    
    public void connectWrite(final WriteSubscription writeRecipe) {
        connectWriteQueue.submit(writeRecipe);
    }

    private final ProcessingQueue<WriteSubscription> disconnectWriteQueue = new ProcessingQueue<>(exec, new Consumer<List<WriteSubscription>>() {
        @Override
        public void accept(List<WriteSubscription> list) {
            for (WriteSubscription channelWriteRecipe : list) {
                try {
                    if (!writeRecipes.remove(channelWriteRecipe)) {
                        log.log(Level.WARNING, "ChannelWriteRecipe {0} was disconnected but was never connected. Ignoring it.", channelWriteRecipe);
                    } else {
                        String channelName = channelWriteRecipe.getChannelName();
                        ChannelHandler channelHandler = channel(channelName);
                        // If the channel is not found, it means it was not found during
                        // connection and a proper notification was sent then. Silently
                        // ignore it.
                        if (channelHandler != null) {
                            channelHandler.removeWriter(channelWriteRecipe.getCollector());
                        }
                    }
                } catch(Exception ex) {
                    // If an error happens while adding the read subscription,
                    // notify the appropriate handler
                    channelWriteRecipe.getCollector().notifyError(ex);
                }
            }
        }
    });
    
    public void disconnectWrite(final WriteSubscription writeRecipe) {
        disconnectWriteQueue.submit(writeRecipe);
    }

    /**
     * Returns the channel handlers for this data source.
     * 
     * @return an unmodifiable collection
     */
    public Map<String, ChannelHandler> getChannels() {
        return Collections.unmodifiableMap(usedChannels);
    }

    /**
     * Closes the DataSource and the resources associated with it.
     */
    public void close() {
        exec.shutdownNow();
    }
    
}
