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
 *  $Id: GUIUpdateNotificationListener.java 4363 2009-01-16 10:37:30Z gregork $
 */
package phex.gui.common;

import phex.event.UpdateNotificationListener;
import phex.gui.dialogs.UpdateNotificationDialog;
import phex.update.UpdateCheckRunner;

public class GUIUpdateNotificationListener
    implements UpdateNotificationListener
{
    public void updateNotification( UpdateCheckRunner updateChecker )
    {
        UpdateNotificationDialog dialog = new UpdateNotificationDialog(
            updateChecker );
        dialog.setVisible( true );
    }
}