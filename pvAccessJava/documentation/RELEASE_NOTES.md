Release 5.0.0
===========

* Duplicate channel response from the same server warning removed
* Server "all providers" support
* ServerContextImpl.startPVAServer method added
* "pvAccess" provider name deprecated, use "pva" instead
* Server-side channel destroy
* pipeline: ackAny parameter percentage support
* PVGet utility monitor support
* pvDS code removed
* pipeline support added

Release release/3.1
===========

The main changes since release 3.0.2 are:

* pvAccess API is changed.


pvAccess API is changed.
------------

Instead of data (PVStructure) appearing in connect callback (for example channelGetConnect)
it now is present in method called by whoever delivers the data.
See pvAccessJava.html for details.

Release 3.0.4
==========
This was the starting point for RELEASE_NOTES
