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
 *  --- SVN Information ---
 *  $Id: NetFavoritesPanel.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.network;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import phex.common.address.DefaultDestAddress;
import phex.common.address.DestAddress;
import phex.common.address.MalformedDestAddressException;
import phex.common.log.NLogger;
import phex.connection.OutgoingConnectionDispatcher;
import phex.gui.common.GUIRegistry;
import phex.host.FavoriteHost;
import phex.host.FavoritesContainer;
import phex.net.repres.PresentationManager;
import phex.servent.Servent;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 */
public class NetFavoritesPanel
    extends JPanel
    //extends FormDebugPanel
{
    private final FavoritesContainer favoritesContainer;
    
    private JTextField newFavoriteHostTF;
    private JButton addToFavoritesHostBtn;
    
    private JList favoritesList;
    
    private JButton removeFromFavoritesHostBtn;
    private JButton connectToFavoritesHostBtn;
    
    public NetFavoritesPanel( FavoritesContainer favoritesContainer )
    {
        this.favoritesContainer = favoritesContainer;
        init();
    }
    
    private void init()
    {
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout(
            "8dlu, d, 2dlu, d, 8dlu", // columns
            "p, 3dlu, p, 4dlu, p, 2dlu, p, 2dlu, p:grow"); //rows
        PanelBuilder favoritesBuilder = new PanelBuilder( layout, this );
        
        favoritesBuilder.addSeparator( Localizer.getString( "NetworkTab_Favorites" ),
            cc.xywh( 1, 1, 5, 1 ) );
        
        newFavoriteHostTF = new JTextField( 20 );
        favoritesBuilder.add( newFavoriteHostTF, cc.xy( 2, 3 ) );
        
        addToFavoritesHostBtn = new JButton( Localizer.getString( "Add" ) );
        addToFavoritesHostBtn.addActionListener( new AddToFavoritesHostAction() );
        favoritesBuilder.add( addToFavoritesHostBtn, cc.xy( 4, 3 ) );
        
        favoritesList = new JList( new FavoritesListModel( favoritesContainer ) );
        favoritesList.setPrototypeCellValue( "123.123.123.123:12345" );
        favoritesList.setVisibleRowCount( 5 );
        favoritesList.setCellRenderer( new FavoritesListRenderer() );
        favoritesBuilder.add( new JScrollPane( favoritesList ), cc.xywh( 2, 5, 1, 5 ) );
        
        connectToFavoritesHostBtn = new JButton( Localizer.getString( "Connect" ) );
        connectToFavoritesHostBtn.addActionListener( new ConnectToFavoritesHostAction());
        favoritesBuilder.add( connectToFavoritesHostBtn, cc.xy( 4, 5 ) );
        
        removeFromFavoritesHostBtn = new JButton( Localizer.getString( "Remove" ) );
        removeFromFavoritesHostBtn.addActionListener( new RemoveFromFavoritesHostAction());
        favoritesBuilder.add( removeFromFavoritesHostBtn, cc.xy( 4, 7 ) );
    }
    
    private final class RemoveFromFavoritesHostAction implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                FavoriteHost host = (FavoriteHost) favoritesList.getSelectedValue();
                favoritesContainer.removeBookmarkedHost( host );
            }
            catch ( Exception exp )
            {// catch all errors left
                NLogger.error( NetFavoritesPanel.class, exp, exp );
            }
        }
    }
    
    private final class ConnectToFavoritesHostAction implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                FavoriteHost host = (FavoriteHost) favoritesList.getSelectedValue();
                if ( host == null )
                {// nothing to do..
                    return;
                }
                // Add new host and connect.
                OutgoingConnectionDispatcher.dispatchConnectToHost( host.getHostAddress(),
                    GUIRegistry.getInstance().getServent() );
            }
            catch ( Exception exp )
            {// catch all errors left
                NLogger.error( NetFavoritesPanel.class, exp, exp );
            }
        }
    }
    
    private final class AddToFavoritesHostAction implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                String host = newFavoriteHostTF.getText();
                host = host.trim();
                if ( host.length() > 0 )
                {
                    try
                    {
                        DestAddress address = PresentationManager.getInstance()
                            .createHostAddress( host, DefaultDestAddress.DEFAULT_PORT );
                        favoritesContainer.addFavorite( address );
                        newFavoriteHostTF.setText("");
                    }
                    catch ( MalformedDestAddressException exp )
                    {// TODO2 bring friendly error message about wrong format.
                    }
                }                
            }
            catch ( Exception exp )
            {// catch all errors left
                NLogger.error( NetFavoritesPanel.class, exp, exp );
            }
        }
    }
}
