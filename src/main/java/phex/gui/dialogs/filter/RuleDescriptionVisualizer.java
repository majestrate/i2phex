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
 *  Created on 29.11.2005
 *  --- CVS Information ---
 *  $Id: RuleDescriptionVisualizer.java 3682 2007-01-09 15:32:14Z gregork $
 */
package phex.gui.dialogs.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.*;

import phex.utils.Localizer;

public class RuleDescriptionVisualizer
{
    protected static final Pattern REPL_PATTERN = Pattern.compile( "--__.*__--" );
    
    /**
     * @param displayString
     * @return
     */
    public static String cleanDisplayString( String displayString )
    {
        int startIdx = displayString.indexOf( "--__" );
        int endIdx = displayString.indexOf( "__--" );
        
        if ( startIdx == -1 || endIdx == -1 )
        {
            return displayString;
        }

        return displayString.substring( 0, startIdx )
            + displayString.substring( startIdx + 4, endIdx )
            + displayString.substring( endIdx + 4, displayString.length() );

        // in Java 1.5 html in the DefaultTableCellRenderer causes rows to be
        // drawn empty when clicking around for a while, therefore we just dont
        // show the underline on the selection table for now

        //        return "<html>" + conditionString.substring( 0, startIdx )
        //             + "<u>" + conditionString.substring( startIdx+4, endIdx ) + "</u>"
        //             + conditionString.substring( endIdx+4, conditionString.length() ) 
        //             + "</html>";
    }
    
    
    /**
     * @param displayString
     * @param className
     * @param doc
     * @throws BadLocationException
     */
    public static void insertDisplayString( String displayString,
        Object reference, Document doc ) throws BadLocationException
    {
        int startIdx = displayString.indexOf( "--__" );
        int endIdx = displayString.indexOf( "__--" );
        
        if ( startIdx == -1 || endIdx == -1 )
        {
            insertStandardPart( displayString, reference, doc );
        }
        else
        {
            insertStandardPart( displayString.substring( 0, startIdx ), reference,
                doc );
            insertLinkPart( displayString.substring( startIdx + 4, endIdx ),
                reference, doc );
            insertStandardPart( displayString.substring( endIdx + 4, displayString
                .length() ), reference, doc );
        }
    }

    protected static void insertLinkPart( String text, Object reference,
        Document doc ) throws BadLocationException
    {
        SimpleAttributeSet set = new SimpleAttributeSet();
        set.addAttribute( "Link", reference );
        set.addAttribute( "RuleObjReference", reference );
        StyleConstants.setUnderline( set, true );
        doc.insertString( doc.getLength(), text, set );
    }

    protected static void insertStandardPart( String text, Object reference,
        Document doc ) throws BadLocationException
    {
        if ( text.length() == 0 )
        {
            return;
        }
        SimpleAttributeSet set = new SimpleAttributeSet();
        set.addAttribute( "RuleObjReference", reference );
        doc.insertString( doc.getLength(), text, set );
    }

    public static void visualizeNext( boolean isAndConcat, 
        Object reference, Document doc ) throws BadLocationException
    {
        insertStandardPart( "\n", reference, doc );
        if ( isAndConcat )
        {
            insertStandardPart( Localizer.getString( 
                "RuleVisualization_And" ) + " ", reference, doc);
        }
    }
    
    public static String replacePlaceholderPattern( String source, String replacement )
    {
        Matcher matcher = REPL_PATTERN.matcher( source );
        // make sure replacement is properly quoted.
        String replacedStr = Matcher.quoteReplacement( replacement );
        return matcher.replaceAll( "--__" + replacedStr + "__--" );
    }    
}
