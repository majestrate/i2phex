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
 *  $Id: SelectNetworkDialog.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import phex.common.GnutellaNetwork;
import phex.gui.common.GUIRegistry;
import phex.gui.common.PlainMultiLinePanel;
import phex.prefs.core.ConnectionPrefs;
import phex.prefs.core.NetworkPrefs;
import phex.prefs.core.PhexCorePrefs;
import phex.prefs.core.PrivateNetworkConstants;
import phex.servent.Servent;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 */
public class SelectNetworkDialog extends JDialog
{
    private JComboBox networkCbx;
    //private JCheckBox autoJoinNetworkChkbx;
    private JCheckBox autoConnectChkbx;
    
    public SelectNetworkDialog()
    {
        super( GUIRegistry.getInstance().getMainFrame(),
            Localizer.getString( "SelectNet_SelectNetwork" ), true );
        prepareComponent();
    }
    
    private void prepareComponent()
    {
        addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing( WindowEvent evt )
                {
                    closeDialog( false );
                }
            }
        );
        
        Container contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );
        JPanel contentPanel = new JPanel();
        //JPanel contentPanel = new FormDebugPanel();
        contentPane.add( contentPanel, BorderLayout.CENTER );
        
        FormLayout layout = new FormLayout(
            "10dlu, right:d, 4dlu, max(200dlu;d):grow", // columns 
            "p, 3dlu, p:grow, 6dlu, p, 3dlu, p, 3dlu, p, 9dlu, p"  // rows
        );
        PanelBuilder builder = new PanelBuilder( layout, contentPanel );
        CellConstraints cc = new CellConstraints();
        
        builder.setDefaultDialogBorder();
        
        builder.addSeparator( Localizer.getString(
            "SelectNet_SelectNetworkToJoin" ),
            cc.xywh( 1, 1, 4, 1 ) );
            
        PlainMultiLinePanel multiLineText = new PlainMultiLinePanel(
            Localizer.getString( "SelectNet_NetworkSelectionText" ) );
        builder.add( multiLineText, cc.xywh( 2, 3, 3, 1 ) );
        
        networkCbx = new JComboBox();
        networkCbx.setEditable(true);
        networkCbx.addItem( PrivateNetworkConstants.DEFAULT_NETWORK_TO_USE );
        // if the default network is not the general gnutella network we also offer it..
        if ( !PrivateNetworkConstants.DEFAULT_NETWORK_TO_USE.equals( 
                         NetworkPrefs.GENERAL_GNUTELLA_NETWORK ) ) 
        {
            networkCbx.addItem( NetworkPrefs.GENERAL_GNUTELLA_NETWORK );
        }
        for ( String item : NetworkPrefs.NetworkHistory.get() )
        {
            networkCbx.addItem( item );
        }
        Servent servent = GUIRegistry.getInstance().getServent();
        networkCbx.setSelectedItem( servent.getGnutellaNetwork().getName() );
        builder.addLabel( Localizer.getString( "SelectNet_NetworkName" ),
            cc.xy( 2, 5 ) );
        builder.add( networkCbx, cc.xy( 4, 5 ) );
            
//        autoJoinNetworkChkbx = new JCheckBox( Localizer.getString(
//            "SelectNet_AutoJoinNetwork" ), ServiceManager.sCfg.mAutoJoin );
//        builder.add( autoJoinNetworkChkbx, cc.xywh( 2, 7, 3, 1 ) );
        autoConnectChkbx = new JCheckBox( Localizer.getString(
            "SelectNet_AutoConnectNetwork"),
            ConnectionPrefs.AutoConnectOnStartup.get().booleanValue() );
        builder.add( autoConnectChkbx, cc.xywh( 2, 9, 3, 1 ) );
        
        ButtonActionHandler actionHandler = new ButtonActionHandler();
        
        JButton okButton = new JButton( Localizer.getString( "OK" ) );
        okButton.setDefaultCapable( true );
        okButton.addActionListener( actionHandler );
        okButton.setActionCommand( "OK" );
        JButton cancelButton = new JButton( Localizer.getString( "Cancel" ) );
        cancelButton.addActionListener( actionHandler );
        cancelButton.setActionCommand( "CANCEL" );
        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(
            okButton, cancelButton );
        builder.add( buttonPanel, cc.xywh( 1, 11, 4, 1 ) );

        contentPane.validate();
        contentPanel.doLayout();
        contentPanel.revalidate();
        doLayout();
        pack();
        setLocationRelativeTo( getParent() );
    }
    
    private void closeDialog( boolean triggerSave )
    {
        if ( triggerSave )
        {
            PhexCorePrefs.save( false );
            GnutellaNetwork network = GnutellaNetwork.getGnutellaNetworkFromString( 
                NetworkPrefs.CurrentNetwork.get() );
            GUIRegistry.getInstance().getServent().setGnutellaNetwork( network );
        }
        setVisible(false);
        dispose();
    }
    
    private class ButtonActionHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            boolean triggerSave = false;
            if ( e.getActionCommand().equals( "OK" ) )
            {
                String networkName = ((String)networkCbx.getEditor().getItem());
                networkName = networkName.trim();
                if ( networkName.length() == 0 )
                {
                    networkName = PrivateNetworkConstants.DEFAULT_NETWORK_TO_USE;
                }
                if ( !NetworkPrefs.CurrentNetwork.get().equals( networkName ) )
                {
                    NetworkPrefs.CurrentNetwork.set( networkName );
                    triggerSave = true;
                }
                
//                boolean autoJoin = autoJoinNetworkChkbx.isSelected();
//                if ( ServiceManager.sCfg.mAutoJoin != autoJoin )
//                {
//                    ServiceManager.sCfg.mAutoJoin = autoJoin;
//                    triggerSave = true;
//                }
                
                boolean autoConnect = autoConnectChkbx.isSelected();
                ConnectionPrefs.AutoConnectOnStartup.set( Boolean.valueOf( autoConnect ));
                
                if ( !networkName.equals( NetworkPrefs.GENERAL_GNUTELLA_NETWORK ) )
                {
                    NetworkPrefs.NetworkHistory.get().remove( networkName );
                    NetworkPrefs.NetworkHistory.get().add( 0, networkName );

                    if (NetworkPrefs.NetworkHistory.get().size() > 20)
                    {
                        NetworkPrefs.NetworkHistory.get().remove( 20 );
                    }
                    triggerSave = true;
                }
            }
            closeDialog( triggerSave );
        }
    }
}
