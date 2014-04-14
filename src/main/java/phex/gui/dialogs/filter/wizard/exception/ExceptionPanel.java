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
 *  Created on 24.11.2005
 *  --- CVS Information ---
 *  $Id: ExceptionPanel.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.dialogs.filter.wizard.exception;

import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import phex.common.log.NLogger;
import phex.gui.common.table.FWTable;
import phex.gui.dialogs.filter.wizard.FilterWizardDialog;
import phex.rules.Rule;
import phex.rules.condition.Condition;
import phex.rules.condition.NotCondition;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ExceptionPanel extends JPanel
{
    private FilterWizardDialog parent;
    private ExceptionTableModel exceptionTableModel;
    private FWTable exceptionsTable;
    private JLabel selectExceptionLabel;

    public ExceptionPanel( FilterWizardDialog parent )
    {
        this.parent = parent;
        prepareComponent();
    }
    
    private void prepareComponent()
    {
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout("fill:d:grow", // columns
            "p, 2dlu, fill:75dlu:grow" );
        PanelBuilder contentPB = new PanelBuilder(layout, this);
        //int columnCount = layout.getColumnCount();
        //int rowCount = layout.getRowCount();
        
        selectExceptionLabel = new JLabel( 
                    Localizer.getString("RuleWizard_SelectRuleException") );
        contentPB.add( selectExceptionLabel, cc.xywh(1, 1, 1, 1));
        
        exceptionTableModel = new ExceptionTableModel( this );
        exceptionsTable = new FWTable( exceptionTableModel );
        exceptionsTable.setShowVerticalLines(false);
        JTableHeader tableHeader = exceptionsTable.getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        // adjust column witdh of checkbox
        JCheckBox box = (JCheckBox) exceptionsTable.getDefaultRenderer(Boolean.class);
        TableColumn column = exceptionsTable.getColumnModel().getColumn(0);
        column.setMaxWidth( box.getPreferredSize().width+2 );
        column.setMinWidth( box.getPreferredSize().width+2 );
        
        exceptionsTable.getColumnModel().getColumn(1).setCellRenderer( new ExceptionCellRenderer() );
        contentPB.add( FWTable.createFWTableScrollPane( exceptionsTable ), 
            cc.xywh(1, 3, 1, 1));
    }
    
    public void updateRuleData()
    {
        Rule rule = parent.getEditRule();

        // reset all selections.
        int rowCount = exceptionTableModel.getRowCount();
        for ( int i = 0; i < rowCount; i++ )
        {
            exceptionTableModel.conditions[i][0] = Boolean.FALSE;
        }
        
        // reselect rows
        List conditionsList = rule.getConditions();
        Iterator iterator = conditionsList.iterator();
        while( iterator.hasNext() )
        {
            Condition condition = (Condition) iterator.next();
            if ( !(condition instanceof NotCondition) )
            {
                continue;
            }
            int row = exceptionTableModel.getRowOf(condition);
            exceptionTableModel.conditions[row][0] = Boolean.TRUE;
        }
        // limit editing capabilities.
        exceptionsTable.setEnabled( !rule.isDefaultRule() );
        selectExceptionLabel.setEnabled( !rule.isDefaultRule() );
        
        // update view...
        exceptionTableModel.fireTableDataChanged();
    }
    
    public void ruleStatusChanged( Class conditionClass, boolean status )
    {
        Rule editRule = parent.getEditRule();
        if ( status )
        {
            // create condition
            try
            {
                Condition newCondition = (Condition) conditionClass.newInstance();
                editRule.addCondition( new NotCondition( newCondition ) );
                parent.updateRuleData();
            }
            catch (InstantiationException exp)
            {
                NLogger.error(ExceptionPanel.class, exp, exp);
            }
            catch (IllegalAccessException exp)
            {
                NLogger.error(ExceptionPanel.class, exp, exp);
            }
        }
        else
        {
            // in this case we need to remove all not conditions containing this 
            // class type.
            List conditions = editRule.getConditions();
            for ( int i = conditions.size()-1; i >=0; i-- )
            {
                Condition condition = (Condition)conditions.get(i);
                if ( condition instanceof NotCondition )
                {
                    NotCondition notCondition = (NotCondition)condition;
                    if ( notCondition.getContainedCondition().getClass() == 
                         conditionClass )
                    {
                        editRule.removeCondition( condition );
                    }
                }
                
            }
            parent.updateRuleData();
        }
    }
}
