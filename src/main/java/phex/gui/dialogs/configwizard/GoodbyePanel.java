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
 *  $Id: GoodbyePanel.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex.gui.dialogs.configwizard;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import phex.gui.common.HTMLMultiLinePanel;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class GoodbyePanel extends JPanel
{
    private ConfigurationWizardDialog parent;
    private JCheckBox openOptions;
    
    public GoodbyePanel( ConfigurationWizardDialog parent )
    {
        this.parent = parent;
        prepareComponent();
    }
    
    private void prepareComponent()
    {
        FormLayout layout = new FormLayout(
            "10dlu, 200dlu:grow, 2dlu", // columns
            "p, 3dlu, top:p, 16dlu, p" );// rows 
        
        setLayout( layout );
        
        PanelBuilder builder = new PanelBuilder( layout, this );
        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator( Localizer.getString( "ConfigWizard_WelcomeToPhexHeader" ),
            cc.xywh( 1, 1, 3, 1 ) );
        
        HTMLMultiLinePanel welcomeLines = new HTMLMultiLinePanel(
            Localizer.getString( "ConfigWizard_GoodbyeText" ) );
        builder.add( welcomeLines, cc.xy( 2, 3 ) );
        
        openOptions = new JCheckBox( Localizer.getString( "ConfigWizard_OpenOptions" ) );
        builder.add( openOptions, cc.xy( 2, 5 ) );
    }
    
    public boolean isOpenOptionsSelected()
    {
        return openOptions.isSelected();
    }
}
