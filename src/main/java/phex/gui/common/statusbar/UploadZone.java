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
 *  $Id: UploadZone.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.common.statusbar;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import phex.common.bandwidth.BandwidthController;
import phex.common.bandwidth.BandwidthManager;
import phex.common.format.NumberFormatUtils;
import phex.common.log.NLogger;
import phex.gui.common.GUIRegistry;
import phex.gui.common.IconPack;
import phex.prefs.core.BandwidthPrefs;
import phex.servent.Servent;
import phex.statistic.StatisticProvider;
import phex.statistic.StatisticProviderConstants;
import phex.statistic.StatisticsManager;
import phex.upload.UploadManager;
import phex.utils.Localizer;

public class UploadZone extends JPanel
{
    private UploadManager uploadMgr;
    private StatisticsManager statsMgr;
    private BandwidthManager bwMgr;
    private JLabel uploadLabel;

    private JLabel bwLabel;

    public UploadZone( BandwidthManager bandwidthMgr )
    {
        super(  );
        SpringLayout layout = new SpringLayout();
        setLayout( layout );
        Servent servent = GUIRegistry.getInstance().getServent();
        uploadMgr = servent.getUploadService();
        statsMgr = servent.getStatisticsService();
        bwMgr = bandwidthMgr;
        
        uploadLabel = new JLabel();
        add( uploadLabel );
        
        bwLabel = new JLabel();
        add( bwLabel );
        
        updateZone();
        
        layout.putConstraint(SpringLayout.NORTH, uploadLabel, 3, SpringLayout.NORTH, this );
        layout.putConstraint(SpringLayout.WEST, uploadLabel, 5, SpringLayout.WEST, this );
        
        
        layout.putConstraint(SpringLayout.WEST, bwLabel, 5, SpringLayout.EAST, uploadLabel );
        layout.putConstraint(SpringLayout.NORTH, bwLabel, 3, SpringLayout.NORTH, this );
        
        layout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST, bwLabel );
        //layout.putConstraint(SpringLayout.SOUTH, this, 2, SpringLayout.SOUTH, bwLabel );
        
        setupIcons();
    }
    
    private void setupIcons()
    {
        IconPack factory = GUIRegistry.getInstance().getPlafIconPack();
        uploadLabel.setIcon( factory.getIcon( "StatusBar.Upload" ) );
    }

    public void updateZone()
    {
        try
        {        
            StatisticProvider uploadCountProvider = statsMgr.getStatisticProvider(
                StatisticProviderConstants.SESSION_UPLOAD_COUNT_PROVIDER );
            Object[] args = new Object[]
            { 
                Integer.valueOf( uploadMgr.getUploadingCount() ),
                Integer.valueOf( uploadMgr.getUploadQueueSize() ),
                uploadCountProvider.getValue()
            };
            String text = Localizer.getFormatedString( "StatusBar_Uploads", args );
            uploadLabel.setText( text );
            uploadLabel.setToolTipText( Localizer.getString( "StatusBar_TTTUploads" ) );
            
            
            BandwidthController bwController = bwMgr.getUploadBandwidthController();
            long transferRate = bwController.getShortTransferAvg().getAverage();
            long throttlingRate = bwController.getThrottlingRate();
            String transferRateStr = NumberFormatUtils.formatSignificantByteSize(transferRate);
            String throttlingRateStr;
            if ( throttlingRate == BandwidthPrefs.UNLIMITED_BANDWIDTH )
            {
                throttlingRateStr = Localizer.getDecimalFormatSymbols().getInfinity();
            }
            else
            {
                throttlingRateStr = NumberFormatUtils.formatSignificantByteSize(throttlingRate);
            }
            bwLabel.setText( transferRateStr + Localizer.getString("PerSec") + " ("
                + throttlingRateStr + ")");
            
            validate();
        }
        catch ( Throwable th )
        {
            // dont let errors occuring during display of values cause trouble
            // at caller, this could i.e. interrupt startup
            NLogger.error( UploadZone.class, th, th );
        }
    }
    
}
