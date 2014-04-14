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
 *  --- SVN Information ---
 *  $Id: PhexTestSuite.java 4417 2009-03-22 22:33:44Z gregork $
 */
package phex.test;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;
import phex.common.AltLocContainerTest;
import phex.common.ThreadTracking;
import phex.prefs.core.PhexCorePrefs;
import phex.upload.HrrnQueuePerformanceTest;
import phex.utils.IOUtilTest;
import phex.utils.Localizer;
import phex.utils.SystemProperties;
import phex.xml.thex.TestThexHashTreeSaxHandler;
import phex.xml.thex.ThexHashTreeCodecTest;


public class PhexTestSuite extends TestSuite
{

    public PhexTestSuite(String s)
    {
        super(s);
    }

    protected void setUp()
        throws Exception
    {
        StringBuffer path = new StringBuffer(20);
        path.append( System.getProperty("user.home") );
        path.append( File.separator );

        //phex config files are hidden on all UNIX systems (also MacOSX. Since
        //there are many UNIX like operation systems with Java support out there,
        //we can not recognize the OS through it's name. Thus we check if the
        //root of the filesystem starts with "/" since only UNIX uses such
        //filesystem conventions
        if ( File.separatorChar == '/' )
        {
            path.append ('.');
        }
        path.append ("phex");
        path.append( File.separator );
        path.append( "testSuite" );
        Localizer.initialize( "en_US" );
        System.setProperty( SystemProperties.PHEX_CONFIG_PATH_SYSPROP, 
            path.toString() );
        
        PhexCorePrefs.init();
        ThreadTracking.initialize();
    }

    public static Test suite()
        throws Exception
    {
        PhexTestSuite suite = new PhexTestSuite("PhexTestSuite");
        suite.setUp();
        suite.addTestSuite(IOUtilTest.class);
        suite.addTestSuite(ThexHashTreeCodecTest.class);
        suite.addTestSuite(TestThexHashTreeSaxHandler.class);
        suite.addTestSuite(TestHTTPRangeSet.class);
        suite.addTestSuite(TestConnection.class);
        suite.addTestSuite(TestDownloadScopeList.class);
        suite.addTestSuite(TestRatedDownloadScopeList.class);
        suite.addTestSuite(TestLogBuffer.class);
        suite.addTestSuite(TestMagmaParser.class);
        suite.addTestSuite(phex.test.TestStringUtils.class);
        suite.addTestSuite(phex.test.TestIp2CountryManager.class);
        suite.addTestSuite(phex.test.TestFileUtils.class);
        suite.addTestSuite(phex.test.TestAlternateLocation.class);
        suite.addTestSuite(AltLocContainerTest.class);
        suite.addTestSuite(phex.test.TestSWDownloadCandidate.class);
        suite.addTestSuite(phex.test.TestCatchedHostCache.class);
        suite.addTestSuite(phex.test.TestGGEPBlock.class);
        suite.addTestSuite(phex.test.TestURN.class);
        suite.addTestSuite(phex.msg.PongMsgTest.class);
        suite.addTestSuite(phex.test.TestReadWriteLock.class);
        suite.addTestSuite(phex.test.TestHTTPProcessor.class);
        suite.addTestSuite(phex.test.TestHostAddress.class);
        suite.addTestSuite(phex.test.TestXQueueParameters.class);
        suite.addTestSuite(phex.msghandling.MessageRoutingTest.class);
        suite.addTestSuite(phex.test.TestGUID.class);
        suite.addTestSuite(phex.test.TestQueryRoutingTable.class);
        suite.addTestSuite(phex.msg.QueryResponseMsgTest.class);
        suite.addTestSuite(phex.msg.QueryMsgTest.class);
        suite.addTestSuite(phex.test.TestCircularQueue.class);
        suite.addTestSuite(phex.test.TestDownload.class);

        
        //suite.addTestSuite(phex.test.TestThrottleController.class);
        //suite.addTestSuite(phex.test.TestUpdateChecker.class);

        return suite;
    }
}
