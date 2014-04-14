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
 *  $Id: TestURN.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.test;

import phex.common.URN;
import junit.framework.TestCase;

/**
 * 
 */
public class TestURN extends TestCase
{
    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }
    
    public void testValidation()
    {
        // test urns
        // valid
        assertTrue( URN.isValidURN( "urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB" ) );
        assertTrue( URN.isValidURN( "urn:SHA1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB" ) );
        assertTrue( URN.isValidURN( 
            "urn:bitprint:J2LHXO34QXD4Q6ITI3TRT5WF2BFOHISE.AS3DST7FHA6Q5TY3ANBKCZY5YQK5POZHM3T6ZXA" ) );
        
        // invalid
        assertFalse( URN.isValidURN( "urn:blub:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB" ) );
        assertFalse( URN.isValidURN( "urt:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB" ) );
        assertFalse( URN.isValidURN( "urn:sha1:PLSTHIPQGSSZTS5FUPAKUZWUGYQYPFB" ) );
        assertFalse( URN.isValidURN( "urn:sha1PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB" ) );
        
        URN urn = new URN(
            "urn:bitprint:J2LHXO34QXD4Q6ITI3TRT5WF2BFOHISE.AS3DST7FHA6Q5TY3ANBKCZY5YQK5POZHM3T6ZXA");
        URN sha1Urn = new URN( "urn:sha1:J2LHXO34QXD4Q6ITI3TRT5WF2BFOHISE");
        assertTrue( urn.getSHA1Nss().equals( "J2LHXO34QXD4Q6ITI3TRT5WF2BFOHISE" ) );
        assertEquals( urn, sha1Urn );
    }
}
