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
 *  Created on 28.11.2005
 *  --- CVS Information ---
 *  $Id: ConsequenceVisualizer.java 3506 2006-07-25 22:45:00Z gregork $
 */
package phex.gui.dialogs.filter;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import phex.rules.consequence.*;
import phex.utils.Localizer;

public class ConsequenceVisualizer
{    
    public static String buildDisplayString( Consequence consequence )
    {
        if ( consequence instanceof FilterFromSearchConsequence )
        {
            String displayString = Localizer.getString( "RuleVisualization_HideFromSearchConseq" );
            return displayString;
        }
        else if ( consequence instanceof RemoveFromSearchConsequence )
        {
            String displayString = Localizer.getString( "RuleVisualization_RemoveFromSearchConseq" );
            return displayString;
        }
        else if ( consequence instanceof DownloadFileConsequence )
        {
            String displayString = Localizer.getString( "RuleVisualization_DownloadFileConseq" );
            return displayString;
        }
        else if ( consequence instanceof BanHostConsequence )
        {
            String displayString = Localizer.getString( "RuleVisualization_BanHostConseq" );
            return displayString;
        }
        throw new IllegalArgumentException( "Unknown consequence to visualize: " +
            consequence );
    }
    
    public static String buildCleanDisplayString( Consequence consequence )
    {
        String displayString = buildDisplayString( consequence );
        return RuleDescriptionVisualizer.cleanDisplayString( displayString );
    }

    public static void visualize( Consequence consequence, boolean isAndConcat, 
        Document doc )
        throws BadLocationException
    {
        RuleDescriptionVisualizer.visualizeNext(isAndConcat, consequence, doc);

        String displayString = buildDisplayString( consequence );        
        RuleDescriptionVisualizer.insertDisplayString( displayString, consequence, doc );
    }
}
