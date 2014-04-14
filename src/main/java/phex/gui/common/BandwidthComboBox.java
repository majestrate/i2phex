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
 *  --- SVN Information ---
 *  $Id: BandwidthComboBox.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.gui.common;

import javax.swing.JComboBox;

import phex.utils.Localizer;

public class BandwidthComboBox extends JComboBox
{
    /** 
     * Modem = 56K Modem
     * ISDN = 64K ISDN
     * DualISDN = 128K Dual ISDN
     * DSLCable1 = 512Kbps DSL / Cable
     * DSLCable2 = 1024Kbps DSL / Cable
     * T1 = 1.5Mbps T1
     * DSLCable3 = 6Mbps DSL / Cable
     * 10LAN = 10Mbps LAN
     * T3 = 44 Mbps T3
     * 100LAN = 100Mbps LAN
     * 1000LAN = 1Gbps LAN
     */
    public static final SpeedDefinition[] SPEED_DEFINITIONS =
    { 
        new SpeedDefinition("Modem", 56), 
        new SpeedDefinition("ISDN", 64),
        new SpeedDefinition("DualISDN", 128),
        new SpeedDefinition("DSLCable1", 1024),
        new SpeedDefinition("T1", 1544),
        new SpeedDefinition("DSLCable2", 2048),
        new SpeedDefinition("DSLCable3", 6144),
        new SpeedDefinition("10LAN", 10000),
        new SpeedDefinition("DSLCable4", 16384),
        new SpeedDefinition("T3", 44736),
        new SpeedDefinition("100LAN", 100000),
        new SpeedDefinition("1000LAN", 1000000)
    };
    
    public BandwidthComboBox()
    {
        super( SPEED_DEFINITIONS );
    }
    
    public SpeedDefinition getSelectedSpeedDefinition()
    {
        return (SpeedDefinition)getSelectedItem();
    }
    
    public static class SpeedDefinition
    {
        private String representation;

        /**
         * The speed of the connection in kilo bits per second.
         */
        private int speedInKbps;

        /**
         * @param aRepresentation the not localized string representation
         */
        public SpeedDefinition( String aRepresentation, int aSpeedInKbps )
        {
            representation = Localizer.getString( aRepresentation );
            speedInKbps = aSpeedInKbps;
        }

        /**
         * Returns the speed of the connection in kilo bytes per second.
         */
        public double getSpeedInKB()
        {
            return speedInKbps / 8.0;
        }

        public int getSpeedInKbps()
        {
            return speedInKbps;
        }

        @Override
        public String toString()
        {
            return representation;
        }
    }
}
