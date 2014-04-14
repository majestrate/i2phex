/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2007 Phex Development Group
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
 *  --- SVN Information ---
 *  $Id: Main.java 4510 2011-06-01 20:56:35Z complication $
 */
package phex;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.lang.SystemUtils;

import phex.common.Environment;
import phex.common.EnvironmentConstants;
import phex.common.ThreadTracking;
import phex.common.log.NLogger;
import phex.connection.LoopbackDispatcher;
import phex.event.PhexEventService;
import phex.event.PhexEventTopics;
import phex.gui.common.GUIRegistry;
import phex.gui.common.MainFrame;
import phex.gui.common.SplashWindow;
import phex.gui.prefs.InterfacePrefs;
import phex.gui.prefs.PhexGuiPrefs;
import phex.prefs.OldCfg;
import phex.prefs.core.PhexCorePrefs;
import phex.servent.Servent;
import phex.utils.FileUtils;
import phex.utils.Localizer;
import phex.utils.SystemProperties;


public class Main
{
    /**
     * Don't use NLogger before arguments have been read ( -c )
     * @param args
     */
    public static void main( String args[] )
    {
        long start = System.currentTimeMillis();
        long end;
        
        validateJavaVersion();

        // parse args...
        Iterator<String> iterator = Arrays.asList( args ).iterator();
        
        // I2PFIXME:
        // Loopback, magma, rss: consider supportability on I2P.
        /*
        String loopbackUri = null;
        String magmaFile = null;
        String rssFile = null; 
        */
        String argument;
         
        while ( (argument = readArgument( iterator ) ) != null ) 
        {
            if ( argument.equalsIgnoreCase("-c") )
            {
                System.out.println("Config path argument found!");
                String path = readArgument(iterator);
                if (path != null)
                {
                    System.out.println("Config path set to [" + path + "].");
                    System.setProperty( SystemProperties.PHEX_CONFIG_PATH_SYSPROP, 
                        path );
                }
            }
            // I2PFIXME:
            // Loopback, magma, rss: consider supportability on I2P.
            /*
            else if ( argument.equalsIgnoreCase("-uri") )
            {
                loopbackUri = readArgument(iterator);
            }
            else if ( argument.equalsIgnoreCase("-magma") )
            {
                magmaFile = readArgument(iterator);
            }
            else if ( argument.equalsIgnoreCase("-rss") )
            {
                rssFile = readArgument(iterator);
            }
            */
        }

        PhexCorePrefs.init();
        
        // I2PFIXME:
        // Loopback, magma, rss: consider supportability on I2P.
        /*
        if ( loopbackUri != null && LoopbackDispatcher.dispatchUri( loopbackUri ) )
        {// correctly dispatched uri
            System.exit( 0 );
        }
        if ( magmaFile != null && LoopbackDispatcher.dispatchMagmaFile( magmaFile ) )
        {// correctly dispatched uri
            System.exit( 0 );
        }
        if ( rssFile != null && LoopbackDispatcher.dispatchRSSFile( rssFile ) )
        {// correctly dispatched uri
            System.exit( 0 );
        }
        */
        
        try
        {
            SplashWindow splashWindow = null;
            try {
                splashWindow = new SplashWindow();
                splashWindow.showSplash();
            }
            catch ( java.awt.HeadlessException ex )
            {
                // running in headless mode so of course the splash
                // doesn't work
            }
            
            // initialize settings

            // I2P: disabled, since it wanted to run config migration routines
            // every single time on Unix systems.
            // SystemProperties.migratePhexConfigRoot();

            PhexGuiPrefs.init();
            File oldConfigFile = Environment.getInstance().getPhexConfigFile(
                EnvironmentConstants.OLD_CONFIG_FILE_NAME );
            if ( oldConfigFile.exists() )
            {
                OldCfg oldCfg = new OldCfg( oldConfigFile );
                oldCfg.load();
                PhexGuiPrefs.updatePreV30Config( oldCfg );
                PhexCorePrefs.updatePreV30Config( oldCfg );
                FileUtils.deleteFileMultiFallback( oldConfigFile );
            }
            
            Localizer.initialize( InterfacePrefs.LocaleName.get() );
            ThreadTracking.initialize();
            Servent.getInstance();
            Servent.getInstance().start();

            end = System.currentTimeMillis();
            NLogger.debug( Main.class, "Pre GUI startup time: " + (end-start) );

            try 
            {
                GUIRegistry.getInstance().initialize( Servent.getInstance() );
            }
            catch ( ExceptionInInitializerError ex )
            {
                // running in headless mode so of course this
                // doesn't work
            }
            if ( splashWindow != null ) splashWindow.dispose();
            MainFrame mainFrame = null;
            mainFrame = GUIRegistry.getInstance().getMainFrame();
            if ( mainFrame != null )
                mainFrame.setVisible(true);
            
            end = System.currentTimeMillis();
            NLogger.debug( Main.class, "Full startup time: " + (end-start) );
            
            PhexEventService eventService = Servent.getInstance().getEventService();
            // I2PFIXME:
            // Loopback, magma, rss: consider supportability on I2P.
            /*
            if ( loopbackUri != null )
            {// correctly dispatched uri
                eventService.publish( PhexEventTopics.Incoming_Uri, loopbackUri );
            }
            if ( magmaFile != null )
            {// correctly dispatched uri
                eventService.publish( PhexEventTopics.Incoming_Magma, magmaFile );
            }
            if ( rssFile != null )
            {// correctly dispatched uri
                eventService.publish( PhexEventTopics.Incoming_Rss, rssFile );
            }
            */
        }
        catch ( Throwable th )
        {
            th.printStackTrace();
            NLogger.error( Main.class, th, th );
            // unhandled application exception... exit
            System.exit( 1 );
        }
    }

    /**
     * @param iterator
     * @return
     */
    private static String readArgument(Iterator<String> iterator)
    {
        if ( !iterator.hasNext() )
        {
            return null;
        }
        String value = iterator.next();
//        if ( value.startsWith( "\"" ))
//        {
//            while (iterator.hasNext())
//            {
//                String additional = (String)iterator.next();
//                value += additional;
//                if ( additional.endsWith("\""))
//                {
//                    break;
//                }
//            }
//            if ( !value.endsWith("\"") )
//            {
//                throw new IllegalArgumentException( "Unterminated argument" );
//            }
//            // cut of starting and ending "
//            value = value.substring( 1, value.length() - 1 );
//        }
        return value;
    }

    /**
     * 
     */
    private static void validateJavaVersion()
    {
        if ( SystemUtils.isJavaVersionAtLeast( 1.5f ) )
        {
            return;
        }
        
        JFrame frame = new JFrame( "Wrong Java Version" );
        frame.setSize( new Dimension( 0, 0 ) );
        frame.setVisible(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension winSize = frame.getSize();
        Rectangle rect = new Rectangle(
            (screenSize.width - winSize.width) / 2,
            (screenSize.height - winSize.height) / 2,
            winSize.width, winSize.height );
        frame.setBounds(rect);
        JOptionPane.showMessageDialog( frame,
            "Please use a newer Java VM.\n" +
            "Phex requires at least Java 1.5.0. You are using Java " + SystemUtils.JAVA_VERSION + "\n" +
        	"To get the latest Java release go to http://java.com.",
            "Wrong Java Version", JOptionPane.WARNING_MESSAGE );
        System.exit( 1 );
    }
}
