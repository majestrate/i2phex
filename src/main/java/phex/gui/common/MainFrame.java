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
 *  $Id: MainFrame.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeListener;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.common.Environment;
import phex.common.log.NLogger;
import phex.event.ChangeEvent;
import phex.event.PhexEventTopics;
import phex.gui.actions.ExitPhexAction;
import phex.gui.actions.FWToggleAction;
import phex.gui.common.menubar.MainMenuBar;
import phex.gui.common.statusbar.MainStatusBar;
import phex.gui.dialogs.RespectCopyrightDialog;
import phex.gui.dialogs.configwizard.ConfigurationWizardDialog;
import phex.gui.prefs.UpdatePrefs;
import phex.gui.tabs.FWTab;
import phex.gui.tabs.StatisticsTab;
import phex.gui.tabs.download.SWDownloadTab;
import phex.gui.tabs.library.LibraryTab;
import phex.gui.tabs.network.NetworkTab;
import phex.gui.tabs.search.SearchTab;
import phex.gui.tabs.search.monitor.ResultMonitorTab;
import phex.gui.tabs.search.monitor.SearchMonitorTab;
import phex.gui.tabs.security.SecurityTab;
import phex.gui.tabs.upload.UploadTab;
import phex.msg.QueryMsg;
import phex.prefs.core.ProxyPrefs;
import phex.query.QueryHistoryMonitor;
import phex.query.QueryManager;
import phex.servent.Servent;
import phex.xml.sax.gui.DGuiSettings;
import phex.xml.sax.gui.DTab;

import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;

public class MainFrame extends JFrame
{
    public static final int NETWORK_TAB_ID = 1000;
    public static final int SEARCH_TAB_ID = 1101;
    public static final int DOWNLOAD_TAB_ID = 1003;
    public static final int UPLOAD_TAB_ID = 1004;
    public static final int SECURITY_TAB_ID = 1005;
    public static final int STATISTICS_TAB_ID = 1006;
    public static final int LIBRARY_TAB_ID = 1007;
    public static final int SEARCH_MONITOR_TAB_ID = 1008;
    public static final int RESULT_MONITOR_TAB_ID = 1009;

    public static final int NETWORK_TAB_INDEX = 0;
    public static final int SEARCH_TAB_INDEX = 1;
    public static final int DOWNLOAD_TAB_INDEX = 2;
    public static final int UPLOAD_TAB_INDEX = 3;
    public static final int LIBRARY_TAB_INDEX = 4;
    public static final int SECURITY_TAB_INDEX = 5;
    public static final int STATISTICS_TAB_INDEX = 6;
    public static final int SEARCH_MONITOR_TAB_INDEX = 7;
    public static final int RESULT_MONITOR_TAB_INDEX = 8;
    

    private JTabbedPane tabbedPane;
    private JPanel logoPanel;

    private FWToolBar toolbar;
    private NetworkTab networkTab;
    private SearchTab searchTab;
    private UploadTab uploadTab;
    private LibraryTab libraryTab;
    private SWDownloadTab swDownloadTab;
    private SecurityTab securityTab;
    private StatisticsTab statisticsTab;
    private SearchMonitorTab searchMonitorTab;
    private ResultMonitorTab resultMonitorTab;
    private MainStatusBar statusBar;

    public MainFrame( SplashWindow splash, DGuiSettings guiSettings )
    {
        super( );
        
        Icon frameIcon = GUIRegistry.getInstance().getSystemIconPack().getIcon(
            "Frame.IconImage" );
        if (frameIcon != null)
        {
            setIconImage( ((ImageIcon)frameIcon).getImage() );
        }

        //SkinLookAndFeelLoader.tryLoadingSkinLookAndFeel();
        //GUIUtils.setLookAndFeel( "com.jgoodies.looks.windows.ExtWindowsLookAndFeel" );
        //GUIUtils.setLookAndFeel( "com.jgoodies.looks.plastic.PlasticLookAndFeel" );
        //GUIUtils.setLookAndFeel( "com.jgoodies.looks.plastic.Plastic3DLookAndFeel" );
        //GUIUtils.setLookAndFeel( "com.jgoodies.looks.plastic.PlasticXPLookAndFeel" );
        
        setupComponents( guiSettings );
/*
        Hashtable ht = UIManager.getDefaults();
        Enumeration enumr = ht.keys();
        while (enumr.hasMoreElements())
        {
            Object	key = enumr.nextElement();
            //System.out.println(key + "=" + ht.get(key));
        }
*/
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowHandler() );

        DesktopIndicator indicator = GUIRegistry.getInstance().getDesktopIndicator();
        // if sys tray supported
        if ( indicator != null )
        {
            indicator.addDesktopIndicatorListener( new DesktopIndicatorHandler() );
        }

        pack();
        initFrameSize(guiSettings);

        setTitle();
        
        GUIRegistry.getInstance().getServent().getEventService().processAnnotations( this );
    }

    private void initFrameSize(DGuiSettings guiSettings)
    {
        GUIUtils.centerAndSizeWindow(this, 7, 8);
        if ( guiSettings == null )
        {
            return;
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle bounds = getBounds();
        if ( guiSettings.isSetWindowWidth() )
        {
            bounds.width = Math.min( screenSize.width, guiSettings.getWindowWidth() );
        }
        if ( guiSettings.isSetWindowHeight() )
        {
            bounds.height = Math.min( screenSize.height, guiSettings.getWindowHeight() );
        }
        if ( guiSettings.isSetWindowPosX() )
        {
            int posX = guiSettings.getWindowPosX();
            bounds.x = Math.max( 0, Math.min( posX+bounds.width,
                (int)screenSize.getWidth() )-bounds.width );
        }
        if ( guiSettings.isSetWindowPosY() )
        {
            int posY = guiSettings.getWindowPosY();
            bounds.y = Math.max( 0, Math.min( posY+bounds.height,
                (int)screenSize.getHeight() )-bounds.height );
        }
        NLogger.debug(MainFrame.class, "Frame position: " + bounds );
        setBounds( bounds );
    }

    private void setupComponents( DGuiSettings guiSettings )
    {
        tabbedPane = new JTabbedPane();
        tabbedPane.setMinimumSize(new Dimension(50, 50));
        tabbedPane.addChangeListener( new ChangeListener()
            {
                public void stateChanged( javax.swing.event.ChangeEvent e )
                {
                    Component comp = tabbedPane.getSelectedComponent();
                    if ( comp instanceof FWTab )
                    {
                        ((FWTab)comp).tabSelectedNotify();
                    }
                }
            } );
        
        Servent servent = GUIRegistry.getInstance().getServent();

        // Net Tab
        networkTab = new NetworkTab( );
        networkTab.initComponent( guiSettings );
        initializeTab( networkTab, NETWORK_TAB_ID, guiSettings );

        // Search Tab
        QueryManager queryService = servent.getQueryService();
        searchTab = new SearchTab( queryService.getSearchContainer(), 
            queryService.getSearchFilterRules(), 
            servent.getDownloadService() );
        searchTab.initComponent( guiSettings );
        initializeTab( searchTab, SEARCH_TAB_ID, guiSettings );

        //  SWDownload Tab
        swDownloadTab = new SWDownloadTab( servent.getDownloadService() );
        swDownloadTab.initComponent( guiSettings );
        initializeTab( swDownloadTab, DOWNLOAD_TAB_ID, guiSettings );

        //  Upload Tab
        uploadTab = new UploadTab( );
        uploadTab.initComponent( guiSettings );
        initializeTab( uploadTab, UPLOAD_TAB_ID, guiSettings );
        
        //  Library Tab
        libraryTab = new LibraryTab( );
        libraryTab.initComponent( guiSettings );
        initializeTab( libraryTab, LIBRARY_TAB_ID, guiSettings );

        //  Security Tab
        securityTab = new SecurityTab( );
        securityTab.initComponent( guiSettings );
        initializeTab( securityTab, SECURITY_TAB_ID, guiSettings );

        //  Statistics Tab
        statisticsTab = new StatisticsTab( );
        statisticsTab.initComponent( guiSettings );
        initializeTab( statisticsTab, STATISTICS_TAB_ID, guiSettings );
        
        //  Search Monitor Tab
        QueryHistoryMonitor queryHistoryMonitor = new QueryHistoryMonitor();
        servent.getMessageService().addMessageSubscriber(
            QueryMsg.class, queryHistoryMonitor );
        searchMonitorTab = new SearchMonitorTab( queryHistoryMonitor );
        searchMonitorTab.initComponent( guiSettings );
        initializeTab( searchMonitorTab, SEARCH_MONITOR_TAB_ID, guiSettings );
        
        //  Result Monitor Tab
        resultMonitorTab = new ResultMonitorTab( servent.getDownloadService() );
        resultMonitorTab.initComponent( guiSettings );
        initializeTab( resultMonitorTab, RESULT_MONITOR_TAB_ID, guiSettings );

        if ( tabbedPane.getTabCount() == 0 )
        {
            getContentPane().add( BorderLayout.CENTER, getLogoPanel() );
        }
        else
        {
            tabbedPane.setSelectedIndex( 0 );
            getContentPane().add(BorderLayout.CENTER, tabbedPane);
        }

        // menu bar
        JMenuBar menubar = new MainMenuBar( this, guiSettings );
        setJMenuBar( menubar );
        // property changed to HeaderStyle.BOTH in case a toolbar is activated.
        menubar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.SINGLE);

        // toolbar
        boolean isToolbarVisible = true;
        if ( guiSettings != null && guiSettings.isSetToolbarVisible() )
        {
            isToolbarVisible = guiSettings.isToolbarVisible();
        }
        if ( isToolbarVisible )
        {
            setToolbarVisible( true );
        }
        
        // Status Bar
        boolean isStatusbarVisible = true;
        if ( guiSettings != null && guiSettings.isSetStatusbarVisible() )
        {
            isStatusbarVisible = guiSettings.isStatusbarVisible();
        }
        if ( isStatusbarVisible )
        {
            setStatusbarVisible( true );
        }
    }

    public void setTitle()
    {
        StringBuilder builder = new StringBuilder( Environment.getPhexVendor() );
        builder.append(" - ");
        builder.append( GUIRegistry.getInstance().getServent().getGnutellaNetwork().getName() );

        if ( ProxyPrefs.UseSocks5.get().booleanValue() )
        {
            builder.append( "  (via Proxy Server)" );
        }
        super.setTitle( builder.toString() );
    }

    public void setTabVisible( final FWTab tab, boolean state )
    {
        if ( state )
        {
            int tabCount = tabbedPane.getTabCount();
            int modelPos = tab.getIndex();
            int pos;
            if ( tabCount == 0 )
            {
                pos = 0;
            }
            else
            {
                pos = tabCount;
                // check prev tab to find right position to show
                FWTab tmpTab = (FWTab) tabbedPane.getComponentAt( pos - 1 );
                int tmpIdx = tmpTab.getIndex();
                while( tmpIdx > modelPos )
                {
                    pos --;
                    if ( pos == 0 )
                    {//we are at the first position.
                        break;
                    }
                    tmpTab = (FWTab) tabbedPane.getComponentAt( pos - 1 );
                    tmpIdx = tmpTab.getIndex();
                }
            }

            int orgTabCount = tabbedPane.getTabCount();


            if ( orgTabCount == 0 )
            {
                getContentPane().remove( getLogoPanel() );
                getContentPane().add( BorderLayout.CENTER, tabbedPane );
                tabbedPane.setVisible( true );
                getContentPane().invalidate();
                getContentPane().repaint();
            }
            tabbedPane.insertTab( tab.getName(), tab.getIcon(), tab,
                tab.getToolTip(), pos );
            tabbedPane.setSelectedIndex( pos );
            if ( orgTabCount == 0 )
            {
                tab.setVisible( true );
            }
        }
        else
        {
            tabbedPane.remove( tab );

            if ( tabbedPane.getTabCount() == 0 )
            {
                getContentPane().remove( tabbedPane );
                getContentPane().add( BorderLayout.CENTER, getLogoPanel() );
                getContentPane().invalidate();
                getContentPane().repaint();
                tabbedPane.setVisible( false );
            }
        }
    }

    public void setSelectedTab( int tabID )
    {
        FWTab tab = getTab( tabID );
        setSelectedTab( tab );
    }

    public FWTab getSelectedTab()
    {
        if ( tabbedPane.getTabCount() == 0 )
        {
            return null;
        }
        return (FWTab)tabbedPane.getSelectedComponent();
    }

    public void setSelectedTab( FWTab tab )
    {
        // Sanity check
        if (tab == null)
        {
            return;
        }

        // If the tab is not visible, then first make it visible.
        FWToggleAction action = tab.getToggleTabViewAction();
        // hope the action selected state is always matching the tab visible state...
        if ( !action.isSelected() )
        {
            action.actionPerformed( new ActionEvent( this, 0, null ) );
        }

        // Select the tab.
        tabbedPane.setSelectedComponent( tab );
    }

    public FWTab getTab( int tabID )
    {
        switch( tabID )
        {
            case NETWORK_TAB_ID:
                return networkTab;
            case SEARCH_TAB_ID:
                return searchTab;
            case DOWNLOAD_TAB_ID:
                return swDownloadTab;
            case SECURITY_TAB_ID:
                return securityTab;
            case STATISTICS_TAB_ID:
                return statisticsTab;
            case UPLOAD_TAB_ID:
                return uploadTab;
            case LIBRARY_TAB_ID:
                return libraryTab;
            case SEARCH_MONITOR_TAB_ID:
                return searchMonitorTab;
            case RESULT_MONITOR_TAB_ID:
                return resultMonitorTab;
            default:
                NLogger.warn( MainFrame.class, "Unknown tab id: " + tabID );
                return null;
        }
    }

    public void saveGUISettings( DGuiSettings dSettings )
    {
        Rectangle bounds = getBounds();
        dSettings.setWindowHeight( bounds.height );
        dSettings.setWindowWidth( bounds.width );
        dSettings.setWindowPosX( bounds.x );
        dSettings.setWindowPosY( bounds.y );
        dSettings.setToolbarVisible( toolbar != null );
        networkTab.appendDGuiSettings( dSettings );
        searchTab.appendDGuiSettings( dSettings );
        swDownloadTab.appendDGuiSettings( dSettings );
        uploadTab.appendDGuiSettings( dSettings );
        libraryTab.appendDGuiSettings( dSettings );
        securityTab.appendDGuiSettings( dSettings );
        statisticsTab.appendDGuiSettings( dSettings );
        searchMonitorTab.appendDGuiSettings( dSettings );
        resultMonitorTab.appendDGuiSettings( dSettings );
    }
    
    private void initializeTab( FWTab tab, int tabID, DGuiSettings guiSettings )
    {
//long start = System.currentTimeMillis();
        DTab dTab = GUIUtils.getDGuiTabById( guiSettings, tabID );
        boolean state = tab.isVisibleByDefault();
        if ( dTab != null && dTab.isSetVisible() )
        {
            state = dTab.isVisible();
        }
        setTabVisible( tab, state );
//long stop = System.currentTimeMillis();
//System.out.println( tabID + " - " + (stop - start) + "" );
    }

    private JPanel getLogoPanel()
    {
        if ( logoPanel == null )
        {
			ImageIcon icon = new ImageIcon( MainFrame.class.getResource(
				SplashWindow.SPLASH_IMAGE_NAME ) );
			Image image = icon.getImage();
            logoPanel = new FWLogoPanel( image );
            logoPanel.setBorder( BorderFactory.createLoweredBevelBorder() );
        }
        return logoPanel;
    }
    
    public boolean isToolbarVisible()
    {
        return toolbar != null;
    }

    public void setToolbarVisible( boolean state )
    {
        if ( state )
        {
            if ( toolbar != null)
            {
                //already visible
                return;
            }
            toolbar = new FWToolBar( JToolBar.HORIZONTAL );
            toolbar.setShowText( false );
    
            toolbar.addAction( GUIRegistry.getInstance().getGlobalAction(
                GUIRegistry.EXIT_PHEX_ACTION ) );
            toolbar.addSeparator();
            toolbar.addAction( GUIRegistry.getInstance().getGlobalAction(
                GUIRegistry.CONNECT_NETWORK_ACTION ) );
            toolbar.addAction( GUIRegistry.getInstance().getGlobalAction(
                GUIRegistry.DISCONNECT_NETWORK_ACTION ) );
            toolbar.addSeparator();
    
            toolbar.addAction( networkTab.getToggleTabViewAction() );
            //toolbar.addAction( searchTab.getToggleTabViewAction() );
            toolbar.addAction( searchTab.getToggleTabViewAction() );
            toolbar.addAction( swDownloadTab.getToggleTabViewAction() );
            toolbar.addAction( uploadTab.getToggleTabViewAction() );
            toolbar.addAction( libraryTab.getToggleTabViewAction() );
            toolbar.addAction( securityTab.getToggleTabViewAction() );
            toolbar.addAction( statisticsTab.getToggleTabViewAction() );
            toolbar.addAction( searchMonitorTab.getToggleTabViewAction() );
            toolbar.addAction( resultMonitorTab.getToggleTabViewAction() );
            
            toolbar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
            getJMenuBar().putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
            
            getContentPane().add(BorderLayout.NORTH, toolbar);
            getContentPane().validate();
        }
        else
        {
            getContentPane().remove( toolbar );
            getContentPane().validate();
            toolbar = null;
        }
    }
    
    public void setStatusbarVisible( boolean state )
    {
        if ( state )
        {
            if ( statusBar != null)
            {
                //already visible
                return;
            }
            statusBar = new MainStatusBar();        
            getContentPane().add( BorderLayout.SOUTH, statusBar );
            getContentPane().validate();
        }
        else
        {
            getContentPane().remove( statusBar );
            getContentPane().validate();
            statusBar = null;
        }
    }
    
    /**
     * Reacts on online status changes.
     */
    @EventTopicSubscriber(topic=PhexEventTopics.Servent_OnlineStatus)
    public void onOnlineStatusEvent( String topic, ChangeEvent event )
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                setTitle();
            }
        });
    }

    /**
     * Class to handle the WindowClosing event on the main frame.
     * 
     */
    private class WindowHandler extends WindowAdapter
    {
        /**
         * Just delegate to the ExitPhexAction acion.
         */
        @Override
        public void windowClosing(WindowEvent e)
        {
            ExitPhexAction.performCloseGUIAction();
        }
        
        @Override
        public void windowOpened(WindowEvent e)
        {
            if ( GUIRegistry.getInstance().isRespectCopyrightNoticeShown() )
            {
                RespectCopyrightDialog dialog = new RespectCopyrightDialog( );
                dialog.setVisible(true);
            }
            if ( UpdatePrefs.ShowConfigWizard.get().booleanValue() )
            {
                ConfigurationWizardDialog dialog = new ConfigurationWizardDialog();
                dialog.setVisible( true );
            }
        }
    }

    private class DesktopIndicatorHandler implements DesktopIndicatorListener
    {
        public void onDesktopIndicatorClicked( DesktopIndicator source )
        {
            setVisible(true);
            source.hideIndicator();
            if ( MainFrame.this.getState() != JFrame.NORMAL )
            {
                MainFrame.this.setState( Frame.NORMAL );
            }
            MainFrame.this.requestFocus();
        }
    }
}