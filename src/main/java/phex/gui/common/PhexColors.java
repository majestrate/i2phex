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
 *  $Id: PhexColors.java 4364 2009-01-16 10:56:15Z gregork $
 */
package phex.gui.common;

import java.awt.Color;

import javax.swing.UIManager;

import org.apache.commons.lang.SystemUtils;

/**
 * 
 */
public class PhexColors
{
    public static final Color[] NETWORK_HOST_CONNECTING_COLORS = 
    {
        new Color( 0x7F, 0x00, 0x00 ),
        new Color( 0xFF, 0xd4, 0xd4 )
    };
    
    public static final Color[] NETWORK_HOST_CONNECTED_COLORS = 
    {
        new Color( 0x00, 0x7F, 0x00 ),
        new Color( 0xB5, 0xff, 0xB5 )
    };
    
    
    private static Color boxPanelBorderColor;
    private static Color boxPanelBackground;
    private static Color boxHeaderBackground;
    private static Color boxHeaderGradientFrom;
    private static Color boxHeaderGradientTo;
    private static Color linkLabelRolloverForeground;
    
    private static Color scopeProgressBarBackground;
    private static Color scopeProgressBarForeground;
    private static Color finishedScopeProgressBarForeground;
    private static Color unverifiedScopeProgressBarForeground;
    private static Color blockedScopeProgressBarForeground;
    
    public static Color getBoxPanelBorderColor()
    {
        return boxPanelBorderColor;//new Color( 0xDD6900 );
    }

    public static Color getBoxPanelBackground()
    {
        return boxPanelBackground;//new Color( 0xFFCF9B );
    }

    public static Color getBoxHeaderBackground()
    {
        return boxHeaderBackground;//new Color( 0xFF8400 );
    }

    public static Color getBoxHeaderGradientFrom()
    {
        return boxHeaderGradientFrom;//new Color( 0xFFAD42 );
    }

    public static Color getBoxHeaderGradientTo()
    {
        return boxHeaderGradientTo;//new Color( 0xFFBD6B );
    }
    
    public static Color getElegantHeaderGradientFrom()
    {
        return boxHeaderGradientFrom;
    }

    public static Color getElegantHeaderGradientTo()
    {
        return boxHeaderGradientTo;
    }

    public static Color getLinkLabelRolloverForeground()
    {
        return linkLabelRolloverForeground;//new Color( 0xEC6400 );
    }
    
    public static Color getScopeProgressBarBackground()
    {
        return scopeProgressBarBackground;
    }
    
    public static Color getScopeProgressBarForeground()
    {
        return scopeProgressBarForeground;
    }
    
    public static Color getFinishedScopeProgressBarForeground()
    {
        return finishedScopeProgressBarForeground;
    }
    
    public static Color getUnverifiedScopeProgressBarForeground()
    {
        return unverifiedScopeProgressBarForeground;
    }
    
    public static Color getBlockedScopeProgressBarForeground()
    {
        return blockedScopeProgressBarForeground;
    }

    /**
     * Colors get usually update when a UI update is performed.
     */
    public static void updateColors()
    {
        Color activeCaptionBorderColor = UIManager.getColor("activeCaptionBorder");
        Color infoColor = UIManager.getColor("info");
        
        if( (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_WINDOWS) 
            && UIManager.getLookAndFeel().isNativeLookAndFeel() )
        {
            // in case this is native LAF we use our special orange color set
            // this is done because the standard ui colors we use for other LAF
            // just look so bad and ugly on Mac OSX.
            activatePhexColors();
        }
        else if ( infoColor == null || activeCaptionBorderColor == null )
        {
            // to prevent errors on LAF with some missing UI Colors we use
            // the Phex color set on this LAF.
            // (occures on Linux with GTK LAF)
            activatePhexColors();
        }
        else
        {
            boxPanelBorderColor = GUIUtils.darkerColor(
	            activeCaptionBorderColor, 0.8 );
            boxPanelBackground = new Color(
	            infoColor.getRGB() );
	        boxHeaderBackground = GUIUtils.darkerColor(
	            activeCaptionBorderColor, 0.9 );
	        boxHeaderGradientFrom = new Color(
	            activeCaptionBorderColor.getRGB() );
	        boxHeaderGradientTo = GUIUtils.brighterColor(
	            infoColor, 0.8 );
	        linkLabelRolloverForeground = GUIUtils.darkerColor(
	            activeCaptionBorderColor, 0.8 );
            
            scopeProgressBarBackground = UIManager.getColor( "window"  );
            scopeProgressBarForeground = UIManager.getColor( "ProgressBar.foreground"  );

            finishedScopeProgressBarForeground = scopeProgressBarForeground;

            // give it a touch of red and saturation
            unverifiedScopeProgressBarForeground = new Color( 0xf04656 );

            // use a lighter color
            blockedScopeProgressBarForeground = GUIUtils.brighterColor(
                infoColor, 0.7 );
        }
    }

    /**
     * in case this is native LAF we use our special orange color set
     * this is done because the standard ui colors we use for other LAF
     * just look so bad and ugly on Mac OSX.
     */
    private static void activatePhexColors()
    {
        boxPanelBorderColor = new Color( 0xDD6900 );
        boxPanelBackground = new Color( 0xFFCF9B );
        boxHeaderBackground = new Color( 0xFF8400 );
        boxHeaderGradientFrom = new Color( 0xFFAD42 );
        boxHeaderGradientTo = new Color( 0xFFD9A9 );
        linkLabelRolloverForeground = new Color( 0xEC6400 );
        scopeProgressBarBackground = Color.WHITE;
        scopeProgressBarForeground = new Color( 0xFFA645 );
        finishedScopeProgressBarForeground = scopeProgressBarForeground;
        unverifiedScopeProgressBarForeground = new Color( 0xFF4430 );
        blockedScopeProgressBarForeground = new Color( 0xFF998f );
    }
}