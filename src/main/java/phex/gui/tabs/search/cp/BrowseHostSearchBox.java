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
 *  Created on 05.01.2006
 *  --- CVS Information ---
 *  $Id: BrowseHostSearchBox.java 3612 2006-11-17 15:58:21Z gregork $
 */
package phex.gui.tabs.search.cp;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.text.Keymap;

import phex.common.address.DestAddress;
import phex.gui.common.BoxPanel;
import phex.gui.common.GUIRegistry;
import phex.gui.common.GUIUtils;
import phex.gui.prefs.PhexGuiPrefs;
import phex.gui.prefs.SearchTabPrefs;
import phex.query.BrowseHostResults;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class BrowseHostSearchBox extends BoxPanel
{
    private SearchControlPanel controlPanel;
    
    private DefaultComboBoxModel hostNameComboModel;
    private JComboBox hostNameComboBox;
    private JButton browseButton;
    private JButton stopButton;
    
    public BrowseHostSearchBox( SearchControlPanel cp )
    {
        super( Localizer.getString( "SearchTab_BrowseHost" ) );
        controlPanel = cp;
        
        CellConstraints cc = new CellConstraints();
        FormLayout searchBoxLayout = new FormLayout(
            "6dlu, p, 6dlu", // columns
            "4dlu, p, 2dlu, p, 6dlu, p, 4dlu" ); // rows
        PanelBuilder searchBoxBuilder = new PanelBuilder( searchBoxLayout, 
            getContentPanel() );
        
        searchBoxBuilder.addLabel( Localizer.getString( "SearchTab_TypeHostAddress" ),
            cc.xy(2, 2) );
        
        SubmitSearchHandler submitSearchHandler = new SubmitSearchHandler();
        
        hostNameComboModel = new DefaultComboBoxModel(
            SearchTabPrefs.BrowseHostHistory.get().toArray() );
        hostNameComboBox = new JComboBox( hostNameComboModel );
        hostNameComboBox.setEditable( true );
        JTextField editor = ((JTextField)hostNameComboBox.getEditor().getEditorComponent());
        Keymap keymap = JTextField.addKeymap( "SearchTermEditor", editor.getKeymap() );
        editor.setKeymap( keymap );
        keymap.addActionForKeyStroke( KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            submitSearchHandler );
        GUIUtils.assignKeymapToComboBoxEditor( keymap, hostNameComboBox );
        hostNameComboBox.setSelectedItem( "" );
        searchBoxBuilder.add( hostNameComboBox, cc.xy(2, 4) );
        
        browseButton = new JButton( Localizer.getString( "SearchTab_StartBrowseHost" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Search.Search" ) );
        browseButton.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
        browseButton.setToolTipText( Localizer.getString( "SearchTab_TTTStartBrowseHost") );
        browseButton.setMargin( GUIUtils.NARROW_BUTTON_INSETS );
        browseButton.addActionListener( submitSearchHandler );
        
        StopSearchHandler stopSearchHandler = new StopSearchHandler();
        stopButton = new JButton( Localizer.getString( "SearchTab_StopSearch" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Search.Stop" ) );
        stopButton.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
        stopButton.setToolTipText( Localizer.getString( "SearchTab_TTTStopSearch") );
        stopButton.setMargin( GUIUtils.NARROW_BUTTON_INSETS );        
        stopButton.addActionListener( stopSearchHandler );
        
        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.setLeftToRightButtonOrder(true);
        builder.addFixedNarrow( browseButton );
        builder.addRelatedGap();
        builder.addFixedNarrow( stopButton );
        builder.addGlue();
        JPanel btnBar = builder.getPanel();
        btnBar.setOpaque(false);
        searchBoxBuilder.add( btnBar, cc.xy(2, 6) );
        
        adjustComponents();
    }
    
    private void adjustComponents()
    {
        if ( hostNameComboBox != null )
        {
            GUIUtils.adjustComboBoxHeight( hostNameComboBox );
            // adjust combobox width
            ListCellRenderer renderer = hostNameComboBox.getRenderer();
            if ( renderer != null )
            {
                FontMetrics fm = hostNameComboBox.getFontMetrics( hostNameComboBox.getFont() );
                int maxWidth = fm.getMaxAdvance() * 10;
                int minWidth = fm.getMaxAdvance() * 8;
                Dimension dim = hostNameComboBox.getMaximumSize();
                dim.width = Math.max( minWidth, Math.min( maxWidth, dim.width ) );
                hostNameComboBox.setMaximumSize( dim );
                dim = hostNameComboBox.getPreferredSize();
                dim.width = Math.max( minWidth, Math.min( maxWidth, dim.width ) );
                hostNameComboBox.setPreferredSize( dim );
            }
        }
        
        if ( browseButton != null )
        {
            String orgText = browseButton.getText();
            browseButton.setText( Localizer.getString( "SearchTab_BrowseHost" ) );
            Dimension dim = browseButton.getPreferredSize();
            browseButton.setText( Localizer.getString( "SearchTab_Searching" ) );
            Dimension dim2 = browseButton.getPreferredSize();
            dim.width = Math.max(dim.width, dim2.width);
            browseButton.setPreferredSize(dim);
            browseButton.setText( orgText );
        }
    }
    
    /**
     * Clears the search history in the search control panel and configuration.
     */
    public void clearBrowseHostHistory()
    {
        hostNameComboModel.removeAllElements();
        SearchTabPrefs.BrowseHostHistory.get().clear();
        SearchTabPrefs.BrowseHostHistory.changed();
        PhexGuiPrefs.save( false );
    }
    
    public void focusInputField()
    {
        hostNameComboBox.requestFocus();
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
    
    public void updateControlPanel( BrowseHostResults search)
    {
        if ( search != null )
        {
            DestAddress destAddress = ((BrowseHostResults)search).getDestAddress();
            hostNameComboBox.setSelectedItem( destAddress.getFullHostName() );
            ((JTextField)hostNameComboBox.getEditor().getEditorComponent()).setText(
                destAddress.getFullHostName() );
            
            if ( search.isSearching() )
            {
                browseButton.setText( Localizer.getString( "SearchTab_Searching" ) );
                browseButton.setToolTipText( Localizer.getString( "SearchTab_TTTSearching" ) );
                browseButton.setEnabled( false );
                hostNameComboBox.setEnabled( false );
            }
            else
            {
                browseButton.setText( Localizer.getString( "SearchTab_BrowseHost" ) );
                browseButton.setToolTipText( Localizer.getString( "SearchTab_TTTBrowseHost") );
                browseButton.setEnabled( true );
                hostNameComboBox.setEnabled( true );
            }
        }
        else
        {// this is the case for a new search.
            hostNameComboBox.setSelectedItem( null );
            ((JTextField)hostNameComboBox.getEditor().getEditorComponent()).setText( "" );
            browseButton.setText( Localizer.getString( "SearchTab_BrowseHost" ) );
            browseButton.setToolTipText( Localizer.getString( "SearchTab_TTTBrowseHost") );
            browseButton.setEnabled(true);
            hostNameComboBox.setEnabled( true );
        }
    }
    
    /**
     * Submits a new search.
     */
    private class SubmitSearchHandler extends AbstractAction implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            String hostName = (String)hostNameComboBox.getEditor().getItem();
            hostNameComboModel.setSelectedItem( hostName );
            hostName = hostName.trim();
            if ( hostName.length() == 0 )
            {
                return;
            }
            
            boolean succ = controlPanel.startBrowseHost( hostName );
            if ( !succ )
            {
                return;
            }
            
            int idx = hostNameComboModel.getIndexOf( hostName );
            if ( idx < 0 )
            {
                hostNameComboModel.insertElementAt( hostName, 0 );
                if ( hostNameComboModel.getSize() >
                    SearchTabPrefs.MaxSearchHistorySize.get().intValue() )
                {
                    hostNameComboModel.removeElementAt(
                        hostNameComboModel.getSize() - 1 );
                }
                saveSearchList();
            }
            else if ( idx > 0 )
            {
                hostNameComboModel.removeElementAt( idx );
                hostNameComboModel.insertElementAt( hostName, 0 );
                saveSearchList();
            }
        }

        private void saveSearchList()
        {
            int length = hostNameComboModel.getSize();
            ArrayList searchList = new ArrayList( length );
            for ( int i = 0; i < length; i++ )
            {
                searchList.add( hostNameComboModel.getElementAt( i ) );
            }
            SearchTabPrefs.BrowseHostHistory.get().clear();
            SearchTabPrefs.BrowseHostHistory.get().addAll( searchList );
            SearchTabPrefs.BrowseHostHistory.changed();
            PhexGuiPrefs.save( false );
        }
    }
    
    /**
     * Submits a new search.
     */
    private class StopSearchHandler extends AbstractAction implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            controlPanel.stopSearching();
        }
    }
}
