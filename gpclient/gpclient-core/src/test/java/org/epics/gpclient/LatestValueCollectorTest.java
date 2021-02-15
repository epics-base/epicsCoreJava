/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.epics.util.compat.legacy.functional.Consumer;
import org.hamcrest.Matchers;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.InOrder;
import static org.mockito.Mockito.*;

/**
 *
 * @author carcassi
 */
public class LatestValueCollectorTest {

    public LatestValueCollectorTest() {
    }

    public ReadCollector createCollector() {
        return new LatestValueCollector<Object>(Object.class);
    }

    @Test
    public void updateValue1() {
        Consumer<PVEvent> listener = mock(Consumer.class);

        ReadCollector coll = createCollector();
        coll.setUpdateListener(listener);

        coll.updateValue(new Object());

        verify(listener).accept(PVEvent.valueEvent());
    }

    @Test
    public void updateValue2() {
        Consumer<PVEvent> listener = mock(Consumer.class);

        ReadCollector coll = createCollector();
        coll.setUpdateListener(listener);

        coll.updateValue(new Object());
        coll.updateValue(new Object());
        coll.updateValue(new Object());

        verify(listener, times(3)).accept(PVEvent.valueEvent());
    }

    @Test
    public void updateValueAndConnection1() {
        Consumer<PVEvent> listener = mock(Consumer.class);

        ReadCollector coll = createCollector();
        coll.setUpdateListener(listener);

        coll.updateValueAndConnection(new Object(), true);

        verify(listener).accept(PVEvent.readConnectionValueEvent());
    }

    @Test
    public void updateConnection1() {
        Consumer<PVEvent> listener = mock(Consumer.class);

        ReadCollector coll = createCollector();
        coll.setUpdateListener(listener);

        coll.updateConnection(true);

        verify(listener).accept(PVEvent.readConnectionEvent());
    }

    @Test
    public void notifyError1() {
        Consumer<PVEvent> listener = mock(Consumer.class);
        Exception ex = new RuntimeException();

        ReadCollector coll = createCollector();
        coll.setUpdateListener(listener);


        coll.notifyError(ex);

        verify(listener).accept(PVEvent.exceptionEvent(ex));
    }

    @Test
    public void mixedNotifications1() {
        Consumer<PVEvent> listener = mock(Consumer.class);
        Exception ex = new RuntimeException();

        ReadCollector coll = createCollector();
        coll.setUpdateListener(listener);

        coll.updateValueAndConnection(0, true);
        coll.updateValue(2);
        coll.updateValue(3);
        coll.updateConnection(false);
        coll.notifyError(ex);
        coll.updateConnection(true);
        coll.updateValue(4);

        InOrder inOrder = inOrder(listener);

        inOrder.verify(listener).accept(PVEvent.readConnectionValueEvent());
        inOrder.verify(listener, times(2)).accept(PVEvent.valueEvent());
        inOrder.verify(listener).accept(PVEvent.readConnectionEvent());
        inOrder.verify(listener).accept(PVEvent.exceptionEvent(ex));
        inOrder.verify(listener).accept(PVEvent.readConnectionEvent());
        inOrder.verify(listener).accept(PVEvent.valueEvent());
    }

    @Test
    public void retrieveValue1() {
        ReadCollector<Object, Object> coll = new LatestValueCollector<Object>(Object.class);
        coll.updateValue(0);
        assertThat(coll.getValue(), Matchers.<Object>equalTo(0));
        coll.updateValue(1);
        coll.updateValue(2);
        assertThat(coll.getValue(), Matchers.<Object>equalTo(2));
        coll.updateValue(3);
        assertThat(coll.getValue(), Matchers.<Object>equalTo(3));
        coll.updateValue(4);
        coll.updateValue(5);
        assertThat(coll.getValue(), Matchers.<Object>equalTo(5));
    }
}
