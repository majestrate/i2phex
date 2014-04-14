/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  $Id: SecurityRuleRowRenderer.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.security;

import java.awt.*;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import phex.gui.common.GUIRegistry;
import phex.gui.common.table.FWTable;
import phex.security.*;
import phex.servent.Servent;

public class SecurityRuleRowRenderer implements TableCellRenderer
{
    private static final Color darkGreen = new Color( 0x00, 0x7F, 0x00 );
    
    private PhexSecurityManager securityMgr;

    public SecurityRuleRowRenderer()
    {
        Servent servent = GUIRegistry.getInstance().getServent();
        securityMgr = servent.getSecurityService();
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
    {
        TableCellRenderer renderer = table.getDefaultRenderer(
            table.getColumnClass( column ) );
        Component comp = renderer.getTableCellRendererComponent( table, value,
            isSelected, hasFocus, row, column );
        FWTable fwTable = (FWTable) table;
        int modelRow = fwTable.translateRowIndexToModel( row );
        SecurityRule rule = securityMgr.getIPAccessRule( modelRow );
        comp.setForeground( table.getSelectionForeground() );
        if ( rule == null || isSelected )
        {
            return comp;
        }
        if ( rule.isDisabled() )
        {
            comp.setForeground( Color.lightGray );
            return comp;
        }
        if ( rule.isSystemRule() )
        {
            comp.setForeground( Color.gray );
            return comp;
        }

        if ( rule.isDenyingRule() )
        {
            comp.setForeground( Color.red );
        }
        else
        {
            comp.setForeground( darkGreen );
        }
        return comp;
    }
}