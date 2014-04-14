
#include "stdafx.h"
#include "DesktopIndicatorImages.h"


extern HINSTANCE g_instance;


DesktopIndicatorImages g_DesktopIndicatorImages;


//  DVB19Oct99 - new version of add that accepts resource id, and assumes icon is in this DLL
jint DesktopIndicatorImages::addID( jint resourceid )
{
	// Load icon from this dll's resource fork
    HICON l_icon = LoadIcon( g_instance, MAKEINTRESOURCE( resourceid ) );

	if( !l_icon )
		return -1;

	return (jint) l_icon;
}

//  DVB19Oct99 - new version of add that accepts resource id, and assumes icon is in this DLL
jint DesktopIndicatorImages::addIDStr( const char *resourceid )
{
	// Load icon from this dll's resource fork
    HICON l_icon = LoadIcon( g_instance, resourceid );
 
	if( !l_icon )
		return -1;

	return (jint) l_icon;
}


jint DesktopIndicatorImages::add( const char *filename )
{
	// Extract icon from file
	HICON l_icon = ExtractIcon( g_instance, filename, 0 );

	if( !l_icon )
		return -1;

	return (jint) l_icon;
}


void DesktopIndicatorImages::remove( jint image )
{
	HICON l_icon = (HICON) image;

	// Destroy icon
	DestroyIcon( l_icon );
}
