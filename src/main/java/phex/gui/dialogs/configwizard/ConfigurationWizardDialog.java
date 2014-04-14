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
 *  Created on 22.11.2005
 *  --- CVS Information ---
 *  $Id: ConfigurationWizardDialog.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.dialogs.configwizard;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import phex.common.bandwidth.BandwidthManager;
import phex.common.log.NLogger;
import phex.gui.common.DialogBanner;
import phex.gui.common.GUIRegistry;
import phex.gui.dialogs.options.OptionsDialog;
import phex.gui.prefs.UpdatePrefs;
import phex.servent.Servent;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ConfigurationWizardDialog extends JDialog
{
    private static final int WELCOME_PAGE = 1;
    private static final int BANDWIDTH_PAGE = 2;
    private static final int DIRECTORY_PAGE = 3;
    private static final int CONTENTCOMMUNITY_PAGE = 4; 
    private static final int SHARING_PAGE = 5;
    
    private static final int GOODBYE_PAGE = 6;
    
    private JPanel wizardContentPanel;
    
    private WelcomePanel welcomePanel;
    private BandwidthPanel bandwidthPanel;
    private DirectoryPanel directoryPanel;
    private SharingPanel sharingPanel;
    private ContentCommunityPanel contentCommunityPanel;
    private GoodbyePanel goodbyePanel;
    
    private int currentPage;
    private JButton finishBtn;
    private JButton backBtn;
    private JButton nextBtn;

    public ConfigurationWizardDialog( )
    {
        super( GUIRegistry.getInstance().getMainFrame(), 
            Localizer.getString( "ConfigWizard_DialogTitle" ), false );
        currentPage = WELCOME_PAGE;
        prepareComponent();
    }

    private void prepareComponent()
    {
        CloseEventHandler closeEventHandler = new CloseEventHandler();
        addWindowListener( closeEventHandler );
        
        Container contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );
        
        JPanel contentPanel = new JPanel();
        //JPanel contentPanel = new FormDebugPanel();
        contentPane.add(contentPanel, BorderLayout.CENTER);
        
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout("4dlu, fill:d:grow, 4dlu", // columns
            "p, p, 4dlu, fill:p:grow, 8dlu," +  // rows
            "p, 2dlu, p, 4dlu" ); //btn rows
        PanelBuilder contentPB = new PanelBuilder(layout, contentPanel);
        int columnCount = layout.getColumnCount();
        int rowCount = layout.getRowCount();
        
        DialogBanner banner = new DialogBanner(
            Localizer.getString( "ConfigWizard_BannerHeader" ), 
            Localizer.getString( "ConfigWizard_BannerSubHeader" ) );
        contentPB.add(banner, cc.xywh(1, 1, columnCount, 1));
        
        contentPB.add(new JSeparator(), cc.xywh(1, 2, columnCount, 1));
        
        wizardContentPanel = new JPanel( new GridLayout(1,1) );
        //wizardContentPanel.setLayout(new BorderLayout());
        contentPB.add( wizardContentPanel, cc.xywh( 2, 4, 1, 1 ) );
                
        // button bar
        contentPB.add( new JSeparator(), cc.xywh( 1, rowCount - 3, columnCount, 1 ) );
        
        backBtn = new JButton( Localizer.getString( "WizardDialog_Back" ) );
        backBtn.addActionListener( new BackBtnListener());
        
        nextBtn = new JButton( Localizer.getString( "WizardDialog_Next" ) );
        nextBtn.setDefaultCapable( true );
        nextBtn.setRequestFocusEnabled( true );
        nextBtn.addActionListener( new NextBtnListener());
        
        finishBtn = new JButton( Localizer.getString( "WizardDialog_Finish" ) );
        finishBtn.addActionListener( new FinishBtnListener());
        
        JButton cancelBtn = new JButton( Localizer.getString( "WizardDialog_Cancel" ) );
        cancelBtn.addActionListener( closeEventHandler );
        
        JPanel btnPanel = ButtonBarFactory.buildWizardBar(backBtn, nextBtn,
            finishBtn, cancelBtn);        
        contentPB.add( btnPanel, cc.xywh( 2, rowCount - 1, columnCount - 2, 1 ) );
        
        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        getRootPane().setDefaultButton( nextBtn );
        
        // set first panel to show...
        updatePage();
        pack();
        int height = Math.max( 400, getHeight() );
        setSize( height*5/4, height );
        
        setLocationRelativeTo( getParent() );
    }
    
    public void setFinishBtnEnabled( boolean state )
    {
        finishBtn.setEnabled(state);
    }
    
    private void closeDialog()
    {
        UpdatePrefs.ShowConfigWizard.set( Boolean.FALSE );
        setVisible(false);
        dispose();
    }
        
    private void updatePage()
    {
        wizardContentPanel.removeAll();
        JPanel newPage = null;
        switch ( currentPage )
        {
        case WELCOME_PAGE:
            if ( welcomePanel == null )
            {
                welcomePanel = new WelcomePanel(this);
            }
            newPage = welcomePanel;
            backBtn.setEnabled(false);
            nextBtn.setEnabled(true);
            break;
        case BANDWIDTH_PAGE:
            if ( bandwidthPanel == null )
            {
                BandwidthManager bandwidthService = GUIRegistry.getInstance().getServent().getBandwidthService();
                bandwidthPanel = new BandwidthPanel(bandwidthService, this);
            }
            newPage = bandwidthPanel;
            backBtn.setEnabled(true);
            nextBtn.setEnabled(true);
            break;
        case DIRECTORY_PAGE:
            if ( directoryPanel == null )
            {
                directoryPanel = new DirectoryPanel(this);
            }
            newPage = directoryPanel;
            backBtn.setEnabled(true);
            nextBtn.setEnabled(true);
            break;
        case SHARING_PAGE:
            if ( sharingPanel == null )
            {
                sharingPanel = new SharingPanel(this);
            }
            newPage = sharingPanel;
            nextBtn.setEnabled(true);
            backBtn.setEnabled(true);
            break;
        case CONTENTCOMMUNITY_PAGE:
            if ( contentCommunityPanel == null )
            {
                contentCommunityPanel = new ContentCommunityPanel(this);
            }
            newPage = contentCommunityPanel;
            nextBtn.setEnabled(true);
            backBtn.setEnabled(true);
            break;
        case GOODBYE_PAGE:
            if ( goodbyePanel == null )
            {
                goodbyePanel = new GoodbyePanel(this);
            }
            newPage = goodbyePanel;
            nextBtn.setEnabled(false);
            backBtn.setEnabled(true);
            break;
        }
        wizardContentPanel.add(newPage, BorderLayout.CENTER);
        
        wizardContentPanel.doLayout();
        wizardContentPanel.revalidate();
        wizardContentPanel.repaint();

        // here we adjust the size of the dialog if necessary
        Dimension prefSize = getPreferredSize();
        Dimension currSize = getSize();
        if ( prefSize.height > currSize.height )
        {
            int height = Math.max( prefSize.height, currSize.height );
            setSize( height*5/4, height );
            doLayout();
        }        
    }
    
    private final class NextBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                switch ( currentPage )
                {
                case WELCOME_PAGE:
                    currentPage = BANDWIDTH_PAGE;
                    break;
                case BANDWIDTH_PAGE:
                    currentPage = DIRECTORY_PAGE;
                    break;
                case DIRECTORY_PAGE:
                    if ( !directoryPanel.checkInput() )
                    {
                        currentPage = DIRECTORY_PAGE;
                    }
                    else
                    {
                        currentPage = CONTENTCOMMUNITY_PAGE;
                    }
                    break;
                case CONTENTCOMMUNITY_PAGE:
                    currentPage = SHARING_PAGE;
                    break;
                case SHARING_PAGE:
                    currentPage = GOODBYE_PAGE;
                    break;
                }
                updatePage();
            }
            catch ( Throwable th )
            {
                NLogger.error( NextBtnListener.class, th, th );
            }
        }
    }
    
    private final class BackBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                switch ( currentPage )
                {
                case BANDWIDTH_PAGE:
                    currentPage = WELCOME_PAGE;
                    break;
                case DIRECTORY_PAGE:
                    currentPage = BANDWIDTH_PAGE;
                    break;
                case CONTENTCOMMUNITY_PAGE:
                    currentPage = DIRECTORY_PAGE;
                    break;
                case SHARING_PAGE:
                    currentPage = CONTENTCOMMUNITY_PAGE;
                    break;
                case GOODBYE_PAGE:
                    currentPage = SHARING_PAGE;
                    break;
                }
                updatePage();
            }
            catch ( Throwable th )
            {
                NLogger.error( BackBtnListener.class, th, th );
            }
        }
    }

    private final class FinishBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                if ( bandwidthPanel != null )
                {
                    bandwidthPanel.saveSettings();
                }
                if ( directoryPanel != null )
                {
                    if ( !directoryPanel.checkInput() )
                    {
                        currentPage = DIRECTORY_PAGE;
                        updatePage();
                        return;
                    }
                    directoryPanel.saveSettings();
                }
                if ( contentCommunityPanel != null )
                {
                    contentCommunityPanel.saveSettings();
                }
                boolean openOptions = false;
                if ( goodbyePanel != null )
                {
                    openOptions = goodbyePanel.isOpenOptionsSelected();
                }
                closeDialog();
                if ( openOptions )
                {
                    OptionsDialog dialog = new OptionsDialog( );
                    dialog.setVisible( true );
                }
            }
            catch ( Throwable th )
            {
                NLogger.error( FinishBtnListener.class, th, th );
            }
        }
    }
    
    private final class CloseEventHandler extends WindowAdapter implements ActionListener
    {
        @Override
        public void windowClosing(WindowEvent evt)
        {
            closeDialog();
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            closeDialog();
        }
    }
}
