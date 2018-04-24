/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import java.time.Duration;

/**
 *
 * @author carcassi
 */
class GPClientGlobalChecks {
    public static void validateMaxRate(Duration maxRate) {
        if (maxRate == null) {
            throw new IllegalArgumentException("maxRate cannot be null");
        }
        if (maxRate.getSeconds() == 0 && maxRate.toMillis() < 5) {
            throw new IllegalArgumentException("Current implementation limits the rate to >5ms or <200Hz (requested " + maxRate + "s)");
        }
    }
}
