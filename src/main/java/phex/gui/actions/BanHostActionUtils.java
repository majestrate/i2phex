/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  Created on 14.04.2006
 *  --- SVN Information ---
 *  $Id: BanHostActionUtils.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JMenu;

import org.apache.commons.lang.time.DateUtils;

import phex.common.Environment;
import phex.common.ExpiryDate;
import phex.common.address.DestAddress;
import phex.common.address.IpAddress;
import phex.common.log.NLogger;
import phex.gui.common.GUIRegistry;
import phex.gui.dialogs.security.SecurityRuleDialog;
import phex.security.AccessType;
import phex.security.PhexSecurityManager;
import phex.servent.Servent;
import phex.utils.Localizer;

public abstract class BanHostActionUtils extends FWAction
{
    public static BanHostActionMenu createActionMenu( 
        BanHostActionProvider addressProvider )
    {
        FWAction[] actions = new FWAction[ 4 ];
        JMenu mainMenu = new JMenu( Localizer.getString( "BanHostAction_BanHost" ) );
        mainMenu.setIcon( GUIRegistry.getInstance().getPlafIconPack().getIcon( "Security.BanHost" ) );
        
        // 1 day
        BanHostAction action = new BanHostAction( 
            Localizer.getString( "BanHostAction_1Day" ),
            addressProvider, ExpiryDate.getExpiryDate( 
                System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY ) );
        actions[0] = action;
        mainMenu.add( action );
        // 7 days
        action = new BanHostAction( Localizer.getString( "BanHostAction_7Days" ),
            addressProvider, ExpiryDate.getExpiryDate( 
                System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY * 7 ) );
        actions[1] = action;
        mainMenu.add( action );
        // never
        action = new BanHostAction( Localizer.getString( "BanHostAction_Unlimited" ),
            addressProvider, ExpiryDate.getExpiryDate( ExpiryDate.EXPIRES_NEVER ) );
        actions[2] = action;
        mainMenu.add( action );
        // custom
        action = new BanHostAction( Localizer.getString( "BanHostAction_Custom" ),
            addressProvider, null );
        actions[3] = action;
        mainMenu.add( action );
        
        
        // we return an extra wrapper class to have the option of two return
        // values.
        BanHostActionMenu actionMenu = new BanHostActionMenu();
        actionMenu.menu = mainMenu;
        actionMenu.actions = actions;
        return actionMenu;
    }
    
    public static FWAction createToolBarAction( BanHostActionProvider addressProvider )
    {
        // 7 days
        BanHostAction action = new BanHostAction( 
            Localizer.getString( "BanHostAction_BanHost" ), 
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Security.BanHost" ),
            Localizer.getString( "BanHostAction_TTTBanHost7Days" ),
            addressProvider, ExpiryDate.getExpiryDate( 
                System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY * 7 ) );
        return action;
    }
    
    public static class BanHostActionMenu
    {
        public FWAction[] actions;
        public JMenu menu;
    }
    
    public interface BanHostActionProvider
    {
        public DestAddress[] getBanHostAddresses();
        public boolean isBanHostActionEnabled( boolean allowMultipleAddresses );
    }
    
    private static class BanHostAction extends FWAction
    {
        private BanHostActionProvider addressProvider;
        private ExpiryDate expiryDate;

        private BanHostAction( String name,
            BanHostActionProvider addressProvider, ExpiryDate expiryDate )
        {
            this( name, null, null, addressProvider, expiryDate );
        }
        
        private BanHostAction( String name, Icon icon, String tooltiptext,
            BanHostActionProvider addressProvider, ExpiryDate expiryDate )
        {
            super( name, icon, tooltiptext );
            this.addressProvider = addressProvider;
            this.expiryDate = expiryDate;
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            try
            {
                if ( expiryDate == null )
                {   
                    // this is the custom ban situation... we do the same as we
                    // would when we create a new ban.
                    final DestAddress[] addresses = addressProvider.getBanHostAddresses();
                    // we only honor the first ban address the holder should make
                    // sure this element is disabled in case multiple hosts are 
                    // selected
                    IpAddress ip = addresses[0].getIpAddress();
                    if ( ip == null )
                    {
                        return;
                    }
                    SecurityRuleDialog dialog = new SecurityRuleDialog();
                    dialog.customPrefillBanSingleIp(
                        Localizer.getString("UserBanned" ), ip.getHostIP() );
                    dialog.setVisible(true);
                }
                else
                {
                    final DestAddress[] addresses = addressProvider.getBanHostAddresses();
                    Runnable runner = new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                banHosts( addresses, expiryDate );
                            }
                            catch ( Throwable th )
                            {
                                NLogger.error( BanHostAction.class, th, th);
                            }
                        }
                    };
                    Environment.getInstance().executeOnThreadPool(runner, "BanHostsAction" );
                }
            }
            catch ( Throwable th )
            {
                NLogger.error( BanHostAction.class, th, th);
            }
        }
    
        @Override
        public void refreshActionState()
        {
            setEnabled( addressProvider.isBanHostActionEnabled( expiryDate != null ) );
        }
    }
    
    private static void banHosts( DestAddress[] addresses, ExpiryDate expiryDate )
    {
        Servent servent = GUIRegistry.getInstance().getServent();
        PhexSecurityManager securityMgr = servent.getSecurityService();
        for ( int i = 0; i < addresses.length; i++ )
        {
            IpAddress ip = addresses[i].getIpAddress();
            if ( ip == null )
            {
                continue;
            }
            AccessType res = securityMgr.controlHostIPAccess( ip.getHostIP() );
            // only add if not already added through earlier batch.
            if (res == AccessType.ACCESS_GRANTED)
            {
                securityMgr.createIPAccessRule( Localizer.getString("UserBanned"), 
                    ip.getHostIP(), (byte) 32, false, expiryDate, true);
            }
        }
    }
}