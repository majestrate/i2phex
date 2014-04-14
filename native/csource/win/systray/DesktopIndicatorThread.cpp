
#include "stdafx.h"
#include "DesktopIndicatorThread.h"
#include "DesktopIndicatorHandler.h"


DesktopIndicatorThread g_DesktopIndicatorThread;


DesktopIndicatorThread::DesktopIndicatorThread()
{
	m_env = NULL;
	m_thread = 0;
	m_vm = NULL;
	m_handlerCount = 0;
}


void DesktopIndicatorThread::MakeSureThreadIsUp( JNIEnv *env )
{
	if( !m_thread )
	{
		// Get VM
		env->GetJavaVM( &m_vm );

		// Start "native" thread
		CreateThread
		(
			NULL,
			0,
			ThreadProc,
			this,
			0,
			&m_thread
		);
	}
}


DesktopIndicatorThread::operator DWORD ()
{
	return m_thread;
}


DWORD WINAPI DesktopIndicatorThread::ThreadProc( LPVOID lpParameter )
{
	DesktopIndicatorThread *l_this = (DesktopIndicatorThread *) lpParameter;

	// Attach the thread to the VM
	l_this->m_vm->AttachCurrentThread( (void**) &l_this->m_env, NULL );

	MSG msg;
	while( GetMessage( &msg, NULL, 0, 0 ) )
	{
		if( msg.message == WM_DESKTOPINDICATOR )
		{
			// Extract handler
			DesktopIndicatorHandler *l_handler = (DesktopIndicatorHandler*) msg.lParam;

			switch( msg.wParam )
			{
			case DesktopIndicatorHandler::enableCode:

				l_this->m_handlerCount++;
				l_handler->doEnable();
				break;

			case DesktopIndicatorHandler::updateCode:

				l_handler->doUpdate();
				break;

			case DesktopIndicatorHandler::hideCode:

				l_handler->doHide();
				break;

			case DesktopIndicatorHandler::disableCode:

				// Destroy it!
				delete l_handler;

				// No more handlers?
				if( !--l_this->m_handlerCount )
				{
					l_this->m_thread = 0;

					// Detach thread from VM
					l_this->m_vm->DetachCurrentThread();

					// Time to die
					ExitThread( 0 );
				}
				break;
			}
		}
		else
		{
			TranslateMessage( &msg );
			DispatchMessage( &msg );
		}
	}

	// Detach thread from VM
	l_this->m_vm->DetachCurrentThread();

	return 0;
}
