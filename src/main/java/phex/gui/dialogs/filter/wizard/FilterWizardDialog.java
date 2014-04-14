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
 *  Created on 22.11.2005
 *  --- CVS Information ---
 *  $Id: FilterWizardDialog.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.dialogs.filter.wizard;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import phex.common.log.NLogger;
import phex.gui.dialogs.filter.RuleDescriptionPanel;
import phex.gui.dialogs.filter.RuleDescOwnerDialog;
import phex.gui.dialogs.filter.wizard.condition.ConditionPanel;
import phex.gui.dialogs.filter.wizard.consequence.ConsequencePanel;
import phex.gui.dialogs.filter.wizard.exception.ExceptionPanel;
import phex.rules.Rule;
import phex.rules.condition.Condition;
import phex.rules.condition.NotCondition;
import phex.utils.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FilterWizardDialog extends JDialog implements RuleDescOwnerDialog
{
    private static final int CONDITION_PAGE = 1;
    private static final int CONSEQUENCE_PAGE = 2;
    private static final int EXCEPTION_PAGE = 3;
    private static final int RULE_PROPERTIES_PAGE = 4;
    private Rule editRule;
    
    private JPanel ruleEditPanel;
    
    private ConditionPanel conditionPanel;
    private ConsequencePanel consequencePanel;
    private ExceptionPanel exceptionPanel;
    private RulePropertiesPanel rulePropertiesPanel;
    private RuleDescriptionPanel ruleDescPanel;
    
    private int currentPage = CONDITION_PAGE;
    private JButton finishBtn;
    private JButton backBtn;
    private JButton nextBtn;

    public FilterWizardDialog( JDialog parent )
    {
        this( new Rule(), parent );
    }
    
    public FilterWizardDialog( Rule ruleToEdit, JDialog parent )
    {
        super( parent, Localizer.getString("RuleWizard_DialogTitle"), true );
        editRule = ruleToEdit;
        if ( editRule.isDefaultRule() )
        {// default rule has limited editing capabilities..
         // we swith to the only relevant page first...
            currentPage = RULE_PROPERTIES_PAGE;
        }
        prepareComponent();
        updateRuleData();
    }
        
    public Rule getEditRule()
    {
        return editRule;
    }
    
    public void updateRuleData()
    {
        if ( conditionPanel != null )
        {
            conditionPanel.updateRuleData();
        }
        if ( consequencePanel != null )
        {
            consequencePanel.updateRuleData();
        }
        if ( exceptionPanel != null )
        {
            exceptionPanel.updateRuleData();
        }
        if ( rulePropertiesPanel != null )
        {
            rulePropertiesPanel.updateRuleData();
        }
        ruleDescPanel.updateRuleData( );
        
        setFinishBtnEnabled( !StringUtils.isEmpty( editRule.getName() ) );
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
        FormLayout layout = new FormLayout("4dlu, fill:d:grow, 4dlu", // columns
            "4dlu, fill:p:grow, 12dlu, fill:p:grow, 8dlu," +  // rows
            "p, 2dlu, p 4dlu" ); //btn rows
        PanelBuilder contentPB = new PanelBuilder(layout, contentPanel);
        int columnCount = layout.getColumnCount();
        int rowCount = layout.getRowCount();
        
        ruleEditPanel = new JPanel();
        ruleEditPanel.setLayout(new BorderLayout());
        contentPB.add( ruleEditPanel, cc.xywh( 2, 2, 1, 1 ) );
        
        ruleDescPanel = new RuleDescriptionPanel( this );
        contentPB.add( ruleDescPanel, cc.xywh( 2, 4, 1, 1 ) );
        
        // button bar
        contentPB.add( new JSeparator(), cc.xywh( 1, rowCount - 3, columnCount, 1 ) );
        
        backBtn = new JButton( Localizer.getString( "WizardDialog_Back" ) );
        backBtn.addActionListener( new BackBtnListener());
        
        nextBtn = new JButton( Localizer.getString( "WizardDialog_Next" ) );
        nextBtn.setDefaultCapable( true );
        nextBtn.setRequestFocusEnabled( true );
        nextBtn.addActionListener( new NextBtnListener());
        
        finishBtn = new JButton( Localizer.getString( "WizardDialog_Finish" ) );
        finishBtn.addActionListener( new FinishBtnListener());
        
        JButton cancelBtn = new JButton( Localizer.getString( "WizardDialog_Cancel" ) );
        cancelBtn.addActionListener( closeEventHandler );
        
        JPanel btnPanel = ButtonBarFactory.buildWizardBar(backBtn, nextBtn,
            finishBtn, cancelBtn);        
        contentPB.add( btnPanel, cc.xywh( 2, rowCount - 1, columnCount - 2, 1 ) );
        
        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        getRootPane().setDefaultButton( nextBtn );
        
        // set first panel to show...
        updatePage();
        
        pack();
        int height = getHeight();
        setSize( height*5/4, height );
        
        setLocationRelativeTo( getParent() );
    }
    
    public void setFinishBtnEnabled( boolean state )
    {
        finishBtn.setEnabled(state);
    }
    
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
    
    private boolean isRuleInputComplete()
    {
        List list = editRule.getConditions();
        Iterator iterator = list.iterator();
        while ( iterator.hasNext() )
        {
            Condition condition = (Condition) iterator.next();
            if ( condition instanceof NotCondition )
            {
                condition = ((NotCondition)condition).getContainedCondition();
            }
            if ( !condition.isComplete() )
            {
                return false;
            }
        }
        return true;
        // currently consequences dont require a input
//        list = editRule.getConsequences();
//        iterator = list.iterator();
//        while ( iterator.hasNext() )
//        {
//            Consequence consequence = (Consequence) iterator.next();
//            if ( !consequence.isComplete() )
//            {
//                return false;
//            }
//        }
    }
    
    private void updatePage()
    {
        ruleEditPanel.removeAll();
        JPanel newPage = null;
        switch ( currentPage )
        {
        case CONDITION_PAGE:
            if ( conditionPanel == null )
            {
                conditionPanel = new ConditionPanel(this);
            }
            newPage = conditionPanel;
            backBtn.setEnabled(false);
            nextBtn.setEnabled(true);
            break;
        case CONSEQUENCE_PAGE:
            if ( consequencePanel == null )
            {
                consequencePanel = new ConsequencePanel(this);
            }
            newPage = consequencePanel;
            backBtn.setEnabled(true);
            nextBtn.setEnabled(true);
            break;
        case EXCEPTION_PAGE:
            if ( exceptionPanel == null )
            {
                exceptionPanel = new ExceptionPanel(this);
            }
            newPage = exceptionPanel;
            backBtn.setEnabled(true);
            nextBtn.setEnabled(true);
            break;
        case RULE_PROPERTIES_PAGE:
            if ( rulePropertiesPanel == null )
            {
                rulePropertiesPanel = new RulePropertiesPanel(this);
            }
            newPage = rulePropertiesPanel;
            nextBtn.setEnabled(false);
            if ( editRule != null && editRule.isDefaultRule() )
            {
                backBtn.setEnabled(false);
            }
            else
            {
                backBtn.setEnabled(true);
            }
            break;
        }
        ruleEditPanel.add(newPage, BorderLayout.CENTER);
        
        updateRuleData();
        
        ruleEditPanel.doLayout();
        ruleEditPanel.revalidate();
        ruleEditPanel.repaint();
        
        // here we adjust the size of the dialog if necessary
        Dimension prefSize = getPreferredSize();
        Dimension currSize = getSize();
        if ( prefSize.height > currSize.height )
        {
            int height = Math.max( prefSize.height, currSize.height );
            setSize( height*5/4, height );
            doLayout();
        }
    }
    
    private final class NextBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                if ( !isRuleInputComplete() )
                {
                    displayRuleInputMissingError();
                    return;
                }
                switch ( currentPage )
                {
                case CONDITION_PAGE:
                    currentPage = CONSEQUENCE_PAGE;
                    break;
                case CONSEQUENCE_PAGE:
                    currentPage = EXCEPTION_PAGE;
                    break;
                case EXCEPTION_PAGE:
                    currentPage = RULE_PROPERTIES_PAGE;
                    break;
                }
                updatePage();
            }
            catch ( Throwable th )
            {
                NLogger.error( NextBtnListener.class, th, th );
            }
        }
        
        private void displayRuleInputMissingError( )
        {
            JOptionPane.showMessageDialog( FilterWizardDialog.this,
                Localizer.getString( "RuleWizard_MissingRuleInputText" ),
                Localizer.getString( "RuleWizard_MissingRuleInputTitle" ),
                JOptionPane.ERROR_MESSAGE  );
        }
    }
    
    private final class BackBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                switch ( currentPage )
                {
                case CONSEQUENCE_PAGE:
                    currentPage = CONDITION_PAGE;
                    break;
                case EXCEPTION_PAGE:
                    currentPage = CONSEQUENCE_PAGE;
                    break;
                case RULE_PROPERTIES_PAGE:
                    currentPage = EXCEPTION_PAGE;
                    break;
                }
                updatePage();
            }
            catch ( Throwable th )
            {
                NLogger.error( BackBtnListener.class, th, th );
            }
        }
    }

    private final class FinishBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                closeDialog();
            }
            catch ( Throwable th )
            {
                NLogger.error( FinishBtnListener.class, th, th );
            }
        }
    }
    
    private final class CloseEventHandler extends WindowAdapter implements ActionListener
    {
        @Override
        public void windowClosing(WindowEvent evt)
        {
            // clear edited rule...
            editRule = null;
            
            closeDialog();
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            // clear edited rule...
            editRule = null;
            
            closeDialog();
        }
    }
}
