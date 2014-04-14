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
 *  --- CVS Information ---
 *  $Id: LookAndFeelUtils.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.common;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.lang.SystemUtils;

import phex.common.log.NLogger;
import phex.utils.ClassUtils;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticTheme;


/**
 * This class holds a collection of Themes with there associated LaF and overs
 * retrieval methods.
 */
public class LookAndFeelUtils
{
    private static final String[] themeNames = {
        "BrownSugar",
        "DarkStar",
        "DesertBlue",   
        "DesertBluer",
        "DesertGreen",  
        "DesertRed",
        "DesertYellow",
        "ExperienceBlue",
        "ExperienceGreen",
        "ExperienceRoyale",
        "LightGray",
        "Silver",
        "SkyBlue",
        "SkyBluer",     
        "SkyGreen",
        "SkyKrupp",
        "SkyPink",
        "SkyRed",
        "SkyYellow"};
    private static ThemeInfo[] plasticThemes;
    
    public static UIManager.LookAndFeelInfo[] getAvailableLAFs()
    {
        List<UIManager.LookAndFeelInfo> list = new ArrayList<UIManager.LookAndFeelInfo>();
        
        if ( SystemUtils.IS_OS_MAC_OSX )
        {
            list.add( new UIManager.LookAndFeelInfo
                ("Macintosh", UIManager.getSystemLookAndFeelClassName() ) );
        }
        
        list.add( new UIManager.LookAndFeelInfo(
            "PlasticXP (default)", Options.PLASTICXP_NAME ) );
        
        list.add( new UIManager.LookAndFeelInfo(
            "Metal", "javax.swing.plaf.metal.MetalLookAndFeel") );
        //list.add( new UIManager.LookAndFeelInfo(
        //    "CDE/Motif", Options.EXT_MOTIF_NAME ) );
            
        if ( SystemUtils.IS_OS_WINDOWS )
        {
            // This LAF will use the Java 1.4.2 avaiable XP look on XP systems
            list.add( new UIManager.LookAndFeelInfo(
                "Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel") );
        }
        
        
        
        // The Java 1.4.2 available GTK+ LAF seems to be buggy and is not working
        // correctly together with the Swing UIDefault constants. Therefore we need
        // to wait with support of it
        Class gtkLAFClass;
        try
        {
            gtkLAFClass =
                Class.forName("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        }
        catch (ClassNotFoundException e)
        {
            gtkLAFClass = null;
        }
        if ( gtkLAFClass != null )
        {
            list.add( new UIManager.LookAndFeelInfo(
                "GTK", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel") );
        }
        
        UIManager.LookAndFeelInfo[] lafs = new UIManager.LookAndFeelInfo[ list.size() ];
        list.toArray( lafs );
        return lafs;
    }

    /**
     * @param lafClassName
     */
    public static ThemeInfo[] getAvailableThemes(String lafClassName)
    {
        if ( lafClassName.equals( Options.PLASTICXP_NAME ) )
        {
            initPlasticThemes();
            return plasticThemes;
        }
        return new ThemeInfo[0];
    }
    
    /**
     * @param lafClassName
     */
    public static ThemeInfo getCurrentTheme( String lafClassName )
    {
        if ( Options.PLASTICXP_NAME.equals( lafClassName ) )
        {
            PlasticTheme myCurrentTheme = PlasticLookAndFeel.getPlasticTheme();
            if ( myCurrentTheme == null )
            {
                return null;
            }
            Class<? extends PlasticTheme> clazz = myCurrentTheme.getClass();
            String name = clazz.getName();
            return new ThemeInfo( name, name );
        }
        return null;
    }
    
    public static void setCurrentTheme( String lafClassName, Object theme )
    {
        if ( lafClassName.equals( Options.PLASTICXP_NAME ) )
        {
            PlasticLookAndFeel.setPlasticTheme( (PlasticTheme)theme );
            try
            {
                // after setting the theme we must reset the PlasticLAF
                UIManager.setLookAndFeel( UIManager.getLookAndFeel() );
            }
            catch ( UnsupportedLookAndFeelException exp )
            {// this is not expected to happen since we reset a existing LAF
                NLogger.error( LookAndFeelUtils.class, exp, exp );
            }
        }
        GUIUtils.updateComponentsUI();
    }
    
    /**
     * Determines the LAF to use for Phex. The LAF is supported and available.
     * It takes three steps:
     * First the given defaultClass is tried.
     * Second the Phex default LAF is tried.
     * Third the Java default is used.
     * @param defaultClass the default class to try.
     * @return the LAF class to use.
     */
    public static LookAndFeel determineLAF( String defaultClass )
    {
        String lafClass = defaultClass;

        // first.. try the requested LAF
        LookAndFeel laf = (LookAndFeel)ClassUtils.newInstanceQuitly( 
            ClassUtils.classForNameQuitly(lafClass) );
        if ( laf != null && laf.isSupportedLookAndFeel() )
        {
            return laf;
        }
        
        // second.. try the Phex default LAF
        lafClass = getDefaultLAFClassName();
        laf = (LookAndFeel)ClassUtils.newInstanceQuitly( 
            ClassUtils.classForNameQuitly(lafClass) );
        if ( laf != null && laf.isSupportedLookAndFeel() )
        {
            return laf;
        }
        
        // third.. try the Swing default LAF
        lafClass = UIManager.getCrossPlatformLookAndFeelClassName();
        laf = (LookAndFeel)ClassUtils.newInstanceQuitly( 
            ClassUtils.classForNameQuitly(lafClass) );
        return laf;
    }
    
    /**
     * Returns the default LAF class name of the system.
     */
    private static String getDefaultLAFClassName()
    {
        if( SystemUtils.IS_OS_MAC_OSX )
        {
            // set the look and feel to System
            return UIManager.getSystemLookAndFeelClassName();
        }
        else
        {
            // set the look and feel to Metal
            //lafClass = UIManager.getCrossPlatformLookAndFeelClassName();
            return Options.PLASTICXP_NAME;
        }
    }

    /**
     * Sets the look and feel with the given class name.
     * @param className the class name of the look and feel to set
     * @throws LookAndFeelFailedException
     */
	public static void setLookAndFeel( String className ) throws LookAndFeelFailedException
    {
        try
        {
            Class lnfClass = Class.forName( className );
            setLookAndFeel( (LookAndFeel) lnfClass.newInstance() );
        }
        catch ( ClassNotFoundException exp )
        {
            NLogger.error( LookAndFeelUtils.class, 
                "Class not found: " + className, exp );
            JOptionPane.showMessageDialog( 
                GUIRegistry.getInstance().getMainFrame(),
                "Error loading Look & Feel " + exp, "Error", 
                JOptionPane.ERROR_MESSAGE );
            throw new LookAndFeelFailedException( "Class not found: " + className );
        }
        catch ( IllegalAccessException exp )
        {
            NLogger.error( LookAndFeelUtils.class, 
                "Illegal access: " + className, exp );
            JOptionPane.showMessageDialog( 
                GUIRegistry.getInstance().getMainFrame(),
                "Error loading Look & Feel " + exp, "Error", 
                JOptionPane.ERROR_MESSAGE );
            throw new LookAndFeelFailedException( "Illegal access: " + className );
        }
        catch ( InstantiationException exp )
        {
            NLogger.error( LookAndFeelUtils.class, 
                "Instantiation failed: " + className, exp );
            JOptionPane.showMessageDialog( 
                GUIRegistry.getInstance().getMainFrame(),
                "Error loading Look & Feel " + exp, "Error", 
                JOptionPane.ERROR_MESSAGE );
            throw new LookAndFeelFailedException( "Instantiation faield: " + className );
        }
        catch ( Throwable th )
        {
            NLogger.error( LookAndFeelUtils.class, 
                "Error loading LAF: " + className, th );
            JOptionPane.showMessageDialog( 
                GUIRegistry.getInstance().getMainFrame(),
                "Error loading Look & Feel " + th, "Error", 
                JOptionPane.ERROR_MESSAGE );
        }
    }
    
    public static void setLookAndFeel( LookAndFeel laf ) 
        throws LookAndFeelFailedException
    {
        try
        {
            // don't update LAF if already set...
            if ( laf.getID().equals( UIManager.getLookAndFeel().getID() ) )
            {
                return;
            }
            UIManager.setLookAndFeel( laf );
            GUIUtils.updateComponentsUI();
        }
        catch ( UnsupportedLookAndFeelException exp )
        {
            NLogger.error( LookAndFeelUtils.class, 
                "Instantiation faield: " + laf.getName(), exp );
            throw new LookAndFeelFailedException( "Instantiation faield: " + laf.getName() );
        }
    }
    
    private static void initPlasticThemes()
    {
        if ( plasticThemes == null )
        {
            
            PlasticTheme theme = PlasticLookAndFeel.createMyDefaultTheme();
            String defaultName = theme.getClass().getName();
            
            String classPrefix = "com.jgoodies.looks.plastic.theme.";
            plasticThemes = new ThemeInfo[ themeNames.length ];
            
            String displayName;
            String name;
            for ( int i = 0; i < themeNames.length; i++ )
            {
                displayName = name = themeNames[i];
                if ( defaultName.endsWith( name ) )
                {
                    displayName = name + " (default)";
                }
                plasticThemes[i] = new ThemeInfo( displayName, classPrefix + name );
            }
        }
    }
    
    public static class ThemeInfo
    {
        private String name;
        private String className;
        
        public ThemeInfo( String name, String className )
        {
            this.name = name;
            this.className = className;
        }
        
        /**
         * @return
         */
        public String getClassName()
        {
            return className;
        }

        /**
         * @return
         */
        public String getName()
        {
            return name;
        }
        
        public boolean equals( Object o )
        {
            if ( !(o instanceof ThemeInfo ) )
            {
                return false;
            }
            
            return className.equals( ((ThemeInfo)o).className );
            
        }
        
    }
}
