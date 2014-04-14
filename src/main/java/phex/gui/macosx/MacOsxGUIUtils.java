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
 *  Created on 28.08.2005
 *  --- CVS Information ---
 *  $Id: MacOsxGUIUtils.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.macosx;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import net.roydesign.event.ApplicationEvent;
import net.roydesign.mac.MRJAdapter;
import net.roydesign.ui.FolderDialog;
import phex.common.log.NLogger;
import phex.event.PhexEventService;
import phex.event.PhexEventTopics;
import phex.gui.actions.ExitPhexAction;
import phex.gui.common.GUIRegistry;
import phex.gui.dialogs.AboutDialog;
import phex.gui.dialogs.options.OptionsDialog;
import phex.servent.Servent;

public class MacOsxGUIUtils
{
    public static final void installEventHandlers()
    {
        MRJAdapter.addQuitApplicationListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent evt )
            {
                try
                {
                    NLogger.debug( MacOsxGUIUtils.class,
                        "Called MacOsX quit handler." );
                    ExitPhexAction.performCloseGUIAction();
                }
                catch (Throwable th)
                {
                    NLogger.error( MacOsxGUIUtils.class, th, th );
                }
            }
        } );

        MRJAdapter.setPreferencesEnabled( true );
        MRJAdapter.addPreferencesListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent evt )
            {
                try
                {
                    NLogger.debug( MacOsxGUIUtils.class,
                        "Called MacOsX preferences handler." );
                    OptionsDialog dialog = new OptionsDialog();
                    dialog.setVisible( true );
                }
                catch (Throwable th)
                {
                    NLogger.error( MacOsxGUIUtils.class, th, th );
                }
            }
        } );

        MRJAdapter.addAboutListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent evt )
            {
                try
                {
                    AboutDialog dialog = new AboutDialog();
                    dialog.setVisible( true );
                }
                catch (Throwable th)
                {
                    NLogger.error( MacOsxGUIUtils.class, th, th );
                }
            }
        } );

        MRJAdapter.addOpenDocumentListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent evt )
            {
                try
                {
                    File file = ((ApplicationEvent) evt).getFile();
                    NLogger.debug( MacOsxGUIUtils.class,
                        "Called MacOsX open file handler: " + file );

                    PhexEventService eventService = GUIRegistry.getInstance().getServent().getEventService();
                    
                    String absFileName = file.getAbsolutePath();
                    if ( absFileName.endsWith( ".magma" ) )
                    {
                        eventService.publish( PhexEventTopics.Incoming_Magma, 
                            absFileName );
                    }
                    if ( absFileName.endsWith( ".xml" ) )
                    {
                        eventService.publish( PhexEventTopics.Incoming_Rss, 
                            absFileName );
                    }
                }
                catch (Throwable th)
                {
                    NLogger.error( MacOsxGUIUtils.class, th, th );
                }
            }
        } );
    }

    /**
     * Create folder dialog here. This prevents 
     * NoClassDefFoundError on Windows systems since the import of the
     * required OS X classes is elsewhere.
     */
    public static final FileDialog createFolderDialog()
    {
        return new FolderDialog( GUIRegistry.getInstance().getMainFrame() );
    }
}
