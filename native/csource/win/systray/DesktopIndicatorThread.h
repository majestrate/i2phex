
#ifndef __DesktopIndicatorThread_h__
#define __DesktopIndicatorThread_h__


class DesktopIndicatorThread
{
public:

	DesktopIndicatorThread();

	void MakeSureThreadIsUp( JNIEnv *env );

	JNIEnv *m_env;

	operator DWORD ();

private:

	DWORD m_thread;
	JavaVM *m_vm;
	int m_handlerCount;

	static DWORD WINAPI ThreadProc( LPVOID lpParameter );
};

extern DesktopIndicatorThread g_DesktopIndicatorThread;


#define WM_DESKTOPINDICATOR (WM_USER)


#endif
