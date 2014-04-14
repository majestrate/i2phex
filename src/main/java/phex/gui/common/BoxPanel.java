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
 *  $Id: BoxPanel.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import phex.common.log.NLogger;

public class BoxPanel extends JPanel
{
    private BoxHeader boxHeader;
    private JPanel contentPanel;
    private boolean isContentShown;
    
    /**
     * 
     */
    public BoxPanel( String headerText )
    {
        super( new BorderLayout() );
        isContentShown = true;
        boxHeader = new BoxHeader( headerText, this );
        add( boxHeader, BorderLayout.NORTH );
        contentPanel = new JPanel( );
        add( contentPanel, BorderLayout.CENTER );
        initialize();
    }
    
    private void initialize()
    {
        UIDefaults defaults = UIManager.getDefaults();
        if ( defaults == null )
        {
            NLogger.error( BoxPanel.class, 
                "UIManager defaults are null!" );
            return;
        }
        Color color = (Color)defaults.get("window");
        if ( color == null )
        {
            NLogger.error( BoxPanel.class, 
                "Failed to get default color: window. Using White." );
            color = Color.WHITE;
        }
        Color newColor = new Color( color.getRGB() );
        Color boxPanelBorderColor = PhexColors.getBoxPanelBorderColor();
        if ( boxPanelBorderColor == null )
        {
            NLogger.error( BoxPanel.class, 
                "Failed to get Phex color: BoxPanelBorderColor." );
            boxPanelBorderColor = Color.BLACK;
        }
        setBorder( BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(6, 6, 6, 6, newColor ),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder( 1, 1, 1, 1, boxPanelBorderColor ),
                BorderFactory.createMatteBorder(1, 1, 1, 1, newColor ) ) ) );
        Color boxPanelBackground = PhexColors.getBoxPanelBackground();
        if ( boxPanelBackground == null )
        {
            NLogger.error( BoxPanel.class, 
                "Failed to get Phex color: BoxPanelBackground." );
            boxPanelBackground = Color.GRAY;
        }
        int rgbValue = boxPanelBackground.getRGB();
        newColor = new Color( rgbValue );
        setBackground( newColor );
        if ( contentPanel != null )
        {
            contentPanel.setBackground( newColor );
            contentPanel.repaint();
        }
    }
    
    public void setHeaderText( String headerText )
    {
        boxHeader.setHeaderText( headerText );
    }
    
    
    public void setShowContent( boolean showContent )
    {
        if ( isContentShown != showContent )
        {
            isContentShown = showContent;
            contentPanel.setVisible( isContentShown );
            repaint();
        }
    }
    
    public void setContentLayout( LayoutManager mgr)
    {
        contentPanel.setLayout( mgr );
    }
    
    public void addContent( Component comp, GridBagConstraints constraints )
    {
        contentPanel.add( comp, constraints );
    }
    
    public JPanel getContentPanel()
    {
        return contentPanel;
    }
    
    @Override
    public void updateUI()
    {
        super.updateUI();
        // prevents calling from JPanel constructor
        // and before BoxPanel constructor initialized contentPanel
        if ( contentPanel != null )
        {
            initialize();
        }
    }
    
    @Override
    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }
    
    @Override
    public Dimension getPreferredSize()
    {
        Dimension max = getMaximumSize();
        Dimension pref = super.getPreferredSize();
        pref.height = Math.min( pref.height, max.height );
        pref.width = Math.min( pref.width, max.width );
        return pref;
    }

    static class BoxHeader extends JComponent implements MouseListener
    {
        private String headerText;
        private boolean isRollover;
        private BoxPanel boxPanel;
        
        public BoxHeader( String headerText, BoxPanel boxPanel )
        {
            super();
            this.headerText = headerText;
            this.boxPanel = boxPanel;
            isRollover = false;
            addMouseListener( this );
            setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
            initialize();
        }
        
        public void initialize()
        {
            setBackground( PhexColors.getBoxHeaderBackground() );
            setFont( UIManager.getFont("Label.font").deriveFont( Font.BOLD ) );
        }
        
        @Override
        public void updateUI()
        {
            super.updateUI();
            initialize();
        }
        
        public void setHeaderText( String headerText )
        {
            this.headerText = headerText;
            invalidate();
            repaint();
        }
        
        @Override
        public Dimension getMinimumSize()
        {
            return getPreferredSize();
        }
        
        @Override
        public Dimension getPreferredSize()
        {
            Font font = getFont();
            FontMetrics fm = getFontMetrics(font);
          
            viewRect.width = Short.MAX_VALUE;
            viewRect.height = Short.MAX_VALUE;
            SwingUtilities.layoutCompoundLabel(
                this, fm, headerText, null,
                SwingConstants.CENTER, SwingConstants.CENTER,
                SwingConstants.CENTER, SwingConstants.CENTER,
                viewRect, iconRect, textRect, 0 );
            Insets insets = getInsets();
            textRect.width += insets.left + insets.right + 10;
            textRect.height += insets.top + insets.bottom + 8;

            return textRect.getSize();
        }

        /* These rectangles/insets are allocated once for all 
         * ButtonUI.paint() calls.  Re-using rectangles rather than 
         * allocating them in each paint call substantially reduced the time
         * it took paint to run.  Obviously, this method can't be re-entered.
         */
        private static Rectangle viewRect = new Rectangle();
        private static Rectangle textRect = new Rectangle();
        private static Rectangle iconRect = new Rectangle();

        @Override
        protected void paintComponent(Graphics g)
        {
            // paint background.
            g.setColor( getBackground() );
            g.fillRect( 0, 0, getWidth(), getHeight() );
            
            Insets i = getInsets();
            viewRect.x = i.left;
            viewRect.y = i.top;
            viewRect.width = getWidth() - (i.right + viewRect.x);
            viewRect.height = getHeight() - (i.bottom + viewRect.y);
            
            // paint gradient
            Graphics2D g2 = (Graphics2D)g;
            Paint gradient = new GradientPaint(
                0, 0, PhexColors.getBoxHeaderGradientFrom(),
                0, getHeight(),
                PhexColors.getBoxHeaderGradientTo() );
            g2.setPaint( gradient );
            g2.fillRect( viewRect.x, viewRect.y,
                viewRect.width, viewRect.height - 2 );
        
            UIDefaults defaults = UIManager.getDefaults();
            Color color = (Color)defaults.get("window");
            g2.setColor(color);
            g2.fillRect( i.left, getHeight() - (i.bottom + i.top + 2), getWidth() - (i.right + i.left), 1 );
            
            FontMetrics fm = g.getFontMetrics();
            textRect.x = textRect.y = textRect.width = textRect.height = 0;
            iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

            // layout the text and icon
            String text = SwingUtilities.layoutCompoundLabel(
                this, fm, headerText, null,
                SwingConstants.CENTER, SwingConstants.CENTER,
                SwingConstants.CENTER, SwingConstants.CENTER,
                viewRect, iconRect, textRect, 0 );
            paintText(g, textRect, text);
        }

        protected void paintText( Graphics g, Rectangle textRect, String text)
        {
            FontMetrics fm = g.getFontMetrics();
            
            if ( isRollover )
            {
                g.setColor( PhexColors.getLinkLabelRolloverForeground() );
            }
            else
            {
                g.setColor( getForeground() );
            }
            
            // who knows what drived me to use this function! But this is only
            // J2SE 1.4 available and since we are not using the underline 
            // functionality anyway, we now just draw the string directly!
            g.drawString( text, textRect.x, textRect.y + fm.getAscent() );
            //BasicGraphicsUtils.drawStringUnderlineCharAt(
            //    g, text, -1, textRect.x,
            //    textRect.y + fm.getAscent() );
        }
        
        public void mouseEntered(MouseEvent e)
        {
            isRollover = true;
            repaint();
        }

        public void mouseExited(MouseEvent e)
        {
            isRollover = false;
            repaint();
        }

        public void mouseClicked(MouseEvent e)
        {
            boxPanel.setShowContent( !boxPanel.isContentShown );
        }
        
        public void mousePressed(MouseEvent e)
        {}
        public void mouseReleased(MouseEvent e)
        {}
        
    }
}