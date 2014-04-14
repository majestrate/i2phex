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
 *  $Id: ProxyPane.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.dialogs.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import phex.common.address.AddressUtils;
import phex.common.address.IpAddress;
import phex.common.address.LocalServentAddress;
import phex.gui.common.GUIRegistry;
import phex.gui.common.GUIUtils;
import phex.gui.common.IPTextField;
import phex.gui.common.IntegerTextField;
import phex.gui.common.PlainMultiLinePanel;
import phex.prefs.core.ProxyPrefs;
import phex.servent.Servent;
import phex.utils.Localizer;
import phex.utils.SystemProperties;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ProxyPane extends OptionsSettingsPane
{
    private static final String EXPORT_IP_KEY = "ExportIP";
    private static final String SOCKS5_PROXY_PORT_KEY = "SOCKS5ProxyPort";
    private static final String HTTP_PROXY_PORT_KEY = "HTTPProxyPort";

    private static final String EXPORT_IP_ERROR_KEY = "ExportIPError";
    private static final String WRONG_EXPORT_IP_FORMAT = "WrongExportIPFormat";

    private JLabel exportIPLabel;
    private IPTextField exportIPTF;
    private JCheckBox socks5ProxyCkbx;
    private JLabel socks5HostLabel;
    private JTextField socks5ProxyHostTF;
    private JLabel socks5PortLabel;
    private IntegerTextField socks5ProxyPortTF;
    private JCheckBox socks5AuthenticationCkbx;
    private JLabel socks5UserLabel;
    private JTextField socks5UserNameTF;
    private JLabel socks5PasswordLabel;
    private JPasswordField socks5PasswordTF;

    private JCheckBox httpProxyCkbx;
    private PlainMultiLinePanel infoLabel;
    private JLabel httpHostLabel;
    private JTextField httpProxyHostTF;
    private JLabel httpPortLabel;
    private IntegerTextField httpProxyPortTF;

    public ProxyPane()
    {
        super( "FirewallProxy" );
    }

    /**
     * Called when preparing this settings pane for display the first time. Can
     * be overriden to implement the look of the settings pane.
     */
    @Override
    protected void prepareComponent()
    {
        FormLayout layout = new FormLayout(
            "10dlu, right:d, 2dlu, d, " + // 4 columns
            "10dlu, right:d, 2dlu, d, 2dlu:grow", // 5 columns
            "p, 3dlu, p, 3dlu, p, 3dlu, " + // 6 rows
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 6dlu, " + //8 rows
            "p, 3dlu, fill:d:grow, 3dlu, p, 3dlu, p, 6dlu" //8 rows
        );
        layout.setRowGroups( new int[][]{{3, 5, 9, 11, 13, 15, 21}} );

        setLayout( layout );
        //FormDebugPanel contentPanel = new FormDebugPanel();
        //JPanel contentPanel = new JPanel();
        //add( this, BorderLayout.CENTER );
        PanelBuilder builder = new PanelBuilder( layout, this );
        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator( Localizer.getString( "FirewallSettings" ),
            cc.xywh( 1, 1, 9, 1 ) );
        
        exportIPLabel = builder.addLabel( Localizer.getString( "ExportIPAs" ) + ": ",
            cc.xy( 2, 3 ) );
        exportIPTF = new IPTextField( );
        exportIPTF.setIPString( ProxyPrefs.ForcedIp.get() );
        builder.add( exportIPTF, cc.xy( 4, 3 ) );
        
        builder.addSeparator( Localizer.getString( "SOCKSSettings" ),
            cc.xywh( 1, 5, 9, 1 ) );

        socks5ProxyCkbx = new JCheckBox(
            Localizer.getString( "UseSocks5Proxy" ), 
            ProxyPrefs.UseSocks5.get().booleanValue() );
        socks5ProxyCkbx.addActionListener( new CheckboxActionListener() );
        builder.add( socks5ProxyCkbx, cc.xywh( 2, 7, 3, 1 ) );
        
        socks5HostLabel = builder.addLabel( Localizer.getString( "ProxyHostIP" ) + ": ",
            cc.xy( 2, 9 ) );
        socks5ProxyHostTF = new JTextField( 
            ProxyPrefs.Socks5Host.get(), 15 );
        builder.add( socks5ProxyHostTF, cc.xy( 4, 9 ) );
        
        socks5PortLabel = builder.addLabel( Localizer.getString( "Port" ) + ": ",
            cc.xy( 6, 9 ) );
        socks5ProxyPortTF = new IntegerTextField(
            ProxyPrefs.Socks5Port.get().toString(), 5, 5 );
        builder.add( socks5ProxyPortTF, cc.xy( 8, 9 ) );
            
        socks5AuthenticationCkbx = new JCheckBox(
            Localizer.getString( "Authentication" ),
            ProxyPrefs.Socks5Authentication.get().booleanValue() );
        socks5AuthenticationCkbx.addActionListener( new CheckboxActionListener() );
        builder.add( socks5AuthenticationCkbx, cc.xywh( 2, 11, 3, 1 ) );

        socks5UserLabel = builder.addLabel( Localizer.getString( "Username" ) + ": ",
            cc.xy( 2, 13 ) );
        socks5UserNameTF = new JTextField( ProxyPrefs.Socks5User.get(), 10 );
        builder.add( socks5UserNameTF, cc.xy( 4, 13 ) );
        
        socks5PasswordLabel = builder.addLabel( Localizer.getString( "Password" ) + ": ",
            cc.xy( 6, 13 ) );
        socks5PasswordTF = new JPasswordField( ProxyPrefs.Socks5Password.get(), 10 );
        builder.add( socks5PasswordTF, cc.xy( 8, 13 ) );
 
        builder.addSeparator( Localizer.getString( "HTTPProxySettings" ),
            cc.xywh( 1, 15, 9, 1 ) );
                
        infoLabel = new PlainMultiLinePanel( Localizer.getString( "HttpProxyInfoText" ) );
        builder.add( infoLabel, cc.xywh( 2, 17, 8, 1 ) );

        httpProxyCkbx = new JCheckBox( Localizer.getString( "UseHTTPProxy" ),
            ProxyPrefs.UseHttp.get().booleanValue() );
        httpProxyCkbx.addActionListener( new CheckboxActionListener() );
        builder.add( httpProxyCkbx, cc.xywh( 2, 19, 3, 1 ) );
        
        httpHostLabel = builder.addLabel( Localizer.getString( "ProxyHostIP" ) + ": ",
            cc.xy( 2, 21 ) );
        httpProxyHostTF = new JTextField( ProxyPrefs.HttpHost.get(), 15 );
        builder.add( httpProxyHostTF, cc.xy( 4, 21 ) );
        
        httpPortLabel = builder.addLabel( Localizer.getString( "Port" ) + ": ",
            cc.xy( 6, 21 ) );
        httpProxyPortTF = new IntegerTextField(
            ProxyPrefs.HttpPort.get().toString(), 5, 5 );
        builder.add( httpProxyPortTF, cc.xy( 8, 21 ) );

        refreshEnableState();
    }

    /**
     * Override this method if you like to verify inputs before storing them.
     * A input dictionary is given to the pane. It can be used to store values
     * like error flags or prepared values for saving. The dictionary is given
     * to every settings pane checkInput(), displayErrorMessage() and
     * saveAndApplyChanges() method.
     * When the input has been flaged as invalid with the method setInputValid()
     * the method displayErrorMessage() is called directly after return of
     * checkInput() and the focus is given to settings pane.
     * After checking all settings pane without any error the method
     * saveAndApplyChanges() is called for all settings panes to save the
     * changes.
     */
    public void checkInput( HashMap inputDic )
    {
        if ( !exportIPTF.isFieldEmpty() )
        {
            if ( exportIPTF.isInputValid() )
            {
                inputDic.put( EXPORT_IP_KEY, exportIPTF.getIP() );
            }
            else
            {
                inputDic.put( EXPORT_IP_ERROR_KEY, WRONG_EXPORT_IP_FORMAT );
                setInputValid( inputDic, false );
                return;
            }
        }// forced ip is not in dictionary if it is empty!!

        try
        {
            String socks5ProxyPortStr = socks5ProxyPortTF.getText();
            Integer socks5ProxyPort = Integer.valueOf( socks5ProxyPortStr );
            inputDic.put( SOCKS5_PROXY_PORT_KEY, socks5ProxyPort );
        }
        catch ( NumberFormatException exp )
        {// we don't care because we will use default value on saving...
        }

        try
        {
            String httpProxyPortStr = httpProxyPortTF.getText();
            Integer httpProxyPort = Integer.valueOf( httpProxyPortStr );
            inputDic.put( HTTP_PROXY_PORT_KEY, httpProxyPort );
        }
        catch ( NumberFormatException exp )
        {// we don't care because we will use default value on saving...
        }

        setInputValid( inputDic, true );
    }

    /**
     * When isInputValid() returns a false this method is called.
     * The input dictionary should contain the settings pane specific information
     * of the error.
     * The settings pane should override this method to display a error
     * message. Before calling the method the focus is given to the
     * settings pane.
     */
    public void displayErrorMessage( HashMap inputDic )
    {
        if ( inputDic.containsKey( NUMBER_FORMAT_ERROR_KEY ) )
        {
            displayNumberFormatError( inputDic );
        }
        if ( inputDic.containsKey( EXPORT_IP_ERROR_KEY ) )
        {
            exportIPTF.requestFocus();
            GUIUtils.showErrorMessage(
                Localizer.getString( WRONG_EXPORT_IP_FORMAT ),
                Localizer.getString( "IPFormatError" ) );
        }
    }

    /**
     * Override this method if you like to apply and save changes made on
     * settings pane. To trigger saving of the configuration if any value was
     * changed call triggerConfigSave().
     */
    @Override
    public void saveAndApplyChanges( HashMap inputDic )
    {
        byte[] exportIP = (byte[])inputDic.get( EXPORT_IP_KEY );
        String exportIPStr = "";
        if ( exportIP != null )
        {
            exportIPStr = AddressUtils.ip2string( exportIP );
        }
        if ( !ProxyPrefs.ForcedIp.get().equals( exportIPStr ) )
        {
            LocalServentAddress serventAddr = (LocalServentAddress)GUIRegistry.getInstance().getServent().getLocalAddress();
            if ( exportIP != null )
            {
                IpAddress ip = new IpAddress( exportIP );
                serventAddr.setForcedHostIP( ip );
            }
            else
            {
                serventAddr.setForcedHostIP( null );
            }
        }

        boolean useSocks5Proxy = socks5ProxyCkbx.isSelected();
        ProxyPrefs.UseSocks5.set( Boolean.valueOf( useSocks5Proxy ) );

        String proxyHost = socks5ProxyHostTF.getText();
        ProxyPrefs.Socks5Host.set( proxyHost );

        Integer proxyPortInt = (Integer) inputDic.get( SOCKS5_PROXY_PORT_KEY );
        int proxyPort = ProxyPrefs.DEFAULT_SOCKS5_PORT;
        if ( proxyPortInt != null )
        {
            proxyPort = proxyPortInt.intValue();
        }
        ProxyPrefs.Socks5Port.set( Integer.valueOf( proxyPort ) );

        boolean useAuthentication = socks5AuthenticationCkbx.isSelected();
        ProxyPrefs.Socks5Authentication.set( Boolean.valueOf( useAuthentication ) );

        String userName = socks5UserNameTF.getText();
        ProxyPrefs.Socks5User.set( userName );

        String password = new String( socks5PasswordTF.getPassword() );
        ProxyPrefs.Socks5Password.set( password );

        // http
        boolean isHttpProxyUsed = httpProxyCkbx.isSelected();
        ProxyPrefs.UseHttp.set( Boolean.valueOf( isHttpProxyUsed ) );

        String httpProxyHost = httpProxyHostTF.getText();
        ProxyPrefs.HttpHost.set( httpProxyHost );

        Integer httpProxyPortInt = (Integer) inputDic.get( HTTP_PROXY_PORT_KEY );
        int httpProxyPort = ProxyPrefs.DEFAULT_HTTP_PORT;
        if ( httpProxyPortInt != null )
        {
            httpProxyPort = httpProxyPortInt.intValue();
        }
        ProxyPrefs.HttpPort.set( Integer.valueOf( httpProxyPort ) );

        // the following two statements could be conditional only when affection
        // values have changed... but not really critical to do them always...
        
        // called to update http proxy settings
        SystemProperties.updateProxyProperties();
        // called to update the frame title to display socks 5 proxy use
        GUIRegistry.getInstance().getMainFrame().setTitle();
    }

    private void refreshEnableState()
    {
        boolean isBehindFirewallSelected = true;
        socks5ProxyCkbx.setEnabled( isBehindFirewallSelected );
        boolean isSocks5ProxySelected = socks5ProxyCkbx.isSelected();
        socks5HostLabel.setEnabled( isBehindFirewallSelected &&
            isSocks5ProxySelected );
        socks5ProxyHostTF.setEnabled( isBehindFirewallSelected &&
            isSocks5ProxySelected );
        socks5PortLabel.setEnabled( isBehindFirewallSelected &&
            isSocks5ProxySelected );
        socks5ProxyPortTF.setEnabled( isBehindFirewallSelected &&
            isSocks5ProxySelected );

        socks5AuthenticationCkbx.setEnabled( isBehindFirewallSelected &&
            isSocks5ProxySelected );
        socks5UserLabel.setEnabled( isBehindFirewallSelected &&
            isSocks5ProxySelected &&
            socks5AuthenticationCkbx.isSelected() );
        socks5UserNameTF.setEnabled( isBehindFirewallSelected &&
            isSocks5ProxySelected &&
            socks5AuthenticationCkbx.isSelected() );
        socks5PasswordLabel.setEnabled( isBehindFirewallSelected &&
            isSocks5ProxySelected &&
            socks5AuthenticationCkbx.isSelected() );
        socks5PasswordTF.setEnabled( isBehindFirewallSelected &&
            isSocks5ProxySelected &&
            socks5AuthenticationCkbx.isSelected() );

        httpHostLabel.setEnabled( httpProxyCkbx.isSelected() );
        httpProxyHostTF.setEnabled( httpProxyCkbx.isSelected() );
        httpPortLabel.setEnabled( httpProxyCkbx.isSelected() );
        httpProxyPortTF.setEnabled( httpProxyCkbx.isSelected() );
        
        // I2PMOD:
        // After everything else has run, disable fields
        // which shouldn't be altered in I2P environment.
        exportIPLabel.setEnabled( false );
        exportIPTF.setEnabled( false );
        socks5ProxyCkbx.setEnabled( false );
        httpProxyCkbx.setEnabled( false );
    }

    class CheckboxActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            refreshEnableState();
        }
    }
}