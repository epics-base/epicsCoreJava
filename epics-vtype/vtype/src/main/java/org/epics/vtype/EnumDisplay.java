/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum display information.
 *
 * @author carcassi
 */
public abstract class EnumDisplay {

    /**
     * Returns the possible labels for the enum.
     *
     * @return the choices; not null
     */
    public abstract List<String> getChoices();

    /**
     * Whether the given object is an EnumDisplay with the same choices.
     *
     * @param obj another alarm
     * @return true if equal
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

	if (obj instanceof EnumDisplay) {
            EnumDisplay other = (EnumDisplay) obj;

            return getChoices().equals(other.getChoices());
        }

        return false;
    }

    @Override
    public final int hashCode() {
        return getChoices().hashCode();
    }

    @Override
    public final String toString() {
        return getChoices().toString();
    }

    /**
     * New EnumDisplay with the given choices.
     *
     * @param choices the enum choices
     * @return the new display
     */
    public static EnumDisplay of(final List<String> choices) {
        return new IEnumDisplay(choices);
    }

    /**
     * New EnumDisplay with the given choices.
     *
     * @param choices the enum choices
     * @return the new display
     */
    public static EnumDisplay of(final String... choices) {
        return new IEnumDisplay(Arrays.asList(choices));
    }

    /**
     * New EnumDisplay with numeric labels for the given number of choices.
     *
     * @param nChoices the number of choices
     * @return the new display
     */
    public static EnumDisplay of(final int nChoices) {
        List<String> choices = new ArrayList<String>();
        for (int i = 0; i < nChoices; i++) {
            choices.add(Integer.toString(i));
        }
        return new IEnumDisplay(choices);
    }

}
