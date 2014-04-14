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
package phex.utils;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.zip.InflaterInputStream;

import junit.framework.TestCase;
import phex.utils.IOUtil;

public class IOUtilTest extends TestCase
{
    public IOUtilTest(String s)
    {
        super(s);
    }

    @Override
    protected void tearDown()
    {
    }
    
    @Override
    protected void setUp()
    {
    }

    public void testDeflate()
        throws IOException
    {
        byte[] randomData = setUpRandomData();
        byte[] compressed = IOUtil.deflate( randomData );
        assertTrue( compressed.length < randomData.length );

        InflaterInputStream stream = new InflaterInputStream(
            new ByteArrayInputStream( compressed ) );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[ 1024 ];
        int read = 0;
        do
        {
            read = stream.read( buf );
            if ( read > 0 )
            {
                out.write( buf, 0, read );
            }
        }
        while( read > 0 );
        assertTrue( Arrays.equals( out.toByteArray(), randomData ) );
    }

    public void testSimpleInflate() throws DataFormatException
    {
        byte[] randomData = setUpRandomData();
        byte[] compressed = IOUtil.deflate( randomData );
        byte[] decompressed = IOUtil.inflate( compressed );
        assertTrue( Arrays.equals( decompressed, randomData ) );
    }
    
    private byte[] setUpRandomData()
    {
        byte[] randomData = new byte[ 100000 ];
        Random rand=new Random();
        rand.nextBytes( randomData );
        // fill some compressable data
        for (int i = 300; i < 500; i++)
        {
            randomData[i]=(byte)0;
        }

        for (int i = 3000; i < 5000; i++)
        {
            randomData[i]=(byte)0;
        }
        return randomData;
    }
    
    public void testCobs() throws IOException
    {
        byte[] data = new byte[]{ 1,2,3,4,5,6,7,8,9,0,0,1,0,0,3,0,4,3,4,5,3,6,0,0,0,0,0,0,0,3,4,3,5,4,5,6,0,0,2,3,4,2,3 };
        byte[] encoded = IOUtil.cobsEncode( data );
        byte[] decoded = IOUtil.cobsDecode( encoded );
        assertTrue( Arrays.equals( data, decoded ) ); 
        
        data = new byte[]{0x45,0x00,0x00,0x2C,0x4C,0x79,0x00,0x00,0x40,0x06,0x4F,0x37};
        encoded = IOUtil.cobsEncode( data );
        byte[] expected = new byte[]{0x02,0x45,0x01,0x04,0x2C,0x4C,0x79,0x01,0x05,0x40,0x06,0x4F,0x37};
        assertTrue( Arrays.equals( encoded, expected ) );
        decoded = IOUtil.cobsDecode( expected );
        assertTrue( Arrays.equals( data, decoded ) );
    }
    
    public void testLongDeSer()
    {
        byte[] arr;
        
        arr = IOUtil.serializeLong2MinLE( 0 );
        assertEquals( 0, IOUtil.deserializeLongLE( arr, 0, arr.length ) );
        
        arr = IOUtil.serializeLong2MinLE( 100000 );
        assertEquals( 100000, IOUtil.deserializeLongLE( arr, 0, arr.length ) );
        
        long val = System.currentTimeMillis();
        arr = IOUtil.serializeLong2MinLE( val );
        assertEquals( val, IOUtil.deserializeLongLE( arr, 0, arr.length ) );
        
        arr = IOUtil.serializeLong2MinLE( Long.MAX_VALUE );
        assertEquals( Long.MAX_VALUE, IOUtil.deserializeLongLE( arr, 0, arr.length ) );
        
        Random rand=new Random();
        for ( int i = 0; i < 5000000; i++ )
        {
            val = Math.abs( rand.nextLong() );
            arr = IOUtil.serializeLong2MinLE( val );
            assertEquals( val, IOUtil.deserializeLongLE( arr, 0, arr.length ) );
        }
    }
}
