/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class PVEventTest {
    
    @Test
    public void addEvent1() {
        assertThat(PVEvent.connectionEvent().addEvent(PVEvent.valueEvent()), equalTo(PVEvent.connectionValueEvent()));
        assertThat(PVEvent.valueEvent().addEvent(PVEvent.connectionEvent()), not(equalTo(PVEvent.connectionValueEvent())));
        assertThat(PVEvent.valueEvent().addEvent(PVEvent.connectionEvent()).getType(), equalTo(Arrays.asList(PVEvent.Type.VALUE, PVEvent.Type.READ_CONNECTION)));
    }
}
