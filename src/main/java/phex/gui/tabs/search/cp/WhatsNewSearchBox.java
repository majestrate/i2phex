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
 *  Created on 05.01.2006
 *  --- CVS Information ---
 *  $Id: WhatsNewSearchBox.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.tabs.search.cp;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import phex.gui.common.BoxPanel;
import phex.gui.common.GUIRegistry;
import phex.gui.common.GUIUtils;
import phex.query.WhatsNewSearch;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class WhatsNewSearchBox extends BoxPanel
{
    private SearchControlPanel controlPanel;
    
    private JButton whatsNewButton;
    private JButton whatsNewStopButton;
    
    public WhatsNewSearchBox( SearchControlPanel cp )
    {
        super( Localizer.getString( "SearchTab_WhatsNew" ) );
        controlPanel = cp;
        
        CellConstraints cc = new CellConstraints();
        FormLayout searchBoxLayout = new FormLayout(
            "6dlu, p, 6dlu", // columns
            "4dlu, p:grow, 4dlu" ); // rows
        PanelBuilder searchBoxBuilder = new PanelBuilder( searchBoxLayout, 
            getContentPanel() );
        
        whatsNewButton = new JButton( Localizer.getString( "SearchTab_WhatsNew" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Search.Search" ) );
        whatsNewButton.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
        whatsNewButton.setToolTipText( Localizer.getString( "SearchTab_TTTWhatsNew") );
        whatsNewButton.setMargin( GUIUtils.NARROW_BUTTON_INSETS );
        whatsNewButton.addActionListener( new WhatsNewSearchHandler() );
        
        StopSearchHandler stopSearchHandler = new StopSearchHandler();
        whatsNewStopButton = new JButton( Localizer.getString( "SearchTab_StopSearch" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Search.Stop" ) );
        whatsNewStopButton.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
        whatsNewStopButton.setToolTipText( Localizer.getString( "SearchTab_TTTStopSearch") );
        whatsNewStopButton.setMargin( GUIUtils.NARROW_BUTTON_INSETS );        
        whatsNewStopButton.addActionListener( stopSearchHandler );
        
        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.setLeftToRightButtonOrder(true);
        builder.addFixedNarrow( whatsNewButton );
        builder.addRelatedGap();
        builder.addFixedNarrow( whatsNewStopButton );
        builder.addGlue();
        JPanel btnBar = builder.getPanel();
        btnBar.setOpaque(false);
        searchBoxBuilder.add( btnBar, cc.xy(2, 2) );
        
        adjustComponents();
    }
    
    /**
     */
    private void adjustComponents()
    {
        if ( whatsNewButton != null )
        {
            String orgText = whatsNewButton.getText();
            whatsNewButton.setText( Localizer.getString( "SearchTab_WhatsNew" ) );
            Dimension dim = whatsNewButton.getPreferredSize();
            whatsNewButton.setText( Localizer.getString( "SearchTab_Searching" ) );
            Dimension dim2 = whatsNewButton.getPreferredSize();
            dim.width = Math.max(dim.width, dim2.width);
            whatsNewButton.setPreferredSize(dim);
            whatsNewButton.setText( orgText );
        }
    }
    
    public void focusInputField()
    {
        whatsNewButton.requestFocus();
    }
    
    /**
     * This is overloaded to update the combo box size on
     * every UI update. Like font size change!
     */
    public void updateUI()
    {
        super.updateUI();
        adjustComponents();
    }

    public void updateControlPanel( WhatsNewSearch search )
    {
        if ( search != null )
        {
            if ( search.isSearching() )
            {
                whatsNewButton.setText( Localizer.getString( "SearchTab_Searching" ) );
                whatsNewButton.setToolTipText( Localizer.getString( "SearchTab_TTTSearching" ) );
                whatsNewButton.setEnabled(false);
            }
            else
            {
                whatsNewButton.setText( Localizer.getString( "SearchTab_WhatsNew" ) );
                whatsNewButton.setToolTipText( Localizer.getString( "SearchTab_TTTWhatsNew") );
                whatsNewButton.setEnabled(true);
            }
        }
        else
        {// this is the case for a new search.
            whatsNewButton.setText( Localizer.getString( "SearchTab_WhatsNew" ) );
            whatsNewButton.setToolTipText( Localizer.getString( "SearchTab_TTTWhatsNew") );
            whatsNewButton.setEnabled(true);            
        }
    }
    
    /**
     * Submits a whats new search.
     */
    private class WhatsNewSearchHandler extends AbstractAction implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            controlPanel.startWhatsNewSearch();
        }
    }
    
    private class StopSearchHandler extends AbstractAction implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            controlPanel.stopSearching();
        }
    }
}
