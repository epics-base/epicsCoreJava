# EPICS Core Java Libraries Supermodule

This is a supermodule to build, test, bundle and deploy all Java/Maven modules 
of the EPICS Core Libraries.
It also contains the EPICS parent POM that is inherited by all modules, 
and provides a convenience dependency POM that can be used by other projects 
to depend on a matching set of the EPICS Core libraries.


## Code Submodules
Maven submodules/directories/projects contain the code of EPICS Core:
* pvDataJava
* pvAccessJava
* normativeTypesJava
* epics-util
* epics-vtype
* gpclient

One submodule creates the distribution tar/zip archives:
* bundleJava

One directory/project provides the dependency POM:
* epics-core

One (dummy) directory/project deploys the libraries to Maven Central:
* epics-deploy

### example code
https://github.com/epics-base/exampleJava/

## Maven Profiles

### with-examples
This profile (enabled by default) adds the directories/projects bundleJava as subprojects.

Disable this profile when deploying to Maven repositories, as the artifacts 
of these subprojects contain applications and no libraries.

### release
This profile adds GPG signing of all artifacts.

Enable this profile when releasing/deploying to Sonatype / Maven Central 
repositories, as Sonatype requires all uploaded artifacts to be signed.
