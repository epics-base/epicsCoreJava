package org.epics.ca.impl.remote.io;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface PollEvents {
	void registeredNotify(SelectionKey key, Throwable registrationException);
	void pollNotify(SelectionKey key) throws IOException;
	//void timerNotify(int handle);
}
