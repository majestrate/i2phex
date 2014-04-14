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
 */
package phex.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;

import org.apache.commons.httpclient.URIException;

import phex.gui.common.DialogBanner;
import phex.gui.common.GUIRegistry;
import phex.update.UpdateCheckRunner;
import phex.update.UpdateDownloader;
import phex.utils.Localizer;
import phex.utils.VersionUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class UpdateNotificationDialog extends JDialog
{
	
    private UpdateCheckRunner updateChecker;
    private JCheckBox dontDisplayAgainChkBox;

    public UpdateNotificationDialog( UpdateCheckRunner aChecker )
    {
        super( GUIRegistry.getInstance().getMainFrame(),
            Localizer.getString( "UpdateNotification_DialogTitle" ), false );
        updateChecker = aChecker;
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
        FormLayout layout = new FormLayout("2dlu, fill:d:grow, 2dlu", // columns
            "p, p, 16dlu, fill:p:grow, 16dlu," +  // rows
            "p, 2dlu, p 4dlu" ); //btn rows
        PanelBuilder contentPB = new PanelBuilder(layout, contentPanel);
        int columnCount = layout.getColumnCount();
        int rowCount = layout.getRowCount();
        
        DialogBanner banner = new DialogBanner(
            Localizer.getString( "UpdateNotification_BannerHeader" ), 
            Localizer.getString( "UpdateNotification_BannerSubHeader" ) );
        contentPB.add(banner, cc.xywh(1, 1, columnCount, 1));
        
        contentPB.add(new JSeparator(), cc.xywh(1, 2, columnCount, 1));
        
        
        JPanel notifyPanel = buildNotificationPanel(); 
        contentPB.add( notifyPanel, cc.xy( 2, 4, "center, center" ));
        
        
        contentPB.add( new JSeparator(), cc.xywh( 1, rowCount - 3, columnCount, 1 ) );
        
        JButton closeBtn = new JButton( Localizer.getString( "Close" ) );
        closeBtn.addActionListener( closeEventHandler );
        JButton downloadBtn = new JButton( Localizer.getString( "Download" ) );
        downloadBtn.setDefaultCapable( true );
        downloadBtn.setRequestFocusEnabled( true );
        downloadBtn.addActionListener( new DownloadBtnListener());
        JPanel btnPanel = ButtonBarFactory.buildOKCancelBar( downloadBtn, closeBtn);
        contentPB.add( btnPanel, cc.xywh( 2, rowCount - 1, columnCount - 2, 1 ) );
        
        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        getRootPane().setDefaultButton( downloadBtn );
        
        pack();
        setLocationRelativeTo( getParent() );
    }
    
    private JPanel buildNotificationPanel()
    {
        JPanel panel = new JPanel();
        //JPanel panel = new FormDebugPanel();
        
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout("16dlu, d, 8dlu, right:d, 16dlu", // columns
            "p, 8dlu, p" );  // rows
        layout.setRowGroups( new int[][] {{1,3}} );
        PanelBuilder builder = new PanelBuilder(layout, panel);
        
        builder.addLabel( Localizer.getString( "UpdateNotification_YourVersion" ),
            cc.xy( 2, 1 ));
        builder.addLabel( VersionUtils.getFullProgramVersion(), cc.xy( 4, 1 ) );
        
        
        String releaseVersion = updateChecker.getReleaseVersion();
        String betaVersion = updateChecker.getBetaVersion();
        if ( releaseVersion != null )
        {
            builder.addLabel( Localizer.getString( "UpdateNotification_AvailableStableVersion" ),
                cc.xy( 2, 3 ) );
            JLabel label = builder.addLabel( releaseVersion, cc.xy( 4, 3 ) );
            label.setFont( label.getFont().deriveFont(Font.BOLD) );
        }
        else if ( betaVersion != null )
        {
            builder.addLabel( Localizer.getString( "UpdateNotification_AvailableBetaVersion" ),
                cc.xy( 2, 3 ) );
            JLabel label = builder.addLabel( betaVersion, cc.xy( 4, 3 ) );
            label.setFont( label.getFont().deriveFont(Font.BOLD) );
        }
        
        return panel;
    }

    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
    
    private final class DownloadBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
        	try 
        	{
        		UpdateDownloader.createUpdateDownload(); 
        	}
        	catch (URIException exp)
        	{
        		// TODO1 handle exp and display error
        		
        	}
            closeDialog();
        }
    }

    private final class CloseEventHandler extends WindowAdapter implements ActionListener
    {
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