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
 *  Created on 03.12.2005
 *  --- CVS Information ---
 *  $Id: FWSizeDefComboBox.java 3612 2006-11-17 15:58:21Z gregork $
 */
package phex.gui.common;

import javax.swing.JComboBox;

import phex.common.format.NumberFormatUtils;
import phex.utils.Localizer;

public class FWSizeDefComboBox extends JComboBox
{
    public static final SizeDefinition[] SIZE_DEFINITIONS =
    {
        new SizeDefinition( "BytesToken", 1 ),
        new SizeDefinition( "KBToken", NumberFormatUtils.ONE_KB ),
        new SizeDefinition( "MBToken", NumberFormatUtils.ONE_MB ),
        new SizeDefinition( "GBToken", NumberFormatUtils.ONE_GB )
    };
    
    public FWSizeDefComboBox()
    {
        super( SIZE_DEFINITIONS );
    }
    
    public long getDefMultiplier()
    {
        return ((SizeDefinition)getSelectedItem()).getMultiplier();
    }
    
    public static class SizeDefinition
    {
        private String representation;
        private long multiplier;

        public SizeDefinition( String aRepresentation, long aMultiplier )
        {
            representation = Localizer.getString( aRepresentation );
            multiplier = aMultiplier;
        }

        public long getMultiplier()
        {
            return multiplier;
        }

        public String toString()
        {
            return representation;
        }
    }
}
