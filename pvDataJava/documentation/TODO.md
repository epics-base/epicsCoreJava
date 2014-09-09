TODO
===========

org.epics.pvdata.util.*
------------

Is this being used?
If being used this document should talk about it.
If not being used it should be deleted.

postMonitor: PVUnion, PVUnionArray, and PVStructureArray
--------

PVUnion, PVUnionArray, and PVStructureArray all have elements
that are treated like a top level field.

Currently if a subField of any of these is changed postMonitor is not called for the field itself.

David asked if this could be changed so that it is called.
Marty thinks this may not be a good idea.

valueAlarm
----------

normativeTypes.html describes valueAlarm only for a value field that has type
double.
The implementation also supports all the numeric scalar types.

convert
--------

The array conversion methods must implement stride so that the stride argument of ChannelArray
can be supported.


monitorPlugin
-------------

A debate is on-going about what semantics should be.

PVAuxInfo
---------

This should go away.
This can not be done until pvIOCJava is changed.

sharedData
---------

The share data methods for arrays should go away.
Copy On Write semantics for arrays should be implemented.
