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
 *  $Id: OldCfg.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.Security;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.SystemUtils;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import phex.Res;
import phex.common.Environment;
import phex.common.collections.SortedProperties;
import phex.common.log.NLogger;
import phex.msg.GUID;
import phex.utils.VersionUtils;

/**
 * @deprecated since Phex 3.0, drop support once 2.x is not in use anymore
 */
@SuppressWarnings( justification="Obsolete class will be dropped in the future.")
@Deprecated // since 3.0
public class OldCfg
{

    /**
     * Settings which have to be defined, before you can define your own ones...
     * DON'T CHANGE THESE!
     * ... 
     * ...
     * DON'T CHANGE THESE AGAIN! :)
     */
    public final static String GENERAL_GNUTELLA_NETWORK = "<General Gnutella Network>";
    
    /* 
     * settings you might want to change, 
     * if you want to create a private-network with Phex 
     */

    /** 
     * If you like to use a PRIVATE_NETWORK specify here your chosen network
     * name for a private Net by replacing null with "<Network Name>". 
     * If you do, please choose a short ID for your Network, which will 
     * be displaed in the Vendor-String. 
     * You might want to choose a name, which can last, for you can't know, 
     * how your network will exist. 
     * For better appearance, put a space in front of it 
     * and write it in lowercase. (Your privateversion.number will be appended) 
     */
    public final static String PRIVATE_NETWORK = null;
    public final static String PRIVATE_BUILD_ID = ""; 
    
    /**
     * Assign here the default network to use. This can be the 
     * GENERAL_GNUTELLA_NETWORK or the self defined PRIVATE_NETWORK.
     * If you want to change something, do so above. 
     */
    public static final String DEFAULT_NETWORK_TO_USE = 
        PRIVATE_NETWORK == null ? GENERAL_GNUTELLA_NETWORK : PRIVATE_NETWORK;    

    /**
     * The default value to indicate if this node is forced to be ultrapeer.
     * In a small private network you might want to make everyone an Ultrapeer. 
     */
    public static final boolean DEFAULT_FORCE_TOBE_ULTRAPEER = false;
    

    /* Further settings, you might not want to change */
    private static final String BACKUP_CONFIG_FILENAME = "phex.bk";
    public static final int DEFAULT_SOCKS5_PORT = 1080;
    public static final int DEFAULT_HTTP_PORT = 80;
    public static final int DEFAULT_MAX_MESSAGE_LENGTH = 65536;
    public static final short DEFAULT_LOGGER_VERBOSE_LEVEL = 6;
    public static final boolean DEFAULT_ENABLE_HIT_SNOOPING = true;
    public static final boolean DEFAULT_IS_CHAT_ENABLED = true;
    public static final boolean DEFAULT_ALLOW_TO_BECOME_LEAF = true;
    public static final boolean DEFAULT_ALLOW_TO_BECOME_ULTRAPEER = true;
    public static final boolean DEFAULT_FORCE_UP_CONNECTIONS = true;
    public static final boolean DEFAULT_IS_NOVENDOR_NODE_DISCONNECTED = false;
    public static final int DEFAULT_FREELOADER_FILES = 0;
    public static final int DEFAULT_FREELOADER_SHARE_SIZE = 0;
    public static final int DEFAULT_HOST_ERROR_DISPLAY_TIME = 1000;
    public static final int DEFAULT_TTL = 7;
    public static final int DEFAULT_MAX_NETWORK_TTL = 7;
    public static final int DEFAULT_UP_2_UP_CONNECTIONS = 32;
    public static final int DEFAULT_UP_2_LEAF_CONNECTIONS = 31;
    public static final int DEFAULT_UP_2_PEER_CONNECTIONS = 0;
    public static final int DEFAULT_LEAF_2_UP_CONNECTIONS = 5;
    public static final int MAX_LEAF_2_UP_CONNECTIONS = 5;
    public static final int DEFAULT_LEAF_2_PEER_CONNECTIONS = 0;
    public static final int DEFAULT_PEER_CONNECTIONS = 4;
    public static final int DEFAULT_MAX_CONNECTTO_HISTORY_SIZE = 10;
    public static final int DEFAULT_MAX_SEARCHTERM_HISTORY_SIZE = 10;
    public static final boolean DEFAULT_ARE_PARTIAL_FILES_SHARED = true;
    private static final char LIST_PREFIX = '_';
    
    public static final short DEFAULT_URN_CALCUATION_MODE = 2;
    public static final short DEFAULT_THEX_CALCUATION_MODE = 2;
    
    /**
     * The default value of the X-Max-TTL header for dynamic queries. 
     */
    public static final int DEFAULT_DYNAMIC_QUERY_MAX_TTL = 4;



    /**
     * The default max downloads that are allowed per IP.
     */
    public static final int DEFAULT_MAX_DOWNLOADS_PER_IP = 1;

    /**
     * The default value to indicate whether upload queuing is activated or not.
     */
    public static final boolean DEFAULT_ALLOW_UPLOAD_QUEUING = true;

    /**
     * The default max upload queue slots available.
     */
    public static final int DEFAULT_MAX_UPLOAD_QUEUE_SIZE = 10;

    /**
     * The default min poll time for queued uploads.
     */
    public static final int DEFAULT_MIN_UPLOAD_QUEUE_POLL_TIME = 45;

    /**
     * The default max poll time for queued uploads.
     */
    public static final int DEFAULT_MAX_UPLOAD_QUEUE_POLL_TIME = 120;
    
    /**
     * The default setting if we accept deflate connections.
     */
    public static final boolean DEFAULT_IS_DEFLATE_CONNECTION_ACCEPTED = true;
    
    /**
     * The default socket connect timeout.
     */
    public static final int DEFAULT_SOCKET_CONNECT_TIMEOUT = 60 * 1000;
    
    /**
     * The default socket read/write timeout.
     */
    public static final int DEFAULT_SOCKET_RW_TIMEOUT = DEFAULT_SOCKET_CONNECT_TIMEOUT;
    
    /**
     * The default number of maximum concurrent connection attempts allowed on
     * a XP system. (XP limits this to 10, leave 2 for other process)
     */
    public static final int DEFAULT_MAX_CONCURRENT_CONNECT_ATTEMPTS_XP = 8;
    
    /**
     * The default number of maximum concurrent connection attempts allowed on
     * a other systems then XP. (XP limits this to 10, leave 2 for other process)
     */
    public static final int DEFAULT_MAX_CONCURRENT_CONNECT_ATTEMPTS_OTHERS = 50;
    
    /**
     * The default number of consecutive failed connection after which the servent 
     * is called as offline.
     */
    public static final int DEFAULT_OFFLINE_CONNECTION_FAILURE_COUNT = 100;
    
    /**
     * The default setting of the last shown update info id from the Phex web
     * server.
     */
    public static final int DEFAULT_LAST_SHOWN_UPDATE_INFO_ID = 0;
    
    /**
     * The default number of download worker per download.
     */
    public static final short DEFAULT_MAX_WORKER_PER_DOWNLOAD = 9;
    
    /**
     * The default number of total download worker.
     */
    public static final short DEFAULT_MAX_TOTAL_DOWNLOAD_WORKER = 27;
    
    /**
     * The max number of bytes to buffer per download file. When the buffer is 
     * exceeded the complete buffered data is written to disk.
     * Default: 128KB
     */
    public static final int DEFAULT_MAX_WRITE_BUFFER_PER_DOWNLOAD = 128 * 1024;

    /**
     * The max number of bytes to buffer for all download files. When the buffer
     * is exceeded the complete buffered data is written to disk.
     * Default: 1MB
     */
    public static final int DEFAULT_MAX_TOTAL_DOWNLOAD_WRITE_BUFFER = 1 * 1024 * 1024;
    
    public static final boolean DEFAULT_AUTO_READOUT_DOWNLOADED_MAGMA = true;
    public static final boolean DEFAULT_AUTO_READOUT_DOWNLOADED_METALINK = true;
    public static final boolean DEFAULT_AUTO_READOUT_DOWNLOADED_RSS = true;
    
    /**
     * The default limit of open files the FileManager uses.
     */
    public static final short DEFAULT_OPEN_FILES_LIMIT = 0;
    
    /**
     * The default command to execute for video media types on systems other 
     * then Windows and Mac OSX (usually Unix systems).
     */
    public static final String DEFAULT_OTHER_OS_VIDEO_COMMAND = 
        "xterm -e mplayer %filepath%";
    
    /**
     * The default command to execute for image media types on systems other 
     * then Windows and Mac OSX (usually Unix systems).
     */
    public static final String DEFAULT_OTHER_OS_IMAGE_COMMAND =
        "ee %filepath%";
    
    /**
     * The default command to execute for audio media types on systems other 
     * then Windows and Mac OSX (usually Unix systems).
     */
    public static final String DEFAULT_OTHER_OS_AUDIO_COMMAND =
        "xterm -e mplayer %filepath%";
    
    /**
     * The default command to execute for browser media types on systems other 
     * then Windows and Mac OSX (usually Unix systems).
     */
    public static final String DEFAULT_OTHER_OS_BROWSER_COMMAND =
        "mozilla -remote openURL(%filepath%,new-window)";

    public static final int UNLIMITED_BANDWIDTH = Integer.MAX_VALUE;
    public static int MIN_SEARCH_TERM_LENGTH = 2;

    /**
     * The default segment size for a allocated segment.
     */
    public static final int INITIAL_SEGMENT_SIZE = 16 * 1024; // 16Kb

    /**
     * When a segment has been downloaded by a candidate, the next segment for this candidate
     * should be such that (at the same rate as the previous segment) the segment takes 90 seconds
     * to complete
     */
    public static final short SEGMENT_TRANSFER_TIME = 90;

    /**
     * If a segment is transferred at less than this speed (b/sec), refuse further downloads
     * from this candidate
     */
    public static final int MINIMUM_ALLOWED_TRANSFER_RATE = 1;
    
    /**
     * Do not allow segment to be larger than this value, regardless of the previous
     * segment's download rate
     */
    public static final int MAXIMUM_SEGMENT_SIZE = 10 * 1024 * 1024; // 10Mb
    
    public int initialSegmentSize;
    public int segmentTransferTime;
    public int minimumAllowedTransferRate;
    public int maximumSegmentSize;


    public GUID					mProgramClientID = new GUID();
    public String mMyIP = "";
    public int					mListeningPort = -1;
    public int					mMaxUpload = 4;
    public int					mMaxUploadPerIP = 1;
    public int					mUploadMaxBandwidth = 102400;
    public int                  mNetMaxHostToCatch = 1000;
    public int					mNetMaxSendQueue = 500;
    //public int mPingFrequency = 15000;
    public int					mSearchMaxConcurrent = 10;

    public int					mNetMaxRate = 50000;

    public int mDownloadMaxBandwidth = 102400;
    public boolean				mDownloadAutoRemoveCompleted = false;
    public boolean autoReadoutDownloadedMagma = true;
    public boolean autoReadoutDownloadedMetalink = true;
    public boolean autoReadoutDownloadedRSS = true;
    public boolean downloadSubscriptionsSilently = false;
    public String				mDownloadDir = ".";

    public boolean mAutoConnect = true;
    
    /** 
     * Update-Uris for the various Systems
     *   
     */
    public static final String UPDATE_URI_MAC_OSX = "magnet:?xs=http://draketo.de/magma/phex-update/phex_osx.magma&dn=phex_osx.magma";
    public static final String UPDATE_URI_WINDOWS = "magnet:?xs=http://draketo.de/magma/phex-update/phex_win.magma&dn=phex_win.magma";
    public static final String UPDATE_URI_OTHER = "magnet:?xs=http://draketo.de/magma/phex-update/phex_other.magma&dn=phex_other.magma";
    
    /** 
     * Subscription-Uris
     * These should point to magma-files.  
     */
	public ArrayList subscriptionMagnets;
	public static final String default_subscriptionMagnets = null; /* new String("magnet:?xs=http://draketo.de/magma/filk-filme.magma&dn=filk-filme.magma"); */

    public boolean mAutoCleanup = true;

    /**
     * The max of this value should be 255. The protocol is not able to handle
     * more.
     */
    public int mUploadMaxSearch = 64;
    public boolean mShareBrowseDir = true;
    public int mPushTransferTimeout = 30 * 1000;

    public String mCurrentNetwork = DEFAULT_NETWORK_TO_USE;
    public ArrayList mNetNetworkHistory = new ArrayList();
    public boolean				mAutoJoin = true;
    public boolean				mDisconnectApplyPolicy = false;
    public int					mDisconnectDropRatio = 70;
    public boolean				mProxyUse = false;
    public String				mProxyHost = "";
    public int mProxyPort = DEFAULT_SOCKS5_PORT;
    public boolean useProxyAuthentication = false;
    public String mProxyUserName = "";
    public String mProxyPassword = "";
        
    /**
     * @deprecated since 2.1.5.80 - New variable called sharedDirectoriesSet.
     */
    @Deprecated
    public String mUploadDir = "";
    
    /**
     * @deprecated since 2.1.9.83 - New variable called libraryExclusionRegExList
     */
    @Deprecated
    public String				mUploadFileExclusions = "";
    
    /**
     * @deprecated since 2.1.9.83 - New variable called libraryExclusionRegExList
     */
    @Deprecated
    public String				mUploadFileInclusions = "*";
    public boolean				mUploadAutoRemoveCompleted = false;
    
    /**
     * @since 2.1.5.80
     */
    public HashSet sharedDirectoriesSet;
    
    /**
     * @since 2.1.9.83
     */
    public ArrayList libraryExclusionRegExList;
    
    /**
     * Determines the urn calculation speed mode. Values should range
     * from 0 for high speed (full CPU) calculation up to maybe 10.
     * A good value is 2 which is also the default. The value states
     * the wait cycles between each 64K segment. A value of 2 means
     * wait twice as long as you needed to calculate the last 64K. 
     */
    public short urnCalculationMode;
    
    /**
     * Determines the thex calculation speed mode. Values should range
     * from 0 for high speed (full CPU) calculation up to maybe 10.
     * A good value is 2 which is also the default. The value states
     * the wait cycles between each 128K segment. A value of 2 means
     * wait twice as long as you needed to calculate the last 128K. 
     */
    public short thexCalculationMode;
    
    public boolean monitorSearchHistory = false;
    
    

    /**
     * The file into which searches should be monitored.
     */
    public String searchMonitorFile = "";
    public int searchHistoryLength = 10;

    /**
     * Indicates if the node is connected to a Local Area Network (LAN).
     */
    public boolean connectedToLAN = true;

    /**
     * Indicates whether Phex minimizes to the background on close of the GUI. If set to
     * false it will shutdown. If set to true on windows system it will go into the
     * sys tray, on all other system it will just minimize.
     */
    public boolean minimizeToBackground = true;

    /**
     * The status if a close options dialog should be displayed or not.
     */
    public boolean showCloseOptionsDialog = true;

    /**
     * The directory where incomplete files are stored.
     */
    public String incompleteDir = ".";

    /**
     * When the HostCatcher finds hosts it will first use the port filter
     * to see if it is allowed to use the host
     */
    public ArrayList filteredCatcherPorts = new ArrayList();

    /**
     * The max number of parallel workers per download file.
     */
    public short maxWorkerPerDownload;

    /**
     * The max number of total parallel workers for all download files.
     */
    public short maxTotalDownloadWorker;
    
    /**
     * The max number of bytes to buffer per download file. When the buffer is 
     * exceeded the complete buffered data is written to disk.
     */
    public int maxWriteBufferPerDownload;

    /**
     * The max number of bytes to buffer for all download files. When the buffer
     * is exceeded the complete buffered data is written to disk.
     */
    public int maxTotalDownloadWriteBuffer;

    /**
     * Indicates whether upload queuing is allowed or not.
     */
    public boolean allowUploadQueuing;

    /**
     * The maximal number of upload queue slots available.
     */
    public int maxUploadQueueSize;

    /**
     * The minimum poll time for queued uploads.
     */
    public int minUploadQueuePollTime;

    /**
     * The maximum poll time for queued uploads.
     */
    public int maxUploadQueuePollTime;

    /**
     * The maximum number of downloads that are allowed per IP.
     * Changing this value is not recommended since most, if not all servents
     * only accept one upload per IP.
     */
    public int maxDownloadsPerIP;

    /**
     * The total speed in kilo bits per second of the network connection the
     * user has available. This is not the bandwidth the user has available for
     * Phex.
     * The default of 256 matches a DSL/Cable connection.
     */
    public int networkSpeedKbps = 256;

    /**
     * This is the maximal bandwidth in bytes per second Phex is allowed to use
     * in total. This means network, download and upload bandwidth combined.
     * The default of 16384 matches 50% of the bandwidth a 256kbs DSL/Cable connection
     * is able to offer.
     */
    public int maxTotalBandwidth = 16384;

    /**
     * This is introduced to maintain the current version of Phex.
     * After a update of Phex the version in the cfg and the Phex version differs.
     * In this case we know that we need to upgrade the cfg or other stuff to the
     * new Phex version
     */
    public String runningPhexVersion = "";
    
    /**
     * This is introduced to maintain the current build number of Phex.
     * After a update of Phex the build number in the cfg and the Phex build number
     * differs. In this case we know that we need to upgrade the cfg and maybe
     * also do some other stuff to reach the new Phex version.
     */
    public String runningBuildNumber = "";

    /**
     * Defines if a http proxy is used for HTTP connections (not Gnutella
     * connections.
     */
    public boolean isHttpProxyUsed = false;

    /**
     * Defines the name of the http proxy host.
     */
    public String httpProxyHost = "";

    /**
     * Defines the port of the http proxy host.
     */
    public int httpProxyPort = DEFAULT_HTTP_PORT;

    /**
     * Contains the version number of the last update check.
     */
    public String lastUpdateCheckVersion = "0";

    /**
     * Contains the version number of the last beta update check.
     */
    public String lastBetaUpdateCheckVersion = "0";

    /**
     * Contains the time in millis of the last update check.
     */
    public long lastUpdateCheckTime = 0;

    /**
     * The status if a beta update notification dialog should be displayed or not.
     */
    public boolean showBetaUpdateNotification = false;

    /**
     * The create socket default connect timeout. 
     */
    public int socketConnectTimeout;
    
    /**
     * The sockets default read/write timeout.
     */
    public int socketRWTimeout;
    
    /**
     * The number of maximum concurrent connection attempts allowed.
     * (XP limits this to 10)
     */
    public int maxConcurrentConnectAttempts;

    /**
     * Timeout for network host connections.
     */
    // TODO this field seems not to be used anymore that much.. (just chat and
    // browse host) maybe it should be dropped and alligned with the new fields.
    public int mNetConnectionTimeout = 8 * 1000;

    /**
     * the time after which a automatic candidate search times out
     */
    public int searchRetryTimeout = 30000;
    
    /**
     * The LogBuffer size used for download candidates.
     */
    public long downloadCandidateLogBufferSize = 0;
    
    /**
     * The LogBuffer size used for upload state.
     */
    public long uploadStateLogBufferSize = 0;

    /**
     * Enables QueryHit Snooping.
     */
    public boolean enableHitSnooping;

    /**
     * The max length a message is allowed to have to be accepted.
     */
    public int maxMessageLength;

    /**
     * Indicates if the chat feature is enabled.
     */
    public boolean isChatEnabled;

    /**
     * Indicates that the node is allowed to connect as a leaf to ultrapeers.
     */
    public boolean allowToBecomeLeaf;

    /**
     * Indicates if the node is only accepting ultrapeer connections (as a leaf).
     * This value must always be checked together with allowToBecomeLeaf. If
     * allowToBecomeLeaf is false, a forceUPConnections value of true must be
     * ignored.
     */
    public boolean forceUPConnections;

    /**
     * Indicates if this node is allowed to become a Ultrapeer.
     */
    public boolean allowToBecomeUP;

    /**
     * Indicates if this node force to be a Ultrapeer.
     * This value must always be checked together with allowToBecomeUP. If
     * allowToBecomeUP is false, a forceToBeUltrapeer value of true must be
     * ignored.
     */
    public boolean forceToBeUltrapeer;

    /**
     * The number of ultrapeer to ultrapeer connections the nodes is allowed to
     * have open.
     * TODO2 this value is used for the X-Degree header but to reach high out degree
     * for dynamic query the value should be maintained above 15. There is no
     * way to ensure this yet.
     */
    public int up2upConnections;

    /**
     * The number of ultrapeer to leaf connections the nodes is allowed to
     * have open.
     */
    public int up2leafConnections;

    /**
     * The number of ultrapeer to normal peer connections the nodes is allowed to
     * have open.
     */

    public int up2peerConnections;

    /**
     * The number of leaf to ultrapeer connections the nodes is allowed to
     * have open. The max should be 3.
     */
    public int leaf2upConnections;

    /**
     * The number of leaf to normal peer connections this node is allowed to have
     * open.
     */
    public int leaf2peerConnections;

    /**
     * The number of normal peers the node is allowed to have as a normal peer.
     */
    public int peerConnections;
    
    /**
     * Indicates if the peers has connected incomming the last time it was
     * shutdown. The value is only updated in case of a server shutdown, but  
     * the Server maintains and holds the state changes during runtime.
     */
    public boolean hasConnectedIncomming;
    
    /**
     * The number of consecutive failed connection after which the servent 
     * is called as offline.
     */
    public int offlineConnectionFailureCount;

    /**
     * Indicates if nodes with no vendor code are disconnected.
     */
    public boolean isNoVendorNodeDisconnected;

    /**
     * The number of files a node need to share to not be called a freeloader.
     */
    public int freeloaderFiles;

    /**
     * The number of MB a node need to share to not be called a freeloader.
     */
    public int freeloaderShareSize;

    /**
     * The number of milliseconds a error is displayed in the connection table.
     */
    public int hostErrorDisplayTime;

    /**
     * The TTL Phex uses for messages.
     */
    public int ttl;

    /**
     * The maximim number of hops allowed to be seen in messages otherwise a
     * message is dropped. Also the highest ttl allowed to be seen in messages
     * otherwise the ttl is limited to Cfg.maxNetworkTTL - hops.
     */
    public int maxNetworkTTL;

    /**
     * History of connectTo items.
     */
    public ArrayList connectToHistory;

    /**
     * The max size of the connectToHistory list.
     */
    public int maxConnectToHistorySize;

    /**
     * History of search items.
     */
    public ArrayList searchTermHistory;
    
    /**
     * History of search items.
     */
    public ArrayList browseHostHistory;
    
    /**
     * The maximum number of open files the file manager opens.
     */
    public int openFilesLimit;
    
    /**
     * The command to execute for video media types on systems other then
     * Windows and Mac OSX
     */
    public String otherOsVideoCommand;
    
    /**
     * The command to execute for image media types on systems other then
     * Windows and Mac OSX (usually Unix systems).
     */
    public String otherOsImageCommand;
    
    /**
     * The command to execute for audio media types on systems other then
     * Windows and Mac OSX (usually Unix systems).
     */
    public String otherOsAudioCommand;
    
    /**
     * The browser command to execute for none video, image, audio media types 
     * on systems other then Windows and Mac OSX (usually Unix systems).
     */
    public String otherOsBrowserCommand;

    // if true, a request to preview the initial segment will first result
    // in a copy of it being made, and then the copy's filename passed
    // to the viewer. This is needed (I think) on windows platforms, at least
    public boolean copyBeforePreviewing;
    public static final boolean DEFAULT_COPY_BEFORE_PREVIEWING = true;

    public HashMap previewMethod;
    public String fallbackPreviewMethod;
    public static final String DEFAULT_FALLBACK_PREVIEW_METHOD = "";

    public String completionNotifyMethod;

    public long segmentMultiple;
    public static final long DEFAULT_SEGMENT_MULTIPLE = 4096;
    
    /**
     * The max size of the searchTerm list.
     */
    public int maxSearchTermHistorySize;

    /**
     * Indicates whether partial downloaded files are offered to others for download.
     */
    public boolean arePartialFilesShared;

    /**
     * The total uptime of the last movingTotalUptimeCount starts.
     */
    public long movingTotalUptime;

    /**
     * The number of times the uptime was added to movingTotalUptime.
     */
    public int movingTotalUptimeCount;

    /**
     * The maximal uptime ever seen.
     */
    public long maximalUptime;
    
    /** 
     * Last time Phex was shutdown. Needed for avg. daily uptime calculation.
     */
    public long lastShutdownTime;
    
    /**
     * The last fractional uptime calculated. Needed for avg. daily uptime
     * calculation.
     */
    public float fractionalUptime;
    
    /**
     * Counts the total number of Phex startups.
     */
    public int totalStartupCounter;
    
    /**
     * Indicates if we accept deflated connections.
     */
    public boolean isDeflateConnectionAccepted;
    
    /**
     * The id of the last shown update info from the Phex web server.
     */
    public int lastShownUpdateInfoId;
    
    /// some persistent statistic values...
    
    /**
     * The total number of completed downloads tracked.
     */
    public int totalDownloadCount;
    
    /**
     * The total number of uploads.
     */
    public int totalUploadCount;
    
    // localization
    /**
     * The locale files to use.
     */
    public String usedLocale;

    private File configFile;
    private Properties mSetting;

    public OldCfg( File cfgFile )
    {
        configFile = cfgFile;
        mSetting = new SortedProperties();
    }

    public void load()
    {
        loadDefaultValues();
        try
        {
            FileInputStream is = new FileInputStream( configFile );
            mSetting.load(is);
            is.close();
        }
        catch ( FileNotFoundException exp )
        {
            // no config file found. Is there a backup?
            try 
            {
                FileInputStream is2 = new FileInputStream( new File(configFile.getParentFile(), BACKUP_CONFIG_FILENAME) );
                mSetting.load(is2);
                is2.close();
                NLogger.error( OldCfg.class, "Loading configuration from backup file" );
            }
            catch ( FileNotFoundException exp2 )
            {
                // no backup file either
            }
            catch (Exception exp2)
            {
                NLogger.error( OldCfg.class, exp2, exp2 );
            }
        }
        catch ( Exception exp )
        {
            NLogger.error( OldCfg.class, exp, exp );
        }

        deserializeSimpleFields();
        deserializeComplexFields();

        handlePhexVersionAdjustments();

        // no listening port is set yet. Choose a random port from 4000-63999
        if (mListeningPort == -1 || mListeningPort > 65500 )
        {
            Random random = new Random(System.currentTimeMillis());
            mListeningPort = random.nextInt( 60000 );
            mListeningPort += 4000;
        }
        updateSystemSettings();
        
        // count startup...
        totalStartupCounter ++;
        
        // make sure directories exists...
        File dir = new File( mDownloadDir );
        dir.mkdirs();
        dir = new File( incompleteDir );
        dir.mkdirs();
    }

    private void loadDefaultValues()
    {
        maxDownloadsPerIP = DEFAULT_MAX_DOWNLOADS_PER_IP;
        enableHitSnooping = DEFAULT_ENABLE_HIT_SNOOPING;
        maxMessageLength = DEFAULT_MAX_MESSAGE_LENGTH;
        isChatEnabled = DEFAULT_IS_CHAT_ENABLED;
        allowToBecomeLeaf = DEFAULT_ALLOW_TO_BECOME_LEAF;
        forceUPConnections = DEFAULT_FORCE_UP_CONNECTIONS;
        forceToBeUltrapeer = DEFAULT_FORCE_TOBE_ULTRAPEER;
        allowToBecomeUP = DEFAULT_ALLOW_TO_BECOME_ULTRAPEER;
        isNoVendorNodeDisconnected = DEFAULT_IS_NOVENDOR_NODE_DISCONNECTED;
        freeloaderFiles = DEFAULT_FREELOADER_FILES;
        freeloaderShareSize = DEFAULT_FREELOADER_SHARE_SIZE;
        hostErrorDisplayTime = DEFAULT_HOST_ERROR_DISPLAY_TIME;
        ttl = DEFAULT_TTL;
        maxNetworkTTL = DEFAULT_MAX_NETWORK_TTL;
        up2upConnections = DEFAULT_UP_2_UP_CONNECTIONS;
        up2leafConnections = DEFAULT_UP_2_LEAF_CONNECTIONS;
        up2peerConnections = DEFAULT_UP_2_PEER_CONNECTIONS;
        leaf2upConnections = DEFAULT_LEAF_2_UP_CONNECTIONS;
        leaf2peerConnections = DEFAULT_LEAF_2_PEER_CONNECTIONS;
        peerConnections = DEFAULT_PEER_CONNECTIONS;
        arePartialFilesShared = DEFAULT_ARE_PARTIAL_FILES_SHARED;
        allowUploadQueuing = DEFAULT_ALLOW_UPLOAD_QUEUING;
        maxUploadQueueSize = DEFAULT_MAX_UPLOAD_QUEUE_SIZE;
        minUploadQueuePollTime = DEFAULT_MIN_UPLOAD_QUEUE_POLL_TIME;
        maxUploadQueuePollTime = DEFAULT_MAX_UPLOAD_QUEUE_POLL_TIME;
        isDeflateConnectionAccepted = DEFAULT_IS_DEFLATE_CONNECTION_ACCEPTED;
        lastShownUpdateInfoId = DEFAULT_LAST_SHOWN_UPDATE_INFO_ID;
        initialSegmentSize = INITIAL_SEGMENT_SIZE;
        segmentTransferTime = SEGMENT_TRANSFER_TIME;
        minimumAllowedTransferRate = MINIMUM_ALLOWED_TRANSFER_RATE;
        maximumSegmentSize = MAXIMUM_SEGMENT_SIZE;
        urnCalculationMode = DEFAULT_URN_CALCUATION_MODE;
        thexCalculationMode = DEFAULT_THEX_CALCUATION_MODE;
        fallbackPreviewMethod = DEFAULT_FALLBACK_PREVIEW_METHOD;
        otherOsAudioCommand = DEFAULT_OTHER_OS_AUDIO_COMMAND;
        otherOsImageCommand = DEFAULT_OTHER_OS_IMAGE_COMMAND;
        otherOsBrowserCommand = DEFAULT_OTHER_OS_BROWSER_COMMAND;
        otherOsVideoCommand = DEFAULT_OTHER_OS_VIDEO_COMMAND;
        
        completionNotifyMethod = null;
        // TODO: detect OS and set initial value based on this
        copyBeforePreviewing = DEFAULT_COPY_BEFORE_PREVIEWING;
        segmentMultiple = DEFAULT_SEGMENT_MULTIPLE;
        socketConnectTimeout = DEFAULT_SOCKET_CONNECT_TIMEOUT;
        socketRWTimeout = DEFAULT_SOCKET_RW_TIMEOUT;
        offlineConnectionFailureCount = DEFAULT_OFFLINE_CONNECTION_FAILURE_COUNT;
        maxWorkerPerDownload = DEFAULT_MAX_WORKER_PER_DOWNLOAD;
        maxTotalDownloadWorker = DEFAULT_MAX_TOTAL_DOWNLOAD_WORKER;
        maxWriteBufferPerDownload = DEFAULT_MAX_WRITE_BUFFER_PER_DOWNLOAD;
        maxTotalDownloadWriteBuffer = DEFAULT_MAX_TOTAL_DOWNLOAD_WRITE_BUFFER;
        autoReadoutDownloadedMagma = DEFAULT_AUTO_READOUT_DOWNLOADED_MAGMA;
        autoReadoutDownloadedMetalink = DEFAULT_AUTO_READOUT_DOWNLOADED_METALINK;
        autoReadoutDownloadedRSS = DEFAULT_AUTO_READOUT_DOWNLOADED_RSS;

        maxConnectToHistorySize = DEFAULT_MAX_CONNECTTO_HISTORY_SIZE;

        maxSearchTermHistorySize = DEFAULT_MAX_SEARCHTERM_HISTORY_SIZE;
        
        if ( SystemUtils.IS_OS_WINDOWS_XP )
        {
            maxConcurrentConnectAttempts = DEFAULT_MAX_CONCURRENT_CONNECT_ATTEMPTS_XP;
        }
        else
        {
            maxConcurrentConnectAttempts = DEFAULT_MAX_CONCURRENT_CONNECT_ATTEMPTS_OTHERS;
        }
        
        totalStartupCounter = 0;
    }

    private String get(String key)
    {
        String value = (String)mSetting.get(key);
        if (value != null)
            value = value.trim();
        return value;
    }

    private void deserializeSimpleFields()
    {
        Field[] fields = this.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++)
        {
            String name = fields[i].getName();
            int modifiers = fields[i].getModifiers();
            Class type = fields[i].getType();
            String value = "";

            if (!Modifier.isPublic(modifiers) ||
                Modifier.isTransient(modifiers) ||
                Modifier.isStatic(modifiers))
            {
                continue;
            }

            try
            {
                // if this is a map, don't continue after deserialising
                if (deserialiseMap(fields[i]))
                    continue;
            } catch (Exception ex)
            {
                // exception may be thrown if it's not a map. Nothing to worry about.
            }

            try
            {
                // if this is a list, don't continue after deserialising
                if (deserialiseList(fields[i]))
                    continue;
            } catch (Exception ex)
            {
                // exception may be thrown if it's not a list. Nothing to worry about.
            }
            
            try
            {
                // if this is a set, don't continue after deserialising
                if (deserialiseSet(fields[i]))
                    continue;
            } catch (Exception ex)
            {
                // exception may be thrown if it's not a list. Nothing to worry about.
            }
            

            try
            {
                // Load value by field name.
                value = get(name);
                if (value == null)
                {
                    try
                    {
                        value = (String) this.getClass().getDeclaredField(
                            "default_" + fields[i].getName()).get(this);
                    }
                    catch ( NoSuchFieldException exp )
                    {// no such field can be ignored..
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    } // don't worry if there's no default
                }
                if (value == null)
                {
                    continue; // no default value either, so don't do anything!
                }

                if (type.getName().equals("int"))
                {
                    fields[i].setInt(this, Integer.parseInt(value));
                }
                else if (type.getName().equals("short"))
                {
                    fields[i].setShort(this, Short.parseShort(value));
                }
                else if (type.getName().equals("long"))
                {
                    fields[i].setLong(this, Long.parseLong(value));
                }
                else if (type.getName().equals("float"))
                {
                    fields[i].setFloat(this, Float.parseFloat(value));
                }
                else if (type.getName().equals("boolean"))
                {
                    fields[i].setBoolean(this, value.equals("true"));
                }
                else if (type.getName().equals("java.lang.String"))
                {
                    fields[i].set(this, value);
                }
            }
            catch (Exception exp)
            {
                NLogger.error( OldCfg.class, 
                    "Error in field: " + name + ", value: "+ value, exp );
            }
        }
    }

    private boolean deserialiseMap(Field myField)
    {
        boolean isMap = false;
        try
        {
            if (myField.getType().getName().equals("java.util.HashMap"))
            {
                myField.set(this, new HashMap()); // create an empty list
                Map myMap = (Map) myField.get(this);

                String key;                
                String prefix = myField.getName() + LIST_PREFIX;
                for ( Enumeration e = mSetting.propertyNames(); e.hasMoreElements() ; )
                {
                    String foundName = (String) e.nextElement();
                    if ( foundName.startsWith(prefix) )
                    {
                        key = foundName.substring(prefix.length());
                        if ( key.length() > 0 && mSetting.getProperty(foundName).length() > 0 )
                        {
                            myMap.put(key, mSetting.getProperty(foundName));
                        }
                            
                    }
                }
                isMap = true; // successfully processed the list
            }
        }
        catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
            isMap = false;
        }
        return isMap;
    }

    private boolean deserialiseList(Field myField)
    {
        boolean isList = false;
        try
        {
            if (myField.getType().getName().equals("java.util.ArrayList"))
            {
                myField.set(this, new ArrayList()); // create an empty list
                List myList = (List) myField.get(this);

                String value;
                int index;
                for (index = 1;;index++)
                {
                    value = get(myField.getName() + LIST_PREFIX + index);
                    if ( value != null && value.length() > 0 )
                        myList.add(value);
                    else
                        break;
                }
                if ( index == 1 ) // no values read in, so check for default value
                {
                    try
                    {
                        Object defaultValue = this.getClass().getDeclaredField("default_" + myField.getName()).get(this);
                        String buffer = (String)defaultValue;
                        StringTokenizer tokens = new StringTokenizer(buffer, "|");
                        // create the empty list object
                        while (tokens.hasMoreTokens())
                        {
                            myList.add(tokens.nextToken());
                        }
                    }
                    catch (Exception ex)
                    { // no defaults found
                    }
                }
                isList = true; // successfully processed the list
            }
        }
        catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
            isList = false;
        }
        return isList;
    }
    
    private boolean deserialiseSet(Field myField)
    {
        boolean isSet = false;
        try
        {
            if (myField.getType().getName().equals("java.util.HashSet"))
            {
                myField.set(this, new HashSet()); // create an empty set
                Set mySet = (Set) myField.get(this);

                String prefix = new String ( myField.getName() + LIST_PREFIX + "SET");
                for ( Enumeration e = mSetting.propertyNames(); e.hasMoreElements() ; )
                {
                    String foundName = (String) e.nextElement();
                    if ( foundName.startsWith(prefix) )
                    {
                        if ( mSetting.getProperty(foundName).length() > 0 )
                        {
                            mySet.add(mSetting.getProperty(foundName));
                        }
                    }
                }
                isSet = true; // successfully processed the list
            }
        }
        catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
            isSet = false;
        }
        return isSet;
    }

    private void deserializeComplexFields()
    {
        try
        {
            try
            {
                mProgramClientID.fromHexString(get("mProgramClientID"));
            }
            catch (Exception e)
            {
                // ignore.  take the default created value as new client ID.
            }
        }
        catch (Exception exp )
        {
            NLogger.error( OldCfg.class, exp, exp );
        }
    }

    /**
     * For a HTTPURLConnection java uses configured proxy settings.
     */
    public void updateSystemSettings()
    {
        System.setProperty( "http.agent", Environment.getPhexVendor() );
        if ( isHttpProxyUsed )
        {
            System.setProperty( "http.proxyHost", httpProxyHost );
            System.setProperty( "http.proxyPort", String.valueOf( httpProxyPort ) );
        }
        else
        {
            System.setProperty( "http.proxyHost", "" );
            System.setProperty( "http.proxyPort", "" );
        }
        
        // cache DNS name lookups for only 30 minutes
        System.setProperty( "networkaddress.cache.ttl", "1800" );
        Security.setProperty( "networkaddress.cache.ttl", "1800" );
    }

    private void handlePhexVersionAdjustments()
    {
        // first find out if this is the first time Phex is running...
        if ( ( runningPhexVersion == null || runningPhexVersion.length() == 0 ) &&
             ( runningBuildNumber == null || runningBuildNumber.length() == 0 ) )
        {
            // this seems to be the first time phex is running...
            // in this case we are not updating... we use default values...
            
            // unfortunatly there is a bug causing no set version number to be available
            // between 56 and 78
            // since b78 updates can be performed on any version without problems we execute
            // them always if no version is set.
            updatesForBuild78();
        }
        else
        {
            // dropped update support of Phex 0.7 (2005-03-02)
            // dropped update support of Phex 0.8 (2005-11-19)
            // dropped update support of build 36 (2005-11-19)
            // dropped update support of build 42 (2005-11-19)
            // dropped update support of build 56 (2005-11-19)
    
            // update from Phex build <= 35
            if ( runningBuildNumber == null || runningBuildNumber.length() == 0 )
            {
                runningBuildNumber = "35";
            }
            if ( VersionUtils.compare( "78", runningBuildNumber ) > 0 )
            {
                updatesForBuild78();
            }
            if ( VersionUtils.compare( "81", runningBuildNumber ) > 0 )
            {
                updatesForBuild81();
            }
            if ( VersionUtils.compare( "87", runningBuildNumber ) > 0 )
            {
                updatesForBuild87();
            }
            if ( VersionUtils.compare( "88", runningBuildNumber ) > 0 )
            {
                updatesForBuild88();
            }
            if ( VersionUtils.compare( "92", runningBuildNumber ) > 0 )
            {
                updatesForBuild92();
            }
        }
        
        runningBuildNumber = Environment.getInstance().getProperty(
            "build.number" );
        runningPhexVersion = Res.getStr( "Program.Version" );
    }
    
    /**
     * Read in old ArrayLists with tokeniser
     */
    private void updatesForBuild78()
    {
        runningBuildNumber = "78";
        u78("mNetNetworkHistory", " ");
        u78("filteredCatcherPorts", " ");
        u78("connectToHistory", " ");
        u78("searchTermHistory", ",");
    }

    private void u78(String variable, String delim)
    {
        String buffer = get(variable);
        try
        {
            if (buffer != null)
            {
                List myList = (List) this.getClass().getDeclaredField(variable).get(this);
                StringTokenizer tokens = new StringTokenizer(buffer, delim);
                while (tokens.hasMoreTokens()) 
                {
                    myList.add(tokens.nextToken());
                }
            }
        }
        catch (NoSuchFieldException ex)
        {
            throw new Error("No such field: " + variable + "!");
        }
        catch (IllegalAccessException ex)
        {
            throw new Error("Cannot convert " + variable + " to a list!!");
        }
    }
    
    private void updatesForBuild81()
    {
        runningBuildNumber = "81";
        StringTokenizer tokens = new StringTokenizer(mUploadDir, ";");
        while ( tokens.hasMoreTokens() )
        {
            File dir = new File(tokens.nextToken().trim());
            if (!sharedDirectoriesSet.contains(dir.getAbsolutePath()))
            {
                sharedDirectoriesSet.add(dir.getAbsolutePath());
            }
        }
    }
    
    private void updatesForBuild87()
    {
        runningBuildNumber = "87";
        if ( leaf2upConnections > MAX_LEAF_2_UP_CONNECTIONS )
        {
            leaf2upConnections = MAX_LEAF_2_UP_CONNECTIONS;
        }
    }
    
    private void updatesForBuild88()
    {
        runningBuildNumber = "88";
        if ( totalStartupCounter < movingTotalUptimeCount )
        {
            totalStartupCounter = movingTotalUptimeCount;
        }
    }
    
    private void updatesForBuild92()
    {
        runningBuildNumber = "92";
        allowToBecomeLeaf = DEFAULT_ALLOW_TO_BECOME_LEAF;
        forceUPConnections = DEFAULT_FORCE_UP_CONNECTIONS;
        up2peerConnections = DEFAULT_UP_2_PEER_CONNECTIONS;
        leaf2peerConnections = DEFAULT_LEAF_2_PEER_CONNECTIONS;
    }
}
