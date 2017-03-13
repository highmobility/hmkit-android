### What is this repository for? ###

* This is the Android SDK with Mobile and Wear reference apps.

### How do I get set up? ###

* git submodule init
* git submodule update --recursive --remote


### Compile core ###
* If you wish to re compile the bt core, you need to install the NDK through Android SDK Manager(in SDK tools tab).
```
cd {PROJECT_DIR}/hmlink/src/main/jni 
ndk-build
```