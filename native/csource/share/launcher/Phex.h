#ifndef _PHEX_H_
#define _PHEX_H_

#ifndef DEBUG_OUTPUT
#define DEBUG_OUTPUT 0
#endif

//Get system specific defines.
#include <jni.h>
#include "phex_sys.h"

/*
 * Pointers to the needed JNI invocation API, initialized by LoadJavaVM.
 */
typedef jint (JNICALL *CreateJavaVM_t)(JavaVM **pvm, void **env, void *args);
typedef jint (JNICALL *GetDefaultJavaVMInitArgs_t)(void *args);

typedef struct 
{
    CreateJavaVM_t CreateJavaVM;
    GetDefaultJavaVMInitArgs_t GetDefaultJavaVMInitArgs;
} InvocationFunctions;

/*
 * Protoypes for launcher functions in the system specific phex_sys.c.
 */
jboolean
GetJVMPath(const char *jrepath, const char *jvmtype,
	   char *jvmpath, jint jvmpathsize);

jboolean
GetJREPath(char *path, jint pathsize);

jboolean
LoadJavaVM(const char *jvmpath, InvocationFunctions *ifn);

jboolean
GetApplicationHome(char *buf, jint bufsize);

/*
 * Make launcher spit debug output.
 */
extern jboolean debug;

int main(int argc, char **argv);
extern jboolean enableConsole;

#endif /* _PHEX_H_ */
