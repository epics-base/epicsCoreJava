/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.property;
import org.epics.pvdata.pv.PVField;

/**
 * PVEnumerated. Attach to an enumerated structure.
 * @author mrk
 *
 */
public interface PVEnumerated {
    /**
     * Attempt to attach to the enumerated field.
     * The field must either be an enumerated field itself or
     * a subfield of the parent of a field named value.
     * @param pvField The field for which to find an enumerated field,
     * @return (false,true) if enumerated field (not found, found).
     */
    boolean attach(PVField pvField);
    /**
     * Remove attachment to an enumerated field.
     */
    void detach();
    /**
     * Is this attached to an enumerated structure.
     * @return false or true.
     */
    boolean isAttached();
    // each of the following throws logic_error is not attached to PVField
    // a set returns false if field is immutable
    /**
     * Set the index.
     * @param index The value.
     * @return (false,true) if the index (was not, was) modified.
     */
    boolean setIndex(int index);
    /**
     * Get the current index.
     * @return The value.
     */
    int getIndex();
    /**
     * Get the choice corresponding to the current index.
     * @return The value.
     */
    String getChoice();
    /**
     * Are the choices mutable.
     * @return false or true
     */
    boolean choicesMutable();
    /**
     * Get the current choices.
     * @return The choices.
     */
    String[] getChoices();
    /**
     * Set the choices.
     * @param choices The choices.
     * @return (false,true) in choices are (immutable, mutable)
     */
    boolean setChoices(String[] choices);
}
