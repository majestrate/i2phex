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
import phex.msg.GUID;

public class TestGUID extends TestCase
{

  public TestGUID(String s)
  {
    super(s);
  }

  protected void setUp()
  {
  }

  protected void tearDown()
  {
  }

  public void testToString()
  {
      for ( int i = 0; i < 100; i++ )
      {
        GUID guid = new GUID();

        GUID guidCheck = new GUID( guid.getGuid() );
        assertEquals( true, guid.equals( guidCheck ) );
        assertEquals( true, guid.hashCode() == guidCheck.hashCode() );
        assertEquals( true, guid.toHexString().equals( guidCheck.toHexString() ) );
      }
  }
}
