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
 *  Created on 22.12.2006
 *  --- SVN Information ---
 *  $Id: SharingPanel.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex.gui.dialogs.configwizard;

import javax.swing.JPanel;

import phex.gui.common.HTMLMultiLinePanel;
import phex.gui.tabs.library.LibraryTreePane;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SharingPanel extends JPanel
{
    private ConfigurationWizardDialog parent;
    
    public SharingPanel( ConfigurationWizardDialog parent )
    {
        this.parent = parent;
        prepareComponent();
    }
    
    private void prepareComponent()
    {
        FormLayout layout = new FormLayout(
            "10dlu, d, 2dlu, d, right:d:grow", // columns
            "p, 3dlu, p, 8dlu, fill:p:grow" );// rows 
        
        setLayout( layout );
        
        PanelBuilder builder = new PanelBuilder( layout, this );
        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator( Localizer.getString( "ConfigWizard_SharingHeader" ),
            cc.xywh( 1, 1, 5, 1 ) );
        
        HTMLMultiLinePanel welcomeLines = new HTMLMultiLinePanel(
            Localizer.getString( "ConfigWizard_SharingText" ) );        
        welcomeLines.setBorder( null );
        builder.add( welcomeLines, cc.xywh( 2, 3, 4, 1 ) );
        
        LibraryTreePane libraryTree = new LibraryTreePane( this );
        builder.add( libraryTree, cc.xywh(2, 5, 4, 1) );
    }
}
