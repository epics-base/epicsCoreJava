normativeTypesJava
==================

normativeTypesJava is a Java module containing helper classes which implement
and provide support for the EPICS V4 Normative Types.

The latter are a set of standard high-level data types to aid interoperability
of EPICS V4 applications and are specified in the
[NormativeTypes Specification](http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes.html).


Status
------

The current version implements fully the
[16 Mar 2015 version](http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes_20150316.html)
 of the normativeTypes specification.

The module status is alpha and the API and behaviour may change signifcantly
in future versions.


Dependencies
------------

normativeTypesJava depends on pvDataJava.


Build
-----

Building normativeTypesJava from the source module requires maven.

To build type
    mvn compile install

To clean
    mvn clean

Building the module will generate a jars of the .class files, the source
and the javadoc.

To unzip the a jar, for example the javadoc jar, type

    java xf <name-of-jar>
e.g.
    java xf normativeTypesJava-0.2.0-SNAPSHOT-javadoc.jar

from the directory containing the jar.


To Use
------

To use normativeTypesJava in a Java application, add the location of the jar
containing the .class files to your CLASSPATH

    export CLASSPATH=$CLASSPATH:<path-to-nt-jar>


You will also need to do the same for pvDataJava.

