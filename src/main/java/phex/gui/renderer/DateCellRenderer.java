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
package phex.gui.renderer;

import java.text.DateFormat;
import java.util.Date;

import phex.common.ExpiryDate;
import phex.utils.Localizer;


public class DateCellRenderer extends FWTableCellRenderer
{
    private DateFormat format;

    public DateCellRenderer()
    {
         format = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT );
    }

    /**
     * Sets the string for the cell being rendered to <code>value</code>.
     *
     * @param value  the string value for this cell; if value is
     *		<code>null</code> it sets the text value to an empty string
     * @see JLabel#setText
     *
     */
    protected void setValue(Object value)
    {
        if ( value instanceof ExpiryDate )
        {
            ExpiryDate date = (ExpiryDate)value;
            if ( date.isExpiringEndOfSession() )
            {
                setText( Localizer.getString( "Session" ) );
            }
            else if ( date.isExpiringNever() )
            {
                setText( Localizer.getString( "Never" ) );
            }
            else
            {
                setText( format.format( date ) );
            }
        }
        else if ( value instanceof Date )
        {
            Date date = (Date)value;
            if ( date.getTime() == 0 )
            {
                setText( "" );
            }
            else
            {
                setText( format.format( date ) );
            }
        }
    }
}
