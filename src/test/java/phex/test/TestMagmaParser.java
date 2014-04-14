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
 *  Created on 08.02.2005
 *  --- CVS Information ---
 *  $Id: TestMagmaParser.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.test;

import java.io.*;
import java.util.List;

import junit.framework.TestCase;
import phex.utils.MagmaParser;

/**
 *
 */
public class TestMagmaParser extends TestCase
{
    public TestMagmaParser(String s)
    {
        super(s);
    }
    
    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }
    
    public void testValidMagma1()
        throws Throwable
    {
        InputStream stream  = ClassLoader.getSystemResourceAsStream(
            "phex/test/resources/valid_magma_1.magma");
        MagmaParser parser = new MagmaParser( stream );
        parser.start();
        List magnets = parser.getMagnets();
        assertEquals( (String)magnets.get(0), "magnet:?xt=urn:sha1:2KQWDD47VSCDX6IM54NSEPSC44IB3NCG&dn=%28Kurzgeschichte%29+Arne+Babenhauserheide+-++Galactic-Tales+Estiala%2C+Vorgeschichte.txt" );
        assertEquals( 119, magnets.size() );
        
        stream  = ClassLoader.getSystemResourceAsStream(
            "phex/test/resources/bracken_magma.magma");
        parser = new MagmaParser( stream );
        parser.start();
        magnets = parser.getMagnets();
        assertEquals( "magnet:?xt=urn:sha1:MCH2HGBTFRKMGH5LSVH5MKLCCBBQ7XAP&dn=Dildo%20Backenspalter.mp3&xs=http://dlaikar.de/arnebab/musik/dildo-backenspalter.mp3",
            (String)magnets.get(0)  );
        assertEquals( "magnet:?xt=urn:sha1:<another+hash>&dn=<another+filename>&xs=<another+exact+substitute>", 
            (String)magnets.get(1)  );
        assertEquals( "magnet:?xt=urn:sha1:PX2AN5FPSJGORDBQ2YIV3LRR2SRLP5LY&dn=A%20Curse%20upon%20those%20Parents.mp3&xs=http://10.0.1.3:6346/uri-res/N2R?urn:sha1:PX2AN5FPSJGORDBQ2YIV3LRR2SRLP5LY",
            (String)magnets.get(2) );
        assertEquals( "magnet?xt=urn:sha1:<some+hash>&dn=<some+filename>&xs=<another+source>", 
            (String)magnets.get(3) );
        assertEquals( "magnet:?xt=urn:sha1:<some+hash>&dn=<some+name>", 
            (String)magnets.get(4) );
        assertEquals( 5, magnets.size() );
        
        stream  = ClassLoader.getSystemResourceAsStream(
            "phex/test/resources/magmatest.magma");
        parser = new MagmaParser( stream );
        parser.start();
        magnets = parser.getMagnets();
        assertEquals( "magnet:?xt=urn:sha1:OW2VBA5SXFAIYGOUK3DZLB4GSQZWEPOF&dn=MagmaTestv0.2.zip&xs=http://edrikor.dyndns.org:9845/uri-res/N2R?urn:sha1:OW2VBA5SXFAIYGOUK3DZLB4GSQZWEPOF",
            (String)magnets.get(0)  );
        assertEquals( "magnet:?xt=urn:sha1:NTZTRLWMX4EY7AZS2LMLZKCHN2SX4LCG&dn=MagmaTestv0.31.zip&xs=http://edrikor.dyndns.org:9845/uri-res/N2R?urn:sha1:NTZTRLWMX4EY7AZS2LMLZKCHN2SX4LCG", 
            (String)magnets.get(1)  );
        assertEquals( "magnet:?xt=urn:sha1:LRZEJB5FGJECKR6NBY2LNTFWZAOL5FWN&dn=MagmaTestv0.36.zip&xs=http://edrikor.dyndns.org:9845/uri-res/N2R?urn:sha1:LRZEJB5FGJECKR6NBY2LNTFWZAOL5FWN", 
            (String)magnets.get(4) );
        assertEquals( "magnet:?xt=urn:sha1:KK2TVAVPGKAGI73NRVIBBYPWCOAC7OYY&dn=MagmaTest-OSX-v0.36.jar.zip&xs=http://edrikor.dyndns.org:9845/uri-res/N2R?urn:sha1:KK2TVAVPGKAGI73NRVIBBYPWCOAC7OYY", 
            (String)magnets.get(5) );
        assertEquals( 6, magnets.size() );
    }
}
