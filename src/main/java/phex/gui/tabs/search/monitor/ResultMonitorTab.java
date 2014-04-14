/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  $Id: ResultMonitorTab.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.search.monitor;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

import phex.common.Environment;
import phex.common.address.DestAddress;
import phex.common.log.NLogger;
import phex.download.RemoteFile;
import phex.download.swarming.SWDownloadFile;
import phex.download.swarming.SwarmingManager;
import phex.gui.actions.BanHostActionUtils;
import phex.gui.actions.FWAction;
import phex.gui.actions.GUIActionPerformer;
import phex.gui.common.BrowserLauncher;
import phex.gui.common.FWElegantPanel;
import phex.gui.common.FWToolBar;
import phex.gui.common.GUIRegistry;
import phex.gui.common.GUIUtils;
import phex.gui.common.MainFrame;
import phex.gui.common.table.FWTable;
import phex.gui.common.table.FWTableColumn;
import phex.gui.common.table.SortedTableHeaderRenderer;
import phex.gui.common.treetable.JTreeTable;
import phex.gui.dialogs.DownloadConfigDialog;
import phex.gui.models.ISearchDataModel;
import phex.gui.models.SearchTreeTableModel;
import phex.gui.renderer.SearchResultsRowRenderer;
import phex.gui.renderer.SearchTreeCellRenderer;
import phex.gui.tabs.FWTab;
import phex.gui.tabs.search.SearchResultElement;
import phex.msg.QueryResponseMsg;
import phex.query.FilteredQueryResponseMonitor;
import phex.query.SearchFilter;
import phex.servent.Servent;
import phex.share.SharedFilesService;
import phex.utils.Localizer;
import phex.utils.StringUtils;
import phex.utils.URLUtil;
import phex.xml.sax.gui.DGuiSettings;
import phex.xml.sax.gui.DTable;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

// TODO2 add action to delete all passive collected resutls.

/**
 * 
 */
public class ResultMonitorTab extends FWTab
{
    private static final RemoteFile[] EMPTY_REMOTE_FILE_ARRAY = new RemoteFile[0];
    private static final String PASSIVE_SEARCH_TABLE_IDENTIFIER = "PassiveSearchTable";
    
    private final SwarmingManager downloadService;
    
    private JPopupMenu searchResultPopup;
    
    private JTextField mPassiveSearch;
    private JButton mPassiveButton;
    
    private JTreeTable searchTreeTable;
    private SearchTreeTableModel searchTreeTableModel;
    private ResultMonitorDataModel resultDataModel;
    
    private JScrollPane searchTreeTableScrollPane;
    
    public ResultMonitorTab( SwarmingManager downloadService )
    {
        super( MainFrame.RESULT_MONITOR_TAB_ID,
            Localizer.getString( "ResultMonitorTab_ResultMonitor" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "ResultMonitor.Tab" ),
            Localizer.getString( "ResultMonitorTab_TTTResultMonitor" ),
            Localizer.getChar( "ResultMonitorTab_Mnemonic"),
            KeyStroke.getKeyStroke( Localizer.getString(
            "ResultMonitorTab_Accelerator" ) ),
            MainFrame.RESULT_MONITOR_TAB_INDEX);
        if ( downloadService == null )
        {
            throw new NullPointerException( "DownloadService missing" );
        }
        this.downloadService = downloadService;
    }
    
    public void initComponent( DGuiSettings guiSettings )
    {
        CellConstraints cc = new CellConstraints();
        FormLayout tabLayout = new FormLayout("2dlu, fill:d:grow, 2dlu", // columns
            "2dlu, fill:p:grow, 2dlu"); //rows
        PanelBuilder tabBuilder = new PanelBuilder(tabLayout, this);
        JPanel contentPanel = new JPanel();
        FWElegantPanel banner = new FWElegantPanel( Localizer.getString("ResultMonitorTab_ResultMonitor"),
            contentPanel );
        tabBuilder.add(banner, cc.xy(2, 2));
        
        FormLayout contentLayout = new FormLayout(
            "fill:d:grow", // columns
            "p, 1dlu, fill:d:grow, 1dlu, p"); //rows
        PanelBuilder contentBuilder = new PanelBuilder(contentLayout, contentPanel);
                
        JPanel headerPanel = new JPanel( );
        contentBuilder.add( headerPanel, cc.xy( 1, 1 ) );
        FormLayout headerLayout = new FormLayout(
            "d, 1dlu, d, 1dlu, d, 1dlu, d", // columns
            "p"); //rows
        PanelBuilder headerBuilder = new PanelBuilder( headerLayout, headerPanel );
        
        headerBuilder.addLabel( Localizer.getString(
            "ResultMonitorTab_ResultFilter" ), cc.xy( 1, 1 ) );
        mPassiveSearch = new JTextField( "", 20 );
        mPassiveSearch.setToolTipText( Localizer.getString(
            "ResultMonitorTab_TTTPassiveSearch" ) );
        headerBuilder.add( mPassiveSearch, cc.xy( 3, 1 ) );
        mPassiveButton = new JButton( Localizer.getString(
            "ResultMonitorTab_StartPassiveSearch" ) );
        mPassiveButton.setToolTipText( Localizer.getString(
            "ResultMonitorTab_TTTStartPassiveSearch" ) );
        mPassiveButton.addActionListener( new PassiveSearchHandler() );
        headerBuilder.add( mPassiveButton, cc.xy( 5, 1 ) );        

        MouseHandler mouseHandler = new MouseHandler();
        searchTreeTableModel = new SearchTreeTableModel();
        searchTreeTable = new JTreeTable( searchTreeTableModel );
        GUIUtils.updateTableFromDGuiSettings( guiSettings, searchTreeTable, 
            PASSIVE_SEARCH_TABLE_IDENTIFIER );

        searchTreeTable.activateHeaderPopupMenu();
        searchTreeTable.activateColumnResizeToFit();
        JTableHeader header = searchTreeTable.getTableHeader();
        header.setDefaultRenderer( new SortedTableHeaderRenderer(searchTreeTable) );
        header.addMouseListener( new TableHeaderMouseHandler() );
        searchTreeTable.addMouseListener( mouseHandler );
        searchTreeTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        searchTreeTable.getSelectionModel().addListSelectionListener(
            new SearchTreeTableSelectionListener() );
        searchTreeTable.setTreeCellRenderer( new SearchTreeCellRenderer( downloadService ) );
        
        SharedFilesService sharedFilesService = GUIRegistry.getInstance().getServent().getSharedFilesService();
        SearchResultsRowRenderer resultRowRenderer = new SearchResultsRowRenderer(
            sharedFilesService, downloadService );
        List<TableColumn> colList = searchTreeTable.getColumns( true );
        for ( TableColumn column : colList )
        {
            column.setCellRenderer( resultRowRenderer );
        }
        searchTreeTableScrollPane = FWTable.createFWTableScrollPane( searchTreeTable );
        searchTreeTableScrollPane.addMouseListener( mouseHandler );
        
        contentBuilder.add( searchTreeTableScrollPane, cc.xy( 1, 3 ) );
        
        
        resultDataModel = new ResultMonitorDataModel();
        searchTreeTableModel.setDisplayedSearch( resultDataModel );
        
        
        FWToolBar resultToolbar = new FWToolBar( JToolBar.HORIZONTAL );
        resultToolbar.setBorderPainted( false );
        resultToolbar.setFloatable( false );
        contentBuilder.add( resultToolbar, cc.xy( 1, 5 ) );
        
        // init popup menu
        searchResultPopup = new JPopupMenu();

        // add actions to toolbar
        FWAction action = new SWQuickDownloadAction();
        addTabAction( QUICK_DOWNLOAD_ACTION_KEY, action );
        resultToolbar.addAction( action );
        searchResultPopup.add( action );

        action = new SWConfigDownloadAction();
        addTabAction( CONFIG_DOWNLOAD_ACTION_KEY, action );
        resultToolbar.addAction( action );
        searchResultPopup.add( action );

        //action = new AddAsCandidateAction();
        //addTabAction( ADD_AS_CANDIDATE_ACTION_KEY, action );
        //resultToolbar.addAction( action );
        //searchResultPopup.add( action );

        action = new ViewBitziTicketAction();
        addTabAction( VIEW_BITZI_TICKET_ACTION_KEY, action );
        resultToolbar.addAction( action );
        searchResultPopup.add( action );
        searchResultPopup.addSeparator();
        
        action = new AddToFavoritesAction();
        addTabAction( ADD_TO_FAVORITES_ACTION_KEY, action );
        searchResultPopup.add( action );

        action = new BrowseHostAction();
        addTabAction( BROWSE_HOST_ACTION_KEY, action );
        resultToolbar.addAction( action );
        searchResultPopup.add( action );

        action = new ChatToHostAction();
        addTabAction( CHAT_TO_HOST_ACTION_KEY, action );
        resultToolbar.addAction( action );
        searchResultPopup.add( action );
        
        BanHostActionProvider banHostActionProvider = new BanHostActionProvider();
        BanHostActionUtils.BanHostActionMenu bhActionMenu = 
            BanHostActionUtils.createActionMenu( 
            banHostActionProvider );
        
        searchResultPopup.add( bhActionMenu.menu );
        addTabActions( bhActionMenu.actions );
        action = BanHostActionUtils.createToolBarAction( banHostActionProvider );
        resultToolbar.addAction( action );
        addTabAction( action );

        searchResultPopup.addSeparator();
        
        /*searchResultPopup.add( searchTab.getTabAction(
            SearchTab.CREATE_NEW_SEARCH_ACTION ) );
        searchResultPopup.add( searchTab.getTabAction(
            SearchTab.CLEAR_SEARCH_RESULTS_ACTION ) );
        searchResultPopup.add( searchTab.getTabAction(
            SearchTab.REMOVE_FILTER_ACTION ) );
        searchResultPopup.add( searchTab.getTabAction(
            SearchTab.CLOSE_SEARCH_ACTION ) );*/
    }
    
    /**
     * Indicates if this tab is visible by default, when there is no known 
     * visible setting from the user.
     * @return true if visible by default false otherwise.
     */
    @Override
    public boolean isVisibleByDefault()
    {
        return false;
    }
    
    @Override
    public void updateUI()
    {
        super.updateUI();
        if ( searchTreeTableScrollPane != null )
        {
            FWTable.updateFWTableScrollPane( searchTreeTableScrollPane );
        }
    }
    
    /**
     * Returns the selected RemoteFile. In case useRepresent is true and a
     * SearchResultElement containing multiple RemoteFiles is selected a
     * representiv RemoteFile for this collection is returned, if useRepresent is
     * false null is returned in this case.
     * Null is also returned when there are no displayed results datas,
     * or the tree selection path is null.
     * @return the selected RemoteFile or null.
     */
    private RemoteFile getSelectedRemoteFile( boolean useRepresent )
    {
        ISearchDataModel searchDataModel = searchTreeTableModel.getDisplayedResultsData();
        if (searchDataModel == null)
        {
            return null;
        }
        
        TreePath selectionPath = searchTreeTable.getTreeSelectionModel().getSelectionPath();
        if ( selectionPath == null )
        {
            return null;
        }
        RemoteFile remoteFile;
        if ( selectionPath.getPathCount() == 3 )
        {
            remoteFile = (RemoteFile)selectionPath.getPathComponent( 2 );
        }
        else
        {
            SearchResultElement element = (SearchResultElement)selectionPath.getPathComponent( 1 );
            if ( !useRepresent && element.getRemoteFileListCount() != 0 )
            {
                return null;
            }
            remoteFile = element.getSingleRemoteFile();
        }
        
        return remoteFile;
    }
    
    /**
     * Returns all selected remote files. If the represent of a group of
     * remote files is selected all remote files of the represent are returned.
     * If singleForAll is true, then all remote files of the represent are 
     * returned if a single remote file of the represent set is selected.
     * @param allOfRepresent
     * @return list of selected remote files
     */
    public RemoteFile[] getSelectedRemoteFiles( boolean singleForAll )
    {
        ISearchDataModel searchDataModel = searchTreeTableModel.getDisplayedResultsData();
        if (searchDataModel == null)
        {
            return EMPTY_REMOTE_FILE_ARRAY;
        }
        
        TreePath[] selectionPaths = searchTreeTable.getTreeSelectionModel().getSelectionPaths();
        if ( selectionPaths.length == 0 )
        {
            return EMPTY_REMOTE_FILE_ARRAY;
        }
        Set<RemoteFile> remoteFileSet = new HashSet<RemoteFile>();
        for ( int i = 0; i < selectionPaths.length; i++ )
        {
            if ( selectionPaths[i].getPathCount() == 3 )
            {
                if ( singleForAll )
                {
                    SearchResultElement element = (SearchResultElement)selectionPaths[i].getPathComponent( 1 );
                    RemoteFile[] files = element.getRemoteFiles();
                    remoteFileSet.addAll( Arrays.asList( files ) );
                }
                else
                {
                    RemoteFile remoteFile = (RemoteFile)selectionPaths[i].getPathComponent( 2 );
                    remoteFileSet.add( remoteFile );
                }
            }
            else
            {
                SearchResultElement element = (SearchResultElement)selectionPaths[i].getPathComponent( 1 );
                RemoteFile[] files = element.getRemoteFiles();
                remoteFileSet.addAll( Arrays.asList( files ) );
            }            
        }
        RemoteFile[] result = new RemoteFile[ remoteFileSet.size() ];
        remoteFileSet.toArray( result );
        return result;
    }
    
    //////////////////////////////////////////////////////////////////////////
    /// START XML serializing and deserializing
    //////////////////////////////////////////////////////////////////////////

    @Override
    public void appendDGuiSettings( DGuiSettings dSettings )
    {
        super.appendDGuiSettings( dSettings );
        DTable dTable = GUIUtils.createDTable( searchTreeTable, PASSIVE_SEARCH_TABLE_IDENTIFIER );
        dSettings.getTableList().getTableList().add( dTable );
    }

    //////////////////////////////////////////////////////////////////////////
    /// End XML serializing and deserializing
    //////////////////////////////////////////////////////////////////////////
    
    /////////////////////// Start Table Actions///////////////////////////////////

    private static final String QUICK_DOWNLOAD_ACTION_KEY = "SWQuickDownloadAction";
    private static final String CONFIG_DOWNLOAD_ACTION_KEY = "SWConfigDownloadAction";
    private static final String VIEW_BITZI_TICKET_ACTION_KEY = "ViewBitziTicketAction";
    private static final String CHAT_TO_HOST_ACTION_KEY = "ChatToHostAction";
    private static final String BROWSE_HOST_ACTION_KEY = "BrowseHostAction";
    private static final String ADD_TO_FAVORITES_ACTION_KEY = "AddToFavoritesAction";

    private class SWQuickDownloadAction extends FWAction
    {
        public SWQuickDownloadAction()
        {
            super( Localizer.getString( "QuickDownload" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon("ResultMonitor.Download"),
                Localizer.getString( "TTTQuickDownload" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            final RemoteFile[] rfiles = getSelectedRemoteFiles( true );
            Runnable runner = new Runnable()
            {
                public void run()
                {
                    try
                    {
                        for ( int i = 0; i < rfiles.length; i++ )
                        {
                            rfiles[i].setInDownloadQueue( true );
            
                            SWDownloadFile downloadFile = downloadService.getDownloadFile(
                                rfiles[i].getFileSize(), rfiles[i].getURN() );
            
                            if ( downloadFile != null )
                            {
                                downloadFile.addDownloadCandidate( rfiles[i] );
                            }
                            else
                            {
                                RemoteFile dfile = new RemoteFile( rfiles[i] );
                                String searchTerm = StringUtils.createNaturalSearchTerm( dfile.getFilename() );
                                downloadService.addFileToDownload( dfile,
                                    dfile.getFilename(), searchTerm );
                            }
                        }
                    }
                    catch ( Throwable th )
                    {
                        NLogger.error( SWQuickDownloadAction.class, th, th);
                    }
                }
            };
            Environment.getInstance().executeOnThreadPool(runner, "QuickDownloadAction" );
        }

        @Override
        public void refreshActionState()
        {
            if ( searchTreeTable.getSelectedRow() < 0 )
            {
                setEnabled( false );
            }
            else
            {
                setEnabled( true );
            }
        }
    }

    private class SWConfigDownloadAction extends FWAction
    {
        public SWConfigDownloadAction()
        {
            super( Localizer.getString( "Download" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon("ResultMonitor.ConfigDownload"),
                Localizer.getString( "TTTDownload" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            RemoteFile[] rfiles = getSelectedRemoteFiles( true );

            for ( int i = 0; i < rfiles.length; i++ )
            {
                SWDownloadFile downloadFile = downloadService.getDownloadFile(
                    rfiles[i].getFileSize(), rfiles[i].getURN() );

                if ( downloadFile != null )
                {
                    downloadFile.addDownloadCandidate( rfiles[i] );
                    rfiles[i].setInDownloadQueue( true );
                }
                else
                {
                    RemoteFile dfile = new RemoteFile( rfiles[i] );
                    
                    DownloadConfigDialog dialog = new DownloadConfigDialog( dfile );
                    dialog.setVisible( true );
                    // copy status to item in search list.
                    if ( dfile.isInDownloadQueue() )
                    {
                        rfiles[i].setInDownloadQueue( true );
                    }                    
                }
            }
        }

        @Override
        public void refreshActionState()
        {
            if ( searchTreeTable.getSelectedRow() < 0 )
            {
                setEnabled( false );
            }
            else
            {
                setEnabled( true );
            }
        }
    }

    private class ViewBitziTicketAction extends FWAction
    {
        public ViewBitziTicketAction()
        {
            super( Localizer.getString( "ViewBitziTicket" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon("ResultMonitor.ViewBitzi"),
                Localizer.getString( "TTTViewBitziTicket" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            // since getSelectedRemoteFile() returns null on the top level
            // SearchResultElement when it has multiple childs, we use
            // getSelectedRemoteFiles() as a 
            RemoteFile rfile = getSelectedRemoteFile( true );
            if ( rfile == null )
            {
                return;
            }
            String url = URLUtil.buildBitziLookupURL( rfile.getURN() );
            try
            {
                BrowserLauncher.openURL( url );
            }
            catch ( IOException exp )
            {
                NLogger.warn( ViewBitziTicketAction.class, exp, exp);
                Object[] dialogOptions = new Object[]
                {
                    Localizer.getString( "Yes" ),
                    Localizer.getString( "No" )
                };

                int choice = JOptionPane.showOptionDialog( ResultMonitorTab.this,
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

        @Override
        public void refreshActionState()
        {
            if ( searchTreeTable.getSelectedRowCount() == 1 )
            {
                RemoteFile rfile = getSelectedRemoteFile( true );
                if ( rfile != null && rfile.getURN() != null )
                {
                    setEnabled( true );
                    return;
                }
            }
            else
            {
                setEnabled( false );
            }
        }
    }

    private class ChatToHostAction extends FWAction
    {
        public ChatToHostAction()
        {
            super( Localizer.getString( "ChatToHost" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon("ResultMonitor.Chat"),
                Localizer.getString( "TTTChatToHost" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            RemoteFile rfile = getSelectedRemoteFile( false );
            if ( rfile == null )
            {
                return;
            }

            if ( !rfile.getQueryHitHost().isChatSupported() )
            {
                return;
            }
            GUIActionPerformer.chatToHost( rfile.getHostAddress() );
        }

        @Override
        public void refreshActionState()
        {
            if ( searchTreeTable.getSelectedRowCount() == 1 )
            {
                RemoteFile rfile = getSelectedRemoteFile( false );
                if ( rfile != null
                    && rfile.getQueryHitHost().isChatSupported() )
                {
                    setEnabled( true );
                    return;
                }
            }
            setEnabled( false );
        }
    }

    private class BrowseHostAction extends FWAction
    {
        public BrowseHostAction()
        {
            super( Localizer.getString( "BrowseHost" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon("ResultMonitor.BrowseHost"),
                Localizer.getString( "TTTBrowseHost" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            RemoteFile rfile = getSelectedRemoteFile( false );
            if ( rfile == null )
            {
                return;
            }
            if ( !rfile.getQueryHitHost().isBrowseHostSupported() )
            {
                return;
            }
            GUIActionPerformer.browseHost( rfile.getHostAddress() );
        }

        @Override
        public void refreshActionState()
        {
            if ( searchTreeTable.getSelectedRowCount() == 1 )
            {
                RemoteFile rfile = getSelectedRemoteFile( false );
                if ( rfile != null
                    && rfile.getQueryHitHost().isBrowseHostSupported() )
                {
                    setEnabled( true );
                    return;
                }
            }
            setEnabled( false );
        }
    }
    
    private final class BanHostActionProvider implements BanHostActionUtils.BanHostActionProvider
    {
        public DestAddress[] getBanHostAddresses()
        {
            RemoteFile[] files = getSelectedRemoteFiles( false );
            final DestAddress[] addresses = new DestAddress[files.length];
            for (int i = 0; i < files.length; i++)
            {
                addresses[ i ] = files[i].getHostAddress();
            }
            return addresses;
        }

        public boolean isBanHostActionEnabled( boolean allowMultipleAddresses )
        {
            if ( searchTreeTable.getSelectedRow() < 0 || 
                 ( !allowMultipleAddresses && searchTreeTable.getSelectedRowCount() > 1 ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }
    
    private class AddToFavoritesAction extends FWAction
    {
        public AddToFavoritesAction()
        {
            super( Localizer.getString( "AddToFavorites" ),
                GUIRegistry.getInstance().getPlafIconPack().getIcon( "ResultMonitor.FavoriteHost" ),
                Localizer.getString( "TTTAddToFavorites" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            RemoteFile[] files = getSelectedRemoteFiles( false );
            
            DestAddress[] addresses = new DestAddress[files.length];
            for (int i = 0; i < files.length; i++)
            {
                addresses[ i ] = files[i].getHostAddress();
            }
            GUIActionPerformer.addHostsToFavorites( addresses );
        }

        @Override
        public void refreshActionState()
        {
            if ( searchTreeTable.getSelectedRow() < 0 )
            {
                setEnabled( false );
            }
            else
            {
                setEnabled( true );
            }
        }
    }

    /////////////////////// End Table Actions///////////////////////////////////

    
    private class MouseHandler extends MouseAdapter implements MouseListener
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (e.getClickCount() == 2)
            {
                if ( e.getSource() == searchTreeTable )
                {
                    getTabAction( QUICK_DOWNLOAD_ACTION_KEY ).actionPerformed( null );
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popupMenu((Component)e.getSource(), e.getX(), e.getY());
            }
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popupMenu((Component)e.getSource(), e.getX(), e.getY());
            }
        }

        private void popupMenu(Component source, int x, int y)
        {
            if ( source == searchTreeTable || source == searchTreeTableScrollPane )
            {
                searchResultPopup.show(source, x, y);
            }
        }
    }
    
    private class PassiveSearchHandler implements ActionListener
    {
        private FilteredQueryResponseMonitor monitor;
        
        public void actionPerformed(ActionEvent e)
        {
            Servent servent = GUIRegistry.getInstance().getServent();
            if ( monitor == null )
            {
                monitor = new FilteredQueryResponseMonitor(
                    servent );
            }
            if ( monitor.getPassiveSearchFilter() == null)
            {
                String searchStr = mPassiveSearch.getText().trim();
                SearchFilter filter = new SearchFilter();
                filter.updateSearchFilter( searchStr );
                monitor.updatePassiveSearchFilter( filter );
                servent.getMessageService().addMessageSubscriber(
                    QueryResponseMsg.class, monitor );
                mPassiveButton.setText( Localizer.getString(
                    "ResultMonitorTab_StopPassiveSearch" ));
            }
            else
            {
                monitor.updatePassiveSearchFilter( null );
                servent.getMessageService().removeMessageSubscriber(
                    QueryResponseMsg.class, monitor );
                mPassiveButton.setText( Localizer.getString(
                    "ResultMonitorTab_StartPassiveSearch"));
            }
        }
    }
    
    private class TableHeaderMouseHandler extends MouseAdapter implements MouseListener
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            FWTableColumn column = searchTreeTable.getResizingColumn( e.getPoint() );
            int clickCount = e.getClickCount();

            // only handle sorting on one click count and when not clicked
            // between two columns.
            if ( column == null && clickCount == 1 )
            {
                handleColumnSorting( e );
            }
        }
            
        /**
         * Handles sorting on a column on single click.
         */
        private void handleColumnSorting( MouseEvent e )
        {
            int viewIdx = searchTreeTable.getColumnModel().getColumnIndexAtX( e.getX() );
            int modelIdx = searchTreeTable.convertColumnIndexToModel( viewIdx );
            if( modelIdx == -1)
            {
                return;
            }

            // reverse order on each click
            FWTableColumn column = (FWTableColumn)searchTreeTable.getColumnModel().getColumn( viewIdx );
            boolean isAscending = column.reverseSortingOrder();

            searchTreeTableModel.sortByColumn( modelIdx, isAscending );
        }
    }
    
    /**
     * We like to select all expanded rows of the parent row in the table. 
     */
    private class SearchTreeTableSelectionListener implements ListSelectionListener
    {
        public void valueChanged( ListSelectionEvent e )
        {
            if ( !e.getValueIsAdjusting() )
            {
                 refreshTabActions();
            }
            /*
             // causes all child rows to be selected...
            ListSelectionModel model = (ListSelectionModel)e.getSource();
            int startIdx = e.getFirstIndex();
            int endIdx = e.getLastIndex();
            for (int i = startIdx; i <= endIdx; i++)
            {
                if ( model.isSelectedIndex(i) )
                {
                    Object node = searchTreeTable.getNodeOfRow(i);
                    if ( node == null )
                    {
                        continue;
                    }
                    if ( !searchTreeTableModel.isLeaf( node ) &&
                        searchTreeTable.getTree().isExpanded(i) )
                    {
                        int start = i + 1;
                        int stop = i + searchTreeTableModel.getChildCount( node );
                        model.addSelectionInterval(start, stop);
                    }
                }
            }
            */
        }
    }
}
