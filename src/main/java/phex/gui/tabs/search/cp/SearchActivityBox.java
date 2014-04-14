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
 *  Created on 09.01.2006
 *  --- CVS Information ---
 *  $Id: SearchActivityBox.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.gui.tabs.search.cp;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;

import phex.gui.common.*;
import phex.gui.tabs.search.SearchTab;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SearchActivityBox extends BoxPanel
{
    private static Border ROLLOVER_BUTTON_BORDER = new CompoundBorder( 
        GUIUtils.ROLLOVER_BUTTON_BORDER, new EmptyBorder( 2, 2, 2, 2) );
    private static ButtonUI ACTIVITY_BUTTON_UI = new ActivityButtonUI();
    
    private JPanel newSearchActivityP;
    private JToggleButton keywordSearchBtn;
    private JToggleButton whatsNewBtn;
    private JToggleButton browseHostBtn;
    
    private JPanel runningSearchActivityP;
    private JButton newSearchBtn;
    private JButton closeSearchBtn;
    
    public SearchActivityBox( SearchTab searchTab, final SearchControlPanel cp )
    {
        super( Localizer.getString( "SearchTab_SearchActivity" ) );
        
        CellConstraints cc = new CellConstraints();
        getContentPanel().setLayout( new BorderLayout() );
        
        newSearchActivityP = new JPanel();
        newSearchActivityP.setOpaque(false);
        
        getContentPanel().add( newSearchActivityP, BorderLayout.CENTER );
        FormLayout newSearchLayout = new FormLayout(
            "6dlu, fill:p:grow, 6dlu", // columns
            "2dlu, p, 1dlu, p, 1dlu, p, 2dlu" ); // rows
        PanelBuilder newSearchBuilder = new PanelBuilder( newSearchLayout, 
            newSearchActivityP );
        
        keywordSearchBtn = new JToggleButton( 
            Localizer.getString( "SearchTab_KeywordSearch" ), 
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Search.Search" ) );
        keywordSearchBtn.setToolTipText( Localizer.getString( "SearchTab_TTTKeywordSearch") );
        updateActivityBtnProps( keywordSearchBtn );
        keywordSearchBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    cp.activateKeywordSearchBox( );
                }
            });
        
        whatsNewBtn = new JToggleButton( 
            Localizer.getString( "SearchTab_WhatsNew" ), 
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Search.WhatsNewSearch" ) );
        whatsNewBtn.setToolTipText( Localizer.getString( "SearchTab_TTTWhatsNew") );
        updateActivityBtnProps( whatsNewBtn );
        whatsNewBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    cp.activateWhatsNewBox( );
                }
            });
        
        browseHostBtn = new JToggleButton( 
            Localizer.getString( "SearchTab_BrowseHost" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Search.BrowseHost") );
        browseHostBtn.setToolTipText( Localizer.getString( "SearchTab_TTTBrowseHost") );
        updateActivityBtnProps( browseHostBtn );
        browseHostBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    cp.activateBrowseHostBox( );
                }
            });
        
        ButtonGroup group = new ButtonGroup();
        group.add(keywordSearchBtn);
        group.add(whatsNewBtn);
        group.add(browseHostBtn);
        
        newSearchBuilder.add( keywordSearchBtn, cc.xy(2, 2) );
        newSearchBuilder.add( whatsNewBtn, cc.xy(2, 4) );
        newSearchBuilder.add( browseHostBtn, cc.xy(2, 6) );
        
        runningSearchActivityP = new JPanel();
        runningSearchActivityP.setOpaque(false);
        FormLayout runningLayout = new FormLayout(
            "6dlu, fill:p:grow, 6dlu", // columns
            "2dlu, p, 1dlu, p, 2dlu" ); // rows
        PanelBuilder runningBuilder = new PanelBuilder( runningLayout, 
            runningSearchActivityP );
        
        newSearchBtn = new JButton( Localizer.getString( "SearchTab_NewSearch" ),
                    GUIRegistry.getInstance().getPlafIconPack().getIcon( "Search.Search") );
        newSearchBtn.setToolTipText( Localizer.getString( "SearchTab_TTTNewSearch") );
        newSearchBtn.addActionListener( 
            searchTab.getTabAction( SearchTab.CREATE_NEW_SEARCH_ACTION ) );
        updateActivityBtnProps( newSearchBtn );
        
        closeSearchBtn = new JButton( Localizer.getString( "SearchTab_CloseSearch" ), 
                    GUIRegistry.getInstance().getPlafIconPack().getIcon( "Search.Close") );
        closeSearchBtn.setToolTipText( Localizer.getString( "SearchTab_TTTCloseSearch") );
        closeSearchBtn.addActionListener( 
            searchTab.getTabAction( SearchTab.CLOSE_SEARCH_ACTION ) );
        updateActivityBtnProps( closeSearchBtn );
                
        runningBuilder.add( newSearchBtn, cc.xy(2, 2) );
        runningBuilder.add( closeSearchBtn, cc.xy(2, 4) );
    }
    
    public void postInit()
    {
        keywordSearchBtn.doClick();
    }

    public void displayRunningSearchPanel()
    {
        JPanel activityContentP = getContentPanel();
        activityContentP.removeAll();
        activityContentP.add( runningSearchActivityP, BorderLayout.CENTER );
        activityContentP.doLayout();
        activityContentP.revalidate();
        activityContentP.repaint();
        
        newSearchBtn.getModel().setRollover(false);
        closeSearchBtn.getModel().setRollover(false);
    }

    public void displayNewSearchPanel()
    {
        JPanel activityContentP = getContentPanel();
        activityContentP.removeAll();
        activityContentP.add( newSearchActivityP, BorderLayout.CENTER );
        activityContentP.doLayout();
        activityContentP.revalidate();
        activityContentP.repaint();
        keywordSearchBtn.doClick();
    }
    
    private void updateActivityBtnProps( AbstractButton b )
    {
        b.setUI( ACTIVITY_BUTTON_UI );
        b.setIconTextGap( 8 );
        b.setHorizontalAlignment( SwingConstants.LEFT );
        b.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
        b.setBorder( ROLLOVER_BUTTON_BORDER );
        b.setRolloverEnabled( true );
    }
    
    public void updateUI()
    {
        super.updateUI();
        if ( keywordSearchBtn != null )
        {
            keywordSearchBtn.setUI( ACTIVITY_BUTTON_UI );
        }
        if ( whatsNewBtn != null )
        {
            whatsNewBtn.setUI( ACTIVITY_BUTTON_UI );
        }
        if ( browseHostBtn != null )
        {
            browseHostBtn.setUI( ACTIVITY_BUTTON_UI );
        }
    }
    
    private static class ActivityButtonUI extends BasicButtonUI
    {
        private Color fromColor = PhexColors.getBoxPanelBackground().darker();
        private Color toColor = PhexColors.getBoxPanelBackground().brighter();

        public void update( Graphics g, JComponent c )
        {
            if ( c.isOpaque() )
            {
                g.setColor( PhexColors.getBoxPanelBackground() );                    
                int width = c.getWidth();
                int height = c.getHeight();
                g.fillRect( 0, 0, width, height );
                
                AbstractButton b = (AbstractButton) c;
                if ( b.isSelected() )
                {                      
                    Paint gradient = new GradientPaint( 0, 0, fromColor,
                        width/2f, height/2f, toColor, true );                    
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setPaint( gradient );
                    g2.fillRect( 0, 0, width, height );
                }
            }
            paint( g, c );
        }
    }
}
