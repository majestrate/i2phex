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
 *  $Id: FileSizeCondEditor.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.dialogs.filter.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import phex.common.log.NLogger;
import phex.gui.common.FWSizeDefComboBox;
import phex.gui.common.IntegerTextField;
import phex.gui.dialogs.filter.ConditionVisualizer;
import phex.gui.dialogs.filter.RuleDescriptionPanel;
import phex.rules.condition.FileSizeCondition;
import phex.rules.condition.FileSizeCondition.Range;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FileSizeCondEditor extends JDialog
{
    private RuleDescriptionPanel ruleDescPanel;
    
    private FileSizeCondition orgCondition;
    private FileSizeCondition condition;
    
    private JTextField minFileSizeTF;
    private FWSizeDefComboBox minFileSizeUnitComboBox;
    private JTextField maxFileSizeTF;
    private FWSizeDefComboBox maxFileSizeUnitComboBox;

    private JList rangeList;

    private FileSizeRangeModel fileSizeRangeModel;
    
    public FileSizeCondEditor( FileSizeCondition cond, 
        RuleDescriptionPanel descPanel, JDialog parentDialog )
    {
        super( parentDialog, Localizer.getString("FileSizeCondEditor_DialogTitle"), 
            true );
        ruleDescPanel = descPanel;
        orgCondition = cond;
        condition = new FileSizeCondition( cond );
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
        FormLayout layout = new FormLayout("4dlu, d, 2dlu, d, 2dlu, d, d:grow, 4dlu, d, 4dlu", // columns
            "4dlu, p, 2dlu, p, 2dlu, p, 10dlu, p, 2dlu, p, fill:p:grow, 4dlu, " +  // rows
            "p, 2dlu, p 4dlu" ); //btn rows
        PanelBuilder contentPB = new PanelBuilder(layout, contentPanel);
        int columnCount = layout.getColumnCount();
        int rowCount = layout.getRowCount();
        
        JLabel label = new JLabel( Localizer.getString( "FileSizeCondEditor_ConditionToAdd" ) );
        contentPB.add( label, cc.xywh(2, 2, 5, 1) );
        
        label = new JLabel( Localizer.getString( "FileSizeCondEditor_Minimum" ) );
        label.setToolTipText( Localizer.getString( "FileSizeCondEditor_TTTMinimum" ) );
        contentPB.add( label, cc.xywh(2, 4, 1, 1) );
        
        minFileSizeTF = new IntegerTextField( 9 );
        minFileSizeTF.setToolTipText( Localizer.getString( "FileSizeCondEditor_TTTMinimum" ) );
        contentPB.add( minFileSizeTF, cc.xywh(4, 4, 1, 1) );
        
        minFileSizeUnitComboBox = new FWSizeDefComboBox();
        minFileSizeUnitComboBox.setToolTipText( Localizer.getString( "FileSizeCondEditor_TTTMinimum" ) );
        contentPB.add( minFileSizeUnitComboBox, cc.xywh(6, 4, 1, 1) );
        
        label = new JLabel( Localizer.getString( "FileSizeCondEditor_Maximum" ) );
        contentPB.add( label, cc.xywh(2, 6, 1, 1) );
        
        maxFileSizeTF = new IntegerTextField( 9 );
        maxFileSizeTF.setToolTipText( Localizer.getString( "FileSizeCondEditor_TTTMaximum" ) );
        contentPB.add( maxFileSizeTF, cc.xywh(4, 6, 1, 1) );
        
        maxFileSizeUnitComboBox = new FWSizeDefComboBox();
        maxFileSizeUnitComboBox.setToolTipText( Localizer.getString( "FileSizeCondEditor_TTTMaximum" ) );
        contentPB.add( maxFileSizeUnitComboBox, cc.xywh(6, 6, 1, 1) );
        
        JButton addBtn = new JButton( Localizer.getString( "FileSizeCondEditor_Add") );
        addBtn.setDefaultCapable( true );
        addBtn.addActionListener( new AddActionListener() );
        addBtn.setToolTipText( Localizer.getString( "FileSizeCondEditor_TTTAdd") );
        contentPB.add( addBtn, cc.xywh(9, 6, 1, 1) );
        
        label = new JLabel( Localizer.getString( "FileSizeCondEditor_FileSizeConditions" ) );
        contentPB.add( label, cc.xywh(2, 8, 5, 1) );
        
        fileSizeRangeModel = new FileSizeRangeModel();
        rangeList = new JList( fileSizeRangeModel );
        rangeList.setCellRenderer( new FileSizeRangeRenderer() );
        contentPB.add( new JScrollPane( rangeList ), cc.xywh(2, 10, 6, 2) );
        
        JButton removeBtn = new JButton( Localizer.getString( "FileSizeCondEditor_Remove") );
        removeBtn.addActionListener( new RemoveActionListener() );
        removeBtn.setToolTipText( Localizer.getString( "FileSizeCondEditor_TTTRemove") );
        contentPB.add( removeBtn, cc.xywh(9, 10, 1, 1) );
        
        
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
            String minSizeStr = minFileSizeTF.getText().trim();
            String maxSizeStr = maxFileSizeTF.getText().trim();
            
            long finalMinSize = -1;
            try
            {
                if ( minSizeStr.length() > 0 )
                {
                    long minSize = Integer.parseInt( minSizeStr );
                    long minSizeMultiplier = minFileSizeUnitComboBox.getDefMultiplier();
                    finalMinSize = minSizeMultiplier * minSize;
                }
            }
            catch ( NumberFormatException exp )
            {
                displayWrongNumberFormatError( minFileSizeTF );
                return;
            }
            
            
            long finalMaxSize = -1;
            try
            {
                if ( maxSizeStr.length() > 0 )
                {
                    long maxSize = Integer.parseInt( maxSizeStr );
                    long maxSizeMultiplier = maxFileSizeUnitComboBox.getDefMultiplier();
                    finalMaxSize = maxSizeMultiplier * maxSize;
                }
            }
            catch ( NumberFormatException exp )
            {
                displayWrongNumberFormatError( maxFileSizeTF );
                return;
            }
            if ( finalMinSize == -1 && finalMaxSize == -1)
            {
                return;
            }
            condition.addRange(finalMinSize, finalMaxSize);
            minFileSizeTF.setText("");
            maxFileSizeTF.setText("");
            fileSizeRangeModel.fireChange();
            
            minFileSizeTF.requestFocusInWindow();
        }
        
        private void displayWrongNumberFormatError(JTextField textField)
        {
            textField.requestFocus();
            textField.selectAll();
            JOptionPane.showMessageDialog( FileSizeCondEditor.this,
                Localizer.getString( "WrongNumberFormat" ),
                Localizer.getString( "FormatError" ), JOptionPane.ERROR_MESSAGE  );
        }
    }
    
    private final class RemoveActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            int idx = rangeList.getSelectedIndex();
            Object[] values = rangeList.getSelectedValues();
            for ( int i=0; i < values.length; i++)
            {
                condition.removeRange( (Range)values[i] );
            }
            fileSizeRangeModel.fireChange();
            
            int modelSize = fileSizeRangeModel.getSize();
            if ( modelSize == 0 )
            {
                minFileSizeTF.requestFocusInWindow();
            }
            else
            {
                rangeList.setSelectedIndex( Math.max(0, Math.min(idx, modelSize-1 ) ) );
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
    
    public final class FileSizeRangeModel extends AbstractListModel
    {

        public int getSize()
        {
            return condition.getRangesCount();
        }

        public Object getElementAt( int index )
        {
            return condition.getRangesList().get(index);
        }
        
        public void fireChange()
        {
            fireContentsChanged(this, 0, Integer.MAX_VALUE);
        }
        
    }
    
    public final class FileSizeRangeRenderer extends DefaultListCellRenderer
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
                StringBuffer buffer = new StringBuffer();
                ConditionVisualizer.visualizeFileSizeRange(buffer, 
                    (FileSizeCondition.Range)value);
                setText( buffer.toString() );
            }
            return this;
        }
    }
}
