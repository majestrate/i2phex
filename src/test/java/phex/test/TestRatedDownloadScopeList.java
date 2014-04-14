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
 *  $Id: TestRatedDownloadScopeList.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.test;

import java.util.Iterator;

import junit.framework.TestCase;
import phex.download.*;

/**
 * 
 */
public class TestRatedDownloadScopeList extends TestCase
{
    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }
    
    
    public void testSimplePart()
    {
        RatedDownloadScopeList scopeList = new RatedDownloadScopeList();
        
        RatedDownloadScope scope1 = new RatedDownloadScope( 0,100,7,70);
        RatedDownloadScope scope2 = new RatedDownloadScope( 40,50,5,50);
        scopeList.add( scope1 );
        scopeList.add( scope2 );
        Iterator iterator = scopeList.getScopeIterator();
        
        RatedDownloadScope scope = (RatedDownloadScope)iterator.next();
        assertEquals( 0, scope.getStart() );
        assertEquals( 39, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 40, scope.getStart() );
        assertEquals( 50, scope.getEnd() );
        assertEquals( 12, scope.getCountRating() );
        assertEquals( 120, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 51, scope.getStart() );
        assertEquals( 100, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        
        // start boarders
        scopeList = new RatedDownloadScopeList();
        scope1 = new RatedDownloadScope( 0,100,7,70);
        scope2 = new RatedDownloadScope( 0,10,5,50);
        scopeList.add( scope1 );
        scopeList.add( scope2 );
        iterator = scopeList.getScopeIterator();
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 0, scope.getStart() );
        assertEquals( 10, scope.getEnd() );
        assertEquals( 12, scope.getCountRating() );
        assertEquals( 120, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 11, scope.getStart() );
        assertEquals( 100, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        // end boarders
        scopeList = new RatedDownloadScopeList();
        scope1 = new RatedDownloadScope( 0,100,7,70);
        scope2 = new RatedDownloadScope( 90,100,5,50);
        scopeList.add( scope1 );
        scopeList.add( scope2 );
        iterator = scopeList.getScopeIterator();
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 0, scope.getStart() );
        assertEquals( 89, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 90, scope.getStart() );
        assertEquals( 100, scope.getEnd() );
        assertEquals( 12, scope.getCountRating() );
        assertEquals( 120, scope.getSpeedRating() );
    }
    
    public void testSimpleContains()
    {
        RatedDownloadScopeList scopeList = new RatedDownloadScopeList();
        
        RatedDownloadScope scope1 = new RatedDownloadScope( 0,100,7,70);
        RatedDownloadScope scope2 = new RatedDownloadScope( 40,50,5,50);
        scopeList.add( scope2 );
        scopeList.add( scope1 );
        Iterator iterator = scopeList.getScopeIterator();
        
        RatedDownloadScope scope = (RatedDownloadScope)iterator.next();
        assertEquals( 0, scope.getStart() );
        assertEquals( 39, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 40, scope.getStart() );
        assertEquals( 50, scope.getEnd() );
        assertEquals( 12, scope.getCountRating() );
        assertEquals( 120, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 51, scope.getStart() );
        assertEquals( 100, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        // start boarders
        scopeList = new RatedDownloadScopeList();
        scope1 = new RatedDownloadScope( 0,100,7,70);
        scope2 = new RatedDownloadScope( 0,10,5,50);
        scopeList.add( scope2 );
        scopeList.add( scope1 );
        
        iterator = scopeList.getScopeIterator();
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 0, scope.getStart() );
        assertEquals( 10, scope.getEnd() );
        assertEquals( 12, scope.getCountRating() );
        assertEquals( 120, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 11, scope.getStart() );
        assertEquals( 100, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        // end boarders
        scopeList = new RatedDownloadScopeList();
        scope1 = new RatedDownloadScope( 0,100,7,70);
        scope2 = new RatedDownloadScope( 90,100,5,50);
        scopeList.add( scope2 );
        scopeList.add( scope1 );
        
        iterator = scopeList.getScopeIterator();
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 0, scope.getStart() );
        assertEquals( 89, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 90, scope.getStart() );
        assertEquals( 100, scope.getEnd() );
        assertEquals( 12, scope.getCountRating() );
        assertEquals( 120, scope.getSpeedRating() );
    }
    
    
    public void testComplexContains()
    {
        RatedDownloadScopeList scopeList = new RatedDownloadScopeList();
        
        RatedDownloadScope scope1 = new RatedDownloadScope( 0,19,7,70);
        RatedDownloadScope scope2 = new RatedDownloadScope( 20,39,7,70);
        RatedDownloadScope scope3 = new RatedDownloadScope( 40,60,7,70);
        RatedDownloadScope scope4 = new RatedDownloadScope( 10,50,5,50);
        scopeList.add( scope1 );
        scopeList.add( scope2 );
        scopeList.add( scope3 );
        scopeList.add( scope4 );
        scopeList.compressByRatings();
        Iterator iterator = scopeList.getScopeIterator();
        
        RatedDownloadScope scope = (RatedDownloadScope)iterator.next();
        assertEquals( 0, scope.getStart() );
        assertEquals( 9, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 10, scope.getStart() );
        assertEquals( 50, scope.getEnd() );
        assertEquals( 12, scope.getCountRating() );
        assertEquals( 120, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 51, scope.getStart() );
        assertEquals( 60, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        // start boarders
        scopeList = new RatedDownloadScopeList();
        scope1 = new RatedDownloadScope( 0,19,7,70);
        scope2 = new RatedDownloadScope( 20,39,7,70);
        scope3 = new RatedDownloadScope( 40,60,7,70);
        scope4 = new RatedDownloadScope( 0,50,5,50);
        scopeList.add( scope1 );
        scopeList.add( scope2 );
        scopeList.add( scope3 );
        scopeList.add( scope4 );
        scopeList.compressByRatings();
        
        iterator = scopeList.getScopeIterator();
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 0, scope.getStart() );
        assertEquals( 50, scope.getEnd() );
        assertEquals( 12, scope.getCountRating() );
        assertEquals( 120, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 51, scope.getStart() );
        assertEquals( 60, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        // end boarders
        scopeList = new RatedDownloadScopeList();
        scope1 = new RatedDownloadScope( 0,19,7,70);
        scope2 = new RatedDownloadScope( 20,39,7,70);
        scope3 = new RatedDownloadScope( 40,60,7,70);
        scope4 = new RatedDownloadScope( 10,60,5,50);
        scopeList.add( scope1 );
        scopeList.add( scope2 );
        scopeList.add( scope3 );
        scopeList.add( scope4 );
        scopeList.compressByRatings();
        
        iterator = scopeList.getScopeIterator();
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 0, scope.getStart() );
        assertEquals( 9, scope.getEnd() );
        assertEquals( 7, scope.getCountRating() );
        assertEquals( 70, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 10, scope.getStart() );
        assertEquals( 60, scope.getEnd() );
        assertEquals( 12, scope.getCountRating() );
        assertEquals( 120, scope.getSpeedRating() );
    }
    
    public void testRating()
    {
        // simple
        RatedDownloadScopeList scopeList = new RatedDownloadScopeList();
        
        RatedDownloadScope rscope1 = new RatedDownloadScope( 40,60,2,20);
        scopeList.add( rscope1 );
        
        DownloadScope scope1 = new DownloadScope( 0,100 );
        DownloadScopeList toRateScopeList = new DownloadScopeList();
        toRateScopeList.add(scope1);
        scopeList.rateDownloadScopeList(toRateScopeList, 10);
        
        Iterator iterator = scopeList.getScopeIterator();
        RatedDownloadScope scope = (RatedDownloadScope)iterator.next();
        assertEquals( 40, scope.getStart() );
        assertEquals( 60, scope.getEnd() );
        assertEquals( 3, scope.getCountRating() );
        assertEquals( 30, scope.getSpeedRating() );
        
        assertFalse( iterator.hasNext() );
        
        
        // left overlap
        scopeList = new RatedDownloadScopeList();
        rscope1 = new RatedDownloadScope( 40,60,2,20);
        scopeList.add( rscope1 );
        
        scope1 = new DownloadScope( 30,50 );
        toRateScopeList = new DownloadScopeList();
        toRateScopeList.add(scope1);
        scopeList.rateDownloadScopeList(toRateScopeList, 10);
        
        iterator = scopeList.getScopeIterator();
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 40, scope.getStart() );
        assertEquals( 50, scope.getEnd() );
        assertEquals( 3, scope.getCountRating() );
        assertEquals( 30, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 51, scope.getStart() );
        assertEquals( 60, scope.getEnd() );
        assertEquals( 2, scope.getCountRating() );
        assertEquals( 20, scope.getSpeedRating() );
        
        assertFalse( iterator.hasNext() );
        
        // right overlap
        scopeList = new RatedDownloadScopeList();
        rscope1 = new RatedDownloadScope( 40,60,2,20);
        scopeList.add( rscope1 );
        
        scope1 = new DownloadScope( 50,70 );
        toRateScopeList = new DownloadScopeList();
        toRateScopeList.add(scope1);
        scopeList.rateDownloadScopeList(toRateScopeList, 10);
        
        iterator = scopeList.getScopeIterator();
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 40, scope.getStart() );
        assertEquals( 49, scope.getEnd() );
        assertEquals( 2, scope.getCountRating() );
        assertEquals( 20, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 50, scope.getStart() );
        assertEquals( 60, scope.getEnd() );
        assertEquals( 3, scope.getCountRating() );
        assertEquals( 30, scope.getSpeedRating() );
        
        assertFalse( iterator.hasNext() );
        
        // middle overlap
        scopeList = new RatedDownloadScopeList();
        rscope1 = new RatedDownloadScope( 40,60,2,20);
        scopeList.add( rscope1 );
        
        scope1 = new DownloadScope( 45,55 );
        toRateScopeList = new DownloadScopeList();
        toRateScopeList.add(scope1);
        scopeList.rateDownloadScopeList(toRateScopeList, 10);
        
        iterator = scopeList.getScopeIterator();
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 40, scope.getStart() );
        assertEquals( 44, scope.getEnd() );
        assertEquals( 2, scope.getCountRating() );
        assertEquals( 20, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 45, scope.getStart() );
        assertEquals( 55, scope.getEnd() );
        assertEquals( 3, scope.getCountRating() );
        assertEquals( 30, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 56, scope.getStart() );
        assertEquals( 60, scope.getEnd() );
        assertEquals( 2, scope.getCountRating() );
        assertEquals( 20, scope.getSpeedRating() );
        
        assertFalse( iterator.hasNext() );
        
        // complex overlap
        scopeList = new RatedDownloadScopeList();
        rscope1 = new RatedDownloadScope( 10,60,2,20);
        scopeList.add( rscope1 );
        
        scope1 = new DownloadScope( 0,5 );
        DownloadScope scope2 = new DownloadScope( 10,15 );
        DownloadScope scope3 = new DownloadScope( 20,40 );
        DownloadScope scope4 = new DownloadScope( 50,60 );
        DownloadScope scope5 = new DownloadScope( 60,70 );
        toRateScopeList = new DownloadScopeList();
        toRateScopeList.add(scope1);
        toRateScopeList.add(scope2);
        toRateScopeList.add(scope3);
        toRateScopeList.add(scope4);
        toRateScopeList.add(scope5);
        scopeList.rateDownloadScopeList(toRateScopeList, 10);
        
        iterator = scopeList.getScopeIterator();
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 10, scope.getStart() );
        assertEquals( 15, scope.getEnd() );
        assertEquals( 3, scope.getCountRating() );
        assertEquals( 30, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 16, scope.getStart() );
        assertEquals( 19, scope.getEnd() );
        assertEquals( 2, scope.getCountRating() );
        assertEquals( 20, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 20, scope.getStart() );
        assertEquals( 40, scope.getEnd() );
        assertEquals( 3, scope.getCountRating() );
        assertEquals( 30, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 41, scope.getStart() );
        assertEquals( 49, scope.getEnd() );
        assertEquals( 2, scope.getCountRating() );
        assertEquals( 20, scope.getSpeedRating() );
        
        scope = (RatedDownloadScope)iterator.next();
        assertEquals( 50, scope.getStart() );
        assertEquals( 60, scope.getEnd() );
        assertEquals( 3, scope.getCountRating() );
        assertEquals( 30, scope.getSpeedRating() );
        
        assertFalse( iterator.hasNext() );
    }
}
