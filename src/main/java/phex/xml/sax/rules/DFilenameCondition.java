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
 *  Created on 14.11.2005
 *  --- CVS Information ---
 *  $Id: DFilenameCondition.java 3682 2007-01-09 15:32:14Z gregork $
 */
package phex.xml.sax.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXException;

import phex.rules.condition.Condition;
import phex.rules.condition.FilenameCondition;
import phex.xml.sax.PhexXmlSaxWriter;

/**
 * Filters all files matching the expression.
 */
public class DFilenameCondition implements DCondition
{
    static public final String ELEMENT_NAME = "filename-condition";
    
    private List<String> terms;
    
    /**
     * @param expression
     * @param case1
     */
    public DFilenameCondition( )
    {
        terms = new ArrayList<String>();
    }

    public List<String> getTerms()
    {
        return terms;
    }

    public void setTerms( List<String> terms )
    {
        this.terms = terms;
    }

    public void serialize( PhexXmlSaxWriter writer ) throws SAXException
    {
        writer.startElm( ELEMENT_NAME, null );
        
        if ( terms != null )
        {
            Iterator iterator = terms.iterator();
            while ( iterator.hasNext() )
            {
                writer.startElm( "term", null );
                writer.elmText( (String)iterator.next() );
                writer.endElm( "term" );
            }
        }
        
        writer.endElm( ELEMENT_NAME );
    }

    public Condition createCondition()
    {
        FilenameCondition cond = new FilenameCondition( );
        Iterator iterator = terms.iterator();
        while ( iterator.hasNext() )
        {
            cond.addTerm((String) iterator.next());
        }
        return cond;
    }
}
