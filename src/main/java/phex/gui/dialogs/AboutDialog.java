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
 *  $Id: AboutDialog.java 3633 2006-11-29 16:25:06Z gregork $
 */
package phex.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.apache.commons.lang.SystemUtils;

import phex.Res;
import phex.gui.common.DialogBanner;
import phex.gui.common.GUIRegistry;
import phex.gui.common.HTMLMultiLinePanel;
import phex.prefs.core.PrivateNetworkConstants;
import phex.utils.Localizer;
import phex.utils.VersionUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 */
public class AboutDialog extends JDialog
{
    private JTextArea environmentInfo;

    public AboutDialog()
    {
        super(GUIRegistry.getInstance().getMainFrame(), Localizer
            .getString("AboutPhex_DialogTitle"), false);
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
        FormLayout layout = new FormLayout("4dlu, fill:d:grow, 4dlu", // columns
            "p, p, 2dlu, p, 4dlu, p, 4dlu"); //row
        PanelBuilder contentPB = new PanelBuilder(layout, contentPanel);
        
        Object[] objArr =
        {
            PrivateNetworkConstants.PRIVATE_BUILD_ID + VersionUtils.getProgramVersion(),
            VersionUtils.getBuild()
        };
        DialogBanner banner = new DialogBanner(
            "Phex", 
            Localizer.getFormatedString( "AboutPhex_VersionInfo", objArr ) );
        contentPB.add(banner, cc.xywh(1, 1, 3, 1));
        
        contentPB.add(new JSeparator(), cc.xywh(1, 2, 3, 1));
        
        JTabbedPane tabbedPane = new JTabbedPane( );
        contentPB.add( tabbedPane, cc.xy(2, 4) );
        
        JButton closeBtn = new JButton( Localizer.getString( "Close" ) );
        closeBtn.addActionListener( closeEventHandler );
        contentPB.add( ButtonBarFactory.buildCloseBar( closeBtn ), cc.xy(2, 6) );
        
        JPanel aboutPanel = new JPanel();
        layout = new FormLayout("4dlu, fill:d:grow, 4dlu", // columns
            "4dlu, p, 4dlu"); //row
        PanelBuilder aboutPB = new PanelBuilder(layout, aboutPanel);
        tabbedPane.addTab( Localizer.getString( "AboutPhex_About" ), aboutPanel );
        
        Object[] objArr2 =
        {
            Res.getStr("Program.Url")
        };
        HTMLMultiLinePanel aboutHtml = new HTMLMultiLinePanel(
            Localizer.getFormatedString( "AboutPhex_AboutText", objArr2 ) );
        aboutPB.add(aboutHtml, cc.xy(2, 2));
        
        
        JPanel envPanel = new JPanel();
        layout = new FormLayout("2dlu, fill:d:grow, 2dlu", // columns
            "2dlu, p, 2dlu, p, 2dlu"); //row
        PanelBuilder envPB = new PanelBuilder(layout, envPanel);
        tabbedPane.addTab( Localizer.getString( "AboutPhex_Environment" ), envPanel );
        
        environmentInfo = new JTextArea( 12, 55 );
        environmentInfo.setEditable(false);
        envPB.add( new JScrollPane( environmentInfo ), cc.xy(2, 2) );
        
        StringBuffer envTextBuffer = new StringBuffer();
        Properties pros = System.getProperties();
        ArrayList<String> list = new ArrayList( pros.keySet() );
        Collections.sort(list);
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext())
        {
            String key = iterator.next();
            String value = (String) pros.get(key);
            envTextBuffer.append( key ).append( " = " ).append( value ).append( SystemUtils.LINE_SEPARATOR );
        }
        environmentInfo.setText(envTextBuffer.toString());
        environmentInfo.setCaretPosition(0);
        
        JButton copyBtn = new JButton( Localizer.getString( "Copy" ) );
        copyBtn.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e )
            {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection( environmentInfo.getText() ), null);
            }
        });
        envPB.add( ButtonBarFactory.buildLeftAlignedBar( copyBtn ), cc.xy(2, 4) );

        pack();
        setLocationRelativeTo(getParent());
    }
    
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
    
    protected final class CloseEventHandler extends WindowAdapter implements ActionListener
    {
        @Override
		public void windowClosing(WindowEvent evt)
        {
            closeDialog();
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            closeDialog();
        }
    }
}
