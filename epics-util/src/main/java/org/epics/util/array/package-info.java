/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
/**
 * Provides support for iteration and read-only references to
 * collections and arrays of primitives, without having to code for each
 * individual case.
 * <p>
 * The design is loosely inspired by the Collection framework, but does not
 * directly implement it. Due to the invariant nature of Java generics,
 * it would make the usage of inheritance awkward. See {@link org.epics.util.array.IteratorNumber}
 * for more details.
 */
package org.epics.util.array;
