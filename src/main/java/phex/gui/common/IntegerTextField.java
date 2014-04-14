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
 */
package phex.gui.common;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * This text field allows only digits as characters they can be limited to a
 * specific length.
 */
public class IntegerTextField extends JTextField
{
   private int maxChar;

   public IntegerTextField(int charLimit)
   {
      this(null, charLimit, charLimit);
   }

   public IntegerTextField(int columns, int charLimit)
   {
      this(null, columns, charLimit);
   }

   public IntegerTextField(String string, int columns, int charLimit)
   {
      super(columns);
      setHorizontalAlignment(JTextField.RIGHT);
      maxChar = charLimit;
      setText(string);
   }

   /**
    * Returns the integer value of the text field or null in case of a number
    * format exception... like on a empty text field.
    * @return
    */
   public Integer getIntegerValue()
   {
       String text = getText();
       try
       {
           Integer integer = Integer.valueOf( text );
           return integer;
       }
       catch ( NumberFormatException exp )
       {
           return null;
       }
   }

   protected Document createDefaultModel()
   {
      return new IntegerDocument();
   }

   class IntegerDocument extends PlainDocument
   {
      IntegerDocument()
      {
         super();
      }

      public void insertString(int offset, String str, AttributeSet a) throws BadLocationException
      {
         if (str == null)
         {
            return;
         }

         // check size limit...
         int tomuch = str.length() + getLength() - maxChar;
         if (tomuch > 0)
         {
            str = str.substring(0, str.length() - tomuch);
            Toolkit.getDefaultToolkit().beep();
         }

         // check if digit..
         char[] orgCharArr = str.toCharArray();
         char[] addCharArr = new char[ orgCharArr.length ];
         int j = 0;
         for ( int i = 0; i < orgCharArr.length; i++ )
         {
            if ( Character.isDigit( orgCharArr[i] ) )
            {
                addCharArr[ j ] = orgCharArr[i];
                j++;
            }
         }

         super.insertString(offset, new String( addCharArr, 0, j ), a);
      }
   }
}