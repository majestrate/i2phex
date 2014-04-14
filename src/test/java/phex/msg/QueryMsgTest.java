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
 *  $Id$
 */
package phex.msg;

import junit.framework.TestCase;
import phex.msg.QueryMsg;
import phex.utils.AccessUtils;

public class QueryMsgTest extends TestCase
{
    @Override
    protected void setUp()
    {
    }

    @Override
    protected void tearDown()
    {
    }

    public void testIsBitSet()
        throws Throwable
    {
        Boolean state;
        state =
            (Boolean) AccessUtils.invokeMethod(
                QueryMsg.class,
                "isBitSet",
                new Object[] { Short.valueOf( (short) 0x0001), Integer.valueOf(0)});
        assertTrue(state.booleanValue());
        state =
            (Boolean) AccessUtils.invokeMethod(
                QueryMsg.class,
                "isBitSet",
                new Object[] { Short.valueOf( (short) 0x0001), Integer.valueOf(15)});
        assertFalse(state.booleanValue());

        state =
            (Boolean) AccessUtils.invokeMethod(
                QueryMsg.class,
                "isBitSet",
                new Object[] { Short.valueOf( (short) 0x8000), Integer.valueOf(15)});
        assertTrue(state.booleanValue());
    }
    
    public void testSetBit()
        throws Throwable
    {
        Short result;
        result =
            (Short) AccessUtils.invokeMethod(
                QueryMsg.class,
                "setBit",
                new Object[] { Short.valueOf( (short) 0x0000), Integer.valueOf(0)});
        assertEquals(result.shortValue(), 0x0001);
        // ....
    }

    public void testSettingAndCheckingBits()
        throws Throwable
    {
        Short myShort = Short.valueOf( (short) 0x0000);

        Boolean state;

        // -----------------------------------------------------------------------------------------        
        // Set bit number 0
        myShort =  (Short) AccessUtils.invokeMethod(QueryMsg.class, "setBit",
            new Object[] { myShort, Integer.valueOf(0)});

        // Check it (should be set)
        state = (Boolean) AccessUtils.invokeMethod(QueryMsg.class, "isBitSet",
                new Object[] { myShort, Integer.valueOf(0)});
        assertTrue(state.booleanValue());

        // Check other bits (should be clear)
        for(int i=1; i<16; i++ )
        {
            Integer myInt = Integer.valueOf(i);
            state = (Boolean) AccessUtils.invokeMethod(QueryMsg.class, "isBitSet",
                    new Object[] { myShort, myInt});
            assertFalse(state.booleanValue());
        }

        // -----------------------------------------------------------------------------------------        
        // Set bit number 1
        myShort =  (Short) AccessUtils.invokeMethod(QueryMsg.class, "setBit",
            new Object[] { myShort, Integer.valueOf(1)});

        // Check bits 0-1 (should be set)
        for(int i=0; i<2; i++)
        {
            Integer myInt = Integer.valueOf(i);
            state = (Boolean) AccessUtils.invokeMethod(QueryMsg.class, "isBitSet",
                    new Object[] { myShort, myInt});
            assertTrue(state.booleanValue());
        }

        // Check other bits (should be clear)
        for(int i=2; i<16; i++)
        {
            Integer myInt = Integer.valueOf(i);
            state = (Boolean) AccessUtils.invokeMethod(QueryMsg.class, "isBitSet",
                    new Object[] { myShort, myInt});
            assertFalse(state.booleanValue());
        }

        // -----------------------------------------------------------------------------------------        
        // Set bit number 15 (highest)
        myShort =  (Short) AccessUtils.invokeMethod(QueryMsg.class, "setBit",
            new Object[] { myShort, Integer.valueOf(15)});

        // Check bits 0-1 (should be set)
        for(int i=0; i<2; i++)
        {
            Integer myInt = Integer.valueOf(i);
            state = (Boolean) AccessUtils.invokeMethod(QueryMsg.class, "isBitSet",
                    new Object[] { myShort, myInt});
            assertTrue(state.booleanValue());
        }

        // Check bit 15 (should be set)
        state = (Boolean) AccessUtils.invokeMethod(QueryMsg.class, "isBitSet",
                new Object[] { myShort, Integer.valueOf(15)});
        assertTrue(state.booleanValue());

        // Check other bits (should be clear)
        for(int i=2; i<15; i++)
        {
            Integer myInt = Integer.valueOf(i);
            state = (Boolean) AccessUtils.invokeMethod(QueryMsg.class, "isBitSet",
                    new Object[] { myShort, myInt});
            assertFalse(state.booleanValue());
        }

    }

}
