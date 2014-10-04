Release 4.0 IN DEVELOPMENT
===========

The main changes since release 3.0.2 are:

* methods that change interface removed from PVField and PVStructure
* timeStamp and valueAlarm name changes
* union is new type.
* copy is new.
* monitorPlugin is new.
* PVField no longer extends Requester

methods removed from  PVField and PVStructure
-----------------

The following method was removed from  PVField: rename.
The following methods were removed from PVStrucure: appendPVField, appendPVFields, removePVField,
replacePVField, getExtendsStructureName, and putExtendsStructureName.

With these changes there should be no methods that can change the introspection interface
of any data object after it is created.


timeStamp and valueAlarm name changes
--------------

In timeStamp nanoSeconds is changed to nanoseconds.

In valueAlarm hystersis is changed to hysteresis


union is a new basic type.
------------

There are two new basic types: union_t and unionArray.

A union is like a structure that has a single subfield.
There are two flavors:

* <b>variant union</b> The field can have any type.
* <b>union</b> The field can any of specified set of types.

The field type can be dynamically changed.

copy 
----

This consists of createRequest and pvCopy.
createRequest was moved from pvAccess to here.
pvCopy is moved from pvDatabaseCPP and now depends
only on pvData, i. e. it no longer has any knowledge of PVRecord.

monitorPlugin
-------------

This is for is for use by code that implements pvAccess monitors.
This is prototype and is subject to debate.

PVField
-------

This no longer extends Requester of has method setRequester.
Any code in pvDataJava that called pvField.message now throws an exception instead.
This change was made so that the semantics now more closely follow pvDataCPP.

Release 3.0.2
==========
This was the starting point for RELEASE_NOTES
