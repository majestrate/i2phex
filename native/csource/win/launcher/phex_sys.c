#include <windows.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <jni.h>
#include "Phex.h"

#define JVM_DLL "jvm.dll"
#define JAVA_DLL "java.dll"

/*
 * Prototypes.
 */
static jboolean GetPublicJREHome(char *path, jint pathsize);

/*
 * Win Entry point.
 */
int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow)
{
    enableConsole = JNI_FALSE;
    return main(__argc, __argv);
}

void printErrorMessage( char* msg )
{
    if ( enableConsole )
    {
        fprintf(stderr, msg);
    }
    else
    {
        MessageBox(NULL, msg, "Phex Launcher Error",  MB_OK|MB_ICONSTOP );
    }
}

/*
 * Find path to JRE based on .exe's location or registry settings.
 */
jboolean GetJREPath(char *path, jint pathsize)
{
    char javadll[MAXPATHLEN];
    struct stat s;

    if (GetApplicationHome(path, pathsize)) 
    {
	    /* Is JRE co-located with the application? */
	    sprintf(javadll, "%s\\bin\\" JAVA_DLL, path);
	    if (stat(javadll, &s) == 0)
	    {
	        goto found;
	    }

	    /* Does this app ship a private JRE in <apphome>\jre directory? */
	    sprintf(javadll, "%s\\jre\\bin\\" JAVA_DLL, path);
	    if (stat(javadll, &s) == 0)
	    {
	        strcat(path, "\\jre");
	        goto found;
	    }
    }

    /* Look for a public JRE on this machine. */
    if ( GetPublicJREHome( path, pathsize ) )
    {
        /* Is JRE located in the home path? */
	    sprintf(javadll, "%s\\bin\\" JAVA_DLL, path);
	    if (stat(javadll, &s) == 0)
	    {
	        goto found;
	    }

	    /* Does the home path contain a private JRE in <JavaHome>\jre directory? */
	    sprintf(javadll, "%s\\jre\\bin\\" JAVA_DLL, path);
	    if (stat(javadll, &s) == 0)
	    {
	        strcat(path, "\\jre");
	        goto found;
	    }
    }

    fprintf(stderr, "Error: could not find " JAVA_DLL "\n");
    return JNI_FALSE;

 found:
#if DEBUG_OUTPUT==1
        printf("JRE path is %s\n", path);
#endif
    return JNI_TRUE;
}

/*
 * Given a JRE location and a JVM type, construct what the name the
 * JVM shared library will be.  Return true, if such a library
 * exists, false otherwise.
 */
jboolean GetJVMPath(const char *jrepath, const char *jvmtype,
	   char *jvmpath, jint jvmpathsize)
{
    struct stat s;
    sprintf(jvmpath, "%s\\bin\\%s\\" JVM_DLL, jrepath, jvmtype);
    if (stat(jvmpath, &s) == 0)
    {
	    return JNI_TRUE;
    } 
    else
    {
	    return JNI_FALSE;
    }
}


/*
 * Load JVM of "jvmtype", and intialize the invocation functions.  Notice that
 * if jvmtype is NULL, we try to load hotspot VM as the default.  Maybe we
 * need an environment variable that dictates the choice of default VM.
 */
jboolean LoadJavaVM(const char *jvmpath, InvocationFunctions *ifn)
{
    HINSTANCE handle;
    
#if DEBUG_OUTPUT==1
    printf("JVM path is %s\n", jvmpath);
#endif
    
    /* Load the Java VM DLL */
    if ((handle = LoadLibrary(jvmpath)) == 0)
    {
	    fprintf(stderr, "Error loading: %s\n", jvmpath);
	    return JNI_FALSE;
    }

    /* Now get the function addresses */
    ifn->CreateJavaVM =
	    (void *)GetProcAddress(handle, "JNI_CreateJavaVM");
    ifn->GetDefaultJavaVMInitArgs =
	    (void *)GetProcAddress(handle, "JNI_GetDefaultJavaVMInitArgs");
    if (ifn->CreateJavaVM == 0 || ifn->GetDefaultJavaVMInitArgs == 0)
    {
	    fprintf(stderr, "Error: can't find JNI interfaces in: %s\n", jvmpath);
	    return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * If app is "c:\foo\bin\javac", then put "c:\foo" into buf.
 */
jboolean GetApplicationHome(char *buf, jint bufsize)
{
    char *cp;
    GetModuleFileName(0, buf, bufsize);
    *strrchr(buf, '\\') = '\0'; /* remove .exe file name */
    if ((cp = strrchr(buf, '\\')) == 0) 
    {
	    /* This happens if the application is in a drive root, and
	     * there is no bin directory. */
	    buf[0] = '\0';
	    return JNI_FALSE;
    }
    /* *cp = '\0';  /* remove the bin\ part */
    return JNI_TRUE;
}

/*
 * Helpers to look in the registry for a public JRE.
 */
#define JRE_KEY "Software\\JavaSoft\\Java Runtime Environment"
#define JDK_KEY "Software\\JavaSoft\\Java Development Kit"

static jboolean
GetStringFromRegistry(HKEY key, const char *name, char *buf, jint bufsize)
{
    DWORD type, size;

    if (RegQueryValueEx(key, name, 0, &type, 0, &size) == 0
	&& type == REG_SZ
	&& (size < (unsigned int)bufsize)) 
	{
	    if (RegQueryValueEx(key, name, 0, 0, buf, &size) == 0)
	    {
	        return JNI_TRUE;
	    }
    }
    return JNI_FALSE;
}

static jboolean GetPublicJREHome(char *buf, jint bufsize)
{
    HKEY key, subkey;
    char version[MAXPATHLEN];

    /* Find the current version of the JRE */
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_KEY, 0, KEY_READ, &key) != 0)
    {
	    fprintf(stderr, "Error opening registry key '" JRE_KEY "'\n");
	    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, JDK_KEY, 0, KEY_READ, &key) != 0)
        {
            fprintf(stderr, "Error opening registry key '" JDK_KEY "'\n");
            return JNI_FALSE;
        }
    }

    if (!GetStringFromRegistry(key, "CurrentVersion", version, sizeof(version)))
    {
	    fprintf(stderr, "Failed reading value of registry key:\n\t"
		    JRE_KEY "\\CurrentVersion\n");
	    RegCloseKey(key);
	    return JNI_FALSE;
    }

    // check for a special java version
    //#define DOTRELEASE  "1.3" /* Same for 1.3.1, 1.3.2 etc. */
    //if (strcmp(version, DOTRELEASE) != 0)
    //{
	//    fprintf(stderr, "Registry key '" JRE_KEY "\\CurrentVersion'\nhas "
	//	    "value '%s', but '" DOTRELEASE "' is required.\n", version);
	//    RegCloseKey(key);
	//    return JNI_FALSE;
    //}

    /* Find directory where the current version is installed. */
    if (RegOpenKeyEx(key, version, 0, KEY_READ, &subkey) != 0)
    {
	    fprintf(stderr, "Error opening registry key '"
		    JRE_KEY "\\%s'\n", version);
	    RegCloseKey(key);
	    return JNI_FALSE;
    }

    if (!GetStringFromRegistry(subkey, "JavaHome", buf, bufsize))
    {
	    fprintf(stderr, "Failed reading value of registry key:\n\t"
		    JRE_KEY "\\%s\\JavaHome\n", version);
	    RegCloseKey(key);
	    RegCloseKey(subkey);
	    return JNI_FALSE;
    }
    
#if DEBUG_OUTPUT==1
    {
	    char micro[MAXPATHLEN];
	    if (!GetStringFromRegistry(subkey, "MicroVersion", micro, sizeof(micro)))
	    {
	        printf("Warning: Can't read MicroVersion\n");
	        micro[0] = '\0';
	    }
	    printf("Version major.minor.micro = %s.%s\n", version, micro);
	}
#endif

    RegCloseKey(key);
    RegCloseKey(subkey);
    return JNI_TRUE;
}