package org.epics.pvaccess.impl.remote.io;

import java.nio.channels.SelectableChannel;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface Poller {

	void add(SelectableChannel channel, PollEvents handler, int ops);
	void modify(SelectionKey key, int ops);
	void remove(SelectionKey key);

	void pollOne() throws IOException;

	/*
	int addTimer(PollEvents processor, long timeout);
	void cancelTimer(int handle);
	*/

}
