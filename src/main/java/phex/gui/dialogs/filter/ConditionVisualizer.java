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
 *  $Id: ConditionVisualizer.java 3682 2007-01-09 15:32:14Z gregork $
 */
package phex.gui.dialogs.filter;

import java.util.Iterator;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import phex.common.MediaType;
import phex.common.URN;
import phex.common.format.NumberFormatUtils;
import phex.rules.condition.*;
import phex.utils.Localizer;

public class ConditionVisualizer
{   
    public static String buildDisplayString( Condition condition )
    {
        String postfix = "";
        if ( condition instanceof NotCondition )
        {
            postfix = "_Not";
            condition = ((NotCondition)condition).getContainedCondition();
        }
        
        if ( condition instanceof FilenameCondition )
        {
            FilenameCondition filenameCondition = (FilenameCondition) condition;
            
            String conditionString = Localizer.getString( "RuleVisualization_FilenameCond" + postfix );

            Set terms = filenameCondition.getTerms();
            if ( terms.size() > 0 )
            {
                Iterator iterator = terms.iterator();
                StringBuffer buffer = new StringBuffer();
                while ( iterator.hasNext() )
                {
                    String term = (String) iterator.next();
                    buffer.append( "'" ).append( term ).append( "'" );
                    if ( iterator.hasNext() )
                    {
                        buffer.append( " " )
                              .append( Localizer.getString( "RuleVisualization_Or" ) )
                              .append( " " );
                    }
                }
                
                String replacement = buffer.toString();
                conditionString = RuleDescriptionVisualizer.replacePlaceholderPattern( 
                    conditionString, replacement );
            }
            return conditionString;
        }
        else if ( condition instanceof FileUrnCondition )
        {
            FileUrnCondition fileUrnCondition = (FileUrnCondition) condition;
            
            String conditionString = Localizer.getString( "RuleVisualization_FileUrnCond" + postfix );

            Set<URN> urns = fileUrnCondition.getUrnSet();
            if ( urns.size() > 0 )
            {
                Iterator<URN> iterator = urns.iterator();
                StringBuffer buffer = new StringBuffer();
                while ( iterator.hasNext() )
                {
                    URN urn = iterator.next();
                    buffer.append( "'" ).append( urn.getAsString() ).append( "'" );
                    if ( iterator.hasNext() )
                    {
                        buffer.append( " " )
                              .append( Localizer.getString( "RuleVisualization_Or" ) )
                              .append( " " );
                    }
                }
                
                String replacement = buffer.toString();
                conditionString = RuleDescriptionVisualizer.replacePlaceholderPattern( 
                    conditionString, replacement );
            }
            return conditionString;
        }
        else if ( condition instanceof MediaTypeCondition )
        {
            MediaTypeCondition mediaTypeCondition = (MediaTypeCondition) condition;
            
            String conditionString = Localizer.getString( "RuleVisualization_MediaTypeCond" + postfix );

            Set types = mediaTypeCondition.getTypes();
            if ( types.size() > 0 )
            {
                Iterator iterator = types.iterator();
                StringBuffer buffer = new StringBuffer();
                while ( iterator.hasNext() )
                {
                    MediaType type = (MediaType) iterator.next();
                    buffer.append( "'" ).append( Localizer.getString( type.getName() ) ).append( "'" );
                    if ( iterator.hasNext() )
                    {
                        buffer.append( " " )
                              .append( Localizer.getString( "RuleVisualization_Or" ) )
                              .append( " " );
                    }
                }
                String replacement = buffer.toString();
                conditionString = RuleDescriptionVisualizer.replacePlaceholderPattern( 
                    conditionString, replacement );
            }
            return conditionString;
        }
        else if ( condition instanceof FileSizeCondition )
        {
            FileSizeCondition fileSizeCondition = (FileSizeCondition) condition;
            
            String conditionString = Localizer.getString( "RuleVisualization_FileSizeCond" + postfix );

            Set ranges = fileSizeCondition.getRanges();
            if ( ranges.size() > 0 )
            {
                Iterator iterator = ranges.iterator();
                StringBuffer buffer = new StringBuffer();
                while ( iterator.hasNext() )
                {
                    FileSizeCondition.Range range = (FileSizeCondition.Range) iterator.next();
                    visualizeFileSizeRange( buffer, range );
                    if ( iterator.hasNext() )
                    {
                        buffer.append( " " )
                              .append( Localizer.getString( "RuleVisualization_Or" ) )
                              .append( " " );
                    }
                }
                String replacement = buffer.toString();
                conditionString = RuleDescriptionVisualizer.replacePlaceholderPattern( 
                    conditionString, replacement );
            }
            return conditionString;
        }
        
        throw new IllegalArgumentException( "Unknown condition to visualize: " +
            condition );
    }

    
    public static String buildCleanDisplayString( Condition condition )
    {
        String displayString = buildDisplayString( condition );
        return RuleDescriptionVisualizer.cleanDisplayString( displayString );
    }

    public static void visualize( Condition condition, boolean isAndCondition, 
        Document doc )
        throws BadLocationException
    {
        RuleDescriptionVisualizer.visualizeNext(isAndCondition, condition, doc);

        String displayString = buildDisplayString( condition );        
        RuleDescriptionVisualizer.insertDisplayString( displayString, condition, doc );
    }
    
    
    //////////////////////////////// FileSizeConditon /////////////////////////
    
    /**
     * @param buffer
     * @param range
     */
    public static void visualizeFileSizeRange( StringBuffer buffer, FileSizeCondition.Range range )
    {
        if ( range.min > 0 )
        {
            buffer.append( Localizer.getString("RuleVisualization_FileSizeCond_Min") )
                  .append( " " )
                  .append( NumberFormatUtils.formatSignificantByteSize( range.min ) );
        }
        if ( range.min > 0 && range.max > 0 )
        {
            buffer.append ( " " )
                  .append( Localizer.getString( "RuleVisualization_And" ) )
                  .append ( " " );
        }
        if ( range.max > 0 )
        {
            buffer.append( Localizer.getString("RuleVisualization_FileSizeCond_Max") )
                  .append( " " )
                  .append( NumberFormatUtils.formatSignificantByteSize( range.max ) );
        }
    }
}
