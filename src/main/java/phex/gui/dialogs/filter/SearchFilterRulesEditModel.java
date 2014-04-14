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
 *  Created on 21.12.2005
 *  --- CVS Information ---
 *  $Id: SearchFilterRulesEditModel.java 4364 2009-01-16 10:56:15Z gregork $
 */
package phex.gui.dialogs.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

import phex.rules.Rule;
import phex.rules.SearchFilterRules;

public class SearchFilterRulesEditModel extends AbstractListModel implements ListModel
{
    private final SearchFilterRules currentRules;
    
    // contains Rule or RuleEditWrapper :(
    private final List<Object> allRules;
    
    public SearchFilterRulesEditModel( SearchFilterRules currentRules )
    {
        this.currentRules = currentRules;
        allRules = new ArrayList<Object>();
        allRules.addAll( currentRules.getAsList() );
    }
    
    public void applyChangesToQueryManager()
    {
        ArrayList<Rule> newRulesList = new ArrayList<Rule>();
        Iterator<Object> iterator = allRules.iterator();
        while( iterator.hasNext() )
        {
            Object obj = iterator.next();
            if ( obj instanceof Rule )
            {
                newRulesList.add( (Rule)obj );
            }
            else
            {
                RuleEditWrapper wrapper = (RuleEditWrapper)obj;
                Rule modifiedRule = wrapper.getModifiedRule();
                if ( modifiedRule != null )
                {
                    newRulesList.add( modifiedRule );
                }
            }
        }
        currentRules.setRuleList( newRulesList );
    }
    
    public void addNewRule( Rule rule )
    {
        RuleEditWrapper wrapper = new RuleEditWrapper( rule );
        
        int pos = allRules.size();
        allRules.add( pos, wrapper );
        fireIntervalAdded(this, pos, pos);
    }
    
    public int moveRule( int idx, boolean moveUp )
    {
        Object rule = allRules.remove( idx );
        int newIdx = moveUp ? Math.max( 0, idx-1 ) : Math.min( idx+1, allRules.size() );
        allRules.add( newIdx, rule );
        int start = Math.min(idx, newIdx);
        int end = Math.max(idx, newIdx);
        fireContentsChanged( this, start, end );
        return newIdx;
    }
    
    public void removeRule( int idx )
    {
        RuleEditWrapper wrapper = getRuleEditWrapperAt(idx);        
        wrapper.setModifiedRule( null );
        
        allRules.remove(idx);
        fireIntervalRemoved(this, idx, idx);
    }
    
    public void updateRule( int idx, Rule rule )
    {
        RuleEditWrapper wrapper = (RuleEditWrapper) allRules.get( idx );
        wrapper.setModifiedRule( rule );
        fireContentsChanged(this, idx, idx);
    }
    
    /**
     * Returns the edit rule for the index position. In case the original rule
     * was modified this is the modified rule, otherwise the original rule is
     * returned.
     * @param idx
     * @return
     */
    public Rule getDisplayRuleAt( int idx )
    {
        RuleEditWrapper wrapper;
        Object obj = allRules.get( idx );
        if ( obj instanceof Rule )
        {
            wrapper = createRuleEditWrapper( (Rule)obj );
            allRules.remove( idx );
            allRules.add( idx, wrapper );
        }
        else 
        {
            wrapper = (RuleEditWrapper) obj;
        }

        return wrapper.getModifiedRule();
    }
    
    private RuleEditWrapper getRuleEditWrapperAt( int idx )
    {
        RuleEditWrapper wrapper;
        Object obj = allRules.get( idx );
        if ( obj instanceof Rule )
        {
            wrapper = createRuleEditWrapper( (Rule)obj );
            allRules.remove( idx );
            allRules.add( idx, wrapper );
        }
        else 
        {
            wrapper = (RuleEditWrapper) obj;
        }

        return wrapper;
    }
    
    private RuleEditWrapper createRuleEditWrapper( Rule orgRule )
    {
        Rule modifiableRule = (Rule) orgRule.clone();
        RuleEditWrapper wrapper = new RuleEditWrapper( modifiableRule );
        return wrapper;
    }
    
    public int getSize()
    {
        return allRules.size();
    }

    public Object getElementAt( int index )
    {
        return allRules.get(index);
    }
}
