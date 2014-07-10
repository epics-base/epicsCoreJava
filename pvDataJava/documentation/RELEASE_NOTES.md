Release release/3.1 IN DEVELOPMENT
===========

The main changes since release 3.0.2 are:

* union is new type.
* copy is new.
* monitorPlugin is new.


union is a new basic type.
------------

There are two new basic types: union_t and unionArray.

A union is like a structure that has a single subfield.
There are two flavors:

* <b>varient union</b> The field can have any type.
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

Release 3.0.2
==========
This was the starting point for RELEASE_NOTES
