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
 *  $Id: ConsequencePanel.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.dialogs.filter.wizard.consequence;

import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import phex.common.log.NLogger;
import phex.gui.common.table.FWTable;
import phex.gui.dialogs.filter.wizard.FilterWizardDialog;
import phex.rules.Rule;
import phex.rules.consequence.Consequence;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ConsequencePanel extends JPanel
{
    private FilterWizardDialog parent;
    private ConsequenceTableModel consequenceTableModel;
    private FWTable consequenceTable;
    private JLabel selectConsequenceLbl;

    public ConsequencePanel( FilterWizardDialog parent )
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
        
        selectConsequenceLbl = new JLabel( 
                    Localizer.getString("RuleWizard_SelectRuleConsequence") );
        contentPB.add( selectConsequenceLbl, cc.xywh(1, 1, 1, 1));
        
        consequenceTableModel = new ConsequenceTableModel( this );
        consequenceTable = new FWTable( consequenceTableModel );
        consequenceTable.setShowVerticalLines(false);
        JTableHeader tableHeader = consequenceTable.getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        // adjust column witdh of checkbox
        JCheckBox box = (JCheckBox) consequenceTable.getDefaultRenderer(Boolean.class);
        TableColumn column = consequenceTable.getColumnModel().getColumn(0);
        column.setMaxWidth( box.getPreferredSize().width+2 );
        column.setMinWidth( box.getPreferredSize().width+2 );
        
        consequenceTable.getColumnModel().getColumn(1).setCellRenderer( new ConsequenceCellRenderer() );
        contentPB.add( FWTable.createFWTableScrollPane( consequenceTable ), cc.xywh(1, 3, 1, 1));
    }
    
    public void updateRuleData()
    {
        Rule rule = parent.getEditRule();

        // reset all selections.
        int rowCount = consequenceTableModel.getRowCount();
        for ( int i = 0; i < rowCount; i++ )
        {
            consequenceTableModel.consequences[i][0] = Boolean.FALSE;
        }
        
        // reselect rows
        List consequencesList = rule.getConsequences();
        Iterator iterator = consequencesList.iterator();
        while( iterator.hasNext() )
        {
            Consequence consequence = (Consequence) iterator.next();
            int row = consequenceTableModel.getRowOf(consequence);
            consequenceTableModel.consequences[row][0] = Boolean.TRUE;
        }
        // limit editing capabilities.
        consequenceTable.setEnabled( !rule.isDefaultRule() );
        selectConsequenceLbl.setEnabled( !rule.isDefaultRule() );
        
        // update view...
        consequenceTableModel.fireTableDataChanged();
    }
    
    public void ruleStatusChanged( Class consequenceClass, boolean status )
    {
        Rule editRule = parent.getEditRule();
        if ( status )
        {
            // create condition
            try
            {
                Consequence newConsequence = (Consequence) consequenceClass.newInstance();
                editRule.addConsequence(newConsequence);
                parent.updateRuleData();
            }
            catch (InstantiationException exp)
            {
                NLogger.error(ConsequencePanel.class, exp, exp);
            }
            catch (IllegalAccessException exp)
            {
                NLogger.error(ConsequencePanel.class, exp, exp);
            }
        }
        else
        {
            // in this case we need to remove all consequences of the class type.
            List consequences = editRule.getConsequences();
            for ( int i = consequences.size()-1; i >=0; i-- )
            {
                Consequence consequence = (Consequence)consequences.get(i);
                if ( consequence.getClass() == consequenceClass )
                {
                    editRule.removeConsequence( consequence );
                }
            }
            parent.updateRuleData();
        }
    }
}
