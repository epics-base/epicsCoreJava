EPICS VERSION 4 JAVA IMPLEMENTATION README
==========================================

This README is a guide to the build of the Java implementation of EPICS Version 4. 

Status: This README is up-to-date with respect to release v4.5.0.1 of EPICS Version 4.

Auth:   Greg White, SLAC, 28-Oct-2015


Prerequisites
-------------

The EPICS V4 Java bundle requires recent versions of the following software:

1. Java SDK v1.7 (Java 7)
 
Note that support for Channel Access operations through the pvAccess API
"Channel Provider" interface is provided by CAJ and JCA, which are bundled
in the EPICS-Java tar. 

Build
-----

The tar file distribution of the Java implementation of EPICS v4.5.0.1
contains the jar files of Java executables, sources and documentation, for 
all the software modules of the EPICS v4.
Therefore, simply untar the distribution tar file.

    tar xvfz EPICS-Java-4.5.0.1.tar.gz  [if you got it compressed]
    tar xvf EPICS-Java-4.5.0.1.tar      [if you got it uncompressed]


Further information
-------------------

For the individual modules, consult the documentation in each one. In 
particular:

* README.md
* RELEASE_VERSIONS.md
* The documentation directory

For more information visit the
[EPICS V4 website](http://epics-pvdata.sourceforge.net).

In particular:

* [Getting started guide](http://epics-pvdata.sourceforge.net/gettingStarted.html) - 
  for detailed build instructions and where to go next.
* [Developer guide](http://epics-pvdata.sourceforge.net/informative/developerGuide/developerGuide.html) -
  currently under development.
* [Documentation page](http://epics-pvdata.sourceforge.net/literature.html) -
  Overview documents and doxygen for the various modules.
