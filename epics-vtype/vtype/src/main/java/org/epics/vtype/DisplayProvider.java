/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.util.Optional;

/**
 * A value that provides display information.
 *
 * @author carcassi
 */
public interface DisplayProvider extends DescriptionProvider {
    
    /**
     * The display associated with this value.
     * 
     * @return the display; not null
     */
    Display getDisplay();
    default Optional<String> getDescription() {
        return Optional.ofNullable(getDisplay().getDescription());
    }
}
