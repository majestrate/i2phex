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
package phex.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;

import phex.gui.common.GUIRegistry;
import phex.gui.common.PlainMultiLinePanel;
import phex.gui.prefs.InterfacePrefs;
import phex.gui.prefs.PhexGuiPrefs;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class CloseOptionsDialog extends JDialog
{
    private JRadioButton minimizeToBackgroundRBtn;
    private JRadioButton shutdownRBtn;
    private JCheckBox dontDisplayAgainChkBox;
    private boolean isOkActivated;

    public CloseOptionsDialog( )
    {
        super( GUIRegistry.getInstance().getMainFrame(),
            Localizer.getString( "CloseOptions" ), true );
        isOkActivated = false;
        prepareComponent();
    }

    private void prepareComponent()
    {
        addWindowListener(new WindowAdapter()
            {
                public void windowClosing( WindowEvent evt )
                {
                    closeDialog( false );
                }
            }
        );

        Container contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );
        
        //JPanel contentPanel = new FormDebugPanel();
        JPanel contentPanel = new JPanel();
        contentPane.add( contentPanel, BorderLayout.CENTER );
        
        FormLayout layout = new FormLayout(
            "d:grow", // columns
            "top:p:grow, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p" ); // 9 rows 
        layout.setRowGroups( new int[][]{{3, 5, 9}} );
        contentPanel.setLayout( layout );
        
        PanelBuilder builder = new PanelBuilder( layout, contentPanel );
        CellConstraints cc = new CellConstraints();
        
        builder.setDefaultDialogBorder();

        PlainMultiLinePanel label = new PlainMultiLinePanel(
            Localizer.getString( "CloseOptionsText" ) );
        builder.add( label, cc.xy( 1, 1 ) );

        String backgroundText;
        // whether we have sys tray support or not
        if ( GUIRegistry.getInstance().getDesktopIndicator() != null )
        {
            backgroundText = Localizer.getString( "MinimizeToSysTray" );
        }
        else
        {
            backgroundText = Localizer.getString( "MinimizeToBackground" );
        }

        minimizeToBackgroundRBtn = new JRadioButton( backgroundText,
            InterfacePrefs.MinimizeToBackground.get().booleanValue() );
        Font font = minimizeToBackgroundRBtn.getFont();
        Font newFont = font.deriveFont( Font.BOLD, font.getSize() + 1 );
        minimizeToBackgroundRBtn.setFont( newFont );
        builder.add( minimizeToBackgroundRBtn, cc.xy( 1, 3 ) );

        shutdownRBtn = new JRadioButton( Localizer.getString( "Shutdown" ),
            !InterfacePrefs.MinimizeToBackground.get().booleanValue() );
        builder.add( shutdownRBtn, cc.xy( 1, 5 ) );
        ButtonGroup group = new ButtonGroup();
        group.add( minimizeToBackgroundRBtn );
        group.add( shutdownRBtn );

        JSeparator separator = new JSeparator();
        builder.add( separator, cc.xy( 1, 7 ) );

        ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
        dontDisplayAgainChkBox = new JCheckBox(
            Localizer.getString( "DontAskAnymore" ) );
        btnBuilder.addGridded( dontDisplayAgainChkBox );
        btnBuilder.addRelatedGap();
        btnBuilder.addGlue();

        ButtonActionHandler actionHandler = new ButtonActionHandler();

        JButton okButton = new JButton( Localizer.getString( "OK" ) );
        okButton.setDefaultCapable( true );
        okButton.setRequestFocusEnabled( true );
        okButton.addActionListener( actionHandler );
        okButton.setActionCommand( "OK" );

        JButton cancelButton = new JButton( Localizer.getString( "Cancel" ) );
        cancelButton.setRequestFocusEnabled( true );
        cancelButton.addActionListener( actionHandler );
        cancelButton.setActionCommand( "CANCEL" );
        
        btnBuilder.addGriddedButtons( new JButton[]{okButton, cancelButton} );
        JPanel panel = btnBuilder.getPanel();
        builder.add( panel, cc.xy( 1, 9 ) );

        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        getRootPane().setDefaultButton( okButton );
        
        getContentPane().validate();
        contentPanel.doLayout();
        contentPanel.revalidate();

        pack();
        setLocationRelativeTo( getParent() );
    }

    private void closeDialog( boolean triggerSave )
    {
        boolean showDialog = !dontDisplayAgainChkBox.isSelected();
        InterfacePrefs.ShowCloseOptionsDialog.set( Boolean.valueOf( showDialog ) );
        PhexGuiPrefs.save( false );

        setVisible(false);
        dispose();
    }

    public boolean isOkActivated()
    {
        return isOkActivated;
    }


    private class ButtonActionHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            boolean triggerSave = false;
            if ( e.getActionCommand().equals( "OK" ) )
            {
                boolean minimizeToBackground = minimizeToBackgroundRBtn.isSelected();
                if ( minimizeToBackground != InterfacePrefs.MinimizeToBackground.get().booleanValue() )
                {
                    InterfacePrefs.MinimizeToBackground.set( Boolean.valueOf( minimizeToBackground ) );
                    triggerSave = true;
                }
                isOkActivated = true;
            }

            closeDialog( triggerSave );
        }
    }
}