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
 *  --- SVN Information ---
 *  $Id: IPTextField.java 3817 2007-06-13 08:37:42Z gregork $
 */
package phex.gui.common;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;

import phex.common.address.AddressUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class IPTextField extends JPanel
{
    private JTextField part1;
    private JTextField part2;
    private JTextField part3;
    private JTextField part4;

    private TextFieldFocusHandler textFieldFocusHandler;

    public IPTextField()
    {
        super( );
        
        textFieldFocusHandler = new TextFieldFocusHandler();
        
        Border border = (Border)UIManager.get( "TextField.border" );
        setBorder( new BorderWrapping( border ) );

        part1 = createTextF( null );
        part2 = createTextF( part1 );
        part3 = createTextF( part2 );
        part4 = createTextF( part3 );
        
        FormLayout layout = new FormLayout( "d, d, d, d, d, d, d", "p" );
        PanelBuilder panelBuilder = new PanelBuilder( layout, this );
        
        panelBuilder.add( part1 );
        panelBuilder.nextColumn();
        panelBuilder.add( buildLabel() );
        panelBuilder.nextColumn();
        panelBuilder.add( part2 );
        panelBuilder.nextColumn();
        panelBuilder.add( buildLabel() );
        panelBuilder.nextColumn();
        panelBuilder.add( part3 );
        panelBuilder.nextColumn();
        panelBuilder.add( buildLabel() );
        panelBuilder.nextColumn();
        panelBuilder.add( part4 );

        setBackground( part1.getBackground() );
    }

    public void setEnabled( boolean state )
    {
        super.setEnabled( state );
        part1.setEnabled( state );
        part2.setEnabled( state );
        part3.setEnabled( state );
        part4.setEnabled( state );
    }

    public boolean isInputValid()
    {
        if ( !isFieldValid( part1 ) )
        {
            //invalidTF = part1;
            return false;
        }
        if ( !isFieldValid( part2 ) )
        {
            //invalidTF = part2;
            return false;
        }
        if ( !isFieldValid( part3 ) )
        {
            //invalidTF = part3;
            return false;
        }
        if ( !isFieldValid( part4 ) )
        {
            //invalidTF = part4;
            return false;
        }
        return true;
    }
    
    public boolean isFieldEmpty()
    {
        if ( part1.getText().trim().length() == 0 &&
             part2.getText().trim().length() == 0 &&
             part3.getText().trim().length() == 0 &&
             part4.getText().trim().length() == 0 )
        {
            return true;
        }
        return false;
    }

    private boolean isFieldValid( JTextField textField )
    {
        String str = textField.getText();
        if ( str.length() == 0 )
        {
            return false;
        }
        try
        {
            int result = Integer.parseInt( str );
            if ( result > 255 )
            {
                textField.setText( "" );
                return false;
            }
            return true;
        }
        catch ( NumberFormatException exp )
        {
            textField.setText( "" );
            return false;
        }
    }

    public void setIPString( String ipString )
    {
        Document doc = part1.getDocument();
        try
        {
            doc.remove( 0, doc.getLength() );
            doc.insertString( 0, ipString, new SimpleAttributeSet() );
        }
        catch ( BadLocationException exp )
        {
        }
    }

    public String getIPString()
    {
        byte[] ip = getIP();
        return AddressUtils.ip2string( ip );
    }

    public byte[] getIP()
    {
        String p1 = part1.getText();
        if ( p1.length() == 0 )
        {
            p1 = "0";
        }
        String p2 = part2.getText();
        if ( p2.length() == 0 )
        {
            p2 = "0";
        }
        String p3 = part3.getText();
        if ( p3.length() == 0 )
        {
            p3 = "0";
        }
        String p4 = part4.getText();
        if ( p4.length() == 0 )
        {
            p4 = "0";
        }

        try
        {
            byte[] ip = new byte[4];
            ip[0] = (byte)Integer.parseInt( p1 );
            ip[1] = (byte)Integer.parseInt( p2 );
            ip[2] = (byte)Integer.parseInt( p3 );
            ip[3] = (byte)Integer.parseInt( p4 );
            return ip;
        }
        catch ( NumberFormatException exp )
        {
            return null;
        }
    }

    private JTextField createTextF( JTextField prevTextField )
    {
        IPDocument doc = new IPDocument();
        JTextField field = new JTextField( doc, null, 3 );
        field.addFocusListener( textFieldFocusHandler );
        field.setBorder( null );
        field.setHorizontalAlignment( JTextField.CENTER );

        if ( prevTextField != null )
        {
            ((IPDocument)prevTextField.getDocument()).setNextTextField( field );
        }
        
        return field;
    }

    private JLabel buildLabel()
    {
        JLabel label = new JLabel( "." );
        label.setBackground( part1.getBackground() );
        label.setForeground( part1.getForeground() );
        return label;
    }

    private void addToNextTextField( String nextTextFieldStr, JTextField nextTF )
    {
        if ( nextTF == null )
        {// drop string
            return;
        }
        nextTF.requestFocus();
        Document doc = nextTF.getDocument();
        try
        {
            doc.remove( 0, doc.getLength() );
            doc.insertString( 0, nextTextFieldStr, new SimpleAttributeSet() );
        }
        catch ( BadLocationException exp )
        {
        }
    }

    private void selectNextTextField()
    {
        JTextField nextTF = getNextFocusTextField();
        if ( nextTF == null )
        {// drop string
            return;
        }
        nextTF.requestFocus();
        nextTF.selectAll();
    }

    private JTextField getNextFocusTextField()
    {
        if ( part1.hasFocus() )
        {
            return part2;
        }
        else if ( part2.hasFocus() )
        {
            return part3;
        }
        else if ( part3.hasFocus() )
        {
            return part4;
        }
        return null;
    }
    
    /**
     * This is necessary since the UIResource border bases its insets calculation
     * on a JTextComponent instance. This is simulated by provding the part1 TF.
     */
    public class BorderWrapping implements Border
    {
        private Border border;

        public BorderWrapping(Border border)
        {
            super();
            this.border = border;
        }

        public Insets getBorderInsets(Component c)
        {
            return border.getBorderInsets( part1 );
        }

        public boolean isBorderOpaque()
        {
            return border.isBorderOpaque();
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
        {
            border.paintBorder(c, g, x, y, width, height);
        }
    }

    public class TextFieldFocusHandler extends FocusAdapter
    {
        public void focusLost( FocusEvent e )
        {
            if ( e.isTemporary() )
            {
                return;
            }
            JTextField textField = (JTextField)e.getSource();
            String text = textField.getText();
            if ( text.length() == 0 )
            {
                return;
            }
            try
            {
                int result = Integer.parseInt( text );
                if ( result > 255 )
                {
                    textField.setText( "255" );
                }
            }
            catch ( NumberFormatException exp )
            {
                textField.setText( "0" );
            }
        }
    }

    class IPDocument extends PlainDocument
    {
        protected JTextField nextTextField;
        IPDocument( )
        {
            super();
        }

        public void setNextTextField( JTextField textField )
        {
            nextTextField = textField;
        }

        public void insertString(int offset, String str, AttributeSet a)
                throws BadLocationException
        {
            if (str == null)
            {
                return;
            }

            // add max 3 chars....
            int freeSpace = 3 - getLength();
            if ( freeSpace == 0 )
            {
                return;
            }

            char[] addCharArr = new char[ freeSpace ];
            int addCharCount = 0;
            int strLength = str.length();
            int i = 0;
            boolean continueToNextField = false;
            for ( i = 0; i < strLength; i++ )
            {
                char c = str.charAt( i );
                if ( Character.isDigit( c ) )
                {
                    addCharArr[ addCharCount ] = c;
                    addCharCount++;
                    if ( addCharCount == freeSpace )
                    {
                        break;
                    }
                }
                else if ( c == '.' && addCharCount > 0 )
                {
                    continueToNextField = true;
                    break;
                }
            }

            super.insertString(offset, new String( addCharArr, 0, addCharCount ), a);

            if ( continueToNextField || offset + addCharCount == 3 )
            {
                if ( i + 1 < strLength )
                {
                    addToNextTextField( str.substring( i + 1 ), nextTextField );
                }
                else if ( getLength() == 3 )
                {
                    selectNextTextField();
                }
            }
        }
    }
}