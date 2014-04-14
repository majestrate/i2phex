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
 *  Created on 02.12.2005
 *  --- CVS Information ---
 *  $Id: FileNameCondEditor.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.dialogs.filter.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;

import javax.swing.*;

import phex.common.log.NLogger;
import phex.gui.dialogs.filter.RuleDescriptionPanel;
import phex.rules.condition.FilenameCondition;
import phex.utils.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FileNameCondEditor extends JDialog
{
    private RuleDescriptionPanel ruleDescPanel;
    
    private FilenameCondition orgCondition;
    private FilenameCondition condition;
    
    private JTextField termTF;

    private JList termsList;

    private TermsModel termsModel;
    
    public FileNameCondEditor( FilenameCondition cond, 
        RuleDescriptionPanel descPanel, JDialog parentDialog )
    {
        super( parentDialog, Localizer.getString("FileNameCondEditor_DialogTitle"), 
            true );
        ruleDescPanel = descPanel;
        orgCondition = cond;
        condition = new FilenameCondition( cond );
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
        
        JLabel label = new JLabel( Localizer.getString( "FileNameCondEditor_ConditionToAdd" ) );
        contentPB.add( label, cc.xywh(2, 2, 5, 1) );
        
        label = new JLabel( Localizer.getString( "FileNameCondEditor_Term" ) );
        label.setToolTipText( Localizer.getString( "FileNameCondEditor_TTTTerm" ) );
        contentPB.add( label, cc.xywh(2, 4, 1, 1) );
        
        termTF = new JTextField( 25 );
        termTF.setToolTipText( Localizer.getString( "FileNameCondEditor_TTTTerm" ) );
        contentPB.add( termTF, cc.xywh(4, 4, 1, 1) );
                
        JButton addBtn = new JButton( Localizer.getString( "FileNameCondEditor_Add") );
        addBtn.setDefaultCapable( true );
        addBtn.addActionListener( new AddActionListener() );
        addBtn.setToolTipText( Localizer.getString( "FileNameCondEditor_TTTAdd") );
        contentPB.add( addBtn, cc.xywh(7, 4, 1, 1) );
        
        label = new JLabel( Localizer.getString( "FileNameCondEditor_FileNameConditions" ) );
        contentPB.add( label, cc.xywh(2, 6, 5, 1) );
        
        termsModel = new TermsModel();
        termsList = new JList( termsModel );
        termsList.setCellRenderer( new TermsRenderer() );
        contentPB.add( new JScrollPane( termsList ), cc.xywh(2, 8, 4, 2) );
        
        JButton removeBtn = new JButton( Localizer.getString( "FileNameCondEditor_Remove") );
        removeBtn.addActionListener( new RemoveActionListener() );
        removeBtn.setToolTipText( Localizer.getString( "FileNameCondEditor_TTTRemove") );
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
            String term = termTF.getText().trim();
            if ( StringUtils.isEmpty(term) )
            {
                return;
            }
            condition.addTerm(term);
            termTF.setText("");
            termsModel.fireChange();
            termTF.requestFocusInWindow();
        }
    }
    
    private final class RemoveActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            int idx = termsList.getSelectedIndex();
            Object[] values = termsList.getSelectedValues();
            for ( int i=0; i < values.length; i++)
            {
                condition.removeTerm( (String)values[i] );
            }
            termsModel.fireChange();
            
            int modelSize = termsModel.getSize();
            if ( modelSize == 0 )
            {
                termTF.requestFocusInWindow();
            }
            else
            {
                termsList.setSelectedIndex( Math.max(0, Math.min(idx, modelSize-1 ) ) );
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
    
    public final class TermsModel extends AbstractListModel
    {

        public int getSize()
        {
            return condition.getTermsCount();
        }

        public Object getElementAt( int index )
        {
            return condition.getTermsList().get(index);
        }
        
        public void fireChange()
        {
            fireContentsChanged(this, 0, Integer.MAX_VALUE);
        }
        
    }
    
    public final class TermsRenderer extends DefaultListCellRenderer
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
                setText( "'" + value.toString() + "'" );
            }
            return this;
        }
    }
}
