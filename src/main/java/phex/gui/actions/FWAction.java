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
 */
package phex.gui.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * Super class to implement actions for Phex.
 */
public abstract class FWAction extends AbstractAction
{
    /**
     * This constant should be used as a storage-retreival key when
     * setting or getting the <I>ToolTipText</I>.
     */
    public static final String TOOL_TIP_TEXT = "ToolTipText";

    /**
     * This constant should be used as a storage-retreival key for
     * medium icons used for menus (24 x 24)
     */
    public static final String MEDIUM_ICON = "MediumIcon";

    /**
     * This constant should be used as a storage-retreival key for
     * large icons (32 x 32)
     */
    public static final String LARGE_ICON = "LargeIcon";

    /**
     * Defines an Action object with a default description string
     * and default icon.
     */
    public FWAction()
    {
        this(null, null, null, null, null, null);
    }

    /**
     * Defines an Action object with the specified description string
     * <I>name</I> and a default icon.
     *
     * @see javax.swing.Action#NAME
     */
    public FWAction(String name)
    {
        this(name, null, null, null, null, null);
    }

    /**
     * Defines an Action object with the specified description string
     * <I>name</I> and the specified <I>icon</I>.
     *
     * @see javax.swing.Action#NAME
     * @see javax.swing.Action#SMALL_ICON
     */
    public FWAction(String name, Icon smallIcon)
    {
        this(name, smallIcon, null, null, null, null);
    }

    /**
     * Defines an Action object with the specified description string
     * <I>name</I>, the specified <I>icon</I> and the specified
     * <I>toolTipText</I>.
     *
     * @see javax.swing.Action#NAME
     * @see javax.swing.Action#SMALL_ICON
     */
    public FWAction(String name, Icon smallIcon, Icon mediumIcon)
    {
        this(name, smallIcon, mediumIcon, null, null, null);
    }

    /**
     * Defines an Action object with the specified description string
     * <I>name</I>, the specified <I>icon</I> and the specified
     * <I>toolTipText</I>.
     *
     * @see javax.swing.Action#NAME
     * @see javax.swing.Action#SMALL_ICON
     */
    public FWAction(String name, Icon smallIcon, Icon mediumIcon, String toolTipText)
    {
        this(name, smallIcon, mediumIcon, toolTipText, null, null);
    }

    /**
     * Defines an Action object with the specified description string
     * <I>name</I>, the specified <I>icon</I> and the specified
     * <I>toolTipText</I>.
     *
     * @see javax.swing.Action#NAME
     * @see javax.swing.Action#SMALL_ICON
     */
    public FWAction(String name, Icon smallIcon, String toolTipText)
    {
        this(name, smallIcon, null, toolTipText, null, null);
    }

    /**
     * Defines an Action object with the specified description string
     * <I>name</I>, the specified <I>icon</I>, the specified
     * <I>toolTipText</I> and the specified <I>menuShortCut<I>.
     *
     * @see javax.swing.Action#NAME
     * @see javax.swing.Action#SMALL_ICON
     */
    public FWAction(String name, Icon smallIcon, String toolTipText, Integer mnemonic)
    {
        this(name, smallIcon, null, toolTipText, mnemonic, null);
    }

    /**
     * Defines an Action object with the specified description string
     * <I>name</I>, the specified <I>icon</I>, the specified
     * <I>toolTipText</I> and the specified <I>menuShortCut<I>.
     *
     * @see javax.swing.Action#NAME
     * @see javax.swing.Action#SMALL_ICON
     */
    public FWAction(String name, Icon smallIcon, String toolTipText,
        Integer mnemonic, KeyStroke keyStroke )
    {
        this(name, smallIcon, null, toolTipText, mnemonic, keyStroke);
    }

    /**
     * Defines an Action object with the specified description string
     * <I>name</I>, the specified <I>icon</I>, the specified
     * <I>toolTipText</I> and the specified <I>menuShortCut<I>.
     *
     * @see javax.swing.Action#NAME
     * @see javax.swing.Action#SMALL_ICON
     */
    public FWAction(String name,Icon smallIcon,Icon largeIcon,String toolTipText,
        Integer mnemonic)
    {
        this(name, smallIcon, largeIcon, toolTipText, mnemonic, null);
    }

    /**
     * Defines an Action object with the specified description string
     * <I>name</I>, the specified <I>icon</I>, the specified
     * <I>toolTipText</I>, the specified <I>menuShortCut<I> and
     * the specified <I>accelerator<I>.
     *
     * @see javax.swing.Action#NAME
     * @see javax.swing.Action#SMALL_ICON
     */
    public FWAction(String name, Icon smallIcon, Icon mediumIcon, String toolTipText,
        Integer mnemonic, KeyStroke accelerator)
    {
        super();
        if (name != null)
        {
            putValue(Action.NAME, name );
        }
        if (smallIcon != null)
        {
            putValue(Action.SMALL_ICON, smallIcon);
        }
        if (mediumIcon != null)
        {
            putValue(MEDIUM_ICON, mediumIcon);
        }
        if (toolTipText != null)
        {
            putValue(TOOL_TIP_TEXT, toolTipText);
        }
        if (mnemonic != null)
        {
            putValue( MNEMONIC_KEY, mnemonic );
        }
        if (accelerator != null)
        {
            putValue( ACCELERATOR_KEY, accelerator );
        }
    }

    public void setName( String name )
    {
        if (name != null)
        {
            putValue( NAME, name );
        }
    }

    /**
     * Setting the small icon used for menus (16 x 16)
     */
    public void setSmallIcon(Icon icon)
    {
        if (icon != null)
        {
            putValue(SMALL_ICON, icon);
        }
    }

    /**
     * Setting the medium icon (24 x 24)
     */
    public void setMediumIcon(Icon icon)
    {
        if (icon != null)
        {
            putValue(MEDIUM_ICON, icon);
        }
    }

    /**
     * Setting the medium icon (32 x 32)
     */
    public void setLargeIcon(Icon icon)
    {
        if (icon != null)
        {
            putValue(LARGE_ICON, icon);
        }
    }

    public void setToolTipText(String text)
    {
        if (text != null)
        {
            putValue(TOOL_TIP_TEXT, text);
        }
    }

    /**
     * This could be used by the action to indicate it likes to be displayed as
     * a toggle action.
     * @return If the action is a toggle action or not.
     */
    public boolean isToggleAction()
    {
        return false;
    }

    /**
     * This method is called to refresh the button state. It can check here if
     * it needs to be enabled or disabled.
     */
    public abstract void refreshActionState();
}