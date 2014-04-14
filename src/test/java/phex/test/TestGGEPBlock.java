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
 *  $Id: TestGGEPBlock.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.test;

import java.io.*;

import junit.framework.TestCase;
import phex.msg.GGEPBlock;

/**
 * 
 */
public class TestGGEPBlock extends TestCase
{
    private static final byte[] simpleGGEPBlock = 
    {
        // Extension Prefix
        (byte)0xC3, 
        // Extension Header
        0x02,
        'B', 'H',
        0x40, //length = 0
        // Extension Data not given for BH
        // Next Extension
        // Extension Header
        (byte)0x83, // last extension
        'A', 'L', 'T',
        0x49,
        // Extension Data (dummy data)
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    
    public void testGGEPBlock() throws Exception
    {
        GGEPBlock[] block = GGEPBlock.parseGGEPBlocks( simpleGGEPBlock, 0 );
        assertEquals( 1, block.length );
        assertTrue( block[0].isExtensionAvailable( "BH" ) );
        assertEquals( block[0].getExtensionData( "ALT" ).length, 9 );
        
        block = GGEPBlock.parseGGEPBlocks( new PushbackInputStream( 
            new ByteArrayInputStream( simpleGGEPBlock ) ) );
        assertEquals( 1, block.length );
        assertTrue( block[0].isExtensionAvailable( "BH" ) );
        assertEquals( block[0].getExtensionData( "ALT" ).length, 9 );
        
        byte[] twoGGEPBlocks = new byte[ simpleGGEPBlock.length * 2];
        System.arraycopy( simpleGGEPBlock, 0, twoGGEPBlocks, 0, simpleGGEPBlock.length );
        System.arraycopy( simpleGGEPBlock, 0, twoGGEPBlocks, simpleGGEPBlock.length, simpleGGEPBlock.length );
        
        block = GGEPBlock.parseGGEPBlocks( twoGGEPBlocks, 0 );
        assertEquals( 2, block.length );
        assertTrue( block[0].isExtensionAvailable( "BH" ) );
        assertEquals( block[0].getExtensionData( "ALT" ).length, 9 );
        assertTrue( block[1].isExtensionAvailable( "BH" ) );
        assertEquals( block[1].getExtensionData( "ALT" ).length, 9 );
        
        block = GGEPBlock.parseGGEPBlocks( new PushbackInputStream( 
            new ByteArrayInputStream( twoGGEPBlocks ) ) );
        assertEquals( 2, block.length );
        assertTrue( block[0].isExtensionAvailable( "BH" ) );
        assertEquals( block[0].getExtensionData( "ALT" ).length, 9 );
        assertTrue( block[1].isExtensionAvailable( "BH" ) );
        assertEquals( block[1].getExtensionData( "ALT" ).length, 9 );

    }
}
