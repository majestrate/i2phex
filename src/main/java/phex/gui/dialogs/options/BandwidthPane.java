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
 *  $Id: BandwidthPane.java 4364 2009-01-16 10:56:15Z gregork $
 */
package phex.gui.dialogs.options;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import phex.common.bandwidth.BandwidthManager;
import phex.common.format.NumberFormatUtils;
import phex.gui.common.BandwidthComboBox;
import phex.gui.common.GUIRegistry;
import phex.gui.common.BandwidthComboBox.SpeedDefinition;
import phex.prefs.core.BandwidthPrefs;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class BandwidthPane extends OptionsSettingsPane
{
    private int UNLIMITED_VALUE = 101;

    private Dictionary<Integer,JLabel> linearSliderLabels;

    private BandwidthComboBox connectionSpeedCbx;
    private JLabel totalBandwidthLabel;
    private JSlider totalBandwidthSldr;
    private JLabel netBandwidthLabel;
    private JSlider netBandwidthSldr;
    private JLabel downloadBandwidthLabel;
    private JSlider downloadBandwidthSldr;
    private JLabel uploadBandwidthLabel;
    private JSlider uploadBandwidthSldr;
    private JCheckBox useLogSlider;

    private double maxConnectionBandwidth;

    /**
     * Total bandwidth setting in bytes per second.
     * When bandwidth is set to unlimited the value is set to maxTotalBandwidth
     * for calculation reasons. To find out if set to unlimited check the
     * corresponding slider value for UNLIMITED_VALUE.
     */
    private double currentTotalBandwidth;

    /**
     * Network bandwidth setting in bytes per second.
     * When bandwidth is set to unlimited the value is set to maxTotalBandwidth
     * for calculation reasons. To find out if set to unlimited check the
     * corresponding slider value for UNLIMITED_VALUE.
     */
    private double currentNetBandwidth;

    /**
     * Download bandwidth setting in bytes per second.
     * When bandwidth is set to unlimited the value is set to maxTotalBandwidth
     * for calculation reasons. To find out if set to unlimited check the
     * corresponding slider value for UNLIMITED_VALUE.
     */
    private double currentDownloadBandwidth;

    /**
     * Upload bandwidth setting in bytes per second.
     * When bandwidth is set to unlimited the value is set to maxTotalBandwidth
     * for calculation reasons. To find out if set to unlimited check the
     * corresponding slider value for UNLIMITED_VALUE.
     */
    private double currentUploadBandwidth;

    private boolean updateSliderLabels = true;
    
    private final BandwidthManager bandwidthMgr;

    public BandwidthPane( BandwidthManager bandwidthMgr )
    {
        super( "Bandwidth" );
        this.bandwidthMgr = bandwidthMgr;
        linearSliderLabels = new Hashtable<Integer,JLabel>( 5 );
        linearSliderLabels.put( Integer.valueOf( 0 ), new JLabel( "0%", JLabel.CENTER ) );
        linearSliderLabels.put( Integer.valueOf( 25 ), new JLabel( "25%", JLabel.CENTER ) );
        linearSliderLabels.put( Integer.valueOf( 50 ), new JLabel( "50%", JLabel.CENTER ) );
        linearSliderLabels.put( Integer.valueOf( 75 ), new JLabel( "75%", JLabel.CENTER ) );
        linearSliderLabels.put( Integer.valueOf( 100 ), new JLabel( "100%", JLabel.CENTER ) );
        
    }

    /**
     * Called when preparing this settings pane for display the first time. Can
     * be overridden to implement the look of the settings pane.
     */
    @Override
    protected void prepareComponent()
    {
        // use linear or log scale?
        boolean linearScale = !GUIRegistry.getInstance().useLogBandwidthSlider();
        setLayout( new BorderLayout() );
        
        //JPanel contentPanel = new FormDebugPanel();
        JPanel contentPanel = new JPanel();
        add( contentPanel, BorderLayout.CENTER );
        
        FormLayout layout = new FormLayout(
            "10dlu, d, 2dlu, d, right:d:grow", // columns
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, " + // 8 rows
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, " + // 8 rows
            "p, 3dlu, p, 3dlu, p, 9dlu, p, 3dlu, " + // 8 rows
            "p" ); 
        //layout.setRowGroups( new int[][]{{3, 5, 9, 11, 14, 16, 18}} );
        contentPanel.setLayout( layout );
        
        PanelBuilder builder = new PanelBuilder( layout, contentPanel );
        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator( Localizer.getString( "NetworkSpeedSettings" ),
            cc.xywh( 1, 1, 5, 1 ) );
        
        builder.addLabel( Localizer.getString( "ConnectionTypeSpeed" ) + ": ",
            cc.xy( 2, 3 ) );

        connectionSpeedCbx = new BandwidthComboBox( );
        connectionSpeedCbx.addItemListener( new SpeedItemListener() );
        builder.add( connectionSpeedCbx, cc.xy( 4, 3 ) );
        
        builder.addLabel( Localizer.getString( "TotalBandwidth" ) + ": ",
            cc.xywh( 2, 5, 3, 1 ) );
        totalBandwidthLabel = new JLabel(  );
        builder.add( totalBandwidthLabel, cc.xy( 5, 5 ) );

        totalBandwidthSldr = new JSlider( JSlider.HORIZONTAL, 0, 101, 0 );
        totalBandwidthSldr.addChangeListener( new SliderChangeListener() );
        totalBandwidthSldr.setPaintLabels( true );
        builder.add( totalBandwidthSldr, cc.xywh( 2, 7, 4, 1 ) );

        builder.addSeparator( Localizer.getString( "PhexBandwidthSettings" ),
            cc.xywh( 1, 9, 5, 1 ) );
        
        builder.addLabel( Localizer.getString( "MaxNetworkBandwidth" ) + ": ",
            cc.xywh( 2, 11, 3, 1 ) );
        netBandwidthLabel = new JLabel( );
        builder.add( netBandwidthLabel, cc.xy( 5, 11 ) );

        netBandwidthSldr = new JSlider( JSlider.HORIZONTAL, 0, 101, 0 );
        netBandwidthSldr.addChangeListener( new SliderChangeListener() );
        //netBandwidthSldr.setLabelTable( sliderLabels );
        //netBandwidthSldr.setPaintLabels( linearScale );
        builder.add( netBandwidthSldr, cc.xywh( 2, 13, 4, 1 ) );

        builder.addLabel( Localizer.getString( "MaxDownloadBandwidth" ) + ": ",
            cc.xywh( 2, 15, 3, 1 ) );
        downloadBandwidthLabel = new JLabel( );
        builder.add( downloadBandwidthLabel, cc.xy( 5, 15 ) );

        downloadBandwidthSldr = new JSlider( JSlider.HORIZONTAL, 0, 101, 0 );
        downloadBandwidthSldr.addChangeListener( new SliderChangeListener() );
        //downloadBandwidthSldr.setPaintLabels( linearScale );
        //downloadBandwidthSldr.setLabelTable( sliderLabels );
        builder.add( downloadBandwidthSldr, cc.xywh( 2, 17, 4, 1 ) );

        builder.addLabel( Localizer.getString( "MaxUploadBandwidth" ) + ": ",
            cc.xywh( 2, 19, 3, 1 ) );
        uploadBandwidthLabel = new JLabel( );
        builder.add( uploadBandwidthLabel, cc.xy( 5, 19 ) );

        uploadBandwidthSldr = new JSlider( JSlider.HORIZONTAL, 0, 101, 0 );
        uploadBandwidthSldr.addChangeListener( new SliderChangeListener() );
        //uploadBandwidthSldr.setPaintLabels( linearScale );
        //uploadBandwidthSldr.setLabelTable( sliderLabels );
        builder.add( uploadBandwidthSldr, cc.xywh( 2, 21, 4, 1 ) );
        
        useLogSlider = new JCheckBox( Localizer.getString("BandwidthSettings_UseLogarithmicSliders"),
            !linearScale );
        useLogSlider.setToolTipText( Localizer.getString("BandwidthSettings_TTTUseLogarithmicSliders") );
        useLogSlider.addActionListener( new LogSliderActionListener() );
        builder.add( useLogSlider, cc.xywh( 2, 23, 4, 1, "right, center" ) );

        initConfigValues();
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
    {// wrong input not possible...
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
    {// no error possible...
    }

    /**
     * Override this method if you like to apply and save changes made on
     * settings pane. To trigger saving of the configuration if any value was
     * changed call triggerConfigSave().
     */
    public void saveAndApplyChanges( HashMap inputDic )
    {
        int value;

        /* Set total bandwidth available */
        SpeedDefinition def = connectionSpeedCbx.getSelectedSpeedDefinition();
        value = def.getSpeedInKbps();
        BandwidthPrefs.NetworkSpeedKbps.set( Integer.valueOf( value ) );

        if ( totalBandwidthSldr.getValue() == UNLIMITED_VALUE )
        {
            value = BandwidthPrefs.UNLIMITED_BANDWIDTH;
        }
        else
        {
            value = (int)Math.round( currentTotalBandwidth );
        }
        bandwidthMgr.setServentBandwidth( value );

        /* Set total network bandwidth available */
        if ( netBandwidthSldr.getValue() == UNLIMITED_VALUE )
        {
            value = BandwidthPrefs.UNLIMITED_BANDWIDTH;
        }
        else
        {
            value = (int)Math.round( currentNetBandwidth );
        }
        bandwidthMgr.setNetworkBandwidth( value );

        /* Set total download bandwidth available */
        if ( downloadBandwidthSldr.getValue() == UNLIMITED_VALUE )
        {
            value = BandwidthPrefs.UNLIMITED_BANDWIDTH;
        }
        else
        {
            value = (int)Math.round( currentDownloadBandwidth );
        }
        bandwidthMgr.setDownloadBandwidth( value );

        /* Set total upload bandwidth available */
        if ( uploadBandwidthSldr.getValue() == UNLIMITED_VALUE )
        {
            value = BandwidthPrefs.UNLIMITED_BANDWIDTH;
        }
        else
        {
            value = (int)Math.round( currentUploadBandwidth );
        }
        bandwidthMgr.setUploadBandwidth( value );
    }

    private void initConfigValues()
    {
        int netSpeed = BandwidthPrefs.NetworkSpeedKbps.get().intValue();
        SpeedDefinition currentDef;
        int speedDiff;
        for ( int i = 0; i < BandwidthComboBox.SPEED_DEFINITIONS.length; i++ )
        {
            currentDef = BandwidthComboBox.SPEED_DEFINITIONS[ i ];
            speedDiff = currentDef.getSpeedInKbps() - netSpeed;
            if ( speedDiff >= 0 )
            {
                connectionSpeedCbx.setSelectedIndex( i );
                if ( i == 0 )
                {
                    // if the index stays 0 then the selection didn't change
                    // we need to update manually
                    updateMaxConnectionBandwidth( currentDef );
                }
                break;
            }
        }

        double bandwidth = BandwidthPrefs.MaxTotalBandwidth.get().doubleValue();
        totalBandwidthSldr.setValue( bw2raw(bandwidth, maxConnectionBandwidth) );

        bandwidth = BandwidthPrefs.MaxNetworkBandwidth.get().doubleValue();
        netBandwidthSldr.setValue( bw2raw(bandwidth, currentTotalBandwidth) );


        bandwidth = BandwidthPrefs.MaxDownloadBandwidth.get().doubleValue();
        downloadBandwidthSldr.setValue( bw2raw(bandwidth, currentTotalBandwidth) );

        bandwidth = BandwidthPrefs.MaxUploadBandwidth.get().doubleValue();
        uploadBandwidthSldr.setValue( bw2raw(bandwidth, currentTotalBandwidth) );
    }

    private void calculateBandwidth()
    {
        String labelText;

        int value = totalBandwidthSldr.getValue();
        if ( value == UNLIMITED_VALUE )
        {
            labelText = Localizer.getString( "Unlimited" );
            currentTotalBandwidth = maxConnectionBandwidth;
        }
        else
        {
            currentTotalBandwidth = raw2bw ( value, maxConnectionBandwidth );
            labelText = NumberFormatUtils.formatSignificantByteSize( currentTotalBandwidth ) +
                Localizer.getString( "PerSec" );
        }
        totalBandwidthLabel.setText( labelText );

        value = netBandwidthSldr.getValue();
        if ( value == UNLIMITED_VALUE )
        {
            labelText = Localizer.getString( "Unlimited" );
            currentNetBandwidth = maxConnectionBandwidth;
        }
        else
        {
            currentNetBandwidth = raw2bw ( value, currentTotalBandwidth );
            labelText = NumberFormatUtils.formatSignificantByteSize( currentNetBandwidth ) +
                Localizer.getString( "PerSec" );
        }
        netBandwidthLabel.setText( labelText );

        value = downloadBandwidthSldr.getValue();
        if ( value == UNLIMITED_VALUE )
        {
            labelText = Localizer.getString( "Unlimited" );
            currentDownloadBandwidth = maxConnectionBandwidth;
        }
        else
        {
            currentDownloadBandwidth = raw2bw ( value, currentTotalBandwidth );
            labelText = NumberFormatUtils.formatSignificantByteSize( currentDownloadBandwidth ) +
                Localizer.getString( "PerSec" );
        }
        downloadBandwidthLabel.setText( labelText );

        value = uploadBandwidthSldr.getValue();
        if ( value == UNLIMITED_VALUE )
        {
            labelText = Localizer.getString( "Unlimited" );
            currentUploadBandwidth = maxConnectionBandwidth;
        }
        else
        {
            currentUploadBandwidth = raw2bw ( value, currentTotalBandwidth );
            labelText = NumberFormatUtils.formatSignificantByteSize( currentUploadBandwidth ) +
                Localizer.getString( "PerSec" );
        }
        uploadBandwidthLabel.setText( labelText );
        
        if ( updateSliderLabels )
        {
            if ( GUIRegistry.getInstance().useLogBandwidthSlider() )
            {
                Hashtable<Integer,JLabel> logSliderLabels = new Hashtable<Integer,JLabel>();
                logSliderLabels.put( Integer.valueOf( 0 ), new JLabel( "0%", JLabel.CENTER ) );
                // calc 0.5%
                double subBandwidth = maxConnectionBandwidth / 200.0;
                int val = bw2raw(subBandwidth, maxConnectionBandwidth);
                logSliderLabels.put( Integer.valueOf( val ), new JLabel( "0.5%", JLabel.CENTER ) );
                
                // calc 5%
                subBandwidth = maxConnectionBandwidth / 20.0;
                val = bw2raw(subBandwidth, maxConnectionBandwidth);
                logSliderLabels.put( Integer.valueOf( val ), new JLabel( "5%", JLabel.CENTER ) );
                logSliderLabels.put( Integer.valueOf( 100 ), new JLabel( "100%", JLabel.CENTER ) );
                totalBandwidthSldr.setLabelTable( logSliderLabels );
            }
            else
            {
                totalBandwidthSldr.setLabelTable( linearSliderLabels );
            }
            updateSliderLabels = false;
        }
    }

    /**
     * Return the current rate given the slider value and the maximum rate.
     * sliderValue is 0-100.
     * 0 value must return 0, 100 value must return maximum.
     */
    private double raw2bw(int sliderValue, double maximum)
    {
        if (sliderValue == 0) 
            return 0;
        if ( GUIRegistry.getInstance().useLogBandwidthSlider() )
        {
            double exponent = sliderValue / 100.0;
            double substractor = 0;//maximum / 2;
            double pow = Math.pow(maximum - substractor, exponent);
            double res = pow + substractor * exponent;
            return res;
        }
        else
        {
            return (sliderValue * maximum / 100.0);
        }
    }

    private int bw2raw(double bwValue, double maximum)
    {
        if (bwValue == 0) return 0;
        if (bwValue > maximum) return UNLIMITED_VALUE; // unlimited
        if ( GUIRegistry.getInstance().useLogBandwidthSlider() )
        {
            double exponent = Math.log(bwValue) / Math.log(maximum);
            int sliderValue = (int) Math.round(exponent * 100);
            return sliderValue;
        }
        else
        {
            return (int) (bwValue * 100.0 / maximum);
        }
    }

    private void updateMaxConnectionBandwidth( SpeedDefinition speedDefinition )
    {
        maxConnectionBandwidth = speedDefinition.getSpeedInKB() * NumberFormatUtils.ONE_KB;
        updateSliderLabels = true;
        calculateBandwidth();
    }

    /**
     * 
     */
    private void updateLogSliderUsage()
    {
        updateSliderLabels = true;
        GUIRegistry.getInstance().setLogBandwidthSliderUsed( useLogSlider.isSelected() );
        calculateBandwidth();
    }

    private class SliderChangeListener implements ChangeListener
    {
        public void stateChanged( ChangeEvent e )
        {
            calculateBandwidth();
        }
    }
    
    private class LogSliderActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            updateLogSliderUsage();
        }
    }

    private class SpeedItemListener implements ItemListener
    {
        public void itemStateChanged( ItemEvent e )
        {
            if ( e.getStateChange() == ItemEvent.SELECTED )
            {
                SpeedDefinition speedDefinition = ( SpeedDefinition ) e.getItem();
                updateMaxConnectionBandwidth( speedDefinition );
            }
        }
    }
}
