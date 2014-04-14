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
 *  Created on Aug 12, 2004
 *  --- CVS Information ---
 *  $Id: TestFileCopy.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.test.performance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

//import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

/**
 * @author gregor
 */ 
public class TestFileCopy extends TestCase
{
    private File tempFile1;
    private RandomAccessFile raFile1;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
        tempFile1 = File.createTempFile( "TestFileUtils1", "tmp" );
        raFile1 = new RandomAccessFile( tempFile1, "rw");
        raFile1.setLength( 30 * 1024 * 1024  ); // 30MB
        raFile1.close();
    }
    
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        tempFile1.delete();
    }
    
    public void testCopy()
        throws Exception
    {
        File destinationFile = File.createTempFile( "TestFileUtils2", "tmp" );
        long start = System.currentTimeMillis();
        for ( int i = 0; i < 5; i++ )
        {
            copyFile( tempFile1, destinationFile );
        }
        destinationFile.delete();
        long end = System.currentTimeMillis();
        System.out.println( "standard: " + (end-start) );
        
    }
    
    /*
    public void testApacheCopy()
        throws Exception
    {
        File destinationFile = File.createTempFile( "TestFileUtils2", "tmp" );
        long start = System.currentTimeMillis();
        for ( int i = 0; i < 5; i++ )
        {
            FileUtils.copyFile( tempFile1, destinationFile );
        }
        destinationFile.delete();
        long end = System.currentTimeMillis();
        System.out.println( "apache: " + (end-start) );
        
    }
    */
    
    private static final int BUFFER_LENGTH = 256 * 1024;
    /**
     * Copys the source file to the destination file. Old contents of the
     * destination file will be overwritten.
     * @deprecated use org.apache.commons.io.FileUtils.copyFile( source, destination );
     */
    public static void copyFile( File source, File destination )
        throws IOException
    {   
        // open files
        BufferedInputStream inStream = new BufferedInputStream(
            new FileInputStream( source ) );
        BufferedOutputStream outStream = new BufferedOutputStream(
            new FileOutputStream( destination ) );

        byte[] buffer = new byte[ (int)Math.min( BUFFER_LENGTH, source.length() + 1) ];
        int length;
        while ( true )
        {
            // read the min value of the buffer length or the value left to read
            length = inStream.read( buffer, 0, buffer.length );
            // end of stream
            if ( length == -1 )
            {
                break;
            }

            outStream.write( buffer, 0, length );
        }
        outStream.close();
        inStream.close();
    }
}
