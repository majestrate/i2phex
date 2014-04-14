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
 *  $Id: DialogBanner.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.gui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 */
public class DialogBanner extends JPanel
{
    private String headerText;
    private String subHeaderText;
    private Icon image;
    
    private JLabel titleLabel;
    
    public DialogBanner( String aHeaderText, String aSubHeaderText )
    {
        super();
        
        headerText = aHeaderText;
        subHeaderText = aSubHeaderText;
        
        image = GUIRegistry.getInstance().getPlafIconPack().getIcon( "DialogBanner.DefaultImage" );
        
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout(
            "4dlu, 2dlu, d:grow", // columns
            "3dlu, d, 3dlu, d, 3dlu"); //rows);
        PanelBuilder builder = new PanelBuilder( layout, this );
        setBackground( Color.WHITE );
        
        Font font = UIManager.getFont("TitledBorder.font");
        font = font.deriveFont( Font.BOLD, font.getSize() + 2);
        titleLabel = new JLabel( headerText );
        titleLabel.setFont( font );
        builder.add( titleLabel, cc.xywh( 2, 2, 2, 1 ) );
        
        font = UIManager.getFont( "Label.font" );
        font = font.deriveFont( Font.PLAIN, font.getSize() - 1 );
        JLabel subLabel = new JLabel( subHeaderText );
        subLabel.setFont( font );
        builder.add( subLabel, cc.xy( 3, 4 ) );
    }
    
    public void setHeaderText( String headerText )
    {
        titleLabel.setText( headerText );
    }
    
    /**
     * The default image is PhexWizard... set a different one here...
     * The icon is retrieved from the plaf icon pack.
     * Give null to set no image.
     * @param image
     */
    public void setImageIcon( String imageKey )
    {
        if ( imageKey != null )
        {
            this.image = GUIRegistry.getInstance().getPlafIconPack().getIcon( imageKey );
        }
        else
        {
            this.image = null;
        }
    }
    
    /* These rectangles/insets are allocated once for all 
     * ButtonUI.paint() calls.  Re-using rectangles rather than 
     * allocating them in each paint call substantially reduced the time
     * it took paint to run.  Obviously, this method can't be re-entered.
     */
    private static Rectangle viewRect = new Rectangle();
    
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
            0, 0, PhexColors.getBoxHeaderGradientTo(),
            (int)(getWidth()/2f), 0,
            getBackground() );
        g2.setPaint( gradient );
        g2.fillRect( viewRect.x, viewRect.y,
            viewRect.width, viewRect.height );
        
        if ( image != null )
        {
            int x = Math.max( 0, getWidth() - 7 - image.getIconWidth() );
            int y = 7;
            image.paintIcon( this, g2, x, y );
        }
    }
    
    @Override
    public Dimension getPreferredSize()
    {
        Dimension dim = super.getPreferredSize();
        
        int height = 0;
        int width = 0;
        if ( image != null )
        {// calc img height...
            // border of 7 above and below..
            height = image.getIconHeight() + 14;
            // border of 7 left and right
            width = image.getIconWidth() + 14;
        }
        dim.height = Math.max( dim.height, height );
        dim.width = dim.width + width;
        return dim;
    }
    
    @Override
    public Dimension getMaximumSize()
    {
        Dimension maxDim = super.getMaximumSize();
        Dimension prefDim = getPreferredSize();
        maxDim.height = prefDim.height;
        return maxDim;
    }
    
    public static void main(String args[])
    {
        PhexColors.updateColors();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setSize( 400, 300 );
        frame.getContentPane().setLayout( new BorderLayout() );
        
        DialogBanner banner = new DialogBanner("New Download", "Add a new download from Magnet URI or URL" );
        frame.getContentPane().add( banner, BorderLayout.NORTH );
        
        frame.setVisible(true);
    }
}
