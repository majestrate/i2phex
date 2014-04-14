/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2007 Phex Development Group
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
 *  $Id: RuleDescriptionPanel.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.dialogs.filter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.*;

import phex.common.log.NLogger;
import phex.gui.dialogs.filter.editors.*;
import phex.rules.Rule;
import phex.rules.condition.*;
import phex.rules.consequence.Consequence;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RuleDescriptionPanel extends JPanel
{
    private RuleDescOwnerDialog parent;
    private JTextPane ruleDescArea;

    public RuleDescriptionPanel( RuleDescOwnerDialog parent )
    {
        this.parent = parent;
        prepareComponent();
    }

    private void prepareComponent()
    {
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout( "fill:d:grow", // columns
            "p, 2dlu, fill:75dlu:grow" );
        PanelBuilder contentPB = new PanelBuilder( layout, this );
        //int columnCount = layout.getColumnCount();
        //int rowCount = layout.getRowCount();

        JLabel label = new JLabel( Localizer
            .getString( "RuleWizard_RuleDescription" ) );
        contentPB.add( label, cc.xywh( 1, 1, 1, 1 ) );

        ruleDescArea = new JTextPane();
        ruleDescArea.setEditable( false );
        DescPaneMouseListener mouseListener = new DescPaneMouseListener();
        ruleDescArea.addMouseListener( mouseListener );
        ruleDescArea.addMouseMotionListener( mouseListener );
        //ruleDescArea.setDocument( new DefaultStyledDocument() );
        //ruleDescArea.setContentType( "text/html" );
        //ruleDescArea.setWrapStyleWord(true);
        //ruleDescArea.setLineWrap(true);
        contentPB.add( new JScrollPane( ruleDescArea ), cc.xywh( 1, 3, 1, 1 ) );
    }
    
    public void updateRuleData(  )
    {
        Rule rule = parent.getEditRule();
        try
        {
            // remove content...
            StyledDocument doc = ruleDescArea.getStyledDocument();
            doc.remove(0, doc.getLength());
            
            if ( rule == null )
            {
                // no need to fill anything..
                return;
            }
            
            ruleDescArea.setEnabled( !rule.isDefaultRule() );
            
            RuleDescriptionVisualizer.insertStandardPart(
                Localizer.getString("RuleVisualization_Prefix"), rule, doc);
            
            // fill conditions
            List conditionsList = rule.getConditions();
            Iterator iterator = conditionsList.iterator();
            boolean isFirst = true;
            while( iterator.hasNext() )
            {
                Condition condition = (Condition) iterator.next();
                if ( condition instanceof NotCondition )
                {
                    continue;
                }
                ConditionVisualizer.visualize( condition, !isFirst , doc);
                isFirst = false;
            }
            
            // fill consequences
            List consequencesList = rule.getConsequences();
            iterator = consequencesList.iterator();
            isFirst = true;
            while( iterator.hasNext() )
            {
                Consequence consequence = (Consequence) iterator.next();
                ConsequenceVisualizer.visualize( consequence, !isFirst , doc);
                isFirst = false;
            }
            
            // fill exceptions
            conditionsList = rule.getConditions();
            iterator = conditionsList.iterator();
            isFirst = true;
            while( iterator.hasNext() )
            {
                Condition condition = (Condition) iterator.next();
                if ( !(condition instanceof NotCondition) )
                {
                    continue;
                }
                ConditionVisualizer.visualize( condition, !isFirst , doc);
                isFirst = false;
            }

        }
        catch ( BadLocationException exp )
        {
            NLogger.error(getClass(), exp, exp);
        }
    }
    
    private JDialog determineRuleLinkEditor( Object reference )
    {
        if ( reference instanceof NotCondition )
        {
            reference = ((NotCondition)reference).getContainedCondition();
        }
        
        if ( reference instanceof FileSizeCondition )
        {
            return new FileSizeCondEditor( (FileSizeCondition)reference, this, (JDialog)parent );
        }
        else if ( reference instanceof FilenameCondition )
        {
            return new FileNameCondEditor( (FilenameCondition)reference, this, (JDialog)parent );
        }
        else if ( reference instanceof FileUrnCondition )
        {
            return new FileUrnCondEditor( (FileUrnCondition)reference, this, (JDialog)parent );
        }
        else if ( reference instanceof MediaTypeCondition )
        {
            return new MediaTypeCondEditor( (MediaTypeCondition)reference, this, (JDialog)parent );
        }
        throw new IllegalArgumentException( "Unknown rule link editor.");
    }

    private class DescPaneMouseListener extends MouseAdapter implements
        MouseMotionListener
    {
        private final Cursor LINK_CURSOR = Cursor
            .getPredefinedCursor( Cursor.HAND_CURSOR );

        private final Cursor DEFAULT_CURSOR = Cursor
            .getPredefinedCursor( Cursor.DEFAULT_CURSOR );

        /** 
         * This is used by viewToModel to avoid allocing a new array each
         * time.
         */
        private Position.Bias[] bias = new Position.Bias[1];

        private Element curElem = null;

        private Element linkElem = null;

        public void mouseMoved( MouseEvent e )
        {
            JTextPane editor = (JTextPane) e.getSource();
            boolean adjustCursor = true;
            Cursor newCursor = DEFAULT_CURSOR;
            if ( !editor.isEditable() )
            {
                Point pt = new Point( e.getX(), e.getY() );
                int pos = editor.getUI().viewToModel( editor, pt, bias );
                if ( bias[0] == Position.Bias.Backward && pos > 0 )
                {
                    pos--;
                }
                if ( pos >= 0 )
                {
                    StyledDocument hdoc = (StyledDocument) editor.getDocument();
                    Element elem = hdoc.getCharacterElement( pos );
                    if ( !doesElementContainLocation( editor, elem, pos, e
                        .getX(), e.getY() ) )
                    {
                        elem = null;
                    }

                    if ( curElem != elem )
                    {
                        curElem = elem;
                        Element tmpLinkElem = null;
                        if ( elem != null )
                        {
                            AttributeSet attributes = elem.getAttributes();
                            Object link = attributes.getAttribute( "Link" );
                            if ( link != null )
                            {
                                tmpLinkElem = elem;
                            }
                        }

                        if ( tmpLinkElem != linkElem )
                        {
                            linkElem = tmpLinkElem;
                            if ( tmpLinkElem != null )
                            {
                                newCursor = LINK_CURSOR;
                            }
                        }
                        else
                        {
                            adjustCursor = false;
                        }
                    }
                    else
                    {
                        adjustCursor = false;
                    }
                }
            }
            if ( adjustCursor && editor.getCursor() != newCursor )
            {
                editor.setCursor( newCursor );
            }
        }

        /**
         * Returns true if the View representing <code>e</code> contains
         * the location <code>x</code>, <code>y</code>. <code>offset</code>
         * gives the offset into the Document to check for.
         */
        private boolean doesElementContainLocation( JEditorPane editor,
            Element e, int offset, int x, int y )
        {
            if ( e != null && offset > 0 && e.getStartOffset() == offset )
            {
                try
                {
                    TextUI ui = editor.getUI();
                    Shape s1 = ui.modelToView( editor, offset,
                        Position.Bias.Forward );
                    if ( s1 == null )
                    {
                        return false;
                    }
                    Rectangle r1 = (s1 instanceof Rectangle) ? (Rectangle) s1
                        : s1.getBounds();
                    Shape s2 = ui.modelToView( editor, e.getEndOffset(),
                        Position.Bias.Backward );
                    if ( s2 != null )
                    {
                        Rectangle r2 = (s2 instanceof Rectangle) ? (Rectangle) s2
                            : s2.getBounds();
                        r1.add( r2 );
                    }
                    return r1.contains( x, y );
                }
                catch (BadLocationException ble)
                {
                }
            }
            return true;
        }

        // TODO verify if the RuleClass attribute is necessary... otherwise drop it.
        public void mouseClicked( MouseEvent e )
        {
            if ( linkElem == null )
            {
                return;
            }
            AttributeSet atts = linkElem.getAttributes();
            Object reference = atts.getAttribute( "Link" );
            JDialog dialog = determineRuleLinkEditor( reference );
            dialog.show();
            updateRuleData();
        }

        public void mouseDragged( MouseEvent e )
        {
        }
    }
    
//    private void findRuleClass( String ruleClass, Element parent, ArrayList list )
//    {
//        int count = parent.getElementCount();
//        for ( int i = 0; i < count; i++ )
//        {
//            Element child = parent.getElement(i);
//            Object attVal = child.getAttributes().getAttribute("RuleClass");
//            if ( attVal != null && ruleClass.equals(attVal) )
//            {
//                list.add( 0, child );
//            }
//            else
//            {                    
//                findRuleClass( ruleClass, child, list );
//            }
//        }
//    }
}
