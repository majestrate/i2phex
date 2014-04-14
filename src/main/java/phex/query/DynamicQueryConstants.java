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
 *  $Id: DynamicQueryConstants.java 4159 2008-03-29 22:09:54Z complication $
 *
 * Created on 2003-06-06
 */
package phex.query;

/**
 * 
 */
public interface DynamicQueryConstants
{
    /**
     * The minimum length a search term must have.
     */
    public static int MIN_SEARCH_TERM_LENGTH = 2;
    
    /**
     * The default value defining the number of millis a query is running
     * before it times out. ( 5 minutes ) 
     */
    public static final int DEFAULT_QUERY_TIMEOUT = 5 * 60 * 1000;
    
    /**
     * The max. estimated query horizon that is tried to be reached.
     */
    public static final int MAX_ESTIMATED_QUERY_HORIZON = 250000;
    
    /**
     * The time to wait in millis on queried per hop.
     */
    // I2P:
    // Let's be pessimistic, I2P can be quite laggy.
    public static final int DEFAULT_TIME_TO_WAIT_PER_HOP = 5000;
    
    /**
     * The number of results to get if we are starting a dynamic query
     * as a ultrapeer.
     */
    public static final int DESIRED_ULTRAPEER_RESULTS = 200;
    
    /**
     * The number of results to get if we are starting the dynamic query
     * as or for a leaf.
     */
    public static final int DESIRED_LEAF_RESULTS = 50;
    
    /**
     * The number of results to get if we are starting a dynamic query
     * with hash.
     */
    public static final int DESIRED_HASH_RESULTS = 20;
    
    /**
     * The number of millis after which the time to wait per hop is adjusted.
     */
    public static final int TIMETOWAIT_ADJUSTMENT_DELAY = 6000;
    
    /**
     * The number of millis to adjust the time to wait per hop. This 
     * will be multiplied by a factor calculated from the received results
     * ratio.
     */
    public static final int TIMETOWAIT_ADJUSTMENT = 200;
    
    /**
     * The default max ttl of hosts not providing a max ttl value.
     */
    public static final byte DEFAULT_MAX_TTL = 4;
    
    /**
     * The default degree value of not dynamic query supporting hosts.
     */
    public static final int NON_DYNAMIC_QUERY_DEGREE = 6;
}