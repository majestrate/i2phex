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
 *  Created on 22.12.2006
 *  --- SVN Information ---
 *  $Id: BandwidthPanel.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.gui.dialogs.configwizard;

import javax.swing.JPanel;

import phex.common.bandwidth.BandwidthManager;
import phex.gui.common.BandwidthComboBox;
import phex.gui.common.HTMLMultiLinePanel;
import phex.gui.common.BandwidthComboBox.SpeedDefinition;
import phex.prefs.core.BandwidthPrefs;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class BandwidthPanel extends JPanel
{
    private final ConfigurationWizardDialog parent;
    private final BandwidthManager bandwidthManager;
    private BandwidthComboBox connectionSpeedCbx;
    
    /**
     * Configuration Panel for bandwidth.
     * @param bandwidthManager the {@link BandwidthManager} to configure here.
     * @param parent the dialog this panel is displayed in.
     */
    public BandwidthPanel( BandwidthManager bandwidthManager, ConfigurationWizardDialog parent )
    {
        this.bandwidthManager = bandwidthManager;
        this.parent = parent;
        prepareComponent();
    }
    
    private void prepareComponent()
    {
        FormLayout layout = new FormLayout(
            "10dlu, d, 2dlu, d, right:d:grow", // columns
            "p, 3dlu, p, 8dlu, p, 8dlu, p" );// rows 
        
        setLayout( layout );
        
        PanelBuilder builder = new PanelBuilder( layout, this );
        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator( Localizer.getString( "ConfigWizard_BandwidthHeader" ),
            cc.xywh( 1, 1, 5, 1 ) );
        
        HTMLMultiLinePanel welcomeLines = new HTMLMultiLinePanel(
            Localizer.getString( "ConfigWizard_BandwidthText" ) );        
        builder.add( welcomeLines, cc.xywh( 2, 3, 4, 1 ) );
        
        builder.addLabel( Localizer.getString( "ConfigWizard_ConnectionTypeSpeed" ),
            cc.xy( 2, 5 ) );        
        connectionSpeedCbx = new BandwidthComboBox( );
        builder.add( connectionSpeedCbx, cc.xy( 4, 5 ) );
        
        HTMLMultiLinePanel welcomeLines2 = new HTMLMultiLinePanel(
            Localizer.getString( "ConfigWizard_BandwidthText2" ) );        
        builder.add( welcomeLines2, cc.xywh( 2, 7, 4, 1 ) );
        
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
                break;
            }
        }
    }
    
    public void saveSettings()
    {
        SpeedDefinition def = connectionSpeedCbx.getSelectedSpeedDefinition();
        int value = def.getSpeedInKbps();
        // only change existing values if user modified bandwidth settings. 
        if ( value != BandwidthPrefs.NetworkSpeedKbps.get().intValue() )
        {            
            BandwidthPrefs.NetworkSpeedKbps.set( Integer.valueOf( value ) );
            bandwidthManager.setServentBandwidth( BandwidthPrefs.UNLIMITED_BANDWIDTH );
            bandwidthManager.setNetworkBandwidth( BandwidthPrefs.UNLIMITED_BANDWIDTH );
            bandwidthManager.setDownloadBandwidth( BandwidthPrefs.UNLIMITED_BANDWIDTH );
            bandwidthManager.setUploadBandwidth( BandwidthPrefs.UNLIMITED_BANDWIDTH );
        }
    }
    
}
