/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import java.util.List;

/**
 * Immutable EnumDisplay implementation.
 *
 * @author carcassi
 */
class IEnumDisplay extends EnumDisplay {

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
