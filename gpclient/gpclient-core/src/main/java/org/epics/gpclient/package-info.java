/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
/**
 * Defines the core EPICS Generic Purpose client (gpclient) API.
 * <p>
 * The generic purpose client is meant to be used to build applications that
 * are not specific to a particular deployment environment or to a particular use
 * case. It provides a system of queuing and caching that is appropriate
 * in most instances of multi-threaded applications. It isolates each reader
 * and writer and therefore protects different parts of the applications.
 * As such, it is not suitable for all purposes.
 * <p>
 * You may NOT want to use the gpclient if:
 * <ul>
 * <li>You need low level access to the EPICS communication protocol</li>
 * <li>You need to implement an application specific real-time engine</li>
 * <li>You want to lock the protocol until you have processed the data</li>
 * <li>All your reads/write are tightly coupled to each other</li>
 * </ul>
 * <p>
 * You WILL want to use the gpclient if:
 * <ul>
 * <li>You want data access and do not care about protocol details</li>
 * <li>You want something thread-safe without having to care about locks and race conditions</li>
 * <li>You want to mix data from multiple sources (i.e. other protocols, databases, files, etc.)</li>
 * <li>You are developing a user interface</li>
 * <li>You are developing an extensible application where different user will
 * want data access</li>
 * </ul>
 * <p>
 * The generic purpose client provides the following functionality:
 * <ul>
 * <li>a client API that is always thread-safe</li>
 * <li>the ability to specify on which thread or thread pool the notification
 * should happen</li>
 * <li>a pluggable way to connect to different publish/subscribe sources of
 * data (i.e. {@code DataSource})</li>
 * <li>the ability to create your own data channel implementations, even if
 * they do not follow the common patterns</li>
 * </ul>
 *
 */
package org.epics.gpclient;

