#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <jni.h>
#include "Phex.h"

static char *progname;
jboolean enableConsole = JNI_TRUE;
int status = 0;

/*
 * List of VM options to be specified when the VM is created.
 */
static JavaVMOption *options;
static int numOptions, maxOptions;

int main(int argc, char **argv);
/*
 * Prototypes for functions internal to launcher.
 */
static void AddOption(char *str, void *info);
static void SetClassPath(char *s);
static jboolean InitializeJVM(JavaVM **pvm, JNIEnv **penv, InvocationFunctions *ifn);
static void* MemAlloc(size_t size);
static jstring NewPlatformString(JNIEnv *env, char *s);
static jobjectArray NewPlatformStringArray(JNIEnv *env, char **strv, int strc);
static jclass LoadClass(JNIEnv *env, char *name);
static jstring GetMainClassName(JNIEnv *env, char *jarname);

//static jboolean ParseArguments(int *pargc, char ***pargv, char **pjarfile,
//			       char **pclassname, int *pret);
// static void PrintUsage(void);

/*
 * Entry point.
 */
int main(int argc, char **argv)
{
    JavaVM *vm = 0;
    JNIEnv *env = 0;
    char *jarfile = 0;
    char *classname = 0;
    char *s = 0;
    jclass mainClass;
    jmethodID mainID;
    jobjectArray mainArgs;
    int ret;
    InvocationFunctions ifn;
    const char *jvmtype = 0;
    char jrepath[MAXPATHLEN], jvmpath[MAXPATHLEN];
    jstring mainClassName;

#if DEBUG_OUTPUT==1
    printf("----_JAVA_LAUNCHER_DEBUG----\n");
#endif

    /* Find out where the JRE is that we will be using. */
    if ( !GetJREPath( jrepath, sizeof(jrepath) ) )
    {
	    printErrorMessage( "Could not find Java 2 Runtime Environment.\n");
#if DEBUG_OUTPUT==1
        system( "pause" );
#endif
	    return 2;
    }

	jvmpath[0] = '\0';
	/* First try hotspot then client (j2se 1.4), then try classic... */
	jvmtype = "hotspot";
    if ( !GetJVMPath( jrepath, jvmtype, jvmpath, sizeof( jvmpath ) ) )
    {
#if DEBUG_OUTPUT==1
        printf( "No '%s' JVM at '%s'.\n", jvmtype, jvmpath );
#endif
        jvmtype = "client";
        if ( !GetJVMPath( jrepath, jvmtype, jvmpath, sizeof( jvmpath ) ) )
        {
#if DEBUG_OUTPUT==1
            printf( "No '%s' JVM at '%s'.\n", jvmtype, jvmpath );
#endif
            jvmtype = "classic";
            if ( !GetJVMPath( jrepath, jvmtype, jvmpath, sizeof( jvmpath ) ) )
            {
                char *msgText = MemAlloc( strlen(jvmtype) + strlen(jvmpath) + 29);
                sprintf(msgText, "No '%s' JVM at '%s'.\n", jvmtype, jvmpath );
                printErrorMessage( msgText );
#if DEBUG_OUTPUT==1
                system( "pause" );
#endif
                return 4;
            }
        }
    }

    /* If we got here, jvmpath has been correctly initialized. */
    ifn.CreateJavaVM = 0;
    ifn.GetDefaultJavaVMInitArgs = 0;
    if (!LoadJavaVM(jvmpath, &ifn)) 
    {
#if DEBUG_OUTPUT==1
        system( "pause" );
#endif
	    return 6;
    }
    
    progname = "Phex";
    ++argv;
    --argc;
  
    /* Parse command line options */
    //if (!ParseArguments(&argc, &argv, &jarfile, &classname, &ret)) 
    //{
	//    return ret;
    //}
    
    jarfile = "phex.jar";
	SetClassPath(jarfile);

    /* Initialize the virtual machine */
    if (!InitializeJVM(&vm, &env, &ifn)) 
    {
        printErrorMessage( "Could not create the Java virtual machine.\n" );
#if DEBUG_OUTPUT==1
        system( "pause" );
#endif
    	return 1;
    }

    ret = 1;

    mainClassName = GetMainClassName( env, jarfile );
	if ((*env)->ExceptionOccurred(env))
	{
	    printErrorMessage( "Failed to open 'phex.jar'.\n" );
	    (*env)->ExceptionDescribe(env);
	    goto leave;
	}
	
	if (mainClassName == NULL)
	{
	    char *msgText = MemAlloc( strlen(jarfile) + 55);
        sprintf(msgText, "Failed to load Main-Class manifest attribute from\n%s\n", jarfile);
        
        printErrorMessage( msgText );
	    goto leave;
	}
	
	classname = (char *)(*env)->GetStringUTFChars(env, mainClassName, 0);
    if (classname == NULL)
    {
        (*env)->ExceptionDescribe(env);
        goto leave;
    }
    
    /* At this stage, argc/argv have the applications' arguments */
#if DEBUG_OUTPUT==1
    {
	    int i = 0;
	    printf("Main-Class is '%s'\n", classname ? classname : "");
	    printf("Apps' argc is %d\n", argc);
	    for (; i < argc; i++)
	    {
	        printf("    argv[%2d] = '%s'\n", i, argv[i]);
	    }
	}
#endif
    
	mainClass = LoadClass(env, classname);
    
    (*env)->ReleaseStringUTFChars(env, mainClassName, classname);
    if (mainClass == NULL)
    {
        (*env)->ExceptionDescribe(env);
        printErrorMessage( "Exception occured while getting main class.\n" );
	    goto leave;
    }

    /* Get the application's main method */
    mainID = (*env)->GetStaticMethodID(env, mainClass, "main", "([Ljava/lang/String;)V");
    if (mainID == NULL)
    {
	    if ((*env)->ExceptionOccurred(env))
	    {
	        (*env)->ExceptionDescribe(env);
	        printErrorMessage( "Exception occured while getting main method.\n" );
	    }
	    else
	    {
	        printErrorMessage( "No main method found in specified class.\n" );
	    }
	    goto leave;
    }

    /* Build argument array */
    mainArgs = NewPlatformStringArray(env, argv, argc);
    if (mainArgs == NULL) 
    {
	    (*env)->ExceptionDescribe(env);
        printErrorMessage( "Exception occured while creating main method args.\n" );
	    goto leave;
    }
    
#if DEBUG_OUTPUT==1
	    printf("----_JAVA_LAUNCHER_DEBUG----\n");
#endif

    /* Invoke main method. */
    (*env)->CallStaticVoidMethod(env, mainClass, mainID, mainArgs);
    if ((*env)->ExceptionOccurred(env))
    {
	/* Formerly, we used to call the "uncaughtException" method of the
	   main thread group, but this was later shown to be unnecessary
	   since the default definition merely printed out the same exception
	   stack trace as ExceptionDescribe and could never actually be
	   overridden by application programs. */
	    (*env)->ExceptionDescribe(env);
	    if ( !enableConsole )
        {
            printErrorMessage( "Exception occured while calling main method.\n" );
        }
	    goto leave;
    }

    /*
     * Detach the current thread so that it appears to have exited when
     * the application's main method exits.
     */
    if ((*vm)->DetachCurrentThread(vm) != 0)
    {
        printErrorMessage( "Could not detach main thread.\n" );
	    goto leave;
    }
    ret = 0;

leave:
    (*vm)->DestroyJavaVM(vm);
#if DEBUG_OUTPUT==1
        system( "pause" );
#endif
    return ret;
}

/*
 * Adds a new VM option with the given given name and value.
 */
static void AddOption( char *str, void *info )
{
    /*
     * Expand options array if needed to accomodate at least one more
     * VM option.
     */
    if (numOptions >= maxOptions) 
    {
	    if (options == 0)
	    {
	        maxOptions = 4;
	        options = MemAlloc(maxOptions * sizeof(JavaVMOption));
	    }
	    else
	    {
	        JavaVMOption *tmp;
	        maxOptions *= 2;
	        tmp = MemAlloc(maxOptions * sizeof(JavaVMOption));
	        memcpy(tmp, options, numOptions * sizeof(JavaVMOption));
	        free(options);
	        options = tmp;
	    }
    }
    options[numOptions].optionString = str;
    options[numOptions++].extraInfo = info;
}

static void SetClassPath(char *s)
{
    char *def = MemAlloc(strlen(s) + 40);
    sprintf(def, "-Djava.class.path=%s", s);
    AddOption(def, NULL);
}

/*
 * Initializes the Java Virtual Machine. Also frees options array when
 * finished.
 */
static jboolean InitializeJVM(JavaVM **pvm, JNIEnv **penv, InvocationFunctions *ifn)
{
    JavaVMInitArgs args;
    jint r;

    memset(&args, 0, sizeof(args));
    args.version  = JNI_VERSION_1_2;
    args.nOptions = numOptions;
    args.options  = options;
    args.ignoreUnrecognized = JNI_FALSE;

#if DEBUG_OUTPUT==1
    {
	    int i = 0;
	    printf("JavaVM args:\n    ");
	    printf("version 0x%08lx, ", args.version);
	    printf("ignoreUnrecognized is %s, ",
	       args.ignoreUnrecognized ? "JNI_TRUE" : "JNI_FALSE");
	    printf("nOptions is %ld\n", args.nOptions);
	    for (i = 0; i < numOptions; i++)
	    {
	        printf("    option[%2d] = '%s'\n", i, args.options[i].optionString);
	    }
    }
#endif
    r = ifn->CreateJavaVM(pvm, (void **)penv, &args);
    free(options);
    return r == JNI_OK;
}


#define NULL_CHECK0(e) if ((e) == 0) return 0
#define NULL_CHECK(e) if ((e) == 0) return

/*
 * Returns a pointer to a block of at least 'size' bytes of memory.
 * Prints error message and exits if the memory could not be allocated.
 */
static void * MemAlloc(size_t size)
{
    void *p = malloc(size);
    if (p == 0) 
    {
	    perror("malloc");
	    exit(1);
    }
    return p;
}

/*
 * Returns a new Java string object for the specified platform string.
 */
static jstring NewPlatformString(JNIEnv *env, char *s)
{
    int len = strlen(s);
    jclass cls;
    jmethodID mid;
    jbyteArray ary;

    NULL_CHECK0(cls = (*env)->FindClass(env, "java/lang/String"));
    NULL_CHECK0(mid = (*env)->GetMethodID(env, cls, "<init>", "([B)V"));
    ary = (*env)->NewByteArray(env, len);
    if (ary != 0) 
    {
	    jstring str = 0;
	    (*env)->SetByteArrayRegion(env, ary, 0, len, (jbyte *)s);
	    if (!(*env)->ExceptionOccurred(env))
	    {
	        str = (*env)->NewObject(env, cls, mid, ary);
	    }
	    (*env)->DeleteLocalRef(env, ary);
	    return str;
    }
    return 0;
}

/*
 * Returns a new array of Java string objects for the specified
 * array of platform strings.
 */
static jobjectArray
NewPlatformStringArray(JNIEnv *env, char **strv, int strc)
{
    jarray cls;
    jarray ary;
    int i;

    NULL_CHECK0(cls = (*env)->FindClass(env, "java/lang/String"));
    NULL_CHECK0(ary = (*env)->NewObjectArray(env, strc, cls, 0));
    for (i = 0; i < strc; i++) 
    {
	    jstring str = NewPlatformString(env, *strv++);
	    NULL_CHECK0(str);
	    (*env)->SetObjectArrayElement(env, ary, i, str);
	    (*env)->DeleteLocalRef(env, str);
    }
    return ary;
}

/*
 * Loads a class, convert the '.' to '/'.
 */
static jclass LoadClass(JNIEnv *env, char *name)
{
    char *buf = MemAlloc(strlen(name) + 1);
    char *s = buf, *t = name, c;
    jclass cls;
    
    do 
    {
        c = *t++;
	    *s++ = (c == '.') ? '/' : c;
    }
    while (c != '\0');
    
    cls = (*env)->FindClass(env, buf);
    free(buf);

    return cls;
}

/*
 * Returns the main class name for the specified jar file.
 */
static jstring GetMainClassName(JNIEnv *env, char *jarname)
{
#define MAIN_CLASS "Main-Class"
    jclass cls;
    jmethodID mid;
    jobject jar, man, attr;
    jstring str, result = 0;

    NULL_CHECK0(cls = (*env)->FindClass(env, "java/util/jar/JarFile"));
    NULL_CHECK0(mid = (*env)->GetMethodID(env, cls, "<init>",
					  "(Ljava/lang/String;)V"));
    NULL_CHECK0(str = NewPlatformString(env, jarname));
    NULL_CHECK0(jar = (*env)->NewObject(env, cls, mid, str));
    NULL_CHECK0(mid = (*env)->GetMethodID(env, cls, "getManifest",
					  "()Ljava/util/jar/Manifest;"));
    man = (*env)->CallObjectMethod(env, jar, mid);
    if (man != 0) {
	NULL_CHECK0(mid = (*env)->GetMethodID(env,
				    (*env)->GetObjectClass(env, man),
				    "getMainAttributes",
				    "()Ljava/util/jar/Attributes;"));
	attr = (*env)->CallObjectMethod(env, man, mid);
	if (attr != 0) {
	    NULL_CHECK0(mid = (*env)->GetMethodID(env,
				    (*env)->GetObjectClass(env, attr),
				    "getValue",
				    "(Ljava/lang/String;)Ljava/lang/String;"));
	    NULL_CHECK0(str = NewPlatformString(env, MAIN_CLASS));
	    result = (*env)->CallObjectMethod(env, attr, mid, str);
	}
    }
    return result;
}


/*
 * Prints default usage message.
 */
/*static void
PrintUsage(void)
{
    fprintf(stdout,
	"Usage: %s [-options] class [args...]\n"
	"           (to execute a class)\n"
	"   or  %s -jar [-options] jarfile [args...]\n"
	"           (to execute a jar file)\n"
	"\n"
	"where options include:\n"
	"    -cp -classpath <directories and zip/jar files separated by %c>\n"
	"              set search path for application classes and resources\n"
	"    -D<name>=<value>\n"
	"              set a system property\n"
	"    -verbose[:class|gc|jni]\n"
	"              enable verbose output\n"
	"    -version  print product version\n"
	"    -? -help  print this help message\n"
	"    -X        print help on non-standard options\n",
	progname,
	progname, PATH_SEPARATOR
    );
}*/


/*
 * Parses command line arguments.
 */
/*static jboolean
ParseArguments(int *pargc, char ***pargv, char **pjarfile,
		       char **pclassname, int *pret)
{
    int argc = *pargc;
    char **argv = *pargv;
    jboolean jarflag = JNI_FALSE;
    char *arg;

    *pret = 1;
    while ((arg = *argv) != 0 && *arg == '-')
    {
	argv++; --argc;
	if (strcmp(arg, "-classpath") == 0 || strcmp(arg, "-cp") == 0) {
	    if (argc < 1) {
		fprintf(stderr, "%s requires class path specification\n", arg);
		PrintUsage();
		return JNI_FALSE;
	    }
	    SetClassPath(*argv);
	    argv++; --argc;
	} else if (strcmp(arg, "-help") == 0 ||
		   strcmp(arg, "-h") == 0 ||
		   strcmp(arg, "-?") == 0) {
	    PrintUsage();
	    *pret = 0;
	    return JNI_FALSE;
/*
 * The following case provide backward compatibility with old-style
 * command line options.
 */
/*	} else if (strcmp(arg, "-verbosegc") == 0) {
	    AddOption("-verbose:gc", NULL);
	} else if (strcmp(arg, "-t") == 0) {
	    AddOption("-Xt", NULL);
	} else if (strcmp(arg, "-tm") == 0) {
	    AddOption("-Xtm", NULL);
	} else if (strcmp(arg, "-debug") == 0) {
	    AddOption("-Xdebug", NULL);
	} else if (strcmp(arg, "-noclassgc") == 0) {
	    AddOption("-Xnoclassgc", NULL);
	} else if (strcmp(arg, "-Xfuture") == 0) {
	    AddOption("-Xverify:all", NULL);
	} else if (strcmp(arg, "-verify") == 0) {
	    AddOption("-Xverify:all", NULL);
	} else if (strcmp(arg, "-verifyremote") == 0) {
	    AddOption("-Xverify:remote", NULL);
	} else if (strcmp(arg, "-noverify") == 0) {
	    AddOption("-Xverify:none", NULL);
	} else if (strncmp(arg, "-prof", 5) == 0) {
	    char *p = arg + 5;
	    char *tmp = MemAlloc(strlen(arg) + 50);
	    if (*p) {
	        sprintf(tmp, "-Xrunhprof:cpu=old,file=%s", p + 1);
	    } else {
	        sprintf(tmp, "-Xrunhprof:cpu=old,file=java.prof");
	    }
	    AddOption(tmp, NULL);
	} else if (strncmp(arg, "-ss", 3) == 0 ||
		   strncmp(arg, "-oss", 4) == 0 ||
		   strncmp(arg, "-ms", 3) == 0 ||
		   strncmp(arg, "-mx", 3) == 0) {
	    char *tmp = MemAlloc(strlen(arg) + 6);
	    sprintf(tmp, "-X%s", arg + 1); /* skip '-' */
/*	    AddOption(tmp, NULL);
	} else if (strcmp(arg, "-checksource") == 0 ||
		   strcmp(arg, "-cs") == 0 ||
		   strcmp(arg, "-noasyncgc") == 0) {
	    /* No longer supported */
/*	    fprintf(stderr,
		    "Warning: %s option is no longer supported.\n",
		    arg);
	} else {
	    AddOption(arg, NULL);
	}
    }

    if (--argc >= 0) 
    {
        if (jarflag) 
        {
	        *pjarfile = *argv++;
	        *pclassname = 0;
	    } 
	    else 
	    {
	        *pjarfile = 0;
	        *pclassname = *argv++;
	    }
	    *pargc = argc;
	    *pargv = argv;
    }

    return JNI_TRUE;
}*/