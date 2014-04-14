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
 *  $Id: DebugPane.java 3536 2006-08-05 22:16:44Z gregork $
 */
package phex.gui.dialogs.options;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @deprecated
 */
public class DebugPane extends OptionsSettingsPane
{
    
    private JComboBox logLevelCbBx;

    private JCheckBox performanceTypeChkbx;

    private JCheckBox guiTypeChkbx;

    private JCheckBox netTypeChkbx;

    private JCheckBox searchTypeChkbx;

    private JCheckBox uploadTypeChkbx;

    private JCheckBox downloadTypeChkbx;
    
    private JCheckBox downloadNetTypeChkbx;

    public DebugPane()
    {
        super( "DebugSettings_Debug" );
    }
    
    /**
     * Called when preparing this settings pane for display the first time. Can
     * be overriden to implement the look of the settings pane.
     */
    protected void prepareComponent()
    {
        setLayout( new BorderLayout() );
        
        //JPanel contentPanel = new FormDebugPanel();
        JPanel contentPanel = new JPanel();
        add( contentPanel, BorderLayout.CENTER );
        
        FormLayout layout = new FormLayout(
            "10dlu, d, 6dlu, d, 6dlu, d, 6dlu, d, 2dlu:grow", // columns
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p" ); // rows 
        layout.setColumnGroups( new int[][]{{2,4,6}} );
        layout.setRowGroups( new int[][]{{3, 5, 7 }} );
        contentPanel.setLayout( layout );
        
        PanelBuilder builder = new PanelBuilder( layout, contentPanel );
        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator( Localizer.getString( "DebugSettings_DebugSettings" ), 
            cc.xywh( 1, 1, 7, 1 ) );

        /*downloadTypeChkbx = new JCheckBox(
            Localizer.getString( "Download" ),
            Logger.isTypeLogged( Logger.DOWNLOAD ) );
        downloadTypeChkbx.setToolTipText( Localizer.getString( 
            "DebugSettings_TTTDownloadType" ) );
        builder.add( downloadTypeChkbx, cc.xy( 2, 3 ) );
        
        uploadTypeChkbx = new JCheckBox(
            Localizer.getString( "Upload" ),
            Logger.isTypeLogged( Logger.UPLOAD ) );
        uploadTypeChkbx.setToolTipText( Localizer.getString( 
            "DebugSettings_TTTUploadType" ) );
        builder.add( uploadTypeChkbx, cc.xy( 4, 3 ) );
        
        searchTypeChkbx = new JCheckBox(
            Localizer.getString( "Search" ),
            Logger.isTypeLogged( Logger.SEARCH ) );
        searchTypeChkbx.setToolTipText( Localizer.getString( 
            "DebugSettings_TTTSearchType" ) );
        builder.add( searchTypeChkbx, cc.xy( 6, 3 ) );
        
        downloadNetTypeChkbx = new JCheckBox(
            Localizer.getString( "DownloadNet" ),
            Logger.isTypeLogged( Logger.DOWNLOAD_NET ) );
        downloadNetTypeChkbx.setToolTipText( Localizer.getString( 
            "DebugSettings_TTTDownloadNetType" ) );
        builder.add( downloadNetTypeChkbx, cc.xy( 8, 3 ) );
        
        netTypeChkbx = new JCheckBox(
            Localizer.getString( "Network" ),
            Logger.isTypeLogged( Logger.NETWORK ) );
        netTypeChkbx.setToolTipText( Localizer.getString( 
            "DebugSettings_TTTNetworkType" ) );
        builder.add( netTypeChkbx, cc.xy( 2, 5 ) );
        
        guiTypeChkbx = new JCheckBox(
            Localizer.getString( "GUI" ),
            Logger.isTypeLogged( Logger.GUI ) );
        guiTypeChkbx.setToolTipText( Localizer.getString( 
            "DebugSettings_TTTGuiType" ) );
        builder.add( guiTypeChkbx, cc.xy( 4, 5 ) );
        
        performanceTypeChkbx = new JCheckBox(
            Localizer.getString( "Performance" ),
            Logger.isTypeLogged( Logger.PERFORMANCE ) );
        performanceTypeChkbx.setToolTipText( Localizer.getString( 
            "DebugSettings_TTTPerformanceType" ) );
        builder.add( performanceTypeChkbx, cc.xy( 6, 5 ) );
                
        JLabel label = builder.addLabel(
            Localizer.getString( "DebugSettings_LogLevel" ) + ": ", cc.xy( 2, 7 ) );
        label.setToolTipText( Localizer.getString( 
            "DebugSettings_TTTLogLevel" ) );
        
        String[] logLevels = new String[] 
        {
            "Finest", "Finer", "Fine", "Config", "Info", "Warning", "Severe"
        };
        logLevelCbBx = new JComboBox( logLevels );
        logLevelCbBx.setSelectedIndex( Logger.getVerboseLevel() );
        logLevelCbBx.setToolTipText( Localizer.getString( 
            "DebugSettings_TTTLogLevel" ) );
        builder.add( logLevelCbBx, cc.xy( 4, 7 ) );
        */
    }
    
    /**
     * Override this method if you like to apply and save changes made on
     * settings pane. To trigger saving of the configuration if any value was
     * changed call triggerConfigSave().
     */
    public void saveAndApplyChanges( HashMap inputDic )
    {
        // close options
        /*short logLevel = (short)logLevelCbBx.getSelectedIndex();
        if ( ServiceManager.sCfg.loggerVerboseLevel != logLevel )
        {
            ServiceManager.sCfg.loggerVerboseLevel = logLevel;
            Logger.setVerboseLevel( ServiceManager.sCfg.loggerVerboseLevel );
            OptionsSettingsPane.triggerConfigSave( inputDic );
        }

        int logType = 0x01;
        if ( downloadTypeChkbx.isSelected() )
        {
            logType = logType | Logger.DOWNLOAD;
        }
        if ( uploadTypeChkbx.isSelected() )
        {
            logType = logType | Logger.UPLOAD;
        }
        if ( searchTypeChkbx.isSelected() )
        {
            logType = logType | Logger.SEARCH;
        }
        if ( netTypeChkbx.isSelected() )
        {
            logType = logType | Logger.NETWORK;
        }
        if ( guiTypeChkbx.isSelected() )
        {
            logType = logType | Logger.GUI;
        }
        if ( performanceTypeChkbx.isSelected() )
        {
            logType = logType | Logger.PERFORMANCE;
        }
        if ( downloadNetTypeChkbx.isSelected() )
        {
            logType = logType | Logger.DOWNLOAD_NET;
        }
        if ( ServiceManager.sCfg.logType != logType )
        {
            ServiceManager.sCfg.logType = (short)logType;
            Logger.setLogType( ServiceManager.sCfg.logType );
            OptionsSettingsPane.triggerConfigSave( inputDic );
        }*/
    }
}
