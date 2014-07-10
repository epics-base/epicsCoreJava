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
The implementation should be changed so that it implements PostHandler.
Thus when an element is modified it will call postPut for itself.


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

sharedData
---------

The share data methods for arrays should go away.
Copy On Write semantics for arrays should be implemented.
