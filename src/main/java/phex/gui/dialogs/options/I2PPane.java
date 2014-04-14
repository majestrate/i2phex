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
 *  $Id: ProxyPane.java 3633 2006-11-29 16:25:06Z gregork $
 */
package phex.gui.dialogs.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.*;

import phex.common.address.AddressUtils;
import phex.common.address.IpAddress;
import phex.gui.common.*;
import phex.prefs.core.I2PPrefs;
import phex.utils.Localizer;
import phex.utils.SystemProperties;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class I2PPane extends OptionsSettingsPane
{
    private static final String I2CP_PORT_KEY = "I2CPPort";
    private static final String INBOUND_LENGTH_KEY = "InboundLength";
    private static final String OUTBOUND_LENGTH_KEY = "OutboundLength";
    private static final String INBOUND_LENGTH_VARIANCE_KEY = "InboundLengthVariance";
    private static final String OUTBOUND_LENGTH_VARIANCE_KEY = "OutboundLengthVariance";
    private static final String INBOUND_QUANTITY_KEY = "InboundQuantity";
    private static final String OUTBOUND_QUANTITY_KEY = "OutboundQuantity";
    private static final String INBOUND_BACKUP_QUANTITY_KEY = "InboundBackupQuantity";
    private static final String OUTBOUND_BACKUP_QUANTITY_KEY = "OutboundBackupQuantity";

    private JLabel i2cpHostLabel;
    private JTextField i2cpHostTF;
    private JLabel i2cpPortLabel;
    private IntegerTextField i2cpPortTF;
    
    private JLabel inboundLengthLabel;
    private JTextField inboundLengthTF;
    private JLabel outboundLengthLabel;
    private JTextField outboundLengthTF;
    
    private JLabel inboundLengthVarianceLabel;
    private JTextField inboundLengthVarianceTF;
    private JLabel outboundLengthVarianceLabel;
    private JTextField outboundLengthVarianceTF;
    
    private JLabel inboundQuantityLabel;
    private JTextField inboundQuantityTF;
    private JLabel outboundQuantityLabel;
    private JTextField outboundQuantityTF;
    
    private JLabel inboundBackupQuantityLabel;
    private JTextField inboundBackupQuantityTF;
    private JLabel outboundBackupQuantityLabel;
    private JTextField outboundBackupQuantityTF;
    
    public I2PPane()
    {
        super( "I2P" );
    }

    /**
     * Called when preparing this settings pane for display the first time. Can
     * be overriden to implement the look of the settings pane.
     */
    @Override
    protected void prepareComponent()
    {
        FormLayout layout = new FormLayout(
            "10dlu, right:d, 2dlu, 50dlu, " + // 4 columns
            "10dlu, right:d, 2dlu, 50dlu, 2dlu:grow", // 5 columns
            "p, 3dlu, p, 3dlu, p, 3dlu, " + // 6 rows
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, " + // 8 rows
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu" // 8 rows
        );
        layout.setRowGroups( new int[][]{{3, 5, 9, 11, 13, 15, 21}} );
        layout.setColumnGroups( new int[][]{{4, 8},{2, 6}} );

        setLayout( layout );
        //FormDebugPanel contentPanel = new FormDebugPanel();
        //JPanel contentPanel = new JPanel();
        //add( this, BorderLayout.CENTER );
        PanelBuilder builder = new PanelBuilder( layout, this );
        CellConstraints cc = new CellConstraints();

        builder.addSeparator( Localizer.getString( "I2CPSettings" ),
            cc.xywh( 1, 1, 9, 1 ) );
        
        i2cpHostLabel = builder.addLabel(
            Localizer.getString( "I2CPHost" ) + ": ", cc.xy( 2, 3 ) );
        i2cpHostTF = new JTextField( I2PPrefs.I2CPHost.get(), 15 );
        builder.add( i2cpHostTF, cc.xy( 4, 3 ) );
        
        i2cpPortLabel = builder.addLabel(
            Localizer.getString( "I2CPPort" ) + ": ", cc.xy( 6, 3 ) );
        i2cpPortTF = new IntegerTextField( I2PPrefs.I2CPPort.get().toString(), 5, 5 );
        builder.add( i2cpPortTF, cc.xy( 8, 3 ) );

        builder.addSeparator( Localizer.getString( "I2PTunnelSettings" ),
            cc.xywh( 1, 5, 9, 1 ) );
        
        inboundLengthLabel = builder.addLabel(
            Localizer.getString( "InboundLength" ) + ": ", cc.xy( 2, 9 ) );
        inboundLengthTF = new IntegerTextField(
            I2PPrefs.InboundLength.get().toString(), 1, 1 );
        builder.add( inboundLengthTF, cc.xy( 4, 9 ) );
        
        outboundLengthLabel = builder.addLabel(
            Localizer.getString( "OutboundLength" ) + ": ", cc.xy( 6, 9 ) );
        outboundLengthTF = new IntegerTextField(
            I2PPrefs.OutboundLength.get().toString(), 1, 1 );
        builder.add( outboundLengthTF, cc.xy( 8, 9 ) );
        
        inboundLengthVarianceLabel = builder.addLabel(
            Localizer.getString( "InboundLengthVariance" ) + ": ", cc.xy( 2, 11 ) );
        inboundLengthVarianceTF = new IntegerTextField(
            I2PPrefs.InboundLengthVariance.get().toString(), 1, 1 );
        builder.add( inboundLengthVarianceTF, cc.xy( 4, 11 ) );
        
        outboundLengthVarianceLabel = builder.addLabel(
            Localizer.getString( "OutboundLengthVariance" ) + ": ", cc.xy( 6, 11 ) );
        outboundLengthVarianceTF = new IntegerTextField(
            I2PPrefs.OutboundLengthVariance.get().toString(), 1, 1 );
        builder.add( outboundLengthVarianceTF, cc.xy( 8, 11 ) );

        inboundQuantityLabel = builder.addLabel(
            Localizer.getString( "InboundQuantity" ) + ": ", cc.xy( 2, 13 ) );
        inboundQuantityTF = new IntegerTextField(
            I2PPrefs.InboundQuantity.get().toString(), 1, 1 );
        builder.add( inboundQuantityTF, cc.xy( 4, 13 ) );
        
        outboundQuantityLabel = builder.addLabel(
            Localizer.getString( "OutboundQuantity" ) + ": ", cc.xy( 6, 13 ) );
        outboundQuantityTF = new IntegerTextField(
            I2PPrefs.OutboundQuantity.get().toString(), 1, 1 );
        builder.add( outboundQuantityTF, cc.xy( 8, 13 ) );
        
        inboundBackupQuantityLabel = builder.addLabel(
            Localizer.getString( "InboundBackupQuantity" ) + ": ", cc.xy( 2, 15 ) );
        inboundBackupQuantityTF = new IntegerTextField(
            I2PPrefs.InboundBackupQuantity.get().toString(), 1, 1 );
        builder.add( inboundBackupQuantityTF, cc.xy( 4, 15 ) );
        
        outboundBackupQuantityLabel = builder.addLabel(
            Localizer.getString( "OutboundBackupQuantity" ) + ": ", cc.xy( 6, 15 ) );
        outboundBackupQuantityTF = new IntegerTextField(
            I2PPrefs.OutboundBackupQuantity.get().toString(), 1, 1 );
        builder.add( outboundBackupQuantityTF, cc.xy( 8, 15 ) );

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
        try {
            Integer i2cpPort = new Integer( i2cpPortTF.getText() );
            inputDic.put( I2CP_PORT_KEY, i2cpPort );
        } catch ( NumberFormatException exp ) {
        } // we don't care because we will use default value on saving...
        
        try {
            Integer inboundLength = new Integer( inboundLengthTF.getText() );
            inputDic.put( INBOUND_LENGTH_KEY, inboundLength );
        } catch ( NumberFormatException exp ) {
        } // we don't care because we will use default value on saving...

        try {
            Integer outboundLength = new Integer( outboundLengthTF.getText() );
            inputDic.put( OUTBOUND_LENGTH_KEY, outboundLength );
        } catch ( NumberFormatException exp ) {
        } // we don't care because we will use default value on saving...

        try {
            Integer inboundLengthVariance = new Integer( inboundLengthVarianceTF.getText() );
            inputDic.put( INBOUND_LENGTH_VARIANCE_KEY, inboundLengthVariance );
        } catch ( NumberFormatException exp ) {
        } // we don't care because we will use default value on saving...
        
        try {
            Integer outboundLengthVariance = new Integer( outboundLengthVarianceTF.getText() );
            inputDic.put( OUTBOUND_LENGTH_VARIANCE_KEY, outboundLengthVariance );
        } catch ( NumberFormatException exp ) {
        } // we don't care because we will use default value on saving...

        try {
            Integer inboundQuantity = new Integer( inboundQuantityTF.getText() );
            inputDic.put( INBOUND_QUANTITY_KEY, inboundQuantity );
        } catch ( NumberFormatException exp ) {
        } // we don't care because we will use default value on saving...

        try {
            Integer outboundQuantity = new Integer( outboundQuantityTF.getText() );
            inputDic.put( OUTBOUND_QUANTITY_KEY, outboundQuantity );
        } catch ( NumberFormatException exp ) {
        } // we don't care because we will use default value on saving...
        
        try {
            Integer inboundBackupQuantity = new Integer( inboundBackupQuantityTF.getText() );
            inputDic.put( INBOUND_BACKUP_QUANTITY_KEY, inboundBackupQuantity );
        } catch ( NumberFormatException exp ) {
        } // we don't care because we will use default value on saving...
        
        try {
            Integer outboundBackupQuantity = new Integer( outboundBackupQuantityTF.getText() );
            inputDic.put( OUTBOUND_BACKUP_QUANTITY_KEY, outboundBackupQuantity );
        } catch ( NumberFormatException exp ) {
        } // we don't care because we will use default value on saving...

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
    }

    /**
     * Override this method if you like to apply and save changes made on
     * settings pane. To trigger saving of the configuration if any value was
     * changed call triggerConfigSave().
     */
    @Override
    public void saveAndApplyChanges( HashMap inputDic )
    {
        String i2cpHost = i2cpHostTF.getText();
        I2PPrefs.I2CPHost.set( i2cpHost );

        Integer i2cpPortInt = (Integer) inputDic.get( I2CP_PORT_KEY );
        int i2cpPort = I2PPrefs.DEFAULT_I2CP_PORT;
        if ( i2cpPortInt != null )
            i2cpPort = i2cpPortInt.intValue();
        I2PPrefs.I2CPPort.set( new Integer( i2cpPort ) );
        
        Integer inboundLengthInt = (Integer) inputDic.get( INBOUND_LENGTH_KEY );
        int inboundLength = I2PPrefs.DEFAULT_INBOUND_LENGTH;
        if ( inboundLengthInt != null )
            inboundLength = inboundLengthInt.intValue();
        I2PPrefs.InboundLength.set( new Integer( inboundLength ) );
        
        Integer outboundLengthInt = (Integer) inputDic.get( OUTBOUND_LENGTH_KEY );
        int outboundLength = I2PPrefs.DEFAULT_OUTBOUND_LENGTH;
        if ( outboundLengthInt != null )
            outboundLength = outboundLengthInt.intValue();
        I2PPrefs.OutboundLength.set( new Integer( outboundLength ) );
        
        Integer inboundLengthVarianceInt = (Integer) inputDic.get( INBOUND_LENGTH_VARIANCE_KEY );
        int inboundLengthVariance = I2PPrefs.DEFAULT_INBOUND_LENGTH_VARIANCE;
        if ( inboundLengthVarianceInt != null )
            inboundLengthVariance = inboundLengthVarianceInt.intValue();
        I2PPrefs.InboundLengthVariance.set( new Integer( inboundLengthVariance ) );

        Integer outboundLengthVarianceInt = (Integer) inputDic.get( OUTBOUND_LENGTH_VARIANCE_KEY );
        int outboundLengthVariance = I2PPrefs.DEFAULT_OUTBOUND_LENGTH_VARIANCE;
        if ( outboundLengthVarianceInt != null )
            outboundLengthVariance = outboundLengthVarianceInt.intValue();
        I2PPrefs.OutboundLengthVariance.set( new Integer( outboundLengthVariance ) );
        
        Integer inboundQuantityInt = (Integer) inputDic.get( INBOUND_QUANTITY_KEY );
        int inboundQuantity = I2PPrefs.DEFAULT_INBOUND_QUANTITY;
        if ( inboundQuantityInt != null )
            inboundQuantity = inboundQuantityInt.intValue();
        I2PPrefs.InboundQuantity.set( new Integer( inboundQuantity ) );
        
        Integer outboundQuantityInt = (Integer) inputDic.get( OUTBOUND_QUANTITY_KEY );
        int outboundQuantity = I2PPrefs.DEFAULT_OUTBOUND_QUANTITY;
        if ( outboundQuantityInt != null )
            outboundQuantity = outboundQuantityInt.intValue();
        I2PPrefs.OutboundQuantity.set( new Integer( outboundQuantity ) );
        
        Integer inboundBackupQuantityInt = (Integer) inputDic.get( INBOUND_BACKUP_QUANTITY_KEY );
        int inboundBackupQuantity = I2PPrefs.DEFAULT_INBOUND_BACKUP_QUANTITY;
        if ( inboundBackupQuantityInt != null )
            inboundBackupQuantity = inboundBackupQuantityInt.intValue();
        I2PPrefs.InboundBackupQuantity.set( new Integer( inboundBackupQuantity ) );

        Integer outboundBackupQuantityInt = (Integer) inputDic.get( OUTBOUND_BACKUP_QUANTITY_KEY );
        int outboundBackupQuantity = I2PPrefs.DEFAULT_OUTBOUND_BACKUP_QUANTITY;
        if ( outboundBackupQuantityInt != null )
            outboundBackupQuantity = outboundBackupQuantityInt.intValue();
        I2PPrefs.OutboundBackupQuantity.set( new Integer( outboundBackupQuantity ) );
    }

    private void refreshEnableState()
    {
        // Nothing to do here
    }

    class CheckboxActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            refreshEnableState();
        }
    }
}