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
 *  Created on 23.12.2005
 *  --- CVS Information ---
 *  $Id: FWActionGroup.java 3392 2006-04-17 15:30:08Z gregork $
 */
package phex.gui.actions;

import java.util.HashMap;
import java.util.Iterator;

public class FWActionGroup
{
    /**
     * Contains the actions of this group together with a retrieval key.
     */
    private HashMap actionMap;
    
    public FWActionGroup()
    {
        actionMap = new HashMap();
    }
    
    public void addAction( FWAction action )
    {
        actionMap.put( action, action );
    }
    
    public void addActions( FWAction[] actions )
    {
        for ( int i = 0; i < actions.length; i++ )
        {
            actionMap.put( actions[i], actions[i] );
        }
    }

    public void addAction( String key, FWAction action )
    {
        actionMap.put( key, action );
    }

    public FWAction getAction( String key )
    {
        return (FWAction)actionMap.get( key );
    }

    public void refreshActions()
    {
        Iterator iterator = actionMap.values().iterator();
        while ( iterator.hasNext() )
        {
            FWAction action = (FWAction)iterator.next();
            action.refreshActionState();
        }
    }
}
