/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2005 Phex Development Group
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
 *  Created on 17.08.2006
 *  --- CVS Information ---
 *  $Id: PhexCorePrefs.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.prefs.core;

import phex.Res;
import phex.common.Environment;
import phex.common.EnvironmentConstants;
import phex.prefs.OldCfg;
import phex.prefs.api.Preferences;
import phex.utils.StringUtils;

public class PhexCorePrefs extends Preferences
{
    protected static final PhexCorePrefs instance;
    static
    {
        instance = new PhexCorePrefs();
    }

    protected PhexCorePrefs( )
    {
        super( Environment.getInstance().getPhexConfigFile(
            EnvironmentConstants.CORE_PREFERENCES_FILE_NAME ) );
    }
    
    public static void init()
    {
        instance.load();
        instance.updatePreferences();
        
        // count startup...
        StatisticPrefs.TotalStartupCounter.set( Integer.valueOf(
            StatisticPrefs.TotalStartupCounter.get().intValue() + 1 ) );
    }
    
    public static void save( boolean force )
    {
        if ( force )
        {
            instance.saveRequiredNotify();
        }
        instance.save();
    }
    
    
    public void updatePreferences()
    {
        // first find out if this is the first time Phex is running...
        if ( StringUtils.isEmpty( UpdatePrefs.RunningBuildNumber.get() ) ) 
        {
            // this seems to be the first time phex is running...
            // in this case we are not updating... we use default values...
        }
        UpdatePrefs.RunningBuildNumber.set( 
            Environment.getInstance().getProperty( "build.number" ) );
        UpdatePrefs.RunningPhexVersion.set( Res.getStr( "Program.Version" ) );
    }

    public static void updatePreV30Config( OldCfg cfg )
    {
        // map old values to new values...
        NetworkPrefs.ServentGuid.set( cfg.mProgramClientID.toHexString() );
        NetworkPrefs.ListeningPort.set( Integer.valueOf( cfg.mListeningPort ) );
        NetworkPrefs.CurrentNetwork.set( cfg.mCurrentNetwork );
        NetworkPrefs.NetworkHistory.set( cfg.mNetNetworkHistory );
        NetworkPrefs.ConnectedToLAN.set( Boolean.valueOf( cfg.connectedToLAN ) );
        NetworkPrefs.TcpConnectTimeout.set( Integer.valueOf( cfg.socketConnectTimeout ) );
        NetworkPrefs.TcpRWTimeout.set( Integer.valueOf( cfg.socketRWTimeout ) );
        NetworkPrefs.MaxConcurrentConnectAttempts.set( Integer.valueOf( cfg.maxConcurrentConnectAttempts ) );
        NetworkPrefs.MaxHostInHostCache.set( Integer.valueOf( cfg.mNetMaxHostToCatch ) );
        NetworkPrefs.AllowChatConnection.set( Boolean.valueOf( cfg.isChatEnabled ) );
        
        ConnectionPrefs.AutoConnectOnStartup.set( Boolean.valueOf( cfg.mAutoConnect) );
        ConnectionPrefs.AllowToBecomeUP.set( Boolean.valueOf( cfg.allowToBecomeUP ) );
        ConnectionPrefs.ForceToBeUltrapeer.set( Boolean.valueOf( cfg.forceToBeUltrapeer ) );
        ConnectionPrefs.Up2UpConnections.set( Integer.valueOf( cfg.up2upConnections ) );
        ConnectionPrefs.Up2LeafConnections.set( Integer.valueOf( cfg.up2leafConnections ) );
        ConnectionPrefs.Leaf2UpConnections.set( Integer.valueOf( cfg.leaf2upConnections ) );
        ConnectionPrefs.HasConnectedIncomming.set( Boolean.valueOf( cfg.hasConnectedIncomming ) );
        ConnectionPrefs.OfflineConnectionFailureCount.set( Integer.valueOf( cfg.offlineConnectionFailureCount ) );
        ConnectionPrefs.EnableQueryHitSnooping.set( Boolean.valueOf( cfg.enableHitSnooping ) );
        ConnectionPrefs.AcceptDeflateConnection.set( Boolean.valueOf( cfg.isDeflateConnectionAccepted ) );
        
        MessagePrefs.MaxLength.set( Integer.valueOf( cfg.maxMessageLength ) );
        MessagePrefs.MaxNetworkTTL.set( Integer.valueOf( cfg.maxNetworkTTL ) );
        MessagePrefs.TTL.set( Integer.valueOf( cfg.ttl ) );
        
        DownloadPrefs.DestinationDirectory.set( cfg.mDownloadDir );
        DownloadPrefs.IncompleteDirectory.set( cfg.incompleteDir );
        DownloadPrefs.MaxWorkerPerDownload.set( Integer.valueOf( cfg.maxWorkerPerDownload ) );
        DownloadPrefs.MaxTotalDownloadWorker.set( Integer.valueOf( cfg.maxTotalDownloadWorker ) );
        DownloadPrefs.MaxWriteBufferPerDownload.set( Integer.valueOf( cfg.maxWriteBufferPerDownload ) );
        DownloadPrefs.MaxTotalDownloadWriteBuffer.set( Integer.valueOf( cfg.maxTotalDownloadWriteBuffer ) );
        DownloadPrefs.MaxDownloadsPerIP.set( Integer.valueOf( cfg.maxDownloadsPerIP ) );
        DownloadPrefs.SegmentInitialSize.set(  Integer.valueOf( cfg.initialSegmentSize ) );
        DownloadPrefs.SegmentTransferTargetTime.set(  Integer.valueOf( cfg.segmentTransferTime ) );
        DownloadPrefs.SegmentMaximumSize.set( Integer.valueOf( cfg.maximumSegmentSize ) );
        DownloadPrefs.SegmentMultiple.set( Integer.valueOf( (int)cfg.segmentMultiple ) );
        DownloadPrefs.CandidateMinAllowedTransferRate.set( Integer.valueOf( cfg.minimumAllowedTransferRate ) );
        DownloadPrefs.CandidateLogBufferSize.set( Integer.valueOf( (int)cfg.downloadCandidateLogBufferSize ) );
        DownloadPrefs.PushRequestTimeout.set( Integer.valueOf( cfg.mPushTransferTimeout ) );
        DownloadPrefs.AutoReadoutMagmaFiles.set( Boolean.valueOf( cfg.autoReadoutDownloadedMagma ) );
        DownloadPrefs.AutoReadoutMetalinkFiles.set( Boolean.valueOf( cfg.autoReadoutDownloadedMetalink ) );
        DownloadPrefs.AutoReadoutRSSFiles.set( Boolean.valueOf( cfg.autoReadoutDownloadedRSS ) );
        DownloadPrefs.AutoRemoveCompleted.set( Boolean.valueOf( cfg.mDownloadAutoRemoveCompleted) );
        
        UploadPrefs.MaxParallelUploads.set( Integer.valueOf( cfg.mMaxUpload ) );
        UploadPrefs.MaxUploadsPerIP.set( Integer.valueOf( cfg.mMaxUploadPerIP) );
        UploadPrefs.AutoRemoveCompleted.set( Boolean.valueOf( cfg.mUploadAutoRemoveCompleted ) );
        UploadPrefs.SharePartialFiles.set( Boolean.valueOf( cfg.arePartialFilesShared ) );
        UploadPrefs.AllowQueuing.set( Boolean.valueOf( cfg.allowUploadQueuing ) );
        UploadPrefs.MaxQueueSize.set( Integer.valueOf( cfg.maxUploadQueueSize ) );
        UploadPrefs.MinQueuePollTime.set( Integer.valueOf( cfg.minUploadQueuePollTime ) );
        UploadPrefs.MaxQueuePollTime.set( Integer.valueOf( cfg.maxUploadQueuePollTime ) );
        UploadPrefs.UploadStateLogBufferSize.set( Integer.valueOf( (int)cfg.uploadStateLogBufferSize ) );
        
        BandwidthPrefs.NetworkSpeedKbps.set( Integer.valueOf( cfg.networkSpeedKbps ) );
        BandwidthPrefs.MaxTotalBandwidth.set( Integer.valueOf( cfg.maxTotalBandwidth ) );
        BandwidthPrefs.MaxNetworkBandwidth.set( Integer.valueOf( cfg.mNetMaxRate ) );
        BandwidthPrefs.MaxDownloadBandwidth.set( Integer.valueOf( cfg.mDownloadMaxBandwidth ) );
        BandwidthPrefs.MaxUploadBandwidth.set( Integer.valueOf( cfg.mUploadMaxBandwidth ) );
        
        LibraryPrefs.SharedDirectoriesSet.set( cfg.sharedDirectoriesSet );
        LibraryPrefs.LibraryExclusionRegExList.set( cfg.libraryExclusionRegExList );
        LibraryPrefs.UrnCalculationMode.set( Integer.valueOf( cfg.urnCalculationMode ) );
        LibraryPrefs.ThexCalculationMode.set( Integer.valueOf( cfg.thexCalculationMode ) );
        LibraryPrefs.AllowBrowsing.set( Boolean.valueOf( cfg.mShareBrowseDir ) );
        
        SecurityPrefs.FilteredPorts.set( cfg.filteredCatcherPorts );
        
        ProxyPrefs.ForcedIp.set( cfg.mMyIP );
        ProxyPrefs.UseSocks5.set( Boolean.valueOf( cfg.mProxyUse ) );
        ProxyPrefs.Socks5Host.set( cfg.mProxyHost );
        ProxyPrefs.Socks5Port.set( Integer.valueOf( cfg.mProxyPort ) );
        ProxyPrefs.Socks5Authentication.set( Boolean.valueOf( cfg.useProxyAuthentication ) );
        ProxyPrefs.Socks5User.set( cfg.mProxyUserName );
        ProxyPrefs.Socks5Password.set( cfg.mProxyPassword );
        
        ProxyPrefs.UseHttp.set( Boolean.valueOf( cfg.isHttpProxyUsed ) );
        ProxyPrefs.HttpHost.set( cfg.httpProxyHost );
        ProxyPrefs.HttpPort.set( Integer.valueOf( cfg.httpProxyPort ) );
        
        StatisticPrefs.FractionalUptime.set( Float.valueOf( cfg.fractionalUptime ) );
        StatisticPrefs.LastShutdownTime.set( Long.valueOf( cfg.lastShutdownTime ) );
        StatisticPrefs.MaximalUptime.set( Long.valueOf( cfg.maximalUptime ) );
        StatisticPrefs.MovingTotalUptime.set( Long.valueOf( cfg.movingTotalUptime) );
        StatisticPrefs.MovingTotalUptimeCount.set( Integer.valueOf( cfg.movingTotalUptimeCount ) );
        StatisticPrefs.TotalDownloadCount.set( Integer.valueOf( cfg.totalDownloadCount ) );
        StatisticPrefs.TotalStartupCounter.set( Integer.valueOf( cfg.totalStartupCounter ) );
        StatisticPrefs.TotalUploadCount.set( Integer.valueOf( cfg.totalUploadCount ) );
        
        UpdatePrefs.LastUpdateCheckVersion.set( cfg.lastUpdateCheckVersion );
        UpdatePrefs.LastBetaUpdateCheckVersion.set( cfg.lastBetaUpdateCheckVersion );
        UpdatePrefs.LastUpdateCheckTime.set( Long.valueOf( cfg.lastUpdateCheckTime ) );
        UpdatePrefs.LastShownUpdateInfoId.set( Integer.valueOf( cfg.lastShownUpdateInfoId ) );
        
        FilePrefs.OpenFilesLimit.set( Integer.valueOf( cfg.openFilesLimit ) );
        
        SubscriptionPrefs.DownloadSilently.set( Boolean.valueOf( cfg.downloadSubscriptionsSilently ) );
        SubscriptionPrefs.SubscriptionMagnets.set( cfg.subscriptionMagnets );
        
        // TODO finished mapping... delete old cfg...
        
    }
}
