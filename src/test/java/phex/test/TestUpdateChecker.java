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
 */
package phex.test;

import junit.framework.TestCase;
import phex.update.UpdateCheckRunner;

public class TestUpdateChecker extends TestCase
{
    private UpdateCheckRunner checker;
    private boolean isTestCompleted;

    public TestUpdateChecker(String s)
    {
        super(s);
    }

    protected void setUp()
    {
        checker = new UpdateCheckRunner( new phex.event.UpdateNotificationListener()
            {
                public void updateNotification( UpdateCheckRunner updateChecker )
                {
                    System.out.println( "updateNotify" );
                    isTestCompleted = true;
                }
            }, true);
    }

    protected void tearDown()
    {
    }
    
    public void testNewUpdateCheck()
        throws Exception
    {
        checker.run();
    }
    
/*    public void testRequest()   
        throws Exception
    {
        URL url = new URL( "http://www.phex.org/update/update.php" );
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setUseCaches( false );
        connection.setRequestProperty( "User-Agent", Environment.getPhexVendor() );
        connection.setRequestMethod( "POST" );
        connection.setDoOutput( true );
        connection.addRequestProperty( "Content-Type",
            "text/xml; charset=UTF-8" );
                    
        OutputStream outStream = connection.getOutputStream();
        byte[] data = buildXMLUpdateRequest();
        if ( data == null )
        {
            throw new IOException( "Missing XML update data" );
        }
        outStream.write( data );
        
        // dont need to buffer stream already done by Properties.load()
        InputStream inStream = connection.getInputStream();
        StringBuffer buffer = new StringBuffer();
        byte[] bArr = new byte[1024];
        while( true )
        {
            int count = inStream.read( bArr );
            if ( count == -1 )
            { break; }
            buffer.append( new String( bArr, 0, count, "UTF-8" ) );
        }
        System.out.println( "Output: " + buffer.toString() );
    }
    
    private byte[] buildXMLUpdateRequest()
    {
        try
        {
            XJBPhex xjbPhex = ObjectFactory.createPhexElement();
            XJBUpdateRequest xjbRequest = ObjectFactory.createXJBUpdateRequest();
            xjbPhex.setUpdateRequest( xjbRequest );
            
            xjbRequest.setCurrentVersion( VersionUtils.getProgramVersion() );
            xjbRequest.setSkinLafInstalled( GUIRegistry.getInstance().isSkinLAFInstalled() );
            xjbRequest.setJavaVersion( System.getProperty( "java.version" ) );
            xjbRequest.setOperatingSystem( System.getProperty( "os.name" ) );
            
            Cfg cfg = ServiceManager.sCfg;
            xjbRequest.setHostid( cfg.mProgramClientID.toHexString() );
            xjbRequest.setShowBetaInfo( cfg.showBetaUpdateNotification );
            xjbRequest.setLastInfoId( cfg.lastShownUpdateInfoId );
            
            String lastCheckVersion;
            if ( VersionUtils.compare( cfg.lastUpdateCheckVersion,
                 cfg.lastBetaUpdateCheckVersion ) > 0 )
            {
                lastCheckVersion = cfg.lastUpdateCheckVersion;
            }
            else
            {
                lastCheckVersion = cfg.lastBetaUpdateCheckVersion;
            }
            xjbRequest.setLastCheckVersion( lastCheckVersion );
            
            StatisticsManager statMgr = StatisticsManager.getInstance();
            
            StatisticProvider uptimeProvider = statMgr.getStatisticProvider(
                StatisticsManager.UPTIME_PROVIDER );
            xjbRequest.setAvgUptime(
                ((LongObj)uptimeProvider.getAverageValue()).value );
                
            StatisticProvider downloadProvider = statMgr.getStatisticProvider(
                StatisticProviderConstants.TOTAL_DOWNLOAD_COUNT_PROVIDER );
            xjbRequest.setDownloadCount(
                (int)((LongObj)downloadProvider.getValue()).value );
                
            StatisticProvider uploadProvider = statMgr.getStatisticProvider(
                StatisticProviderConstants.TOTAL_UPLOAD_COUNT_PROVIDER );
            xjbRequest.setUploadCount(
                (int)((LongObj)uploadProvider.getValue()).value );
                
            FileAdministration fileAdmin = ShareManager.getInstance().getFileAdministration();
            xjbRequest.setSharedFiles( fileAdmin.getFileCount() );
            xjbRequest.setSharedSize( fileAdmin.getTotalFileSizeInKb() );
            
            return XMLBuilder.serializeToBytes( xjbPhex );
        }
        catch ( JAXBException exp )
        {
            Logger.logError( exp );
            return null;
        }
    }
*/

//    public void testUpdateCheck()
//        throws Exception
//    {
//        isTestCompleted = false;
//        checker.checkForUpdateAndWait();
//        if ( !isTestCompleted )
//        {
//            System.out.println( "Testfailed" );
//            Throwable th = checker.getUpdateCheckError();
//            if ( th != null )
//            {
//                th.printStackTrace();
//            }
//        }
//        System.out.println( checker.getBetaVersion() );
//        System.out.println( checker.getReleaseVersion() );
//    }
}
