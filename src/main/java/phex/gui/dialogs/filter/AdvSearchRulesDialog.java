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
 *  $Id: AdvSearchRulesDialog.java 4128 2008-03-01 19:12:47Z complication $
 */
package phex.gui.dialogs.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import phex.common.log.NLogger;
import phex.gui.actions.FWAction;
import phex.gui.actions.FWActionGroup;
import phex.gui.common.DialogBanner;
import phex.gui.common.FWToolBar;
import phex.gui.common.GUIRegistry;
import phex.gui.dialogs.filter.wizard.FilterWizardDialog;
import phex.rules.Rule;
import phex.rules.SearchFilterRules;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AdvSearchRulesDialog extends JDialog implements RuleDescOwnerDialog
{
    private FWActionGroup actionGroup;
    private SearchFilterRulesEditModel editModel;
    private JList ruleList;
    private RuleDescriptionPanel ruleDescPanel;

    public AdvSearchRulesDialog( SearchFilterRules currentRules )
    {
        super(GUIRegistry.getInstance().getMainFrame(), Localizer
            .getString("AdvSearchRules_DialogTitle"), false);
        actionGroup = new FWActionGroup();
        editModel = new SearchFilterRulesEditModel( currentRules );
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
        FormLayout layout = new FormLayout("4dlu, fill:d:grow, 4dlu", // columns
            "p, p, 4dlu, p, p, 8dlu, p, 2dlu, fill:90dlu:grow, 8dlu, fill:p:grow, 12dlu," +  // rows
            "p, 2dlu, p 4dlu" ); //btn rows
        PanelBuilder contentPB = new PanelBuilder(layout, contentPanel);
        int columnCount = layout.getColumnCount();
        int rowCount = layout.getRowCount();
        
        DialogBanner banner = new DialogBanner(
            Localizer.getString( "AdvSearchRules_BannerHeader" ), 
            Localizer.getString( "AdvSearchRules_BannerSubHeader" ) );
        contentPB.add(banner, cc.xywh(1, 1, columnCount, 1));
        
        contentPB.add(new JSeparator(), cc.xywh(1, 2, columnCount, 1));
        
        FWToolBar ruleBar = new FWToolBar( FWToolBar.HORIZONTAL );
        ruleBar.setBorderPainted( false );
        ruleBar.setFloatable( false );
        ruleBar.setShowText(true);
        contentPB.add( ruleBar, cc.xywh( 2, 4, 1, 1 ) );
        
        contentPB.add(new JSeparator(), cc.xywh(2, 5, 1, 1));
        
        JLabel label = new JLabel( Localizer
            .getString( "AdvSearchRules_RuleListDescription" ) );
        contentPB.add( label, cc.xywh( 2, 7, 1, 1 ) );
        
        ruleList = new JList();
        ruleList.setModel( editModel );
        ruleList.setCellRenderer( new RuleListCellRenderer() );
        ruleList.getSelectionModel().addListSelectionListener( 
            new RuleListSelectionListener() );
        contentPB.add( new JScrollPane( ruleList ), cc.xywh(2, 9, 1, 1));
        
        ruleDescPanel = new RuleDescriptionPanel( this );
        contentPB.add( ruleDescPanel, cc.xywh(2, 11, 1, 1));
                
        
        // button bar
        contentPB.add( new JSeparator(), cc.xywh( 1, rowCount - 3, columnCount, 1 ) );
        
        JButton okBtn = new JButton( Localizer.getString( "OK" ) );
        okBtn.setDefaultCapable( true );
        okBtn.setRequestFocusEnabled( true );
        okBtn.addActionListener( new OkBtnListener());
        JButton cancelBtn = new JButton( Localizer.getString( "Cancel" ) );
        cancelBtn.addActionListener( closeEventHandler );
        JButton applyBtn = new JButton( Localizer.getString( "Apply" ) );
        applyBtn.addActionListener( new ApplyBtnListener() );
        
        JPanel btnPanel = ButtonBarFactory.buildOKCancelApplyBar( okBtn, 
            cancelBtn, applyBtn );
        contentPB.add( btnPanel, cc.xywh( 2, rowCount - 1, columnCount - 2, 1 ) );
        
        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        getRootPane().setDefaultButton( okBtn );
        
        // add toolbar actions after all components are initialized to prevent
        // NPE in actions refreshActionState()
        FWAction action = new NewRuleAction();
        actionGroup.addAction(action);
        ruleBar.addAction( action );
        
        ruleBar.addSeparator();
        
        action = new ChangeRuleAction();
        actionGroup.addAction(action);
        ruleBar.addAction( action );
        
        action = new CopyRuleAction();
        actionGroup.addAction(action);
        ruleBar.addAction( action );
        
        action = new DeleteRuleAction();
        actionGroup.addAction(action);
        ruleBar.addAction( action );

        ruleBar.addSeparator();
        
        action = new MoveRuleAction( MoveRuleAction.MOVE_UP );
        actionGroup.addAction(action);
        ruleBar.addAction( action );
        
        action = new MoveRuleAction( MoveRuleAction.MOVE_DOWN );
        actionGroup.addAction(action);
        ruleBar.addAction( action );
        
        pack();
        
        int height = getHeight();
        setSize( height*6/5, height );
        
        setLocationRelativeTo( getParent() );
    }
    
    public Rule getEditRule()
    {
        int selectedIndex = ruleList.getSelectedIndex();
        if ( selectedIndex < 0 )
        {
            return null;
        }
        return editModel.getDisplayRuleAt( selectedIndex );
    }
    
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
    
    private class NewRuleAction extends FWAction
    {
        public NewRuleAction()
        {
            super( Localizer.getString( "AdvSearchRules_NewRule" ), null,
                Localizer.getString( "AdvSearchRules_TTTNewRule" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            FilterWizardDialog newFilterDialog = new FilterWizardDialog( 
                AdvSearchRulesDialog.this );
            newFilterDialog.setVisible( true );
            
            Rule resultRule = newFilterDialog.getEditRule();
            if ( resultRule != null )
            {
                editModel.addNewRule( resultRule );
                actionGroup.refreshActions();
            }            
        }

        @Override
        public void refreshActionState()
        {
        }
    }
    
    private class CopyRuleAction extends FWAction
    {
        public CopyRuleAction()
        {
            super( Localizer.getString( "AdvSearchRules_CopyRule" ), null,
                Localizer.getString( "AdvSearchRules_TTTCopyRule" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            int selectedIdx = ruleList.getSelectedIndex();
            if ( selectedIdx < 0 )
            {
                return;
            }
            Rule modRule = editModel.getDisplayRuleAt( selectedIdx );
            
            Rule copyRule = (Rule)modRule.clone();
            copyRule.setName( Localizer.getString( "RuleVisualization_CopyOf"  ) +
                " " + modRule.getName() );
            // clear the id of a phex internal rule to make it a full user rule.
            copyRule.setId( null );
            // reset possible default flag.
            copyRule.setDefaultRule(false);
            editModel.addNewRule( copyRule );
        }

        @Override
        public void refreshActionState()
        {
            setEnabled( !ruleList.isSelectionEmpty() );
        }
    }
    
    private class ChangeRuleAction extends FWAction
    {
        public ChangeRuleAction()
        {
            super( Localizer.getString( "AdvSearchRules_ChangeRule" ), null,
                Localizer.getString( "AdvSearchRules_TTTChangeRule" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            int selectedIdx = ruleList.getSelectedIndex();
            if ( selectedIdx < 0 )
            {
                return;
            }
            Rule modRule = editModel.getDisplayRuleAt( selectedIdx );
            FilterWizardDialog wizardDialog = new FilterWizardDialog( modRule,
                AdvSearchRulesDialog.this );
            wizardDialog.setVisible( true );
            
            Rule resultRule = wizardDialog.getEditRule();
            if ( resultRule != null )
            {
                editModel.updateRule( selectedIdx, resultRule );
            }
            ruleDescPanel.updateRuleData();
        }

        @Override
        public void refreshActionState()
        {
            setEnabled( !ruleList.isSelectionEmpty() );
        }
    }
    
    private class DeleteRuleAction extends FWAction
    {
        public DeleteRuleAction()
        {
            super( Localizer.getString( "AdvSearchRules_DeleteRule" ), null,
                Localizer.getString( "AdvSearchRules_TTTDeleteRule" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            int selectedIdx = ruleList.getSelectedIndex();
            if ( selectedIdx < 0 )
            {
                return;
            }
            editModel.removeRule( selectedIdx );
            ruleDescPanel.updateRuleData();
        }

        @Override
        public void refreshActionState()
        {
            setEnabled( !ruleList.isSelectionEmpty() );
        }
    }
    
    /**
     * Moves a rule
     */
    class MoveRuleAction extends FWAction
    {
        private static final boolean MOVE_UP = true;
        private static final boolean MOVE_DOWN = false;

        private boolean moveUp;

        MoveRuleAction( boolean type )
        {
            super( );
            moveUp = type;
            if( moveUp )
            {
                setSmallIcon( GUIRegistry.getInstance().
                    getPlafIconPack().getIcon( "AdvSearchRules.MoveUp") );
                setToolTipText( Localizer.getString( "AdvSearchRules_TTTMoveUp" ) );
            }
            else
            {
                setSmallIcon( GUIRegistry.getInstance().
                    getPlafIconPack().getIcon( "AdvSearchRules.MoveDown") );
                setToolTipText( Localizer.getString( "AdvSearchRules_TTTMoveDown" ) );
            }
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            try
            {
                int selectedIdx = ruleList.getSelectedIndex();
                if ( selectedIdx < 0 )
                {
                    return;
                }
                int newIdx = editModel.moveRule(selectedIdx, moveUp );
                ruleList.setSelectedIndex( newIdx );
            }
            catch (Throwable th)
            {
                NLogger.error( MoveRuleAction.class, th, th );
            }
        }

        @Override
        public void refreshActionState()
        {
            int selectedIdx = ruleList.getSelectedIndex();
            if ( selectedIdx < 0 )
            {
                setEnabled(false);
            }
            else if ( moveUp && selectedIdx == 0 )
            {
                setEnabled(false);
            }
            else if ( !moveUp && selectedIdx == editModel.getSize()-1 )
            {
                setEnabled(false);
            }
            else
            {
                setEnabled( true );
            }
        }
    }
    
    private final class ApplyBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                editModel.applyChangesToQueryManager();
            }
            catch ( Throwable th )
            {
                NLogger.error( ApplyBtnListener.class, th, th );
            }
        }
    }

    private final class OkBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                editModel.applyChangesToQueryManager();
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
    
    public class RuleListSelectionListener implements ListSelectionListener
    {
        public void valueChanged( ListSelectionEvent e )
        {
            actionGroup.refreshActions();
            ruleDescPanel.updateRuleData();
        }
    }
    
    public class RuleListCellRenderer extends DefaultListCellRenderer
    {

        @Override
        public Component getListCellRendererComponent( JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus )
        {
            super.getListCellRendererComponent(list, value, index, isSelected,
                cellHasFocus);
            if ( value instanceof Rule )
            {
                setText( ((Rule)value).getName() );
            }
            else if ( value instanceof RuleEditWrapper )
            {
                setText( ((RuleEditWrapper)value).getName() );
            }
            return this;
        }

    }
}
