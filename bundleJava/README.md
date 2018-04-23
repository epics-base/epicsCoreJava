# bundleJava
This Maven project downloads all modules of an EPICS V4 distribution (Java parts)
and all dependencies outside of the standard Java installation.

It then creates a gnu-zipped tar file containing all EPICS V4 jars (binaries, sources and Javadoc) in its
root directory (carrying the release name), and all external dependencies (binaries) in the "lib" subdirectory.

This project is intended to be run on a Jenkins based CI infrastructure.
To correctly name the dependencies, it relies on a parent pom (ev4-java-bundle.xml) that is created running the
script jenkins_bundle found in the jenkins directory.

To be able to "build" this project locally, you have to run that script in the top directory of your workspace first.
