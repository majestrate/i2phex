
#include "stdafx.h"
#include "DesktopIndicatorHandler.h"
#include "DesktopIndicatorThread.h"


#define WM_DESKTOPINDICATOR_CLICK (WM_USER + 1)
#define POPUP_OPEN (WM_USER + 2)
#define POPUP_EXIT (WM_USER + 3)

DesktopIndicatorHandler *DesktopIndicatorHandler::extract( JNIEnv *env, jobject object )
{
	// Get field ID			
	jfieldID l_handlerId = env->GetFieldID( env->GetObjectClass( object ), "handler", "I" );

	// Get field
	DesktopIndicatorHandler *l_handler = (DesktopIndicatorHandler *) env->GetIntField( object, l_handlerId );

	return l_handler;
}


DesktopIndicatorHandler::DesktopIndicatorHandler( JNIEnv *env, jobject object, jint image, const char *tooltip )
{
	m_window = NULL;

	m_icon = (HICON) image;

	// Copy string
	m_tooltip = strdup( tooltip );

	// Reference object
	m_object = env->NewGlobalRef( object );

	// Get method ID
	m_fireClicked = env->GetMethodID( env->GetObjectClass( m_object ), "fireClicked", "()V" );
	
	// Get method ID
	m_exitApplication = env->GetMethodID(env->GetObjectClass(m_object), "exitApplication", "()V");

	// Get field ID
	jfieldID l_handlerId = env->GetFieldID( env->GetObjectClass( m_object ), "handler", "I" );

	// Set field
	env->SetIntField( m_object, l_handlerId, (jint) this );
}


DesktopIndicatorHandler::~DesktopIndicatorHandler()
{
	// Get field ID
	jfieldID l_handlerId = g_DesktopIndicatorThread.m_env->GetFieldID( g_DesktopIndicatorThread.m_env->GetObjectClass( m_object ), "handler", "I" );

	// Set field
	g_DesktopIndicatorThread.m_env->SetIntField( m_object, l_handlerId, 0 );

	// Release our reference
	g_DesktopIndicatorThread.m_env->DeleteGlobalRef( m_object );

	// Delete shell icon
	NOTIFYICONDATA m_iconData;
	m_iconData.cbSize = sizeof( m_iconData );
	m_iconData.uID = 0;
	m_iconData.hWnd = m_window;

	Shell_NotifyIcon( NIM_DELETE, &m_iconData );

	// Destroy window
	DestroyWindow( m_window );

	// Free string
	if( m_tooltip )
		delete m_tooltip;
}


void DesktopIndicatorHandler::enable( JNIEnv *env )
{
	g_DesktopIndicatorThread.MakeSureThreadIsUp( env );
	while( !PostThreadMessage( g_DesktopIndicatorThread, WM_DESKTOPINDICATOR, enableCode, (LPARAM) this ) )
		Sleep( 0 );
}


void DesktopIndicatorHandler::doEnable()
{
	// Register window class
	WNDCLASSEX l_Class;
	l_Class.cbSize = sizeof( l_Class );
	l_Class.style = 0;
	l_Class.lpszClassName = TEXT( "DesktopIndicatorHandlerClass" );
	l_Class.lpfnWndProc = WndProc;
	l_Class.hbrBackground = NULL;
	l_Class.hCursor = NULL;
	l_Class.hIcon = NULL;
	l_Class.hIconSm = NULL;
	l_Class.lpszMenuName = NULL;
	l_Class.cbClsExtra = 0;
	l_Class.cbWndExtra = 0;
	l_Class.hInstance = NULL;	//CB enables this code to work in WIN 98

	if( !RegisterClassEx( &l_Class ) )
		return;

	// Create window
	m_window = CreateWindow
	(
		TEXT( "DesktopIndicatorHandlerClass" ),
		TEXT( "DesktopIndicatorHandler" ),
		WS_POPUP,
		0, 0, 0, 0,
		NULL,
		NULL,
		0,
		NULL
	);

	if( !m_window )
		return;

	// Set this pointer
	SetWindowLong( m_window, GWL_USERDATA, (LONG) this );

	// Add shell icon
	NOTIFYICONDATA m_iconData;
	m_iconData.cbSize = sizeof(NOTIFYICONDATA);
	m_iconData.uFlags = NIF_MESSAGE |  NIF_ICON | NIF_TIP;
	m_iconData.uCallbackMessage = WM_DESKTOPINDICATOR_CLICK;
	m_iconData.uID = 0;
	m_iconData.hWnd = m_window;
	m_iconData.hIcon = m_icon;
	strcpy( m_iconData.szTip, m_tooltip );

	Shell_NotifyIcon( NIM_ADD, &m_iconData );
}


void DesktopIndicatorHandler::setMenuText( const char *openStr, const char *exitStr )
{
    // Free string
	if( m_openStr )
	{
		delete m_openStr;
	}
	if( m_exitStr )
	{
		delete m_exitStr;
	}
	
	// Copy string
	m_openStr = strdup( openStr );
	m_exitStr = strdup( exitStr );
}

void DesktopIndicatorHandler::update( jint image, const char *tooltip )
{
	m_icon = (HICON) image;

	// Free string
	if( m_tooltip )
		delete m_tooltip;

	// Copy string
	m_tooltip = strdup( tooltip );

	PostThreadMessage( g_DesktopIndicatorThread, WM_DESKTOPINDICATOR, updateCode, (LPARAM) this );
}


void DesktopIndicatorHandler::doUpdate()
{
	// Modify shell icon
	NOTIFYICONDATA m_iconData;
	m_iconData.cbSize = sizeof(NOTIFYICONDATA);
	m_iconData.uFlags = NIF_MESSAGE |  NIF_ICON | NIF_TIP;
	m_iconData.uCallbackMessage = WM_DESKTOPINDICATOR_CLICK;
	m_iconData.uID = 0;
	m_iconData.hWnd = m_window;
	m_iconData.hIcon = m_icon;
	strcpy( m_iconData.szTip, m_tooltip );

	Shell_NotifyIcon( NIM_DELETE, &m_iconData );
	Shell_NotifyIcon( NIM_ADD, &m_iconData );
}

void DesktopIndicatorHandler::doHide()
{
// Modify shell icon
	NOTIFYICONDATA m_iconData;
	m_iconData.cbSize = sizeof( m_iconData );
	m_iconData.uID = 0;
	m_iconData.hWnd = m_window;
	m_iconData.hIcon = m_icon;
	strcpy( m_iconData.szTip, m_tooltip );
	Shell_NotifyIcon( NIM_DELETE, &m_iconData );
}

void DesktopIndicatorHandler::hide()
{
	PostThreadMessage( g_DesktopIndicatorThread, WM_DESKTOPINDICATOR, hideCode, (LPARAM) this );
}

void DesktopIndicatorHandler::disable()
{
    // hide shell icon without waiting for thread execution...
	doHide();
	PostThreadMessage( g_DesktopIndicatorThread, WM_DESKTOPINDICATOR, disableCode, (LPARAM) this );
}


void DesktopIndicatorHandler::fireClicked()
{
	g_DesktopIndicatorThread.m_env->CallVoidMethod( m_object, m_fireClicked );
}

void DesktopIndicatorHandler::exitApplication()
{
	g_DesktopIndicatorThread.m_env->CallVoidMethod( m_object, m_exitApplication );
}


LRESULT CALLBACK DesktopIndicatorHandler::WndProc( HWND hWnd, UINT uMessage, WPARAM wParam, LPARAM lParam )
{
    if ( uMessage != WM_DESKTOPINDICATOR_CLICK )
    {
        return DefWindowProc( hWnd, uMessage, wParam, lParam );
    }
    
    DesktopIndicatorHandler *l_this = (DesktopIndicatorHandler *) GetWindowLong( hWnd, GWL_USERDATA );
    
    if ( INT(lParam) == WM_RBUTTONUP )
    {
        POINT cursorPos;
        HMENU popMenu;
        GetCursorPos( &cursorPos );
		
		popMenu = CreatePopupMenu();
		AppendMenu( popMenu, MF_STRING, POPUP_OPEN, l_this->m_openStr );
		AppendMenu( popMenu, MF_STRING, POPUP_EXIT, l_this->m_exitStr );
  
		SetForegroundWindow( hWnd );
		switch(TrackPopupMenu( popMenu, TPM_CENTERALIGN | TPM_BOTTOMALIGN | 
							TPM_LEFTBUTTON | TPM_NONOTIFY | TPM_RETURNCMD, 
							cursorPos.x, cursorPos.y, 0, hWnd, NULL))
		{
			case POPUP_EXIT:	
				l_this->exitApplication();
				return 0;
			case POPUP_OPEN:
				l_this->fireClicked();
				return 0;
			default:
				return 0;   
		}
    }
    else if ( INT(lParam) == WM_LBUTTONUP )
    {
        // Click!
		l_this->fireClicked();
		return 0;
    }
    else
    {
        return 0;
    }
}
