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
 *  Created on 19.07.2005
 *  --- CVS Information ---
 *  $Id: FWElegantPanel.java 4363 2009-01-16 10:37:30Z gregork $
 */
package phex.gui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class FWElegantPanel extends JPanel
{
    private JLabel titleLabel;
    private JPanel headerPanel;

    public FWElegantPanel( String title )
    {
        this( title, null );
    }

    public FWElegantPanel( String title, Component container )
    {
        super( new BorderLayout() );
        headerPanel = new ElegantHeaderPanel( PhexColors.getBoxHeaderGradientFrom(), 
            PhexColors.getBoxHeaderGradientTo() );
        headerPanel.add( getTitleLabel(), BorderLayout.CENTER );
        
//        JButton button = new JButton(  );
//        button.setOpaque( false );
//        button.setBorder( new EmptyBorder( 2, 2, 2, 2 ) );
//        button.setText( "X" );
//        top.add( button, BorderLayout.EAST );
        add( headerPanel, BorderLayout.NORTH );
        if ( container != null )
        {
            add( container, BorderLayout.CENTER );
        }
        setTitle( title );
    }

    public String getTitle()
    {
        return getTitleLabel().getText();
    }

    private JLabel getTitleLabel()
    {
        if ( titleLabel != null ) return titleLabel;

        titleLabel = new JLabel();
        Font currentFont = titleLabel.getFont();
        titleLabel.setFont( currentFont.deriveFont( Font.BOLD, currentFont.getSize() + 2 ) );
        titleLabel.setBorder( new EmptyBorder( 6, 6, 6, 4 ) );
        return titleLabel;
    }

    public void setTitle( String title )
    {
        if ( title == null ) title = "";
        title = title.trim();
        getTitleLabel().setText( title );
    }
    
    public void addHeaderPanelComponent( Component comp, String constrain )
    {
        headerPanel.add(comp, constrain );
    }
    
    private static class ElegantHeaderPanel extends GradientPanel
    {
        public ElegantHeaderPanel( Color from, Color to )
        {
            super( from, to );
            setLayout(new BorderLayout());
        }
        
        private static Rectangle viewRect = new Rectangle();
        
        protected void paintComponent( Graphics g )
        {
            super.paintComponent( g );
            Insets in = getInsets();
            
            g.setColor( getBackground() );
            g.fillRect( 0, 0, getWidth(), getHeight() );
            
            Insets i = getInsets();
            viewRect.x = i.left;
            viewRect.y = i.top;
            viewRect.width = getWidth() - (i.right + viewRect.x) - 1;
            viewRect.height = getHeight() - (i.bottom + viewRect.y);
            
            // paint gradient
            Graphics2D g2 = (Graphics2D)g;
            Paint gradient = new GradientPaint(
                0, 0, fromColor,
                0, viewRect.height, toColor );
            g2.setPaint( gradient );
            g2.fillRect( viewRect.x, viewRect.y,
                viewRect.width, viewRect.height );

            g.setColor( PhexColors.getBoxPanelBorderColor() );
            g.drawLine( in.left, in.top, viewRect.width, in.top );
            g.drawLine( in.left, in.top, in.left, viewRect.height );
            g.drawLine( viewRect.width, in.top, viewRect.width, viewRect.height );
        }
    }
}