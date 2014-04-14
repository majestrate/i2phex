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
 *  $Id: TestFileUtils.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.test;

import java.io.*;

import junit.framework.TestCase;
import phex.utils.FileUtils;

public class TestFileUtils
    extends TestCase
{
    private File tempFile1;
    private RandomAccessFile raFile1;
    private File tempFile2;
    private RandomAccessFile raFile2;

    public TestFileUtils( String name )
    {
        super( name );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
        /*tempFile1 = File.createTempFile( "TestFileUtils1", "tmp" );
        raFile1 = new RandomAccessFile( tempFile1, "rw");
        raFile1.setLength( 30 * 1024 * 1024  ); // 30MB

        tempFile2 = File.createTempFile( "TestFileUtils2", "tmp" );
        raFile2 = new RandomAccessFile( tempFile2, "rw");
        raFile2.setLength( 30 * 1024 * 1024  ); // 30MB*/
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        /*tempFile1.delete();
        tempFile2.delete();*/
    }
    
    /*public void testAppendOpenFile()
        throws IOException, InterruptedException
    {
        File destination = new File( "D:/FileService/stapelfahrer_klaus2.wmv");
        File fileToAppend = new File( "D:/FileService/stapelfahrer_klaus3.wmv");;
        System.out.println( "Now open " + destination.getAbsolutePath() );
        Thread.sleep( 10000 );
        System.out.println( "Trying merge..." );
        FileUtils.appendFile( destination, fileToAppend );
        System.out.println( destination.length() + "  -  " + fileToAppend.length() );
    }*/

    /*public void testAppendFile()
        throws IOException
    {
        File destination = tempFile1;
        File fileToAppend = tempFile2;
        long start = System.currentTimeMillis();
        FileUtils.appendFile( destination, fileToAppend );
        long end = System.currentTimeMillis();
        System.out.println( "testAppendFile took: " + (end-start) );
    }*/
    
    /*public void testFileSplit()
        throws IOException
    {
        File tempFile = File.createTempFile( "TestFileUtils3", null );
        BufferedOutputStream outStream = new BufferedOutputStream(
            new FileOutputStream( tempFile ) );
        outStream.write( "0123456789".getBytes() );
        outStream.close();
        
        File destTempFile = File.createTempFile( "TestFileUtils4", null );
        FileUtils.splitFile( tempFile, destTempFile, 3 );
        assertEquals( tempFile.length(), 3 );
        assertEquals( destTempFile.length(), 7 );
    }*/
    
    
    public void testConvert()
    {
        String result = FileUtils.convertToLocalSystemFilename( "test" );
        assertEquals( result, "test" );
    }
}
