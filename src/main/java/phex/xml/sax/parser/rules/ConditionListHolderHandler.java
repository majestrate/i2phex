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
 *  Created on 18.11.2005
 *  --- CVS Information ---
 *  $Id: ConditionListHolderHandler.java 3682 2007-01-09 15:32:14Z gregork $
 */
package phex.xml.sax.parser.rules;

import java.io.CharArrayWriter;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import phex.xml.sax.DSubElementList;
import phex.xml.sax.rules.*;

/**
 * 
 */
public abstract class ConditionListHolderHandler extends DefaultHandler
{
    private CharArrayWriter text = new CharArrayWriter();

    private SAXParser parser;

    private DSubElementList<DCondition> conditionListHolder;

    private DefaultHandler parent;
    
    private String elementName;

    public ConditionListHolderHandler( String elementName, 
        DSubElementList<DCondition> conditionListHolder, DefaultHandler parent, 
        SAXParser parser )
    {
        this.elementName = elementName;
        this.conditionListHolder = conditionListHolder;
        this.parser = parser;
        this.parent = parent;
    }

    /**
     * Receive notification of the start of an element.
     *
     * @param name The element type name.
     * @param attributes The specified or defaulted attributes.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement( String uri, String localName, String qName,
        Attributes attributes ) throws SAXException
    {
        text.reset();
        if ( qName.equals( DAndConcatCondition.ELEMENT_NAME ) )
        {
            DAndConcatCondition condition = new DAndConcatCondition();
            conditionListHolder.getSubElementList().add( condition );

            AndConcatConditionHandler handler = new AndConcatConditionHandler(
                condition, attributes, this, parser );
            parser.getXMLReader().setContentHandler( handler );
        }
        else if ( qName.equals( DNotCondition.ELEMENT_NAME ) )
        {
            DNotCondition condition = new DNotCondition();
            conditionListHolder.getSubElementList().add( condition );

            NotConditionHandler handler = new NotConditionHandler(
                condition, attributes, this, parser );
            parser.getXMLReader().setContentHandler( handler );
        }
        else if ( qName.equals( DFilenameCondition.ELEMENT_NAME ) )
        {
            DFilenameCondition condition = new DFilenameCondition();
            conditionListHolder.getSubElementList().add( condition );

            FilenameConditionHandler handler = new FilenameConditionHandler(
                condition, attributes, this, parser );
            parser.getXMLReader().setContentHandler( handler );
        }
        else if ( qName.equals( DFileSizeCondition.ELEMENT_NAME ) )
        {
            DFileSizeCondition condition = new DFileSizeCondition();
            conditionListHolder.getSubElementList().add( condition );

            FileSizeConditionHandler handler = new FileSizeConditionHandler(
                condition, attributes, this, parser );
            parser.getXMLReader().setContentHandler( handler );
        }
        else if ( qName.equals( DMediaTypeCondition.ELEMENT_NAME ) )
        {
            DMediaTypeCondition condition = new DMediaTypeCondition();
            conditionListHolder.getSubElementList().add( condition );

            MediaTypeConditionHandler handler = new MediaTypeConditionHandler(
                condition, attributes, this, parser );
            parser.getXMLReader().setContentHandler( handler );
        }
        else if ( qName.equals( DFileUrnCondition.ELEMENT_NAME ) )
        {
            DFileUrnCondition condition = new DFileUrnCondition();
            conditionListHolder.getSubElementList().add( condition );

            FileUrnConditionHandler handler = new FileUrnConditionHandler(
                condition, attributes, this, parser );
            parser.getXMLReader().setContentHandler( handler );
        }
        return;
    }

    public void endElement( String uri, String localName, String qName )
        throws SAXException
    {
        if ( qName.equals( elementName ) )
        {
            parser.getXMLReader().setContentHandler( parent );
        }
    }

    public InputSource resolveEntity( String publicId, String systemId )
    {
        return null;
    }

    public void characters( char[] ch, int start, int length )
    {
        text.write( ch, start, length );
    }
}
