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
 *  Created on 28.04.2005
 *  --- CVS Information ---
 *  $Id: SlideInWindow.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.gui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;

import phex.common.Environment;
import phex.common.ThreadTracking;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 */
public class SlideInWindow extends JWindow
{
    private boolean closeAfterAnimation = false;
    private long autoCloseDelay;
    private String title;
    private boolean isHTML;
    private String shortMessage;
    private boolean isHideBtnShown;
    private JButton hideBtn;
    
    public SlideInWindow( String title, long autoCloseDelay )
    {
        super( GUIRegistry.getInstance().getMainFrame() );
        this.title = title;
        this.autoCloseDelay = autoCloseDelay;
        this.isHideBtnShown = true;
    }
    
    public void setShortMessage( String text, boolean isHTML )
    {
        shortMessage = text;
        this.isHTML = isHTML;
    }
    
    public void setHideBtnShown( boolean status )
    {
        isHideBtnShown = status;
    }

    /**
     * @param title
     * @param shortMessage
     */
    public void initializeComponent( )
    {
        getContentPane().setLayout( new BorderLayout() );
        JPanel panel = new JPanel();
        panel.setDoubleBuffered( true );
        panel.setBorder( BorderFactory.createLineBorder( PhexColors.getBoxPanelBorderColor(), 2 ) );
        panel.setLayout( new BorderLayout() );
        getContentPane().add( panel, BorderLayout.CENTER );
        
        DialogBanner dialogBanner = new DialogBanner( title, null );
        dialogBanner.setImageIcon( null );
        panel.add( dialogBanner, BorderLayout.NORTH );
        
        GradientPanel backgrundPanel = new GradientPanel( Color.WHITE, PhexColors.getBoxHeaderGradientTo() );
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout(
            "4px, fill:d:grow, 4px", // columns
            "4px, fill:d:grow, 10px, p, 4px"); //row
        PanelBuilder contentPB = new PanelBuilder(layout, backgrundPanel);
        
        JEditorPane editorPane = new JEditorPane();
        if ( isHTML )
        {
            editorPane.setContentType("text/html");
        }
        Font font = editorPane.getFont();
        font = font.deriveFont( Font.PLAIN, font.getSize() + 1);
        editorPane.setFont( font );
        editorPane.setOpaque( false );
        editorPane.setText( shortMessage );
        editorPane.setEditable( false );
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder( BorderFactory.createEmptyBorder() );
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setViewportView(editorPane);
        
        backgrundPanel.add( scrollPane, cc.xy(2, 2 ) );
        
        if ( isHideBtnShown )
        {
            hideBtn = new JButton( Localizer.getString( "SlideInWindow_Hide" ) );
            hideBtn.setMargin( new Insets( 1, 1, 1, 3 ) );
            hideBtn.addActionListener( new ActionListener()
                {
                    public void actionPerformed( ActionEvent e )
                    {
                        Environment.getInstance().executeOnThreadPool( new Runnable()
                            {
                                public void run()
                                {
                                    slideOut();
                                }
                            }, "SlideOutJob" );
                    }
                });
            JPanel buttonPanel = ButtonBarFactory.buildRightAlignedBar( hideBtn );
            buttonPanel.setOpaque(false);
            backgrundPanel.add( buttonPanel, cc.xy(2, 4) );
        }
        
        panel.add( backgrundPanel, BorderLayout.CENTER );
        validate();
        pack();
        setSize( 250, 200 );
    }
    
    public void slideIn(  )
    {
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
            getGraphicsConfiguration() );
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize( );
                
        Point startPoint = new Point( screenSize.width - 5 - screenInsets.left, 
            screenSize.height - getHeight() - 5 - screenInsets.bottom );
        Point stopPoint = new Point( screenSize.width - getWidth() - 5 - screenInsets.left,
            screenSize.height - getHeight() - 5 - screenInsets.bottom );
        
        setLocation( startPoint.x, startPoint.y );
        setVisible(true);
        repaint();
        
        closeAfterAnimation = false;
        AnimatorThread animator = new AnimatorThread( this, startPoint, stopPoint,
            50, 30 );
        animator.start();
    }
    
    public void slideOut( )
    {
        synchronized( this )
        {
            if ( closeAfterAnimation == true )
            {// if we are already animating the close operation we drop out..
                return;
            }
            closeAfterAnimation = true;
        }
        
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
            getGraphicsConfiguration() );
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize( );
        
        Point startPoint = new Point( screenSize.width - getWidth() - 5 - screenInsets.left,
            screenSize.height - getHeight() - 5 - screenInsets.bottom );
        Point stopPoint = new Point( screenSize.width - getWidth() - 5 - screenInsets.left, 
            screenSize.height - screenInsets.bottom );
        
        AnimatorThread animator = new AnimatorThread( this, startPoint, stopPoint,
            50, 30 );
        animator.start();
    }
    
    private void animationFinished()
    {
        if ( closeAfterAnimation )
        {
            setVisible(false);
            dispose();
        }
        else if ( autoCloseDelay > 0 )
        {
            Environment.getInstance().executeOnThreadPool(new Runnable()
                {
                    public void run()
                    {
                        long now = System.currentTimeMillis();
                        long closeTime = now + autoCloseDelay;
                        while( now < closeTime )
                        {
                            int secLeft = (int)Math.round( (double)(closeTime - now)/1000.0 );
                            hideBtn.setText( 
                                Localizer.getFormatedString( "SlideInWindow_HideSec",
                                    new Object[] {Integer.valueOf(secLeft)} ) );
                            try
                            {
                                Thread.sleep(250);
                            }
                            catch (InterruptedException e)
                            {
                            }
                            now = System.currentTimeMillis();
                        }
                        slideOut();
                    }
                }, "SlideInWindowHideBtnAnimator");
        }
    }
    
    private class AnimatorThread extends Thread
    {
        private Component comp;
        private long stepDelay;
        private int animationSteps;
        private Point startPoint;
        private Point stopPoint;
        
        public AnimatorThread( Component component, Point startPoint, 
            Point stopPoint, int steps, long stepDelay )
        {
            super( ThreadTracking.rootThreadGroup, "AnimatorThread" );
            comp = component;
            this.stepDelay = stepDelay;
            this.animationSteps = steps;
            this.startPoint = startPoint;
            this.stopPoint = stopPoint;
        }
        
        public void run()
        {
            for ( int i = 0; i <= animationSteps; i++ )
            {
                updateComponentAtStep( i );
                try
                {
                    Thread.sleep( stepDelay );
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            animationFinished();
        }
        
        private void updateComponentAtStep( int step )
        {
            final int x = startPoint.x + ( stopPoint.x - startPoint.x ) * step / animationSteps;
            final int y = startPoint.y + ( stopPoint.y - startPoint.y ) * step / animationSteps;
            EventQueue.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        comp.setLocation( x, y );
                        comp.validate();
                    }
                });
        }
    }
}
