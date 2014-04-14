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
 *  $Id: LibraryTab.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.library;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.common.Environment;
import phex.common.QueryRoutingTable;
import phex.common.URN;
import phex.common.format.NumberFormatUtils;
import phex.common.log.NLogger;
import phex.event.PhexEventTopics;
import phex.gui.actions.FWAction;
import phex.gui.actions.GUIActionPerformer;
import phex.gui.common.BrowserLauncher;
import phex.gui.common.FWElegantPanel;
import phex.gui.common.FWPopupMenu;
import phex.gui.common.FWToolBar;
import phex.gui.common.GUIRegistry;
import phex.gui.common.GUIUtils;
import phex.gui.common.MainFrame;
import phex.gui.common.table.FWSortedTableModel;
import phex.gui.common.table.FWTable;
import phex.gui.dialogs.ExportDialog;
import phex.gui.dialogs.FilterLibraryDialog;
import phex.gui.tabs.FWTab;
import phex.servent.Servent;
import phex.share.ShareFile;
import phex.share.SharedFilesService;
import phex.utils.Localizer;
import phex.utils.SystemShellExecute;
import phex.utils.URLUtil;
import phex.xml.sax.gui.DGuiSettings;
import phex.xml.sax.gui.DTable;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LibraryTab extends FWTab
{
    private static final String SHARED_FILES_TABLE_IDENTIFIER = "SharedFilesTable";
    
    private LibraryTreePane libraryTreePane;

    private JLabel sharedFilesLabel;

    private FWTable sharedFilesTable;

    private FWPopupMenu fileTablePopup;

    private JScrollPane sharedFilesTableScrollPane;

    private SharedFilesTableModel sharedFilesModel;

    public LibraryTab()
    {
        super(MainFrame.LIBRARY_TAB_ID, Localizer.getString("Library"),
            GUIRegistry.getInstance().getPlafIconPack().getIcon("Library.Tab"),
            Localizer.getString("TTTLibrary"), Localizer
                .getChar("LibraryMnemonic"), KeyStroke.getKeyStroke(Localizer
                .getString("LibraryAccelerator")), MainFrame.LIBRARY_TAB_INDEX);
        GUIRegistry.getInstance().getServent().getEventService().processAnnotations( this );
    }

    public void initComponent(DGuiSettings guiSettings)
    {
        CellConstraints cc = new CellConstraints();
        FormLayout tabLayout = new FormLayout("2dlu, fill:d:grow, 2dlu", // columns
            "2dlu, fill:p:grow, 2dlu"); //rows
        PanelBuilder tabBuilder = new PanelBuilder(tabLayout, this);
        JPanel contentPanel = new JPanel();
        FWElegantPanel elegantPanel = new FWElegantPanel( Localizer.getString("Library"),
            contentPanel );
        tabBuilder.add(elegantPanel, cc.xy(2, 2));
        
        FormLayout contentLayout = new FormLayout("fill:d:grow", // columns
            "fill:d:grow"); //rows
        PanelBuilder contentBuilder = new PanelBuilder(contentLayout, contentPanel);
        
        
        MouseHandler mouseHandler = new MouseHandler();
        
        libraryTreePane = new LibraryTreePane( this );
        libraryTreePane.addTreeSelectionListener(
            new SelectionHandler() );
        JPanel tablePanel = createTablePanel( guiSettings, mouseHandler );

        JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
            libraryTreePane, tablePanel );
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        splitPane.setDividerSize(4);
        splitPane.setOneTouchExpandable(false);
        contentBuilder.add(splitPane, cc.xy(1, 1));
       

        sharedFilesLabel = new JLabel( " " );
        sharedFilesLabel.setHorizontalAlignment( JLabel.RIGHT );
        elegantPanel.addHeaderPanelComponent(sharedFilesLabel, BorderLayout.EAST );
        
        fileTablePopup = new FWPopupMenu();
        
        FWAction action;
        
        action = getTabAction( OPEN_FILE_ACTION_KEY );
        fileTablePopup.addAction( action );
        
        action = getTabAction( VIEW_BITZI_ACTION_KEY );
        fileTablePopup.addAction( action );
        
        fileTablePopup.addSeparator();
        libraryTreePane.appendPopupSeparator();
        
        action = getTabAction( RESCAN_ACTION_KEY );
        fileTablePopup.addAction( action );
        libraryTreePane.appendPopupAction( action );
        
        action = getTabAction( EXPORT_ACTION_KEY );
        fileTablePopup.addAction( action );
        libraryTreePane.appendPopupAction( action );
        
        action = getTabAction( FILTER_ACTION_KEY );
        fileTablePopup.addAction( action );
        libraryTreePane.appendPopupAction( action );
    }
    
    private JPanel createTablePanel(DGuiSettings guiSettings, 
        MouseHandler mouseHandler )
    {
        JPanel panel = new JPanel();
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout("fill:d:grow", // columns
            "fill:d:grow, 1dlu, p"); //rows
        PanelBuilder tabBuilder = new PanelBuilder(layout, panel);
        
        sharedFilesModel = new SharedFilesTableModel();
        sharedFilesTable = new FWTable(new FWSortedTableModel(
            sharedFilesModel) );
        GUIUtils.updateTableFromDGuiSettings( guiSettings, sharedFilesTable, 
            SHARED_FILES_TABLE_IDENTIFIER );
        
        sharedFilesTable.activateAllHeaderActions();
        sharedFilesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        sharedFilesTable.addMouseListener(mouseHandler);
        sharedFilesTable.getSelectionModel().addListSelectionListener(
            new SelectionHandler());
        sharedFilesTableScrollPane = FWTable
            .createFWTableScrollPane(sharedFilesTable);
        
        tabBuilder.add(sharedFilesTableScrollPane, cc.xy(1, 1));
        
        FWToolBar shareToolbar = new FWToolBar(FWToolBar.HORIZONTAL);
        shareToolbar.setBorderPainted(false);
        shareToolbar.setFloatable(false);
        tabBuilder.add(shareToolbar, cc.xy(1, 3));
        
        FWAction action;
        
        action = new OpenFileAction();
        addTabAction( OPEN_FILE_ACTION_KEY, action );
        shareToolbar.addAction( action );
        
        action = new ViewBitziTicketAction();
        addTabAction( VIEW_BITZI_ACTION_KEY, action );
        shareToolbar.addAction( action );
        
        shareToolbar.addSeparator();
        
        action = new RescanAction();
        addTabAction( RESCAN_ACTION_KEY, action );
        shareToolbar.addAction( action );
        
        action = new ExportAction();
        addTabAction( EXPORT_ACTION_KEY, action );
        shareToolbar.addAction( action );
        
        action = new FilterAction();
        addTabAction( FILTER_ACTION_KEY, action );
        shareToolbar.addAction( action );
        
        return panel;
    }

    /**
     * This is overloaded to update the table scroll pane on every UI update.
     */
    @Override
    public void updateUI()
    {
        super.updateUI();
        if (sharedFilesTableScrollPane != null)
        {
            FWTable.updateFWTableScrollPane(sharedFilesTableScrollPane);
        }
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Share_Update)
    public void onShareUpdateEvent( String topic, Object event )
    {
        if ( sharedFilesLabel == null )
        {
            // UI not initialized yet.
            return;
        }
        EventQueue.invokeLater( new Runnable()
        {
            public void run()
            {
                SharedFilesService filesService = GUIRegistry.getInstance().getServent().getSharedFilesService();
                QueryRoutingTable qrt = filesService.getLocalRoutingTable();
                
                String label = Localizer.getFormatedString( "LibraryTab_StatsHeader",
                    NumberFormatUtils.formatNumber( filesService.getFileCount() ),
                    NumberFormatUtils.formatSignificantByteSize( filesService.getTotalFileSizeInKb() * 1024L ),
                    NumberFormatUtils.formatDecimal( qrt.getFillRatio(), 2 ),
                    NumberFormatUtils.formatDecimal( qrt.getTableSize()/1024.0, 0 ) );            
                sharedFilesLabel.setText( label );
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////
    /// XML serializing and deserializing
    //////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public void appendDGuiSettings(DGuiSettings dSettings)
    {
        super.appendDGuiSettings(dSettings);
        DTable dTable = GUIUtils.createDTable( sharedFilesTable, SHARED_FILES_TABLE_IDENTIFIER );        
        dSettings.getTableList().getTableList().add(dTable);
    }

    //////////////////////////////////////////////////////////////////////////
    /// Actions
    //////////////////////////////////////////////////////////////////////////
    
    private static final String RESCAN_ACTION_KEY = "RescanAction";
    private static final String VIEW_BITZI_ACTION_KEY = "ViewBitziTicketAction";
    private static final String EXPORT_ACTION_KEY = "ExportAction";
    private static final String FILTER_ACTION_KEY = "FilterAction";
    private static final String OPEN_FILE_ACTION_KEY = "OpenFileAction";
    
    
    class RescanAction extends FWAction
    {
        RescanAction()
        {
            super(Localizer.getString("LibraryTab_Rescan"), 
                GUIRegistry.getInstance().getPlafIconPack().getIcon(
                "Library.Refresh"), Localizer.getString("LibraryTab_TTTRescan"));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            libraryTreePane.updateFileSystem();
            GUIActionPerformer.rescanSharedFiles();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void refreshActionState()
        {
        }
    }
    
    private class ExportAction extends FWAction
    {
        ExportAction()
        {
            super(Localizer.getString("LibraryTab_Export"), 
                GUIRegistry.getInstance().getPlafIconPack().getIcon(
                "Library.Export"), Localizer.getString("LibraryTab_TTTExport"));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            List<ShareFile> selectionList;
            
            int[] viewRows = sharedFilesTable.getSelectedRows();
            if ( viewRows.length > 0 )
            {
                int[] modelRows = sharedFilesTable.convertRowIndicesToModel( viewRows );
                selectionList = new ArrayList<ShareFile>();
                for (int i = 0; i < modelRows.length; i++)
                {
                    if ( modelRows[i] >= 0 )
                    {
                        Object obj = sharedFilesModel.getValueAt( 
                            modelRows[i], SharedFilesTableModel.FILE_MODEL_INDEX);
                        if ( obj == null )
                        {
                            continue;
                        }
                        if ( obj instanceof ShareFile )
                        {
                            ShareFile sFile = (ShareFile)obj;
                            selectionList.add( sFile );
                        }
                    }
                }
            }
            else
            {
                selectionList = Collections.emptyList();
            }
            ExportDialog dialog;
            if ( selectionList.isEmpty() )
            {
                dialog = new ExportDialog(  );
            }
            else
            {
                dialog = new ExportDialog( selectionList );
            }
            dialog.setVisible(true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void refreshActionState()
        {
        }
    }
    
    private class OpenFileAction extends FWAction
    {
        OpenFileAction()
        {
            super(Localizer.getString("LibraryTab_OpenFile"), 
                GUIRegistry.getInstance().getPlafIconPack().getIcon(
                "Library.OpenFile"), Localizer.getString("LibraryTab_TTTOpenFile"));
            refreshActionState();
        }
        
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                int row = sharedFilesTable.getSelectedRow();
                row = sharedFilesTable.translateRowIndexToModel(row);
                if ( row < 0 )
                {
                    return;
                }
                Object obj = sharedFilesModel.getValueAt(row, SharedFilesTableModel.FILE_MODEL_INDEX);            
                if ( obj == null )
                {
                    return;
                }
                final File file;
                if ( obj instanceof ShareFile )
                {
                    ShareFile sFile = (ShareFile)obj;
                    file = sFile.getSystemFile();
                    
                }
                else if ( obj instanceof File )
                {
                    file = (File)obj;
                }
                else
                {
                    return;
                }
                
                Runnable runnable = new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            SystemShellExecute.launchFile( file );
                        }
                        catch ( IOException exp )
                        {// ignore and do nothing..
                        }
                        catch ( Throwable th )
                        {
                            NLogger.error( LibraryTab.class, th, th);
                        }
                    }
                };
                Environment.getInstance().executeOnThreadPool(runnable, "SystenShellExecute");
            }
            catch ( Throwable th )
            {
                NLogger.error( LibraryTab.class, th, th);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void refreshActionState()
        {
            int row = sharedFilesTable.getSelectedRow();
            row = sharedFilesTable.translateRowIndexToModel(row);
            if ( row < 0 )
            {
                setEnabled(false);
                return;
            }
            Object obj = sharedFilesModel.getValueAt(row, SharedFilesTableModel.FILE_MODEL_INDEX);            
            if ( obj == null )
            {
                setEnabled(false);
                return;
            }
            if ( obj instanceof ShareFile ||  obj instanceof File  )
            {
                setEnabled(true);
            }
            else
            {
                setEnabled(false);
            }
        }
    }
    
    private class ViewBitziTicketAction extends FWAction
    {
        public ViewBitziTicketAction()
        {
            super( Localizer.getString( "ViewBitziTicket" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon("Library.ViewBitzi"),
                Localizer.getString( "TTTViewBitziTicket" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            int row = sharedFilesTable.getSelectedRow();
            row = sharedFilesTable.translateRowIndexToModel(row);
            if ( row < 0 )
            {
                return;
            }
            
            Object obj = sharedFilesModel.getValueAt(row, SharedFilesTableModel.FILE_MODEL_INDEX);
            
            if ( obj == null || !(obj instanceof ShareFile) )
            {
                return;
            }
            ShareFile sFile = (ShareFile)obj;
            URN urn = sFile.getURN();
            String url = URLUtil.buildBitziLookupURL( urn );
            try
            {
                BrowserLauncher.openURL( url );
            }
            catch ( IOException exp )
            {
                NLogger.warn( LibraryTab.class, exp);

                Object[] dialogOptions = new Object[]
                {
                    Localizer.getString( "Yes" ),
                    Localizer.getString( "No" )
                };

                int choice = JOptionPane.showOptionDialog( LibraryTab.this,
                    Localizer.getString( "FailedToLaunchBrowserURLInClipboard" ),
                    Localizer.getString( "FailedToLaunchBrowser" ),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
                    dialogOptions, Localizer.getString( "Yes" ) );
                if ( choice == 0 )
                {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new StringSelection( url ), null);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void refreshActionState()
        {
            int row = sharedFilesTable.getSelectedRow();
            row = sharedFilesTable.translateRowIndexToModel(row);
            if ( row < 0 )
            {
                setEnabled( false );
                return;
            }
            Object obj = sharedFilesModel.getValueAt(row, SharedFilesTableModel.FILE_MODEL_INDEX);
            if ( obj == null || !(obj instanceof ShareFile) )
            {
                setEnabled( false );
            }
            else
            {
                setEnabled( true );
            }            
        }
    }

    private class FilterAction extends FWAction
    {
        public FilterAction()
        {
            super( Localizer.getString( "LibraryTab_Filter" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon("Library.Filter"),
                Localizer.getString( "LibraryTab_TTTFilter" ) );
        }
        
        public void actionPerformed(ActionEvent e)
        {
            FilterLibraryDialog dialog = new FilterLibraryDialog();
            dialog.setVisible(true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void refreshActionState()
        {            
        }
    }
    
    
    
    

    //////////////////////////////////////////////////////////////////////////
    /// Listeners
    //////////////////////////////////////////////////////////////////////////

    private class SelectionHandler implements ListSelectionListener,
        TreeSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            if (!e.getValueIsAdjusting())
            {
                refreshTabActions();
            }
        }

        public void valueChanged( TreeSelectionEvent e )
        {
            final Object lastPathComponent = e.getPath().getLastPathComponent();

            // run in separate thread.. not event thread to make sure tree selection
            // changes immediately while table needs a little more to update.
            Environment.getInstance().executeOnThreadPool( new Runnable()
            {
                public void run()
                {
                    sharedFilesTable.clearSelection();
                    if ( lastPathComponent instanceof LibraryNode )
                    {
                        // then fill data..
                        sharedFilesModel.setDisplayDirectory( 
                            ((LibraryNode)lastPathComponent).getSystemFile() );
                    }
                    else
                    {
                        sharedFilesModel.setDisplayDirectory( null );
                    }
                }
            }, "LibraryTableUpdate" );
            refreshTabActions();
        }
    }

    private class MouseHandler extends MouseAdapter implements MouseListener
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(MouseEvent e)
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popupMenu((Component) e.getSource(), e.getX(), e.getY());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popupMenu((Component) e.getSource(), e.getX(), e.getY());
            }
        }

        private void popupMenu(Component source, int x, int y)
        {
            if (source == sharedFilesTable )
            {
                refreshTabActions();
                fileTablePopup.show(source, x, y);
            }
        }
    }
}