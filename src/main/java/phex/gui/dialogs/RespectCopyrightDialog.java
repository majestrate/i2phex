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
 *  Created on 27.02.2005
 *  --- CVS Information ---
 *  $Id: RespectCopyrightDialog.java 4067 2007-12-01 11:02:44Z complication $
 */
package phex.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.*;

import javax.swing.*;

import phex.gui.common.GUIRegistry;
import phex.gui.common.HTMLMultiLinePanel;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 */
public class RespectCopyrightDialog extends JDialog
{	
    private HTMLMultiLinePanel textPane;
    private JCheckBox dontShowAgainChkbx;
    
    public RespectCopyrightDialog()
    {
        super(GUIRegistry.getInstance().getMainFrame(), Localizer
            .getString("RespectCopyrightDialog_DialogTitle"), true);
        prepareComponent();
    }
    
    /**
     * 
     */
    private void prepareComponent()
    {
        CloseEventHandler closeEventHandler = new CloseEventHandler(); 
        addWindowListener( closeEventHandler );
        
        Container contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );
        JPanel contentPanel = new JPanel();
        //JPanel contentPanel = new FormDebugPanel();
        contentPane.add(contentPanel, BorderLayout.CENTER);
        
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout("4dlu, 200dlu, 4dlu", // columns
            "4dlu, p, 4dlu, p, 4dlu"); //row
        PanelBuilder contentPB = new PanelBuilder(layout, contentPanel);
        
        textPane = new HTMLMultiLinePanel(
            Localizer.getString( "RespectCopyrightDialog_Text" ) );
        contentPB.add( textPane, cc.xy(2, 2) );
        
        dontShowAgainChkbx = new JCheckBox( 
            Localizer.getString( "RespectCopyrightDialog_DontShowAgain" ) );
        
        JButton okBtn = new JButton( Localizer.getString( "OK" ) );
        okBtn.addActionListener( closeEventHandler );        
        
        ButtonBarBuilder btnBarBuilder = new ButtonBarBuilder();
        btnBarBuilder.addGridded( dontShowAgainChkbx );
        btnBarBuilder.addRelatedGap();
        btnBarBuilder.addGlue();
        btnBarBuilder.addGriddedButtons( new JButton[] {okBtn});
        
        contentPB.add( btnBarBuilder.getPanel(), cc.xy(2, 4) );
        
        pack();
        // causes the textPane to layout correctly which is necessary for 
        // correct window size
        textPane.getPreferredSize();
        setSize( getPreferredSize() );
        setLocationRelativeTo(getParent());
        setAlwaysOnTop( true );
    }
    
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
    
    private final class CloseEventHandler extends WindowAdapter implements ActionListener
    {
        public void windowClosing(WindowEvent evt)
        {
            handleClose();
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            handleClose();
        }

        /**
         * 
         */
        private void handleClose()
        {
            boolean isSelected = dontShowAgainChkbx.isSelected();
            if ( isSelected )
            {
                GUIRegistry.getInstance().setRespectCopyrightNoticeShown(!isSelected);
            }
            closeDialog();
        }
    }
}
