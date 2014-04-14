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
 *  $Id: MediaTypeCondEditor.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.dialogs.filter.editors;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.*;
import java.util.Set;

import javax.swing.*;
import javax.swing.table.*;

import phex.common.MediaType;
import phex.common.log.NLogger;
import phex.gui.dialogs.filter.RuleDescriptionPanel;
import phex.rules.condition.MediaTypeCondition;
import phex.utils.Localizer;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MediaTypeCondEditor extends JDialog
{
    private RuleDescriptionPanel ruleDescPanel;
    
    private MediaTypeCondition orgCondition;
    private MediaTypeCondition condition;

    private JTable mediaTypeTable;

    private MediaTypeModel mediaTypeModel;
    
    public MediaTypeCondEditor( MediaTypeCondition cond, 
        RuleDescriptionPanel descPanel, JDialog parentDialog )
    {
        super( parentDialog, Localizer.getString("MediaTypeCondEditor_DialogTitle"), 
            true );
        ruleDescPanel = descPanel;
        orgCondition = cond;
        condition = new MediaTypeCondition( cond );
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
        FormLayout layout = new FormLayout("4dlu, d:grow, 4dlu", // columns
            "4dlu, p, 2dlu, fill:100dlu:grow, 4dlu, " +  // rows
            "p, 2dlu, p 4dlu" ); //btn rows
        PanelBuilder contentPB = new PanelBuilder(layout, contentPanel);
        int columnCount = layout.getColumnCount();
        int rowCount = layout.getRowCount();
        
        JLabel label = new JLabel( Localizer.getString( "MediaTypeCondEditor_ConditionToAdd" ) );
        contentPB.add( label, cc.xywh(2, 2, 1, 1) );
        
        mediaTypeModel = new MediaTypeModel( );
        mediaTypeTable = new JTable( mediaTypeModel );
        JTableHeader tableHeader = mediaTypeTable.getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        // adjust column witdh of checkbox
        JCheckBox box = (JCheckBox) mediaTypeTable.getDefaultRenderer(Boolean.class);
        TableColumn column = mediaTypeTable.getColumnModel().getColumn(0);
        column.setMaxWidth( box.getPreferredSize().width+2 );
        column.setMinWidth( box.getPreferredSize().width+2 );
        mediaTypeTable.getColumnModel().getColumn(1).setCellRenderer( new MediaTypeCellRenderer() );
        //ToolTipManager.sharedInstance().registerComponent( mediaTypeTable );
        contentPB.add( new JScrollPane( mediaTypeTable ), cc.xywh(2, 4, 1, 1));
        
        // button bar
        contentPB.add( new JSeparator(), cc.xywh( 1, rowCount - 3, columnCount, 1 ) );
        
        JButton okBtn = new JButton( Localizer.getString( "OK" ) );
        okBtn.addActionListener( new OkBtnListener());
        okBtn.setDefaultCapable( true );
        okBtn.setRequestFocusEnabled( true );
        
        JButton cancelBtn = new JButton( Localizer.getString( "Cancel" ) );
        cancelBtn.addActionListener( closeEventHandler );
        
        JPanel btnPanel = ButtonBarFactory.buildOKCancelBar( okBtn, cancelBtn);        
        contentPB.add( btnPanel, cc.xywh( 2, rowCount - 1, columnCount - 2, 1 ) );
        
        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        getRootPane().setDefaultButton( okBtn );
        
        pack();
        setLocationRelativeTo( getParent() );
    }
    
    private void closeDialog()
    {
        setVisible(false);
        dispose();
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
    
    private final class MediaTypeModel extends AbstractTableModel
    {
        protected Object[][] conditionTypes;

        public MediaTypeModel()
        {
            conditionTypes = new Object[MediaType.getAllMediaTypes().length][2];
            MediaType[] mediaTypes = MediaType.getAllMediaTypes();
            Set types = condition.getTypes();
            for ( int i = 0; i < mediaTypes.length; i++ )
            {
                if ( types.contains(mediaTypes[i]) )
                {
                    conditionTypes[i][0] = Boolean.TRUE;
                }
                else
                {
                    conditionTypes[i][0] = Boolean.FALSE;
                }
                conditionTypes[i][1] = mediaTypes[i];
            }
        }

        public int getColumnCount()
        {
            return 2;
        }

        public int getRowCount()
        {
            return conditionTypes.length;
        }

        public Object getValueAt( int rowIndex, int columnIndex )
        {
            return conditionTypes[rowIndex][columnIndex];
        }

        public String getColumnName( int column )
        {
            return "";
        }

        public Class getColumnClass( int col )
        {
            switch ( col )
            {
            case 0:
                return Boolean.class;
            case 1:
                return String.class;
            }
            return Object.class;
        }

        public boolean isCellEditable( int row, int col )
        {
            return (col == 0);
        }

        public void setValueAt( Object aValue, int row, int column )
        {
            Boolean bolVal = (Boolean)aValue;
            conditionTypes[row][0] = aValue;
            if ( bolVal.booleanValue() )
            {
                condition.addType((MediaType) conditionTypes[row][1]);
            }
            else
            {
                condition.removeType((MediaType) conditionTypes[row][1]);
            }
        }

        /**
         * Returns the index of the row that represents the given condition type
         * @param mediaType
         * @return
         */
        public int getRowOf( MediaType mediaType )
        {
            for ( int i = 0; i < conditionTypes.length; i++ )
            {
                if ( conditionTypes[i][1] == mediaType )
                {
                    return i;
                }
            }
            return -1;
        }
    }
    
    public final class MediaTypeCellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public void setValue( Object value )
        {       
            if ( value instanceof MediaType )
            {
                MediaType mediaType = (MediaType) value;
                setText( Localizer.getString( mediaType.getName() ) );
                setToolTipText( mediaType.getFileTypesUIText() );
            }
        }
    }
}
