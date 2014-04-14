/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2007 Phex Development Group
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
 *  Created on 26.12.2006
 *  --- CVS Information ---
 *  $Id: FileUrnCondEditor.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.dialogs.filter.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;

import javax.swing.*;

import phex.common.URN;
import phex.common.log.NLogger;
import phex.gui.dialogs.filter.RuleDescriptionPanel;
import phex.rules.condition.FileUrnCondition;
import phex.utils.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FileUrnCondEditor extends JDialog
{
    private RuleDescriptionPanel ruleDescPanel;
    
    private FileUrnCondition orgCondition;
    private FileUrnCondition condition;
    
    private JTextField urnTF;

    private JList urnList;

    private UrnModel urnModel;
    
    public FileUrnCondEditor( FileUrnCondition cond, 
        RuleDescriptionPanel descPanel, JDialog parentDialog )
    {
        super( parentDialog, Localizer.getString("FileUrnCondEditor_DialogTitle"), 
            true );
        ruleDescPanel = descPanel;
        orgCondition = cond;
        condition = new FileUrnCondition( cond );
        prepareComponent();
    }

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
        FormLayout layout = new FormLayout("4dlu, d, 2dlu, d, d:grow, 4dlu, d, 4dlu", // columns
            "4dlu, p, 2dlu, p, 10dlu, p, 2dlu, p, fill:p:grow, 4dlu, " +  // rows
            "p, 2dlu, p 4dlu" ); //btn rows
        PanelBuilder contentPB = new PanelBuilder(layout, contentPanel);
        int columnCount = layout.getColumnCount();
        int rowCount = layout.getRowCount();
        
        JLabel label = new JLabel( Localizer.getString( "FileUrnCondEditor_ConditionToAdd" ) );
        contentPB.add( label, cc.xywh(2, 2, 5, 1) );
        
        label = new JLabel( Localizer.getString( "FileUrnCondEditor_Urn" ) );
        label.setToolTipText( Localizer.getString( "FileUrnCondEditor_TTTUrn" ) );
        contentPB.add( label, cc.xywh(2, 4, 1, 1) );
        
        urnTF = new JTextField( 30 );
        urnTF.setToolTipText( Localizer.getString( "FileUrnCondEditor_TTTUrn" ) );
        contentPB.add( urnTF, cc.xywh(4, 4, 2, 1) );
                
        JButton addBtn = new JButton( Localizer.getString( "FileUrnCondEditor_Add") );
        addBtn.setDefaultCapable( true );
        addBtn.addActionListener( new AddActionListener() );
        addBtn.setToolTipText( Localizer.getString( "FileUrnCondEditor_TTTAdd") );
        contentPB.add( addBtn, cc.xywh(7, 4, 1, 1) );
        
        label = new JLabel( Localizer.getString( "FileUrnCondEditor_UrnConditions" ) );
        contentPB.add( label, cc.xywh(2, 6, 5, 1) );
        
        urnModel = new UrnModel();
        urnList = new JList( urnModel );
        urnList.setCellRenderer( new UrnRenderer() );
        contentPB.add( new JScrollPane( urnList ), cc.xywh(2, 8, 4, 2) );
        
        JButton removeBtn = new JButton( Localizer.getString( "FileUrnCondEditor_Remove") );
        removeBtn.addActionListener( new RemoveActionListener() );
        removeBtn.setToolTipText( Localizer.getString( "FileUrnCondEditor_TTTRemove") );
        contentPB.add( removeBtn, cc.xywh(7, 8, 1, 1) );
        
        
        // button bar
        contentPB.add( new JSeparator(), cc.xywh( 1, rowCount - 3, columnCount, 1 ) );
        
        JButton okBtn = new JButton( Localizer.getString( "OK" ) );
        okBtn.addActionListener( new OkBtnListener());
        okBtn.setRequestFocusEnabled( true );
        
        JButton cancelBtn = new JButton( Localizer.getString( "Cancel" ) );
        cancelBtn.addActionListener( closeEventHandler );
        
        JPanel btnPanel = ButtonBarFactory.buildOKCancelBar( okBtn, cancelBtn);        
        contentPB.add( btnPanel, cc.xywh( 2, rowCount - 1, columnCount - 2, 1 ) );
        
        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        getRootPane().setDefaultButton( addBtn );
        
        pack();
        setLocationRelativeTo( getParent() );
    }
    
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
    
    private final class AddActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            String urn = urnTF.getText().trim();
            if ( StringUtils.isEmpty( urn ) )
            {
                return;
            }
            if ( urn.indexOf( ':' ) == -1 )
            {// no urn segment separator char... try to add urn:sha1:
                urn = URN.URN_PREFIX + URN.SHA1 + ":" + urn;
            }
            if ( !URN.isValidURN( urn ) )
            {
                return;
            }
            condition.addUrn( new URN( urn ) );
            urnTF.setText("");
            urnModel.fireChange();
            urnTF.requestFocusInWindow();
        }
    }
    
    private final class RemoveActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            int idx = urnList.getSelectedIndex();
            Object[] values = urnList.getSelectedValues();
            for ( int i=0; i < values.length; i++)
            {
                condition.removeHash( (URN)values[i] );
            }
            urnModel.fireChange();
            
            int modelSize = urnModel.getSize();
            if ( modelSize == 0 )
            {
                urnTF.requestFocusInWindow();
            }
            else
            {
                urnList.setSelectedIndex( Math.max(0, Math.min(idx, modelSize-1 ) ) );
            }
        }
    }

    private final class OkBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                orgCondition.update(condition);
                ruleDescPanel.updateRuleData();
                closeDialog();
            }
            catch ( Throwable th )
            {
                NLogger.error( OkBtnListener.class, th, th );
            }
        }
    }
    
    private final class CloseEventHandler extends WindowAdapter implements ActionListener
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
    
    public final class UrnModel extends AbstractListModel
    {

        public int getSize()
        {
            return condition.getUrnCount();
        }

        public Object getElementAt( int index )
        {
            return condition.getUrnList().get(index);
        }
        
        public void fireChange()
        {
            fireContentsChanged(this, 0, Integer.MAX_VALUE);
        }
        
    }
    
    public final class UrnRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent( JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus )
        {
            super.getListCellRendererComponent(list, value, index, 
                isSelected, cellHasFocus);

            if ( value == null )
            {
                setText( "" );
            }
            else
            {
                setText( ((URN)value).getAsString() );
            }
            return this;
        }
    }
}
