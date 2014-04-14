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
 *  $Id: TestDownloadScopeList.java 3536 2006-08-05 22:16:44Z gregork $
 */
package phex.test;

import java.util.Iterator;

import junit.framework.TestCase;
import phex.download.*;

/**
 * 
 */
public class TestDownloadScopeList extends TestCase
{
    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }
    
    
    public void testRetainAll()
    {
        DownloadScopeList reatinList = new DownloadScopeList();
        DownloadScope rscope1 = new DownloadScope( 10, 20 );
        reatinList.add( rscope1 );
        
        DownloadScopeList thisList = new DownloadScopeList();
        DownloadScope scope1 = new DownloadScope( 0, 5 );
        DownloadScope scope2 = new DownloadScope( 25, 30 );
        thisList.add( scope1 );
        thisList.add( scope2 );
        
        thisList.retainAll(reatinList);
        assertEquals( 0, thisList.size() );
        
        
        thisList = new DownloadScopeList();
        scope1 = new DownloadScope( 0, 5 );
        scope2 = new DownloadScope( 25, 30 );
        DownloadScope scope3 = new DownloadScope( 12, 22 );
        thisList.add( scope1 );
        thisList.add( scope2 );
        thisList.add( scope3 );
        
        thisList.retainAll(reatinList);
        assertEquals( 1, thisList.size() );
        
        Iterator<DownloadScope> iterator = thisList.iterator();
        
        DownloadScope scope = iterator.next();
        assertEquals( 12, scope.getStart() );
        assertEquals( 20, scope.getEnd() );
        
        
        
        reatinList = new DownloadScopeList();
        rscope1 = new DownloadScope( 0, 20 );
        reatinList.add( rscope1 );
        
        thisList = new DownloadScopeList();
        scope1 = new DownloadScope( 0, 5 );
        scope2 = new DownloadScope( 18, 30 );
        thisList.add( scope1 );
        thisList.add( scope2 );
        
        thisList.retainAll(reatinList);
        assertEquals( 2, thisList.size() );
        
        iterator = thisList.iterator();
        
        scope = iterator.next();
        assertEquals( 0, scope.getStart() );
        assertEquals( 5, scope.getEnd() );
        
        scope = iterator.next();
        assertEquals( 18, scope.getStart() );
        assertEquals( 20, scope.getEnd() );
        
        
        
        reatinList = new DownloadScopeList();
        rscope1 = new DownloadScope( 10, 11 );
        reatinList.add( rscope1 );
        
        thisList = new DownloadScopeList();
        scope1 = new DownloadScope( 0, 20 );
        thisList.add( scope1 );
        
        thisList.retainAll(reatinList);
        assertEquals( 1, thisList.size() );
        
        iterator = thisList.iterator();
        
        scope = iterator.next();
        assertEquals( 10, scope.getStart() );
        assertEquals( 11, scope.getEnd() );
        
        
        
        reatinList = new DownloadScopeList();
        rscope1 = new DownloadScope( 0, 20 );
        reatinList.add( rscope1 );
        
        thisList = new DownloadScopeList();
        scope1 = new DownloadScope( 10, 11 );
        thisList.add( scope1 );
        
        thisList.retainAll(reatinList);
        assertEquals( 1, thisList.size() );
        
        iterator = thisList.iterator();
        
        scope = iterator.next();
        assertEquals( 10, scope.getStart() );
        assertEquals( 11, scope.getEnd() );
    }
}
