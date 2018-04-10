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
        assertThat(PVEvent.readConnectionEvent().addEvent(PVEvent.valueEvent()), equalTo(PVEvent.readConnectionValueEvent()));
        assertThat(PVEvent.valueEvent().addEvent(PVEvent.readConnectionEvent()), not(equalTo(PVEvent.readConnectionValueEvent())));
        assertThat(PVEvent.valueEvent().addEvent(PVEvent.readConnectionEvent()).getType(), equalTo(Arrays.asList(PVEvent.Type.VALUE, PVEvent.Type.READ_CONNECTION)));
    }
}
