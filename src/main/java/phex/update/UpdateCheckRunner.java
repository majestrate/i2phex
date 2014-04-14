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
 *  --- CVS Information ---
 *  $Id: UpdateCheckRunner.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.time.DateUtils;

import phex.common.Environment;
import phex.common.LongObj;
import phex.common.log.NLogger;
import phex.event.UpdateNotificationListener;
import phex.gui.common.GUIRegistry;
import phex.prefs.core.PhexCorePrefs;
import phex.prefs.core.StatisticPrefs;
import phex.prefs.core.UpdatePrefs;
import phex.servent.Servent;
import phex.share.SharedFilesService;
import phex.statistic.StatisticProvider;
import phex.statistic.StatisticProviderConstants;
import phex.statistic.StatisticsManager;
import phex.utils.IOUtil;
import phex.utils.VersionUtils;
import phex.xml.sax.DPhex;
import phex.xml.sax.DUpdateRequest;
import phex.xml.sax.DUpdateResponse;
import phex.xml.sax.XMLBuilder;
import phex.xml.sax.DUpdateResponse.VersionType;

/**
 * The UpdateCheckRunner handles regular update check against the phex website.
 * It is also used to collect Phex statistics to identify possible problem areas
 * with new versions.<br/>
 * Phex informations are transmitted in an XML structure, the update information
 * response is also returned in an XML structure.
 */
public class UpdateCheckRunner implements Runnable
{
    // I2PMOD:
    // Since updates aren't signed, and to avoid leaking needless information
    // like the fact of using Phex instead of merely I2P, don't check for updates.
    // Emptying the URL here is merely a crude first part of disabling them.
    private static final String UPDATE_CHECK_URL = "http://";
    
    private Throwable updateCheckError;
    private UpdateNotificationListener listener;
    private String releaseVersion;
    private String betaVersion;
    private boolean isBetaInfoShown;
    
    public UpdateCheckRunner( UpdateNotificationListener updateListener, boolean showBetaInfo )
    {
        listener = updateListener;
        this.isBetaInfoShown = showBetaInfo;
    }
    
    /**
     * Trigger a automated background update check. This is the standard Phex
     * check done every week to display the update dialog or just collect Phex
     * statistics.
     * The call is not blocking.
     */
    public static void triggerAutoBackgroundCheck(
        final UpdateNotificationListener updateListener, 
        final boolean showBetaInfo )
    {
        // I2PMOD:
        // For the I2P version, update checks are disabled for now.
        return;
        /*
        if ( UpdatePrefs.LastUpdateCheckTime.get().longValue() >
             System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY * 7 )
        {
            NLogger.debug( UpdateCheckRunner.class, "No update check necessary." );
            return;
        }
        NLogger.debug( UpdateCheckRunner.class, "Triggering update check." );
        UpdateCheckRunner runner = new UpdateCheckRunner( updateListener, showBetaInfo );
        Environment.getInstance().executeOnThreadPool( runner, "UpdateCheckRunner" );
        */
    }
    
    public String getReleaseVersion()
    {
        return releaseVersion;
    }

    public String getBetaVersion()
    {
        return betaVersion;
    }

    /**
     * Returns a possible Throwable that could be thrown during the update check
     * or null if no error was caught.
     * @return a possible Throwable that could be thrown during the update check
     * or null if no error was caught.
     */
    public Throwable getUpdateCheckError()
    {
        return updateCheckError;
    }
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        // I2PMOD:
        // For the I2P version, update checks are disabled for now.
        return;
        /*
         try
        {            
            performUpdateCheck();
        }
        catch ( Throwable exp )
        {
            updateCheckError = exp;
            NLogger.warn( UpdateCheckRunner.class, exp, exp );
        }
        */
    }
    
    private void performUpdateCheck()
    {
        URL url;
        DPhex dPhex;
        try
        {
            url = new URL( UPDATE_CHECK_URL );
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setUseCaches( false );
            connection.setRequestProperty( "User-Agent", Environment.getPhexVendor() );
            connection.setRequestMethod( "POST" );
            connection.setDoOutput( true );
            connection.setRequestProperty( "Content-Type",
                "text/xml; charset=UTF-8" );
                        
            OutputStream outStream = connection.getOutputStream();
            byte[] data = buildXMLUpdateRequest();
            if ( data == null )
            {
                throw new IOException( "Missing XML update data" );
            }
            outStream.write( data );
            if ( NLogger.isDebugEnabled( UpdateCheckRunner.class ) )
            {
                NLogger.debug( UpdateCheckRunner.class, new String(data) );
            }
            
            // dont need to buffer stream already done by Properties.load()
            InputStream inStream = connection.getInputStream();
            if ( NLogger.isDebugEnabled( UpdateCheckRunner.class ) )
            {
                byte[] respData = IOUtil.toByteArray( inStream );
                NLogger.debug( UpdateCheckRunner.class, new String(respData) );
                inStream = new ByteArrayInputStream( respData );
            }
            dPhex = XMLBuilder.readDPhexFromStream( inStream );
        }
        catch ( MalformedURLException exp )
        {
            updateCheckError = exp;
            NLogger.warn( UpdateCheckRunner.class, exp, exp );
            assert false;
            throw new RuntimeException( );
        }
        catch ( UnknownHostException exp )
        {
            // can't find way to host
            // this maybe means we have no internet connection
            updateCheckError = exp;
            NLogger.warn( UpdateCheckRunner.class, exp, exp );
            return;
        }
        catch ( SocketException exp )
        {
            // can't connect... maybe a proxy is in the way...
            updateCheckError = exp;
            NLogger.warn( UpdateCheckRunner.class, exp, exp );
            return;
        }
        catch ( IOException exp )
        {
            updateCheckError = exp;
            NLogger.warn( UpdateCheckRunner.class, exp, exp );
            return;
        }

        UpdatePrefs.LastUpdateCheckTime.set( Long.valueOf(System.currentTimeMillis()) );
        
        DUpdateResponse response = dPhex.getUpdateResponse();
        List<VersionType> versionList = response.getVersionList();
        VersionType latestReleaseVersion = null;
        VersionType latestBetaVersion = null;
        
        for ( VersionType currentVersion : versionList )
        {
            if ( currentVersion.isBeta() )
            {
                if ( latestBetaVersion == null || VersionUtils.compare(
                    currentVersion.getId(), latestBetaVersion.getId() ) > 0 )
                {
                    latestBetaVersion = currentVersion;
                }
            }
            else
            {
                if ( latestReleaseVersion == null || VersionUtils.compare(
                    currentVersion.getId(), latestReleaseVersion.getId() ) > 0 )
                {
                    latestReleaseVersion = currentVersion;
                }
            }
        }
        
        
        betaVersion = "0";
        releaseVersion = "0";
        if ( latestBetaVersion != null )
        {
            betaVersion = latestBetaVersion.getId();
        }
        if ( latestReleaseVersion != null )
        {
            releaseVersion = latestReleaseVersion.getId();
        }
        
        int releaseCompare = 0;
        int betaCompare = 0;
        betaCompare = VersionUtils.compare( betaVersion,
            VersionUtils.getFullProgramVersion() );
        releaseCompare = VersionUtils.compare( releaseVersion,
            VersionUtils.getFullProgramVersion() );
        
        if ( releaseCompare <= 0 && betaCompare <= 0 )
        {
            PhexCorePrefs.save( false );
            return;
        }
        
        betaCompare = VersionUtils.compare( betaVersion, 
            UpdatePrefs.LastBetaUpdateCheckVersion.get() );        
        releaseCompare = VersionUtils.compare( releaseVersion,
            UpdatePrefs.LastUpdateCheckVersion.get() );

        int verDiff = VersionUtils.compare( betaVersion,
            releaseVersion );

        boolean triggerUpdateNotification = false;
        if ( releaseCompare > 0 )
        {
            UpdatePrefs.LastUpdateCheckVersion.set( releaseVersion );
            triggerUpdateNotification = true;
        }
        if ( betaCompare > 0 )
        {
            UpdatePrefs.LastBetaUpdateCheckVersion.set( betaVersion );
            triggerUpdateNotification = true;
        }

        if ( verDiff > 0 )
        {
            // reset release version since beta is more up-to-date
            releaseVersion = null;
        }
        else
        {
            // reset beta version since release is the one to go.
            betaVersion = null;
        }

        PhexCorePrefs.save( false );
        if ( triggerUpdateNotification )
        {
            fireUpdateNotification();
        }
    }
    
    private void fireUpdateNotification()
    {
        listener.updateNotification( this );
    }
    
    private byte[] buildXMLUpdateRequest()
    {
        Servent servent = Servent.getInstance();
        try
        {
            DPhex dPhex = new DPhex();
            DUpdateRequest dRequest = new DUpdateRequest();
            dPhex.setUpdateRequest( dRequest );
            
            // I2PFIXME:
            // Some details over here might be undesirable to transmit,
            // even when in future we might be able to check for signed updates
            // and do it over eepproxy.
            dRequest.setCurrentVersion( VersionUtils.getFullProgramVersion() );
            dRequest.setStartupCount( StatisticPrefs.TotalStartupCounter.get().intValue() );
            dRequest.setLafUsed( GUIRegistry.getInstance().getUsedLAFClass() );
            dRequest.setJavaVersion( System.getProperty( "java.version" ) );
            dRequest.setOperatingSystem( SystemUtils.OS_NAME );
            
            dRequest.setHostid( servent.getServentGuid().toHexString() );
            dRequest.setShowBetaInfo( isBetaInfoShown );
            dRequest.setLastInfoId( UpdatePrefs.LastShownUpdateInfoId.get().intValue() );
            
            String lastCheckVersion;
            if ( VersionUtils.compare( UpdatePrefs.LastUpdateCheckVersion.get(),
                UpdatePrefs.LastBetaUpdateCheckVersion.get() ) > 0 )
            {
                lastCheckVersion = UpdatePrefs.LastUpdateCheckVersion.get();
            }
            else
            {
                lastCheckVersion = UpdatePrefs.LastBetaUpdateCheckVersion.get();
            }
            dRequest.setLastCheckVersion( lastCheckVersion );
            
            StatisticsManager statMgr = servent.getStatisticsService();
            
            StatisticProvider uptimeProvider = statMgr.getStatisticProvider(
                StatisticsManager.UPTIME_PROVIDER );
            dRequest.setAvgUptime(
                ((LongObj)uptimeProvider.getAverageValue()).value );
            
            StatisticProvider dailyUptimeProvider = statMgr.getStatisticProvider(
                StatisticsManager.DAILY_UPTIME_PROVIDER );
            dRequest.setDailyAvgUptime(
                ((Integer)dailyUptimeProvider.getValue()).intValue() );
                
            StatisticProvider downloadProvider = statMgr.getStatisticProvider(
                StatisticProviderConstants.TOTAL_DOWNLOAD_COUNT_PROVIDER );
            dRequest.setDownloadCount(
                (int)((LongObj)downloadProvider.getValue()).value );
                
            StatisticProvider uploadProvider = statMgr.getStatisticProvider(
                StatisticProviderConstants.TOTAL_UPLOAD_COUNT_PROVIDER );
            dRequest.setUploadCount(
                (int)((LongObj)uploadProvider.getValue()).value );
                
            SharedFilesService sharedFilesService = Servent.getInstance().getSharedFilesService();
            dRequest.setSharedFiles( sharedFilesService.getFileCount() );
            dRequest.setSharedSize( sharedFilesService.getTotalFileSizeInKb() );
            
            dRequest.setErrorLog( getErrorLogFileTail() );
            
            return phex.xml.sax.XMLBuilder.serializeToBytes( dPhex );
        }
        catch ( IOException exp )
        {
            NLogger.error( UpdateCheckRunner.class, exp, exp );
            return null;
        }
    }
    
    private String getErrorLogFileTail()
    {
        try
        {
            File logFile = Environment.getInstance().getPhexConfigFile( "phex.error.log" );
            if ( !logFile.exists() )
            {
                return null;
            }
            RandomAccessFile raf = new RandomAccessFile( logFile, "r" );
            long pos = Math.max( raf.length() - 10 * 1024, 0 );
            raf.seek(pos);
            byte[] buffer = new byte[ (int)Math.min( 10*1024, raf.length() ) ];
            int lenRead = raf.read( buffer );
            return new String( buffer, 0, lenRead );
        }
        catch ( IOException exp )
        {
            NLogger.error( UpdateCheckRunner.class, exp, exp );
            return exp.toString();
        }
    }
}
