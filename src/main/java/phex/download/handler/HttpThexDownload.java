package phex.download.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.httpclient.ChunkedInputStream;

import phex.common.log.NLogger;
import phex.download.DownloadEngine;
import phex.download.HostBusyException;
import phex.download.RemotelyQueuedException;
import phex.download.ThexVerificationData;
import phex.download.swarming.SWDownloadCandidate;
import phex.download.swarming.SWDownloadFile;
import phex.download.swarming.SWDownloadSet;
import phex.download.swarming.SWDownloadCandidate.ThexStatus;
import phex.http.GnutellaHeaderNames;
import phex.http.HTTPHeader;
import phex.http.HTTPHeaderNames;
import phex.http.HTTPMessageException;
import phex.http.HTTPProcessor;
import phex.http.HTTPRequest;
import phex.http.HTTPResponse;
import phex.http.HTTPRetryAfter;
import phex.http.XQueueParameters;
import phex.net.connection.Connection;
import phex.prefs.core.DownloadPrefs;
import phex.thex.TTHashCalcUtils;
import phex.utils.IOUtil;
import phex.utils.LengthLimitedInputStream;
import phex.xml.thex.ThexHashTree;
import phex.xml.thex.ThexHashTreeCodec;

import com.bitzi.util.Base32;
import com.onionnetworks.dime.DimeParser;
import com.onionnetworks.dime.DimeRecord;

public class HttpThexDownload extends AbstractHttpDownload
{   
    private ThexVerificationData thexData;
    
    private InputStream inStream;
    
    private long replyContentLength;
    
    private boolean isDownloadSuccessful;
    
    public HttpThexDownload( DownloadEngine engine, ThexVerificationData thexData )
    {
        super( engine );
        if ( thexData == null )
        {
            throw new NullPointerException( "ThexData is null." );
        }
        this.thexData = thexData;
    }
    
    public void preProcess()
    {
    }
    
    public void processHandshake() 
        throws IOException, HTTPMessageException
    {
        isDownloadSuccessful = false;
        
        Connection connection = downloadEngine.getConnection();
        SWDownloadCandidate candidate = downloadEngine.getDownloadSet().getCandidate();
        

        OutputStreamWriter writer = new OutputStreamWriter(
            connection.getOutputStream() );
        // reset to default input stream
        inStream = connection.getInputStream();
        
        String requestUrl = candidate.getThexUri();
        HTTPRequest request = new HTTPRequest( "GET", requestUrl, true );
        request.addHeader( new HTTPHeader( HTTPHeaderNames.HOST,
            candidate.getHostAddress().getFullHostName() ) );
        request.addHeader( new HTTPHeader( GnutellaHeaderNames.X_QUEUE,
            "0.1" ) );
        // request a HTTP keep alive connection, needed for queuing to work.
        request.addHeader( new HTTPHeader( HTTPHeaderNames.CONNECTION,
            "Keep-Alive" ) );

        String httpRequestStr = request.buildHTTPRequestString();

        NLogger.debug( HttpThexDownload.class,
            "HTTP Request to: " + candidate.getHostAddress() + "\n" + httpRequestStr );
        candidate.addToCandidateLog( "HTTP Request:\n" + httpRequestStr );
        // write request...
        writer.write( httpRequestStr );
        writer.flush();

        HTTPResponse response = HTTPProcessor.parseHTTPResponse( connection );
        if ( NLogger.isDebugEnabled( HttpThexDownload.class ) )
        {
            NLogger.debug( HttpThexDownload.class,
                "HTTP Response from: " + candidate.getHostAddress() + "\n" 
                + response.buildHTTPResponseString() );
        }
        if ( DownloadPrefs.CandidateLogBufferSize.get().intValue() > 0 )
        {
            candidate.addToCandidateLog( "HTTP Response:\n" 
                + response.buildHTTPResponseString() );
        }

        HTTPHeader header = response.getHeader( HTTPHeaderNames.SERVER );
        if ( header != null )
        {
            candidate.setVendor( header.getValue() );
        }
        
        header = response.getHeader( HTTPHeaderNames.TRANSFER_ENCODING );
        if ( header != null )
        {
            if ( header.getValue().equals("chunked") )
            {
                inStream = new ChunkedInputStream( connection.getInputStream() );
            }
        }
        
        replyContentLength = -1;
        header = response.getHeader( HTTPHeaderNames.CONTENT_LENGTH );
        if ( header != null )
        {
            try
            {
                replyContentLength = header.longValue();
            }
            catch ( NumberFormatException exp )
            { //unknown 
            }
        }
        updateKeepAliveSupport( response );
                
        int httpCode = response.getStatusCode();
        if ( httpCode >= 200 && httpCode < 300 )
        {// code accepted
            // connection successfully finished
            NLogger.debug( HttpThexDownload.class, "HTTP Handshake successfull.");
            return;
        }
        // check error type
        else if ( httpCode == 503 )
        {// 503 -> host is busy (this can also be returned when remotly queued)
            header = response.getHeader( GnutellaHeaderNames.X_QUEUE );
            XQueueParameters xQueueParameters = null;
            if ( header != null )
            {
                xQueueParameters = XQueueParameters.parseXQueueParameters( header.getValue() );
            }
            // check for persistent connection (gtk-gnutella uses queuing with 'Connection: close')
            if ( xQueueParameters != null && isKeepAliveSupported )
            {
                throw new RemotelyQueuedException( xQueueParameters );
            }
            else
            {
                header = response.getHeader( HTTPHeaderNames.RETRY_AFTER );
                if ( header != null )
                {
                    int delta = HTTPRetryAfter.parseDeltaInSeconds( header );
                    if ( delta > 0 )
                    {
                        throw new HostBusyException( delta );
                    }
                }
                throw new HostBusyException();
            }
        }
        else
        {
            throw new IOException( "Unknown HTTP code: " + httpCode );
        }
    }

    public void processDownload() throws IOException
    {
        SWDownloadSet downloadSet = downloadEngine.getDownloadSet();
        SWDownloadCandidate candidate = downloadSet.getCandidate();
        SWDownloadFile downloadFile = downloadSet.getDownloadFile();
        
        NLogger.debug( HttpThexDownload.class, "Download Engine starts download.");
        LengthLimitedInputStream downloadStream = null;
        try
        {            
            
            // determine the length to download, we start with the MAX
            // which would cause a download until the stream ends.
            long downloadLengthLeft = Long.MAX_VALUE;
            // maybe we know a reply content length
            if ( replyContentLength != -1 )
            {
                downloadLengthLeft = Math.min( replyContentLength, downloadLengthLeft );
            }
            
            downloadStream = new LengthLimitedInputStream( 
                inStream, downloadLengthLeft );
            
            DimeParser dimeParser = new DimeParser( downloadStream );
            
            List<DimeRecord> records = DimeParser.getAllRecords(dimeParser);
            
            if ( records.size() < 2 )
            {
            	throw new IOException( "Required dime records not found." );
            }
            DimeRecord xmlRecord = records.get( 0 );
            DimeRecord hashRecord = records.get( 1 );
                    
            byte[] xmlData = xmlRecord.getData();
            
            // it seems like bearshare is appending artefacts to the xml dime record entry.
            // try to remove them
            if ( candidate.getVendor().toLowerCase().contains( "bearshare" ) )
            {
                int idx = xmlData.length-1;
                while( xmlData[idx] != 0x3e )
                {
                    idx--;
                }
                if ( idx < xmlData.length-1 )
                {
                    byte[] newXmlData = new byte[ idx+1 ];
                    System.arraycopy( xmlData, 0, newXmlData, 0, idx+1 );
                    xmlData = newXmlData;
                }
            }
            
            ThexHashTree xmlTree;
            try
            {
                xmlTree = ThexHashTreeCodec.parseThexHashTreeXML( 
            		new ByteArrayInputStream(xmlData ) );
            }
            catch ( MalformedURLException exp )
            {// catch this exp for debugging purpose.
                NLogger.error( HttpThexDownload.class, "Failed to parse: '" + 
                   new String( xmlData, "UTF-8" ) + "' from: " + candidate.getVendor() );
                candidate.addToCandidateLog( "Failed to parse: '" + new String( xmlData, "UTF-8" ) + "'" );
                NLogger.error( HttpThexDownload.class, exp );
                throw new IOException( "Parsing Thex HashTree failed." );
            }
            
            long fileSize;
            try
            {
            	fileSize = Long.parseLong( xmlTree.getFileSize() );
            }
            catch ( NumberFormatException exp )
            {
            	throw new IOException( "Invalid file size: " + xmlTree.getFileSize() );
            }
            if ( fileSize != downloadFile.getTotalDataSize() )
            {
            	throw new IOException( "Invalid file size: " + fileSize + "/" 
            			+ downloadFile.getTotalDataSize() ); 
            }
            
            byte[] hashData = hashRecord.getData();
            
            // first 24 bytes is the root we can verify...
            if ( hashData.length < 24 )
            {
                throw new IOException( "Invalid hash data size." );
            }
            byte[] rootHash = new byte[24];
            System.arraycopy( hashData, 0, rootHash, 0, 24 );
            String rootHashB32 = Base32.encode( rootHash );
            if ( !candidate.getThexRoot().equals( rootHashB32 ) || 
                 !downloadFile.getThexVerificationData().getRootHash().equals( rootHashB32 ) )
            {
                throw new IOException( "Root hash do not match." );
            }
            
            List <List<byte[]>> merkleNodes = TTHashCalcUtils.resolveMerkleNodes( 
                hashData, fileSize );
            
            List<byte[]> lowestLevelNodes = merkleNodes.get( merkleNodes.size()-1 );
            thexData.setThexData( lowestLevelNodes, merkleNodes.size()-1, fileSize );
            
            isDownloadSuccessful = true;
        }
        finally
        {// dont close managed file since it might be used by parallel threads.
            boolean isAcceptingNextSegment = isAcceptingNextRequest();
            candidate.addToCandidateLog( "Is accepting next segment: " + isAcceptingNextSegment );
            // this is for keep alive support...
            if ( isAcceptingNextSegment && downloadStream != null )
            {
                // only need to close and consume left overs if we plan to
                // continue using this connection.
                downloadStream.close();
            }
            else
            {
                stopDownload();
            }
        }
    }
    
    /**
     * Performs thex download cleanup operations. In this case releasing the
     * running thex request state. 
     */
    public void postProcess()
    {
        // check if data is already released... 
        if ( thexData != null )
        {
            SWDownloadSet downloadSet = downloadEngine.getDownloadSet();
            SWDownloadCandidate candidate = downloadSet.getCandidate();
            synchronized( thexData )
            {
                if ( isDownloadSuccessful )
                {
                    candidate.setThexStatus( ThexStatus.SUCCEDED );
                }
                else if ( !candidate.isBusyOrQueued() )
                {
                    candidate.setThexStatus( ThexStatus.FAILED );
                }
                thexData.setThexRequested( false );
                thexData = null;
            }
        }
    }

    public void stopDownload()
    {
        IOUtil.closeQuietly( inStream );        
    }
    
    /**
     * Indicates whether the connection is keept alive and the next request can 
     * be send.
     * @return true if the next request can be send on this connection
     */
    public boolean isAcceptingNextRequest()
    {
        return isDownloadSuccessful && isKeepAliveSupported && replyContentLength != -1;
    }
}