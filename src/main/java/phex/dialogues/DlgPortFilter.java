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
 *  $Id: DlgPortFilter.java 4322 2008-12-11 10:34:18Z ArneBab $
 */
package phex.dialogues;

import java.util.Iterator;

import phex.gui.common.MainFrame;
import phex.utils.Localizer;

public class DlgPortFilter extends DlgBase
{
  public DlgPortFilter(MainFrame frame, String filterPort, Iterator ports)
  {
      super(frame, filterPort, ports);
      description.setText(Localizer.getString( "FilteredPorts_Title" ));
      textDescription.setText(Localizer.getString("FilteredPorts_FilterPort"));
      listDescription.setText(Localizer.getString("FilteredPorts_FilteredPorts"));
  }
}