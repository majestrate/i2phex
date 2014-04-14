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
 *  $Id: FWTab.java 4138 2008-03-02 13:39:20Z complication $
 */
package phex.gui.tabs;



import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import phex.gui.actions.*;
import phex.xml.sax.gui.DGuiSettings;
import phex.xml.sax.gui.DTab;



/**
 * Base class for all tabs.
 */
public class FWTab extends JPanel
{
    private FWActionGroup actionGroup;
    
    /**
     * The name of the tab in the tabbed pane.
     */
    private String name;

    /**
     * The icon of the tab in the tabbed pane.
     */
    private Icon icon;

    /**
     * The tool tip text of the tab in the tabbed pane.
     */
    private String toolTip;

    /**
     * The mnemonic of the tab.
     */
    private char mnemonic;

    /**
     * The accelerator of the tab.
     */
    private KeyStroke accelerator;

    /**
     * The position of the tab in the tabbed pane.
     */
    private int index;

    /**
     * The unique id of the tab.
     */
    private int tabID;

    private ToggleTabViewAction toggleTabViewAction;

    public FWTab( int aTabID, String aName, Icon aIcon,
        String aToolTip, char mnemonic, KeyStroke accelerator, int aIndex)
    {
        tabID = aTabID;
        name = aName;
        icon = aIcon;
        toolTip = aToolTip;
        index = aIndex;
        this.mnemonic = mnemonic;
        this.accelerator = accelerator;
        actionGroup = new FWActionGroup();
    }

    @Override
    public String getName()
    {
        return name;
    }

    public Icon getIcon()
    {
        return icon;
    }
    
    public void setIcon( Icon icon )
    {
        this.icon = icon; 
    }

    public char getMnemonic()
    {
        return mnemonic;
    }

    public KeyStroke getAccelerator()
    {
        return accelerator;
    }

    public String getToolTip()
    {
        return toolTip;
    }

    public int getIndex()
    {
        return index;
    }
    
    /**
     * Indicates if this tab is visible by default, when there is no known 
     * visible setting from the user.
     * @return true if visible by default false otherwise.
     */
    public boolean isVisibleByDefault()
    {
        return true;
    }
    
    /**
     * This provides a trade-off between the tab hiding and the tab destroy.
     * The <code>start()</code> (<code>stop()</code>) method 
     * creates (destroys) some objects
     * and starts (stops) some subscribes.
     * This allows deeper memory and cpu saves than the <code>setVisible()</code> method.
     * This is not designed to be called often.
     * @see <code>#stop()</code>
     */
    /*public void start()
    {
    }*/
    
    /**
     * This provides a trade-off between the tab hiding and the tab destroy.
     * @see <code>#start()</code>
     */
    /*public void stop()
    {
    }*/


    /**
     * Method is called when the tab will be selected in the tabbed pane. Can be
     * overloaded to do some action.
     */
    public void tabSelectedNotify()
    {
    }
    
    public List getViewMenuActions()
    {
        return null;
    }

    public FWToggleAction getToggleTabViewAction()
    {
        if ( toggleTabViewAction == null )
        {
            toggleTabViewAction = new ToggleTabViewAction( this );
        }
        return toggleTabViewAction;
    }

    public void appendDGuiSettings( DGuiSettings dGuiSettings )
    {
        DTab dTab = new DTab();
        dTab.setTabId( tabID );
        // only store visible state if not default value.
        boolean visibleState = getParent() != null;
        if ( visibleState != isVisibleByDefault() )
        {
            dTab.setVisible( getParent() != null );
        }
        dGuiSettings.getTabList().add( dTab );
    }

    public void addTabAction( FWAction action )
    {
        actionGroup.addAction(action);
    }
    
    public void addTabActions( FWAction[] actions )
    {
        actionGroup.addActions(actions);
    }

    public void addTabAction( String key, FWAction action )
    {
        actionGroup.addAction( key, action );
    }

    public FWAction getTabAction( String key )
    {
        return actionGroup.getAction( key );
    }

    public void refreshTabActions()
    {
        actionGroup.refreshActions();
    }

    public void updateUI()
    {
        // TODO Auto-generated method stub
        return;
    }
}