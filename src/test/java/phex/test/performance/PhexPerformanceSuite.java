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
 *  $Id: PhexPerformanceSuite.java 4362 2009-01-16 10:27:18Z gregork $
 */
package phex.test.performance;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;
import phex.common.ThreadTracking;
import phex.prefs.core.PhexCorePrefs;
import phex.utils.Localizer;
import phex.utils.SystemProperties;


public class PhexPerformanceSuite extends TestSuite
{

    public PhexPerformanceSuite(String s)
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
        PhexPerformanceSuite suite = new PhexPerformanceSuite("PhexPerformanceSuite");
        suite.setUp();
        // suite.addTestSuite( TestLogging.class );
        //suite.addTestSuite( TestQueryResultSearchEngine.class );
        //suite.addTestSuite( TestSHA1.class );
        //suite.addTestSuite( TestFileCopy.class );
        //suite.addTestSuite( TestByteBuffer.class );
        return suite;
    }
}
