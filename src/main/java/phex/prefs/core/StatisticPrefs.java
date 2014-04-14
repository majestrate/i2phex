/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2005 Phex Development Group
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
 *  Created on 18.09.2006
 *  --- CVS Information ---
 *  $Id: StatisticPrefs.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex.prefs.core;

import phex.prefs.api.PreferencesFactory;
import phex.prefs.api.Setting;

public class StatisticPrefs extends PhexCorePrefs
{    
    /**
     * The total number of completed downloads tracked.
     */
    public static final Setting<Integer> TotalDownloadCount;
    
    /**
     * The total number of uploads.
     */
    public static final Setting<Integer> TotalUploadCount;
    
    /**
     * The last fractional uptime calculated. Needed for avg. daily uptime
     * calculation.
     */
    public static final Setting<Float> FractionalUptime;
    
    /**
     * Counts the total number of Phex startups.
     */
    public static final Setting<Integer> TotalStartupCounter;
    
    /**
     * The total uptime of the last movingTotalUptimeCount starts.
     */
    public static final Setting<Long> MovingTotalUptime;

    /**
     * The number of times the uptime was added to movingTotalUptime.
     */
    public static final Setting<Integer> MovingTotalUptimeCount;

    /**
     * The maximal uptime ever seen.
     */
    public static final Setting<Long> MaximalUptime;
    
    /** 
     * Last time Phex was shutdown. Needed for avg. daily uptime calculation.
     */
    public static final Setting<Long> LastShutdownTime;
    
    /**
     * The file into which searches should be monitored.
     */
    public  static final Setting<String> QueryHistoryLogFile;
    
    /**
     * The number of searches to track in monitor.
     */
    public  static final Setting<Integer> QueryHistoryEntries;
    
    static
    {
        TotalDownloadCount = PreferencesFactory.createIntSetting( 
            "Statistic.TotalDownloadCount", 0, instance );
        TotalUploadCount = PreferencesFactory.createIntSetting( 
            "Statistic.TotalUploadCount", 0, instance );
        FractionalUptime = PreferencesFactory.createFloatSetting(
            "Statistic.FractionalUptime", 0, instance );
        TotalStartupCounter = PreferencesFactory.createIntSetting( 
            "Statistic.TotalStartupCounter", 0, instance );
        MovingTotalUptime = PreferencesFactory.createLongSetting( 
            "Statistic.MovingTotalUptime", 0, instance );
        MovingTotalUptimeCount = PreferencesFactory.createIntSetting( 
            "Statistic.MovingTotalUptimeCount", 0, instance );
        MaximalUptime = PreferencesFactory.createLongSetting( 
            "Statistic.MaximalUptime", 0, instance );
        LastShutdownTime = PreferencesFactory.createLongSetting( 
            "Statistic.LastShutdownTime", 0, instance );
        QueryHistoryLogFile = PreferencesFactory.createStringSetting( 
            "Statistic.SearchMonitorFile", "", instance );
        QueryHistoryEntries = PreferencesFactory.createIntSetting( 
            "Statistic.SearchHistoryLength", 10, instance );
    }
}
