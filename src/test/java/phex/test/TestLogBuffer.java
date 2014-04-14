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
 *  $Id: TestLogBuffer.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.test;

import junit.framework.TestCase;
import phex.common.log.LogBuffer;
import phex.common.log.LogRecord;

/**
 * 
 */
public class TestLogBuffer extends TestCase
{
    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }
    
    public void testLogBuffer()
    {
        LogRecord record = new LogRecord(this, "test test");
        LogBuffer buffer = new LogBuffer( record.getSize() * 100 + 5 );
        
        for ( int i = 0; i < 200; i++ )
        {
            buffer.addLogRecord(record);
            
            if ( i < 100 )
            {
                assertEquals( i+1, buffer.getElementCount() );
                assertEquals( (i+1) * record.getSize(), buffer.getFillSize() );
            }
            else
            {
                assertEquals( 100, buffer.getElementCount() );
                assertEquals( 100L * record.getSize(), buffer.getFillSize() );
            }
        }
    }
}
