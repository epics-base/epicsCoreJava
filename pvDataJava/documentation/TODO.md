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

timeStamp, display, control, and valueAlarm
----------

normativeTypes.html defines time_t,  display_t, control_t, and alarmlimit_t.
The definitions are not compatible with how property defined timeStamp, display, control, and valueAlarm.
The definition of alarm_t does match the definition of property alarm.


convert
--------

The array conversion methods must implement stride so that the stride argument of ChannelArray
can be supported.


monitorPlugin
-------------

A debate is on-going about what semantics should be.


PVField, etc should not allow introspection interface to change
------------

Once created an instance of PVField should not allow the introspection
interface to change.
This methods like renameField, etc should be removed.
This can not be done until pvIOCJava is changed
so that it no longer requires the methods.
The main change will be to the xml parser.

PVAuxInfo
---------

This should go away.
This can not be done until pvIOCJava is changed.

sharedData
---------

The share data methods for arrays should go away.
Copy On Write semantics for arrays should be implemented.
