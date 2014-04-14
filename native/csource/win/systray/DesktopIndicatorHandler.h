
#ifndef __DesktopIndicatorHandler_h__
#define __DesktopIndicatorHandler_h__


#include "DesktopIndicatorThread.h"


class DesktopIndicatorHandler
{
	friend DWORD WINAPI DesktopIndicatorThread::ThreadProc( LPVOID lpParameter );

public:

	static DesktopIndicatorHandler *extract( JNIEnv *env, jobject object );

	DesktopIndicatorHandler( JNIEnv *env, jobject object, jint image, const char *tooltip );

	void enable( JNIEnv *env );
	void update( jint image, const char *tooltip );
	void setMenuText( const char *openStr, const char *exitStr );
	void disable();
	void hide();

private:

	enum
	{
		enableCode = 1,
		updateCode = 2,
		disableCode = 3,
		hideCode = 4
	};

	~DesktopIndicatorHandler();

	void doEnable();
	void doUpdate();
	void fireClicked();
	void exitApplication();
	void doHide();

	HWND m_window;
	HICON m_icon;
	jobject m_object;
	jint m_image;
	char *m_tooltip;
	char *m_openStr;
	char *m_exitStr;
	jmethodID m_fireClicked;
	jmethodID m_exitApplication;

	static LRESULT CALLBACK WndProc( HWND hWnd, UINT uMessage, WPARAM wParam, LPARAM lParam );
	static DWORD WINAPI ThreadProc( LPVOID lpParameter );
};


#endif
