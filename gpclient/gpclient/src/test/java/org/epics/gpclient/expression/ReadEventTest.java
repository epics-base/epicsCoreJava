/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class ReadEventTest {
    
    @Test
    public void addEvent1() {
        assertThat(ReadEvent.connectionEvent().addEvent(ReadEvent.valueEvent()), equalTo(ReadEvent.connectionValueEvent()));
        assertThat(ReadEvent.valueEvent().addEvent(ReadEvent.connectionEvent()), not(equalTo(ReadEvent.connectionValueEvent())));
        assertThat(ReadEvent.valueEvent().addEvent(ReadEvent.connectionEvent()).getType(), equalTo(Arrays.asList(ReadEvent.Type.VALUE, ReadEvent.Type.READ_CONNECTION)));
    }
}
