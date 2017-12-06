/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

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
     * @return the labels; not null
     */
    public abstract List<String> getChoices();

    /**
     * Whether the given object is an EnumDisplay with the same choixwa.
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
     * @return the new alarm
     */
    public static EnumDisplay of(final List<String> choices) {
        return new IEnumDisplay(choices);
    }
    
}
