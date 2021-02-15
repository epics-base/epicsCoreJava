/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.joda.time.Instant;


/**
 * Immutable {@code Time} implementation.
 *
 * @author carcassi
 */
final class ITime extends Time {

    private final Instant timestamp;
    private final Integer userTag;
    private final boolean valid;

    ITime(Instant timestamp, Integer userTag, boolean valid) {
        VType.argumentNotNull("timestamp", timestamp);
        this.timestamp = timestamp;
        this.userTag = userTag;
        this.valid = valid;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public Integer getUserTag() {
        return userTag;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

}
