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
 *  $Id: BanneredDialog.java 4064 2007-11-29 22:57:54Z complication $
 */
package phex.gui.common;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public abstract class BanneredDialog extends JDialog
{
    private PanelBuilder panelBuilder;
    
    private DialogBanner dialogBanner;
    
    public BanneredDialog(Dialog owner, String dialogTitle, boolean modal ) 
        throws HeadlessException
    {
        this( owner, dialogTitle, modal, "", "" );
    }
    
    public BanneredDialog(Frame owner, String dialogTitle, boolean modal ) 
        throws HeadlessException
    {
        this( owner, dialogTitle, modal, "", "" );
    }
    
    public BanneredDialog(Dialog owner, String dialogTitle, boolean modal,
        String bannerHeader, String bannerSubHeader ) 
        throws HeadlessException
    {
        super( owner, dialogTitle, modal );
        initComponents(bannerHeader, bannerSubHeader);
    }

    public BanneredDialog(Frame owner, String dialogTitle, boolean modal,
        String bannerHeader, String bannerSubHeader ) 
        throws HeadlessException
    {
        super( owner, dialogTitle, modal );
        initComponents(bannerHeader, bannerSubHeader);
    }
    
    public void setBannerHeaderText( String header )
    {
        dialogBanner.setHeaderText( header );
    }
    
    protected abstract JPanel createDialogContentPanel();
    
    protected abstract JPanel createDialogButtonPanel();
    
    private void initComponents( String bannerHeader, String bannerSubHeader )
    {
        dialogBanner = new DialogBanner( bannerHeader, bannerSubHeader );
        layoutComponents();
    }
    
    private void layoutComponents()
    {
        Container superContentPane = super.getContentPane();
        JPanel mainPanel = new JPanel();
        superContentPane.add( mainPanel );
        
        ColumnSpec[] colSpecs =
        {
            FormFactory.UNRELATED_GAP_COLSPEC,
            new ColumnSpec( "fill:d:grow" ),
            FormFactory.RELATED_GAP_COLSPEC
        };
        FormLayout layout = new FormLayout( colSpecs, new RowSpec[]{} );
        panelBuilder = new PanelBuilder( layout, mainPanel );
        
        panelBuilder.setColumnSpan( 3 );
        
        panelBuilder.appendRow( FormFactory.PREF_ROWSPEC );
        panelBuilder.add( dialogBanner );
        
        panelBuilder.appendRow( FormFactory.PREF_ROWSPEC );
        panelBuilder.nextRow();
        panelBuilder.add( new JSeparator() );
        
        panelBuilder.appendRow( FormFactory.UNRELATED_GAP_ROWSPEC );
        
        panelBuilder.appendRow( FormFactory.PREF_ROWSPEC );
        panelBuilder.nextRow( 2 );
        panelBuilder.setColumnSpan( 1 );
        panelBuilder.setColumn( 2 );
        panelBuilder.add( createDialogContentPanel() );
        
        panelBuilder.appendRow( FormFactory.UNRELATED_GAP_ROWSPEC );
        panelBuilder.nextRow();
        
        panelBuilder.appendRow( FormFactory.UNRELATED_GAP_ROWSPEC );
        panelBuilder.nextRow();
        panelBuilder.setColumn( 1 );
        panelBuilder.setColumnSpan( 3 );
        panelBuilder.add( new JSeparator() );
        
        
        panelBuilder.appendRow( FormFactory.PREF_ROWSPEC );
        panelBuilder.nextRow();
        panelBuilder.setColumnSpan( 1 );
        panelBuilder.setColumn( 2 );
        panelBuilder.add( createDialogButtonPanel() );
        
        panelBuilder.appendRow( FormFactory.UNRELATED_GAP_ROWSPEC );
    }
}
