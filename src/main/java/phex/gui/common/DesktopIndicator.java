/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2006 Phex Development Group
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  --- CVS Information ---
 *  $Id: DesktopIndicator.java 4363 2009-01-16 10:37:30Z gregork $
 */
package phex.gui.common;

import java.util.ArrayList;
import java.util.Iterator;

import phex.gui.actions.ExitPhexAction;
import phex.utils.Localizer;

/**
* A JNI wrapper for desktop indicators (also called tray icons and taskbar icons).
* <br><br>
*
* The actual implementation is platform specific. Currently only Win32 is supported.
* For unsupported platforms, the desktop indicator doesn't do anything, and is
* otherwise harmless. Likewise, some platforms may not support the tooltip
* feature.
* <br><br>
*
* IMPORTANT!
* <br><br>
*
* Relying on this feature will make your applet or application platform-
* dependant. If possible, treat is as an "added benefit", not a required feature.
**/

public class DesktopIndicator
{
    private static boolean isNativeLibLoaded;

    /**
     * Loads the JNI library, if available.
     * <br><br>
     * Must be called before images are loaded and instances are created.
     **/
    static
    {
        // Assume everything works
        isNativeLibLoaded = true;
        
        // Load JNI library
        try
        {
            System.loadLibrary( "Phex" );
        }
        catch( UnsatisfiedLinkError x )
        {
            // we failed, so unset success flag
            isNativeLibLoaded = false;
        }
    }

    private int image;
    private String tooltip;
    private final ArrayList listeners = new ArrayList();
    
    /**
     * Native handler field. Even though it appears to be unused, don't delete!
     */
    private int handler = 0;

    /**
     * Creates a desktop indicator, initially hidden.
     * <br><br>
     * The image handle is the return value of a previous call to loadImage.
     * @exception UnsupportedOperationException is thrown when library cant
     *   be initialized.
     **/
    public DesktopIndicator()
        throws UnsupportedOperationException
    {
        if ( !isNativeLibLoaded )
        {
            throw new UnsupportedOperationException( "Cant load Phex.dll" );
        }

        image = nativeLoadImageIDStrFromResource( "Phex" );
        tooltip = Localizer.getString( "TTTSysTray" );
        // show and hide once to make it go faster when done the first time.
        showIndicator();
        hideIndicator();
    }


    /**
    * Frees memory used by a image previously loaded with loadImage.
    * <br><br>
    *
    * The memory is freed in any case when the VM is closed.
    **/
    public static void freeImage( int image )
    {
        try
        {
            nativeFreeImage( image );
        }
        catch( UnsatisfiedLinkError x )
        {
        }
    }



    /**
    * Loads an image file to memory.
    * <br><br>
    *
    * The image is loaded from the filename parameter, while the tooltip is
    * supplied in the tooltip parameter. The image file must be in a format
    * recognized by the native platform (for example, a .ico file for Win32).
    * <br><br>
    *
    * A return value of -1 indicates failure. Otherwise, it is the handle of
    * the image to be used for desktop indicators.
    **/
    public static int loadImage( String filename )
    {
        try
        {
            return nativeLoadImage( filename );
        }
        catch( UnsatisfiedLinkError x )
        {
            return -1;
        }
    }

    /**
     * Enables the desktop indicator.
     * <br><br>
     *
     * If the indicator is already active, it is "refreshed" with the filename and tooltip.
     **/
    public void showIndicator()
    {
        setNativeMenuText( Localizer.getString( "OpenPhex" ),
            Localizer.getString( "ExitPhex" ) );
        nativeEnable( image, tooltip );
    }

    /**
     * Hides the desktop indicator.
     **/
    public void hideIndicator()
    {
        nativeHide();
    }

    /**
     * Makes sure that the desktop indicator is hidden before calling.
     **/
    public void removeIndicator()
    {
        nativeDisable();
    }


    /**
    * Updates the desktop indicator with an image and tooltip.
    **/
    public void update( int image, String tooltip )
    {
        this.image = image;
        this.tooltip = tooltip;

        try
        {
            nativeEnable( image, tooltip );
        }
        catch( UnsatisfiedLinkError x )
        {
        }
    }

    ///////////////////// START native methods ////////////////////////
    private synchronized native void setNativeMenuText( String openStr, String exitStr )
        throws UnsatisfiedLinkError;

    private synchronized native void nativeEnable( int image, String tooltip )
        throws UnsatisfiedLinkError;

    private synchronized native void nativeHide()
        throws UnsatisfiedLinkError;

    private synchronized native void nativeDisable()
        throws UnsatisfiedLinkError;

    private synchronized static native int nativeLoadImage( String filename )
        throws UnsatisfiedLinkError;

    private synchronized static native void nativeFreeImage( int image )
        throws UnsatisfiedLinkError;

    private synchronized static native int nativeLoadImageIDFromResource( int inResource )
        throws UnsatisfiedLinkError;
    private synchronized static native int nativeLoadImageIDStrFromResource( String inResource )
        throws UnsatisfiedLinkError;
    ///////////////////// END native methods ////////////////////////



    ///////////////////// START event handling methods ////////////////////////
    /**
     * Adds a listener for clicks.
     **/
    public void addDesktopIndicatorListener( DesktopIndicatorListener listener )
    {
        listeners.add( listener );
    }

    /**
     * Removes a listener for clicks.
     **/
    public void removeDesktopIndicatorListener( DesktopIndicatorListener listener )
    {
        listeners.remove( listener );
    }

    /**
     * Notifies all listeners that the desktop indicator was clicked.
     **/
    public void fireClicked()
    {
        DesktopIndicatorListener listener;
        Iterator iterator = listeners.iterator();
        while( iterator.hasNext() )
        {
            listener = (DesktopIndicatorListener) iterator.next();
            listener.onDesktopIndicatorClicked( this );
        }
    }

    /**
     * Notifies all listeners that the application was closed
     **/
    public void exitApplication()
    {
        // Set a valid ClassLoader. Since we experience troubles
        // with class loader when in Thread comming from native (using JAXBContext)
        // we make sure here that there is a valid class loader.
        if ( Thread.currentThread().getContextClassLoader() == null )
        {
            Thread.currentThread().setContextClassLoader(
                ClassLoader.getSystemClassLoader());
        }
        ExitPhexAction.shutdown();
    }
    ///////////////////// END event handling methods ////////////////////////
}