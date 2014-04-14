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
 *  --- CVS Information ---
 *  $Id: SplashWindow.java 4363 2009-01-16 10:37:30Z gregork $
 */
package phex.gui.common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JWindow;

public class SplashWindow extends JWindow implements MouseListener
{
    public static final String SPLASH_IMAGE_NAME = "/phex/resources/splash.png";

    private Image image;

    public SplashWindow()
    {
        super( );
       
        ImageIcon icon = new ImageIcon( SplashWindow.class.getResource(
            SPLASH_IMAGE_NAME ) );
        image = icon.getImage();

        setSize( image.getWidth( null ) + 4, image.getHeight( null ) + 4 );
        GUIUtils.centerWindowOnScreen( this );
        
        addMouseListener( this );
    }

    public void showSplash()
    {
        setVisible( true );
        repaint();
        toFront();
    }
    
    private void hideSplash()
    {
        setVisible( false );
    }

    public void paint( Graphics g )
    {
        g.drawImage( image, 2, 2, null );
        g.setColor( Color.white );
        g.drawLine( 0, 0, 0, image.getHeight( null ) + 3 );
        g.drawLine( 0, 0, image.getWidth( null ) + 3, 0 );
        g.setColor( Color.lightGray );
        g.drawLine( 1, 1, 1, image.getHeight( null ) + 2 );
        g.drawLine( 1, 1, image.getWidth( null ) + 2, 1 );


        g.setColor( Color.black );
        g.drawLine( 0, image.getHeight( null ) + 3,
            image.getWidth( null ) + 3, image.getHeight( null ) + 3 );
        g.drawLine( image.getWidth( null ) + 3, 0,
            image.getWidth( null ) + 3, image.getHeight( null ) + 3 );

        g.setColor( Color.darkGray );
        g.drawLine( 1, image.getHeight( null ) + 2,
            image.getWidth( null ) + 2, image.getHeight( null ) + 2 );
        g.drawLine( image.getWidth( null ) + 2, 1,
            image.getWidth( null ) + 2, image.getHeight( null ) + 2 );

/*
        g.setColor( Color.lightGray );
        g.drawRect( 0, 0, image.getWidth( null ) + 3, image.getHeight( null ) + 3 );
        g.setColor( Color.darkGray );
        g.drawRect( 1, 1, image.getWidth( null ) + 1, image.getHeight( null ) + 1 );
            */
    }

    public void mouseClicked( MouseEvent e )
    {
        hideSplash();
    }
    public void mouseEntered( MouseEvent e )
    {}
    public void mouseExited( MouseEvent e )
    {}
    public void mousePressed( MouseEvent e )
    {}
    public void mouseReleased( MouseEvent e )
    {}
}
