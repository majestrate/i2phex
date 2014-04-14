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
 *  Created on 08.02.2006
 *  --- CVS Information ---
 *  $Id: SearchFilterPanel.java 4128 2008-03-01 19:12:47Z complication $
 */
package phex.gui.tabs.search.filterpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import phex.gui.common.GUIRegistry;
import phex.gui.common.GUIUtils;
import phex.gui.common.PhexColors;
import phex.gui.tabs.search.SearchResultsDataModel;
import phex.gui.tabs.search.SearchTab;
import phex.rules.SearchFilterRules;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SearchFilterPanel extends JPanel
{
    private static Border LEFT_BUTTON_BORDER = new CompoundBorder( 
        BorderFactory.createMatteBorder(1, 1, 1, 0, 
            UIManager.getDefaults().getColor("window")), 
            new EmptyBorder( 5, 7, 5, 11) );
    
    private SearchTab searchTab;
    private JPanel topLayoutPanel;
    private JPanel leftLayoutPanel;
    private JPanel filterContentPanel;
    
    private JToggleButton quickFilterBtn;
    private JToggleButton filterListBtn;
    private QuickFilterPanel quickFilterPanel;
    private FilterListPanel filterListPanel;
    
    public SearchFilterPanel(  SearchTab tab, SearchFilterRules filterRules )
    {
        super();
        searchTab = tab;
        
        setBorder( BorderFactory.createLineBorder( 
            PhexColors.getBoxPanelBorderColor() ) );
        
        setBackground( UIManager.getDefaults().getColor("window") );
        
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout(
            "p, fill:p:grow", // columns
            "d, fill:d:grow"); //rows
        PanelBuilder panelBuilder = new PanelBuilder( layout, this );
        
        topLayoutPanel = new JPanel();
        topLayoutPanel.setOpaque(false);
        layout = new FormLayout(
            "4dlu, p, fill:8dlu:grow, p, 4dlu", // columns
            "2dlu, p, 2dlu"); //rows
        PanelBuilder topBuilder = new PanelBuilder( layout, topLayoutPanel );
        
        JLabel titelLabel = new JLabel( Localizer.getString( 
            "SearchTab_SearchFilterOptions" ) );
        Font currentFont = titelLabel.getFont();
        Font enlargedFont = currentFont.deriveFont( Font.BOLD, currentFont.getSize() + 2 );
        titelLabel.setFont( enlargedFont );
        topBuilder.add( titelLabel, cc.xy(2, 2) );
        JButton closeBtn = new JButton( tab.getTabAction(
            SearchTab.FILTER_PANEL_TOGGLE_ACTION) );
        closeBtn.setText(null);
        closeBtn.setIcon( GUIRegistry.getInstance().getPlafIconPack().getIcon(
            "Search.CloseFilterPanel") );
        closeBtn.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
        closeBtn.setBorder( GUIUtils.ROLLOVER_BUTTON_BORDER );
        closeBtn.setRolloverEnabled( true );
        closeBtn.setOpaque(false);
        topBuilder.add( closeBtn, cc.xy(4, 2) );
        
        panelBuilder.add( topLayoutPanel, cc.xywh( 1, 1, 2, 1 ) );
        
        leftLayoutPanel = new JPanel();
        leftLayoutPanel.setBackground(getBackground());
        leftLayoutPanel.setOpaque(false);
        layout = new FormLayout(
            "4dlu, p, 0dlu", // columns
            "23px, d, 0dlu, d, 23px"); //rows
        PanelBuilder leftBuilder = new PanelBuilder( layout, leftLayoutPanel );
        
        ButtonGroup leftBtnGroup = new ButtonGroup();
        quickFilterBtn = new JToggleButton( Localizer.getString( "SearchTab_QuickFilter" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon("Search.QuickFilter") );
        quickFilterBtn.setBorder( new CompoundBorder( 
            BorderFactory.createMatteBorder(2, 2, 1, 0, getBackground()), 
                new EmptyBorder( 5, 7, 5, 11) ) );
        leftBtnGroup.add(quickFilterBtn);
        updateLeftBtnProps(quickFilterBtn);
        leftBuilder.add( quickFilterBtn, cc.xy(2, 2) );
        quickFilterBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    activateQuickFilterPanel( );
                }
            });
        
        filterListBtn = new JToggleButton( Localizer.getString( "SearchTab_FilterRules" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon("Search.FilterList") );
        filterListBtn.setBorder( new CompoundBorder( 
            BorderFactory.createMatteBorder(1, 2, 2, 0, getBackground()), 
                new EmptyBorder( 5, 7, 5, 11) ) );
        leftBtnGroup.add(filterListBtn);
        updateLeftBtnProps(filterListBtn);
        leftBuilder.add( filterListBtn, cc.xy(2, 4) );
        filterListBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    activateFilterListPanel( );
                }
            });
        panelBuilder.add( leftLayoutPanel, cc.xywh( 1, 2, 1, 1 ) );
        
        quickFilterPanel = new QuickFilterPanel();
        filterListPanel = new FilterListPanel( filterRules );
        
        filterContentPanel = new JPanel( new BorderLayout() );
        filterContentPanel.setOpaque(false);
        panelBuilder.add( filterContentPanel, cc.xywh( 2, 2, 1, 1 ) );
        
        quickFilterBtn.doClick();
    }
    
    public void setDisplayedSearch( SearchResultsDataModel searchResultsDataModel )
    {
        quickFilterPanel.setDisplayedSearch( searchResultsDataModel );
        filterListPanel.setDisplayedSearch( searchResultsDataModel );
    }
    
    public void activateQuickFilterPanel()
    {
        filterContentPanel.removeAll();
        filterContentPanel.add( quickFilterPanel, BorderLayout.CENTER );
        filterContentPanel.doLayout();
        filterContentPanel.revalidate();
        filterContentPanel.repaint();
    }
    
    public void activateFilterListPanel()
    {
        filterContentPanel.removeAll();
        filterContentPanel.add( filterListPanel, BorderLayout.CENTER );
        filterContentPanel.doLayout();
        filterContentPanel.revalidate();
        filterContentPanel.repaint();
    }
    
    private void updateLeftBtnProps( AbstractButton b )
    {
        b.setUI( new LeftButtonUI() );
        b.setIconTextGap( 8 );
        b.setHorizontalAlignment( SwingConstants.LEFT );
        b.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
        b.setRolloverEnabled( true );
        b.setOpaque(false);
    }
    
    // buffered to prevent recreation
    private Rectangle insideBorderBounds = new Rectangle();
    private Rectangle clipBounds = new Rectangle();

    @Override
    public void paintComponent( Graphics g )
    {
        super.paintComponent(g);
        
        g.getClipBounds( clipBounds );
        // first paint bg if opaque
        if ( isOpaque() )
        {
            g.setColor( getBackground() );
            g.fillRect( clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height );
        }
        
        // honor possible insets for border
        Insets insets = getInsets();
        int w = getWidth() - insets.left - insets.right;
        int h = getHeight()- insets.top - insets.bottom;
        Graphics2D g2 = (Graphics2D) g.create(insets.left, insets.top, w, h);
        try
        {
            insideBorderBounds.setBounds(0, 0, w, h);
            paintInsideBorder( g2 );
        } 
        finally
        {
            g2.dispose();
        }
    }
    
    private void paintInsideBorder( Graphics2D g2 )
    {
        Rectangle topBounds = topLayoutPanel.getBounds();
        int topHeight = topBounds.height;
        
        Rectangle leftBounds = leftLayoutPanel.getBounds();
        int leftWidth = leftBounds.width;
        
        g2.setColor( PhexColors.getBoxPanelBackground() );
        g2.fillRect(0, 0, insideBorderBounds.width, topHeight*2 );
        g2.fillRect(0, topHeight*2, leftWidth, insideBorderBounds.height );
        g2.setColor( getBackground() );
        g2.fillRoundRect(leftWidth, topHeight, 
            insideBorderBounds.width, insideBorderBounds.height, 40, 40 );
    }
    
    private static class LeftButtonUI extends BasicButtonUI
    {
        private Color rolloverColor = PhexColors.getBoxPanelBackground().darker();

        @Override
        public void update( Graphics g, JComponent c )
        {
            int width = c.getWidth();
            int height = c.getHeight();
            if ( c.isOpaque() )
            {
                g.setColor( PhexColors.getBoxPanelBackground() );
                g.fillRect( 0, 0, width, height );
            }
            AbstractButton b = (AbstractButton) c;
            if ( b.isSelected() )
            {                      
                g.setColor( c.getParent().getBackground() );
                g.fillRect( 0, 0, width, height );
            }
            else if ( b.getModel().isRollover() )
            {
                g.setColor( rolloverColor );
                g.fillRect( 0, 0, width, height );
            }
            paint( g, c );
        }
    }
}
