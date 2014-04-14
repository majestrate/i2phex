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
 *  Created on 14.07.2005
 *  --- CVS Information ---
 *  $Id: FileDialogHandler.java 4363 2009-01-16 10:37:30Z gregork $
 */
package phex.gui.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Point;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.SystemUtils;

import phex.gui.macosx.MacOsxGUIUtils;
import phex.utils.DirectoryOnlyFileFilter;

public class FileDialogHandler
{
    public static File openSingleDirectoryChooser( Component parent, 
        String title, String approveBtnText, char approveBtnMnemonic,
        File currentDirectory )
    {
        if ( SystemUtils.IS_OS_MAC_OSX )
        {
            return openMacDirectoryChooser( title, null, null );
        }
        else
        {
            return openDefaultSingleChooser( parent, 
                initDefaultChooser( title, approveBtnText, approveBtnMnemonic, 
                new DirectoryOnlyFileFilter(), JFileChooser.DIRECTORIES_ONLY,
                currentDirectory, null, null ) );
        }
    }
    
    public static File[] openMultipleDirectoryChooser( Component parent, 
        String title, String approveBtnText, char approveBtnMnemonic,
        File currentDirectory )
    {
        return openMultipleDirectoryChooser( parent, title, approveBtnText, 
            approveBtnMnemonic, currentDirectory, null, null );
    }
    
    public static File[] openMultipleDirectoryChooser( Component parent, 
        String title, String approveBtnText, char approveBtnMnemonic,
        File currentDirectory, String notifyPopupTitle, 
        String notifyPopupShortMessage )
    {
        if ( SystemUtils.IS_OS_MAC_OSX )
        {
            return new File[]
            {
                openMacDirectoryChooser( title, notifyPopupTitle, notifyPopupShortMessage )
            };
        }
        else
        {
            return openDefaultMultiChooser( parent, 
                initDefaultChooser( title, approveBtnText, approveBtnMnemonic, 
                new DirectoryOnlyFileFilter(), JFileChooser.DIRECTORIES_ONLY,
                currentDirectory, notifyPopupTitle, notifyPopupShortMessage ) );
        }
    }
    
    private static File openMacDirectoryChooser( String title,
        String notifyPopupTitle, String notifyPopupShortMessage )
    {
        // create folder dialog through other class this prevents 
        // NoClassDefFoundError on Windows systems since the import of the
        // required OS X classes is elsewhere.
        FileDialog dia = MacOsxGUIUtils.createFolderDialog(); 
        dia.setTitle(title);
        
        // unfortunatly its not possible to display notification popup
        // besides heavy weight dialog.
        //if ( notifyPopupTitle != null || notifyPopupShortMessage != null )
        //{
            //displayMacNotificationPopup( dia, notifyPopupTitle, 
            //    notifyPopupShortMessage );
        //}
        
        DirectoryOnlyFileFilter filter = new DirectoryOnlyFileFilter();
        dia.setFilenameFilter( new FileFilterWrapper(
            filter) );
        dia.setVisible( true );
        String dirStr = dia.getDirectory();
        String fileStr = dia.getFile();
        
        if( dirStr == null || fileStr == null )
        {
            return null;
        }
        File file = new File(dirStr, fileStr);
        // validate filter
        if( !filter.accept(file) )
        {
            return null;
        }
        return file;
    }
    
    private static JFileChooser initDefaultChooser( String title,
        String approveBtnText, char approveBtnMnemonic, FileFilter filter,
        int mode, File currentDirectory, String notifyPopupTitle,
        String notifyPopupShortMessage )
    {
        JFileChooser chooser = new JFileChooser();
        
        if ( notifyPopupTitle != null || notifyPopupShortMessage != null )
        {
            displayNotificationPopup( chooser, notifyPopupTitle, 
                notifyPopupShortMessage );
        }
        
        if ( currentDirectory != null )
        {
            chooser.setCurrentDirectory(currentDirectory);
        }
        if ( filter != null )
        {
            chooser.setFileFilter( filter );
        }
        chooser.setFileSelectionMode( mode );
        chooser.setDialogTitle( title );
        chooser.setApproveButtonText( approveBtnText );
        chooser.setApproveButtonMnemonic( approveBtnMnemonic );
        return chooser;
    }
    
    private static File[] openDefaultMultiChooser( Component parent, 
        JFileChooser chooser )
    {
        chooser.setMultiSelectionEnabled( true );
        int returnVal = chooser.showOpenDialog( parent );
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            return chooser.getSelectedFiles();
        }
        return null;
    }
    
    private static File openDefaultSingleChooser( Component parent, 
        JFileChooser chooser )
    {
        chooser.setMultiSelectionEnabled( false );
        int returnVal = chooser.showOpenDialog( parent );
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /**
     * @param chooser
     */
    private static void displayNotificationPopup( JComponent chooser,
        String title, String shortMessage )
    {
        final SlideInWindow window = new SlideInWindow( title, 0);
        window.setShortMessage(shortMessage, true);
        window.setHideBtnShown( false );
        window.initializeComponent();
        window.setSize( 200, 150 );
        chooser.addAncestorListener(new AncestorListener()
            {
                public void ancestorAdded( AncestorEvent event )
                {
                    window.setVisible(true);
                }

                public void ancestorRemoved( AncestorEvent event )
                {
                    window.setVisible(false);                    
                }

                public void ancestorMoved( AncestorEvent event )
                {
                    Container ancestor = event.getAncestor();
                    Point loc = ancestor.getLocationOnScreen();
                    int xPos = loc.x + ancestor.getWidth() + 5;
                    int yPos = loc.y + ancestor.getHeight() - window.getHeight();
                    window.setLocation( xPos, yPos);
                }
            
            });
    }
    
    /**
     * Displays the notification popup with handling the special Mac OSX cases.
     */
//    private static void displayMacNotificationPopup( Window chooser,
//        String title, String shortMessage )
//    {
//        final SlideInWindow window = new SlideInWindow( title, 0);
//        window.setShortMessage(shortMessage, true);
//        window.setHideBtnShown( false );
//        window.initializeComponent();
//        //window.pack();
//        
//        chooser.addComponentListener(new ComponentListener()
//            {
//                public void componentResized( ComponentEvent e )
//                {
//System.out.println("resized " + e);
//                    Component comp = e.getComponent();
//                    Point loc = comp.getLocationOnScreen();
//                    int xPos = loc.x + comp.getWidth() + 5;
//                    int yPos = loc.y + comp.getHeight() - window.getHeight();
//                    window.setLocation( xPos, yPos);
//                }
//
//                public void componentMoved( ComponentEvent e )
//                {
//System.out.println("moved " + e);
//                    Component comp = e.getComponent();
//                    Point loc = comp.getLocationOnScreen();
//                    int xPos = loc.x + comp.getWidth() + 5;
//                    int yPos = loc.y + comp.getHeight() - window.getHeight();
//                    window.setLocation( xPos, yPos);
//                }
//
//                public void componentShown( ComponentEvent e )
//                {
//                    System.out.println("shown " + e);
//                }
//
//                public void componentHidden( ComponentEvent e )
//                {
//                    System.out.println("hidden " +e );
//                    window.setVisible(false);
//                }
//            });
//        chooser.addWindowListener(new WindowListener()
//            {
//                public void windowActivated( WindowEvent e )
//                {
//                    System.out.println("window activated " + e);
//                }
//
//                public void windowClosed( WindowEvent e )
//                {
//                    System.out.println("window closed " + e);
//                    window.setVisible(false);
//                }
//
//                public void windowClosing( WindowEvent e )
//                {
//                    System.out.println("window closing " + e);
//                }
//
//                public void windowDeactivated( WindowEvent e )
//                {
//                    System.out.println("window deactivated " + e);
//                }
//
//                public void windowDeiconified( WindowEvent e )
//                {
//                    System.out.println("window deiconified " + e);
//                }
//
//                public void windowIconified( WindowEvent e )
//                {
//                    System.out.println("window iconified " + e);
//                }
//
//                public void windowOpened( WindowEvent e )
//                {
//                    System.out.println("window opened " + e);
//                    Component comp = e.getComponent();
//                    Point loc = comp.getLocationOnScreen();
//                    int xPos = loc.x + comp.getWidth() + 5;
//                    int yPos = loc.y + comp.getHeight() - window.getHeight();
//                    window.setLocation( xPos, yPos);
//                    window.setVisible(true);
//                }
//            
//            });
//        chooser.addHierarchyBoundsListener(new HierarchyBoundsListener()
//            {
//                public void ancestorMoved( HierarchyEvent e )
//                {
//                    System.out.println("ancestor moved " + e);
//                }
//
//                public void ancestorResized( HierarchyEvent e )
//                {
//                    System.out.println("ancestor resized " + e);
//                }
//            
//            });
//        chooser.addPropertyChangeListener(new PropertyChangeListener()
//            {
//                public void propertyChange( PropertyChangeEvent evt )
//                {
//                    System.out.println("property changed " + evt);
//                }
//            });
//        chooser.addWindowStateListener(new WindowStateListener()
//            {
//                public void windowStateChanged( WindowEvent e )
//                {
//                    System.out.println("state changed " + e);
//                }
//            });
//        
//        for ( int i = 0; i < chooser.getComponentCount(); i++ )
//        {
//            System.out.println( chooser.getComponent(i) );
//        }
//        System.out.println(chooser.getComponentCount());
//        if ( chooser.getComponentCount() > 0 )
//        {
//        Component comp = chooser.getComponent(0);
//        comp.addComponentListener( new ComponentListener()
//            {
//            public void componentResized( ComponentEvent e )
//            {
//System.out.println("comp resized " + e );
//                Component comp = e.getComponent();
//                Point loc = comp.getLocationOnScreen();
//                int xPos = loc.x + comp.getWidth() + 5;
//                int yPos = loc.y + comp.getHeight() - window.getHeight();
//                window.setLocation( xPos, yPos);
//            }
//
//            public void componentMoved( ComponentEvent e )
//            {
//                System.out.println("comp moved " + e );
//                Component comp = e.getComponent();
//                Point loc = comp.getLocationOnScreen();
//                int xPos = loc.x + comp.getWidth() + 5;
//                int yPos = loc.y + comp.getHeight() - window.getHeight();
//                window.setLocation( xPos, yPos);
//            }
//
//            public void componentShown( ComponentEvent e )
//            {
//                System.out.println("comp shown " + e );
//            }
//
//            public void componentHidden( ComponentEvent e )
//            {
//                System.out.println("comp hidden " + e );
//            }
//        });
//        }
//        
//    }

    private static class FileFilterWrapper implements FilenameFilter
    {
        private FileFilter filter;
        
        public FileFilterWrapper( FileFilter filter )
        {
            this.filter = filter;
        }
        
        public boolean accept(File dir, String name)
        {
            return filter.accept(new File(dir, name));
        }
    }
}
