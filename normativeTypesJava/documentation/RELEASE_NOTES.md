Release 0.1
===========

This is the first release of normativeTypesJava.

This release provides support through wrapper classes and builders for the
following Normative Types:

*   NTScalar
*   NTScalarArray
*   NTEnum
*   NTMatrix
*   NTURI
*   NTNameValue
*   NTTable
*   NTAttribute
*   NTMultiChannel
*   NTNDArray
*   NTContinuum
*   NTHistogram
*   NTAggregate
*   NTUnion
*   NTScalarMultiChannel

Release 0.1 therefore implements fully the
[16 Mar 2015 version](http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes_20150316.html)
 of the normativeTypes specification.

Each type has a wrapper class of the same name which has functions for checking 
compatibility (isCompatible) and the reported types (is_a) of existing
structures (either Structures or PVStructures) and of the validity of the data
of wrapped PVStructures with respect to the specification (isValid), wraps
existing PVStructures (wrap, wrapUnsafe) and provides a convenient interface to
all required and optional fields.

Each type has a builder which can create a Structure, a PVStructure or a
wrapper around a new PVStructure. In each case optional or extra fields can be
added and options such as choice of scalar type can be made.

Additional features are:

* Utility classes NTField and NTPVField for standard structure fields and
  NTUtils and NTID for type IDs.
* Unit tests for all the implemented types and other classes, providing
  extensive coverage for all the Normative Types (except NTNDArray).

