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
 *  Created on 29.04.2005
 *  --- CVS Information ---
 *  $Id: LogBufferDialog.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.swing.*;

import org.apache.commons.lang.SystemUtils;

import phex.common.log.LogRecord;
import phex.gui.common.DialogBanner;
import phex.gui.common.GUIRegistry;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 */
public class LogBufferDialog extends JDialog
{
    private Collection logEntries;
    private DateFormat dateFormat;
    
    public LogBufferDialog( Collection logEntries )
    {
        super(GUIRegistry.getInstance().getMainFrame(), Localizer
            .getString("LogBufferDialog_DialogTitle"), false);
        this.logEntries = logEntries;
        
        dateFormat = new SimpleDateFormat(
            "yyMMdd HH:mm:ss,SSSS" );
        
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
            "p, p, 2dlu, fill:p:grow, 4dlu, p, 4dlu"); //row
        PanelBuilder contentPB = new PanelBuilder(layout, contentPanel);
        
        DialogBanner banner = new DialogBanner(
            Localizer.getString( "LogBufferDialog_Log" ), 
            Localizer.getString( "LogBufferDialog_DisplayLog" ) );
        contentPB.add(banner, cc.xywh(1, 1, 3, 1));
        
        contentPB.add(new JSeparator(), cc.xywh(1, 2, 3, 1));
        
        JTextArea logArea = new JTextArea( 40, 100 );
        logArea.setEditable(false);
        contentPB.add( new JScrollPane( logArea ), cc.xy(2, 4) );
        
        JButton closeBtn = new JButton( Localizer.getString( "Close" ) );
        closeBtn.addActionListener( closeEventHandler );
        contentPB.add( ButtonBarFactory.buildCloseBar( closeBtn ), cc.xy(2, 6) );
        
        StringBuffer textBuffer = new StringBuffer();
        Iterator iterator = logEntries.iterator();
        while (iterator.hasNext())
        {
            LogRecord record = (LogRecord) iterator.next();
            textBuffer.append( dateFormat.format( new Date( record.getTimestamp() ) ) )
                .append( "::" ).append( record.getMessage() )
                .append( SystemUtils.LINE_SEPARATOR );
        }
        logArea.setText(textBuffer.toString());
        //logArea.setCaretPosition(0);

        pack();
        setLocationRelativeTo(getParent());
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