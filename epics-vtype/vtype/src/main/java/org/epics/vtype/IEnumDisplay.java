/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.util.List;

/**
 * Immutable {@code EnumDisplay} implementation.
 *
 * @author carcassi
 */
final class IEnumDisplay extends EnumDisplay {

    private final List<String> choices;

    IEnumDisplay(List<String> choices) {
        VType.argumentNotNull("choices", choices);
        this.choices = choices;
    }

    @Override
    public List<String> getChoices() {
        return choices;
    }

}
