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
 *  --- CVS Information ---
 *  $Id: PlainMultiLinePanel.java 4363 2009-01-16 10:37:30Z gregork $
 */
package phex.gui.common;

import javax.swing.JTextArea;
import javax.swing.UIManager;

public class PlainMultiLinePanel extends JTextArea
{
    /**
     * @param text
     */
    public PlainMultiLinePanel(String text)
    {
        super();
        setEditable(false);
        setLineWrap( true );
        setWrapStyleWord( true );
        // adjust the used style sheet to match the label style..
        setFont( UIManager.getFont("Label.font") );
        setForeground( UIManager.getColor("Label.foreground") );
        setBackground( UIManager.getColor("Label.background"));
        
        setText( text );
    }
}
