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
 *  Created on 11.08.2005
 *  --- CVS Information ---
 *  $Id: StatusBar.java 3362 2006-03-30 22:27:26Z gregork $
 */
// Revision    1.2
// Commit info     2004-10-30 10:38:22+0000
/**
 * $ $ License.
 *
 * Copyright $ L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package phex.gui.common.statusbar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Hashtable;

import javax.swing.*;

import com.l2fprod.common.swing.PercentLayout;

/**
 * StatusBar. <BR>A status bar is made of multiple zones. A zone can be any
 * JComponent.
 */
public class StatusBar extends JComponent
{
    private Hashtable idToZones;

    /**
     * Construct a new StatusBar
     *  
     */
    public StatusBar()
    {
        setLayout( new PercentLayout(PercentLayout.HORIZONTAL, 4) );
        idToZones = new Hashtable();
    }

    /**
     * Adds a new zone in the StatusBar
     * 
     * @param id
     * @param zone
     * @param constraints one of the constraint support by the
     *          {@link com.l2fprod.common.swing.PercentLayout}
     */
    public void addZone( String id, Component zone, String constraints )
    {
        // is there already a zone with this id?
        Component previousZone = getZone( id );
        if ( previousZone != null )
        {
            remove( previousZone );
            idToZones.remove( id );
        }
        
        if ( idToZones.size() > 0 )
        {
            JPanel panel = new JPanel( new BorderLayout() );
            JSeparator sep = new JSeparator( JSeparator.VERTICAL );
            panel.add(sep, BorderLayout.CENTER);
            panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            add( panel, "" );
        }

        add( zone, constraints );
        idToZones.put( id, zone );
    }

    public Component getZone( String id )
    {
        return (Component) idToZones.get( id );
    }

    /**
     * For example:
     * 
     * <code>
     *  setZones(new String[]{"A","B"},
     *                     new JComponent[]{new JLabel(), new JLabel()},
     *                     new String[]{"33%","*"});
     * </code>
     * 
     * would construct a new status bar with two zones (two JLabels)
     * named A and B, the first zone A will occupy 33 percents of the
     * overall size of the status bar and B the left space.
     * 
     * @param ids a value of type 'String[]'
     * @param zones a value of type 'JComponent[]'
     * @param constraints a value of type 'String[]'
     */
    public void setZones( String[] ids, Component[] zones, String[] constraints )
    {
        removeAll();
        idToZones.clear();
        for ( int i = 0, c = zones.length; i < c; i++ )
        {
            addZone( ids[i], zones[i], constraints[i] );
        }
    }
}