# org.epis.util

Basic Java utility classes to be shared across EPICS projects until suitable
replacements are available in the JDK.

# org.epics.util.array

This package provides a class hierarchy to handle collections of primitives.
The goals of the API are:
 * Allow access to the numeric data of a primitive array regardless of the
   type stored in the array
 * Allow unmodifiable views of the data, to increase encapsulation or to
   simplify multi-threading (by using effectively immutable types)
 * Allow basic array manipulation operation such as: splitting, copying,
   printing, comparison and so on.
 * Take care of type conversions, including unsigned primitives, which
   are in general not well-supported in Java
 * With some reasonable restriction, there should be no performance cost when
   accessing a primitive array through the collection classes
 * With some reasonable restrictions, allow to access the internal primitive arrays
   for interoperability with other libraries

## Design choices

The library is designed to mimic as much as possible the standard Java collection
classes. We do this for two reason:
 * to make the library more intuitive for a general Java programmer
 * there has been talk about generic reification / value classes, which may make
   the collection classes directly usable to access primitives with no penalties

## Package structure

The package is structured as follows:
 * CollectionNumber - provides the top level class of the hierarchy
 * CollectionXxx - provides the top level class of the hierarchy

