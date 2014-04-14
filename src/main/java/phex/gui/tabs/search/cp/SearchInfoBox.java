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
 *  Created on 31.01.2006
 *  --- CVS Information ---
 *  $Id: SearchInfoBox.java 4064 2007-11-29 22:57:54Z complication $
 */
package phex.gui.tabs.search.cp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import phex.common.log.NLogger;
import phex.gui.common.BoxPanel;
import phex.gui.common.GUIRegistry;
import phex.gui.tabs.search.SearchResultsDataModel;
import phex.gui.tabs.search.SearchVisualizer;
import phex.query.Search;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SearchInfoBox extends BoxPanel
{
    private final UpdateDisplayActionListener updateDisplayListener;
    private SearchControlPanel controlPanel;
    private SearchResultsDataModel displayedDataModel;
    
    private JLabel searchStatusLbl;
    private JProgressBar progressBar;
    private JLabel totalResultsLbl;
    private JLabel displayedResultsLbl;
    private JLabel filteredResultsLbl;
    
        
    public SearchInfoBox( SearchControlPanel cp )
    {
        super( Localizer.getString( "SearchTab_Information" ) );
        controlPanel = cp;
        
        CellConstraints cc = new CellConstraints();
        FormLayout boxLayout = new FormLayout(
            "6dlu, d, 2dlu, fill:p:grow, 6dlu", // columns
            "4dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu," +
            "p, 2dlu, p, 2dlu, p, 2dlu, p, 6dlu" ); // rows
        PanelBuilder searchBoxBuilder = new PanelBuilder( boxLayout, 
            getContentPanel() );
        
        JLabel label = searchBoxBuilder.addLabel( Localizer.getString( "SearchTab_Status" ),
            cc.xy( 2, 2 ) );
        label.setToolTipText( Localizer.getString("SearchTab_TTTStatus") );
        
        searchStatusLbl = new JLabel();
        searchBoxBuilder.add( searchStatusLbl, cc.xy( 4, 2 ) );
        
        progressBar = new JProgressBar( 0, 100 );
        progressBar.setToolTipText( Localizer.getString("SearchTab_TTTProgress") );
        progressBar.setStringPainted(true);
        searchBoxBuilder.add( progressBar, cc.xywh( 2, 4, 3, 1 ) );
        
        label = searchBoxBuilder.addLabel( Localizer.getString( "SearchTab_TotalResults" ),
            cc.xy( 2, 6 ) );
        label.setToolTipText( Localizer.getString("SearchTab_TTTTotalResults") );
        
        totalResultsLbl = new JLabel();
        searchBoxBuilder.add( totalResultsLbl, cc.xy( 4, 6 ) );
        
        label = searchBoxBuilder.addLabel( Localizer.getString( "SearchTab_DisplayedResults" ),
            cc.xy( 2, 8 ) );
        label.setToolTipText( Localizer.getString("SearchTab_TTTDisplayedResults") );
        
        displayedResultsLbl = new JLabel();
        searchBoxBuilder.add( displayedResultsLbl, cc.xy( 4, 8 ) );
        
        label = searchBoxBuilder.addLabel( Localizer.getString( "SearchTab_FilteredResults" ),
            cc.xy( 2, 10 ) );
        label.setToolTipText( Localizer.getString("SearchTab_TTTFilteredResults") );
        
        filteredResultsLbl = new JLabel();
        searchBoxBuilder.add( filteredResultsLbl, cc.xy( 4, 10 ) );
        
        updateDisplayListener = new UpdateDisplayActionListener();
    }
    
    public void setDisplayedSearch( SearchResultsDataModel searchResultsDataModel )
    {
        GUIRegistry.getInstance().getGuiUpdateTimer().removeActionListener( 
            updateDisplayListener );
        
        displayedDataModel = searchResultsDataModel;
        if ( displayedDataModel != null )
        {
            GUIRegistry.getInstance().getGuiUpdateTimer().addActionListener( 
                updateDisplayListener );
        }
        updateDisplay();
    }
    
    private void updateDisplay()
    {
        if ( displayedDataModel != null )
        {
            Search search = displayedDataModel.getSearch();
            searchStatusLbl.setText( SearchVisualizer.visualizeSearchStatus( 
                search ) );
            int total = displayedDataModel.getAllSearchResultCount();
            int filtered = displayedDataModel.getFilteredElementCount();
            progressBar.setValue( search.getProgress() );
            totalResultsLbl.setText( String.valueOf( total ) );
            displayedResultsLbl.setText( String.valueOf( total - filtered ) );
            filteredResultsLbl.setText( String.valueOf( filtered ) );
        }
        else
        {
            searchStatusLbl.setText("");
            progressBar.setValue(0);
            totalResultsLbl.setText("");
            displayedResultsLbl.setText("");
            filteredResultsLbl.setText("");
        }
    }
    
    private final class UpdateDisplayActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                updateDisplay();
            }
            catch ( Throwable th )
            {
                NLogger.error(SearchInfoBox.class, th, th);
            }
        }
    }
}
