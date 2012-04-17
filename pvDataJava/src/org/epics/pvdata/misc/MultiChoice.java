/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.misc;

/**
 * Interface for multiple choice data.
 * The implementation keeps a bitMask which selects an array of string values.
 * @author mrk
 *
 */
public interface MultiChoice {
	/**
	 * The interface returned by getSelectedChoices;
	 * @author mrk
	 *
	 */
	interface Choices {
		/**
		 * The current selected set of choices.
		 * @return The array of choice strings.
		 */
		String[] getChoices();
		/**
		 * Get the number of choices currently selected.
		 * @return The number of choices.
		 */
		int getNumberChoices();
	}
    /**
     * Get the current byte array for the bitMask.
     * @return The long array. The bits are ordered by byte and within a byte by bit where the low order bit is the first bit in the byte.
     */
    byte[] getBitMask();
    /**
     * Get the current complete set of choices.
     * @return The string array of choices.
     */
    String[] getChoices();
    /**
     * Get the currently selected set of bits.
     * @return The Choices interface.
     */
    Choices getSelectedChoices();
    /**
     * Set a selected bit.
     * @param index The bit index.
     */
    void setBit(int index);
    /**
     * Clear the bitMask.
     */
    void clear();
    /**
     * Register a new choice string.
     * If the string is already a choice then this method just returns the index of the already present choice.
     * @param choice The new choice.
     * @return The index for the choice.
     */
    int registerChoice(String choice);
}
