#include "WindowsShellExecute.h"
#include "windows.h"

/** 
 * Execute over shell via its associated application on windows.
 * This works for files and for URLs.
 */
extern "C"
JNIEXPORT jint JNICALL Java_phex_utils_WindowsShellExecute_shellExecute
(JNIEnv *env, jclass jc, jstring shellExec)
{
    const char *l_execStr = env->GetStringUTFChars( shellExec, NULL );
    HINSTANCE errorCode = ShellExecute( NULL, "open", l_execStr, "", "", SW_SHOWNORMAL );

    // Release Java string    
    env->ReleaseStringUTFChars(shellExec, l_execStr);
  
    return (jint)errorCode;
}