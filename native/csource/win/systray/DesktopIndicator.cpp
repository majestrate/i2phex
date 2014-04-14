
#include "stdafx.h"
#include "DesktopIndicator.h"
#include "DesktopIndicatorHandler.h"
#include "DesktopIndicatorImages.h"
#include "resource.h"       //  DVB19Oct99


HINSTANCE g_instance = NULL;


BOOL WINAPI DllMain
(
	HINSTANCE hinstDLL,  // handle to DLL module
	DWORD fdwReason,     // reason for calling function
	LPVOID lpvReserved   // reserved
)
{
    switch( fdwReason )
	{
		case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
		case DLL_PROCESS_DETACH:

		case DLL_PROCESS_ATTACH:
			g_instance = hinstDLL;
			break;
    }
    return TRUE;
}


extern "C"
/*
 * Class:     DesktopIndicator
 * Method:    nativeDisable
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_phex_gui_common_DesktopIndicator_nativeDisable
  (JNIEnv *env, jobject object)
{
	// Get handler
	DesktopIndicatorHandler *l_handler = DesktopIndicatorHandler::extract( env, object );

	// Disable it
	if( l_handler )
		l_handler->disable();
}

extern "C"
/*
 * Class:     phex_gui_common_DesktopIndicator
 * Method:    setNativeMenuText
 * Signature: (ILjava/lang/String;)V, (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_phex_gui_common_DesktopIndicator_setNativeMenuText
  (JNIEnv *env, jobject object, jstring aOpenStr, jstring aExitStr )
{  
	// Get handler
	DesktopIndicatorHandler *l_handler = DesktopIndicatorHandler::extract( env, object );

	if( l_handler ) 
	{
	    jboolean l_IsCopy;
	    // Get Java string
	    const char *l_openStr = env->GetStringUTFChars( aOpenStr, &l_IsCopy );
	    const char *l_exitStr = env->GetStringUTFChars( aExitStr, &l_IsCopy );
	
		// Already exists, so update it
		l_handler->setMenuText( l_openStr, l_exitStr );
		
		// Release Java string
        env->ReleaseStringUTFChars( aOpenStr, l_openStr );
        env->ReleaseStringUTFChars( aExitStr, l_exitStr );
	}
}


extern "C"
/*
 * Class:     DesktopIndicator
 * Method:    nativeEnable
 * Signature: (ILjava/lang/String;)V
 */
 //Java_phex_gui_common_DesktopIndicator_activate
JNIEXPORT void JNICALL Java_phex_gui_common_DesktopIndicator_nativeEnable
  (JNIEnv *env, jobject object, jint image, jstring tooltip)
{
	jboolean l_IsCopy;

	// Get Java string
	const char *l_tooltip = env->GetStringUTFChars( tooltip, &l_IsCopy );

	// Get handler
	DesktopIndicatorHandler *l_handler = DesktopIndicatorHandler::extract( env, object );

	if( l_handler ) 
	{
		// Already exists, so update it
		l_handler->update( image, l_tooltip );
		
	}
	else
	{
		// Create our handler
		l_handler = new DesktopIndicatorHandler( env, object, image, l_tooltip );

		// Enable it
		if( l_handler )
			l_handler->enable( env );
	}

	// Release Java string
    env->ReleaseStringUTFChars( tooltip, l_tooltip );
}


extern "C"
/*
 * Class:     DesktopIndicator
 * Method:    nativeFreeImage
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_phex_gui_common_DesktopIndicator_nativeFreeImage
  (JNIEnv *env, jclass, jint image)
{
	g_DesktopIndicatorImages.remove( image );
}

extern "C"
/*
 * Class:     DesktopIndicator
 * Method:    nativeHide
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_phex_gui_common_DesktopIndicator_nativeHide
  (JNIEnv *env, jobject object)
{
	// Get handler
	DesktopIndicatorHandler *l_handler = DesktopIndicatorHandler::extract( env, object );

	if( l_handler )
	{
		l_handler->hide();
	}	
}


extern "C"
/*
 * Class:     DesktopIndicator
 * Method:    nativeLoadImage
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_phex_gui_common_DesktopIndicator_nativeLoadImage
  (JNIEnv *env, jclass, jstring filename)
{
	jboolean l_IsCopy;

	// Get Java string
	const char *l_filename = env->GetStringUTFChars( filename, &l_IsCopy );

	jint image = g_DesktopIndicatorImages.add( l_filename );

	// Release Java string
    env->ReleaseStringUTFChars( filename, l_filename );

	return image;
}

extern "C"
/*
 * Class:     DesktopIndicator
 * Method:    nativeLoadImageFromResource
 * Signature: (Ljava/lang/int;)I
 */
JNIEXPORT jint JNICALL Java_phex_gui_common_DesktopIndicator_nativeLoadImageIDFromResource
  (JNIEnv *env, jclass, jint resourceid)
{
	jint image = g_DesktopIndicatorImages.addID( resourceid );       //  DVB19Oct99 - use resource rather than external file

	return image;
}

extern "C"
/*
 * Class:     DesktopIndicator
 * Method:    nativeLoadImageFromResource
 * Signature: (Ljava/lang/int;)I
 */
JNIEXPORT jint JNICALL Java_phex_gui_common_DesktopIndicator_nativeLoadImageIDStrFromResource
  (JNIEnv *env, jclass, jstring resourceid)
{
    jboolean l_IsCopy;
	// Get Java string
	const char *l_resourceid = env->GetStringUTFChars( resourceid, &l_IsCopy );	
    
	jint image = g_DesktopIndicatorImages.addIDStr( l_resourceid );       //  DVB19Oct99 - use resource rather than external file

    // Release Java string
    env->ReleaseStringUTFChars( resourceid, l_resourceid );

	return image;
}
