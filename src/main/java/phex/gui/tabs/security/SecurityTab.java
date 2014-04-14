/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  --- SVN Information ---
 *  $Id: SecurityTab.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.tabs.security;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import phex.common.log.NLogger;
import phex.gui.actions.FWAction;
import phex.gui.common.FWElegantPanel;
import phex.gui.common.FWToolBar;
import phex.gui.common.GUIRegistry;
import phex.gui.common.GUIUtils;
import phex.gui.common.MainFrame;
import phex.gui.common.table.FWSortedTableModel;
import phex.gui.common.table.FWTable;
import phex.gui.dialogs.security.SecurityRuleDialog;
import phex.gui.tabs.FWTab;
import phex.security.IpSecurityRule;
import phex.security.IpUserSecurityRule;
import phex.security.PhexSecurityManager;
import phex.security.SecurityRule;
import phex.servent.Servent;
import phex.utils.Localizer;
import phex.xml.sax.gui.DGuiSettings;
import phex.xml.sax.gui.DTable;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class SecurityTab extends FWTab
{
    private static final String SECURITY_TABLE_IDENTIFIER = "SecurityTable";
    private static final SecurityRule[] EMPTY_SECURITYRULE_ARRAY =
        new SecurityRule[0];

    private JPopupMenu securityPopup;
    private SecurityTableModel securityModel;
    private FWTable securityTable;
    private JScrollPane securityTableScrollPane;
    private PhexSecurityManager securityMgr;

    public SecurityTab()
    {
        super( MainFrame.SECURITY_TAB_ID, Localizer.getString( "Security" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Security.Tab" ),
            Localizer.getString( "TTTSecurity" ), Localizer.getChar(
            "SecurityMnemonic"), KeyStroke.getKeyStroke( Localizer.getString(
            "SecurityAccelerator" ) ), MainFrame.SECURITY_TAB_INDEX);
        Servent servent = GUIRegistry.getInstance().getServent();
        securityMgr = servent.getSecurityService();
    }

    public void initComponent( DGuiSettings guiSettings )
    {
        CellConstraints cc = new CellConstraints();
        FormLayout tabLayout = new FormLayout("2dlu, fill:d:grow, 2dlu", // columns
            "2dlu, fill:p:grow, 2dlu"); //rows
        PanelBuilder tabBuilder = new PanelBuilder(tabLayout, this);
        JPanel contentPanel = new JPanel();
        FWElegantPanel banner = new FWElegantPanel( Localizer.getString("Security"),
            contentPanel );
        tabBuilder.add(banner, cc.xy(2, 2));
        
        FormLayout contentLayout = new FormLayout(
            "fill:d:grow", // columns
            "fill:d:grow, 1dlu, p"); //rows
        PanelBuilder contentBuilder = new PanelBuilder(contentLayout, contentPanel);
        
        MouseHandler mouseHandler = new MouseHandler();
            
        securityModel = new SecurityTableModel();
        securityTable = new FWTable( new FWSortedTableModel( securityModel ) );
        GUIUtils.updateTableFromDGuiSettings( guiSettings, securityTable, 
            SECURITY_TABLE_IDENTIFIER );
        
        SecurityRuleRowRenderer securityRowRenderer = new SecurityRuleRowRenderer();
        List<TableColumn> colList = securityTable.getColumns( true );
        for ( TableColumn column : colList  )
        {
            column.setCellRenderer( securityRowRenderer );
        }
        securityTable.getSelectionModel().addListSelectionListener(
            new SelectionHandler() );
        securityTable.activateAllHeaderActions();
        securityTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        securityTable.addMouseListener( mouseHandler );
        GUIRegistry.getInstance().getGuiUpdateTimer().addTable( securityTable );

        securityTableScrollPane = FWTable.createFWTableScrollPane( securityTable );
        securityTableScrollPane.addMouseListener( mouseHandler );
        
        contentBuilder.add( securityTableScrollPane, cc.xy( 1, 1 ) );

        FWToolBar securityToolbar = new FWToolBar( JToolBar.HORIZONTAL );
        securityToolbar.setBorderPainted( false );
        securityToolbar.setFloatable( false );
        contentBuilder.add( securityToolbar, cc.xy( 1, 3 ) );

        securityPopup = new JPopupMenu();

        FWAction action = new NewSecurityRuleAction();
        addTabAction( action );
        securityToolbar.addAction( action );
        securityPopup.add( action );

        action = new EditSecurityRuleAction();
        addTabAction( EDIT_SECURITY_RULE_ACTION_KEY, action );
        securityToolbar.addAction( action );
        securityPopup.add( action );

        action = new RemoveSecurityRuleAction();
        addTabAction( action );
        securityToolbar.addAction( action );
        securityPopup.add( action );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisibleByDefault()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUI()
    {
        super.updateUI();
        if ( securityTableScrollPane != null )
        {
            FWTable.updateFWTableScrollPane( securityTableScrollPane );
        }
    }

    private SecurityRule[] getSelectedSecurityRules()
    {
        if ( securityTable.getSelectedRowCount() == 0 )
        {
            return EMPTY_SECURITYRULE_ARRAY;
        }
        int[] viewIndices = securityTable.getSelectedRows();
        int[] modelIndices = securityTable.convertRowIndicesToModel( viewIndices );
        SecurityRule[] files = securityMgr.getIPAccessRulesAt( modelIndices );
        return files;
    }


    //////////////////////////////////////////////////////////////////////////
    /// XML serializing and deserializing
    //////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public void appendDGuiSettings( DGuiSettings dSettings )
    {
        super.appendDGuiSettings( dSettings );
        DTable dTable = GUIUtils.createDTable( securityTable, SECURITY_TABLE_IDENTIFIER );
        dSettings.getTableList().getTableList().add( dTable );
    }

    //////////////////////////////////////////////////////////////////////////
    /// Table Listeners
    //////////////////////////////////////////////////////////////////////////

    private class SelectionHandler implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            try
            {
                refreshTabActions();
            }
            catch ( Throwable th )
            {
                NLogger.error( SelectionHandler.class, th, th);
            }
            
        }
    }

    /**
     * Handles Mouse events to display popup menus.
     */
    private class MouseHandler extends MouseAdapter implements MouseListener
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(MouseEvent e)
        {
            try
            {
                if (e.getClickCount() == 2)
                {
                    if (e.getSource() == securityTable)
                    {
                        getTabAction(EDIT_SECURITY_RULE_ACTION_KEY)
                            .actionPerformed(null);
                    }
                }
            }
            catch (Throwable th)
            {
                NLogger.error( MouseHandler.class, th, th);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(MouseEvent e)
        {
            try
            {
                if (e.isPopupTrigger())
                {
                    popupMenu((Component) e.getSource(), e.getX(), e.getY());
                }
            }
            catch (Throwable th)
            {
                NLogger.error( MouseHandler.class, th, th);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(MouseEvent e)
        {
            try
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    Component source = (Component) e.getSource();
                    if (source == securityTable)
                    {
                        Point p = e.getPoint();
                        int row = securityTable.rowAtPoint(p);
                        int column = securityTable.columnAtPoint(p);
                        securityTable
                            .changeSelection(row, column, false, false);
                    }
                }
            }
            catch (Throwable th)
            {
                NLogger.error( MouseHandler.class, th, th);
            }
        }

        private void popupMenu(Component source, int x, int y)
        {
            if (source == securityTable || source == securityTableScrollPane)
            {
                securityPopup.show(source, x, y);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //// Start Actions
    ////////////////////////////////////////////////////////////////////////////

    private static final String EDIT_SECURITY_RULE_ACTION_KEY = "EditSecurityRuleAction";

    public class NewSecurityRuleAction extends FWAction
    {
        public NewSecurityRuleAction()
        {
            super(Localizer.getString("NewSecurityRule"), GUIRegistry
                .getInstance().getPlafIconPack().getIcon("Security.New"), Localizer
                .getString("TTTNewSecurityRule"));
            setEnabled(true);
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                SecurityRuleDialog dialog = new SecurityRuleDialog();
                dialog.setVisible( true );
            }
            catch (Throwable th)
            {
                NLogger.error( NewSecurityRuleAction.class, th, th);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void refreshActionState()
        {
        }
    }

    public class EditSecurityRuleAction extends FWAction
    {
        public EditSecurityRuleAction()
        {
            super(Localizer.getString("EditSecurityRule"), GUIRegistry
                .getInstance().getPlafIconPack().getIcon("Security.Edit"), Localizer
                .getString("TTTEditSecurityRule"));
            refreshActionState();
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if (securityTable.getSelectedRowCount() != 1)
                {
                    return;
                }
                int viewIdx = securityTable.getSelectedRow();
                int modelIdx = securityTable.translateRowIndexToModel(viewIdx);
                IpSecurityRule rule = securityMgr.getIPAccessRule(modelIdx);
                if (rule == null || rule.isSystemRule() || !(rule instanceof IpUserSecurityRule) )
                {
                    return;
                }
                SecurityRuleDialog dialog = new SecurityRuleDialog(
                    (IpUserSecurityRule)rule);
                dialog.setVisible( true );
            }
            catch (Throwable th)
            {
                NLogger.error( EditSecurityRuleAction.class, th, th);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void refreshActionState()
        {
            if (securityTable.getSelectedRowCount() == 1)
            {
                int viewIdx = securityTable.getSelectedRow();
                int modelIdx = securityTable.translateRowIndexToModel(viewIdx);
                IpSecurityRule rule = securityMgr.getIPAccessRule(modelIdx);
                if (rule == null || rule.isSystemRule())
                {
                    setEnabled(false);
                }
                else
                {
                    setEnabled(true);
                }
            }
            else
            {
                setEnabled(false);
            }
        }
    }

    public class RemoveSecurityRuleAction extends FWAction
    {
        public RemoveSecurityRuleAction()
        {
            super(Localizer.getString("RemoveSecurityRule"), GUIRegistry
                .getInstance().getPlafIconPack().getIcon("Security.Remove"), Localizer
                .getString("TTTRemoveSecurityRule"));
            refreshActionState();
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if (securityTable.getSelectedRow() < 0)
                {
                    setEnabled(false);
                    return;
                }
                SecurityRule[] securityRules = getSelectedSecurityRules();
                for (int i = 0; i < securityRules.length; i++)
                {
                    if (securityRules[i] != null
                        && !securityRules[i].isSystemRule())
                    {
                        securityMgr.removeSecurityRule( securityRules[i] );
                    }
                }
            }
            catch (Throwable th)
            {
                NLogger.error( RemoveSecurityRuleAction.class, th, th);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void refreshActionState()
        {
            int row = securityTable.getSelectedRow();
            if (row < 0)
            {
                setEnabled(false);
                return;
            }

            SecurityRule[] securityRules = getSelectedSecurityRules();
            for (int i = 0; i < securityRules.length; i++)
            {
                if (securityRules[i] != null
                    && !securityRules[i].isSystemRule())
                {
                    setEnabled(true);
                    return;
                }
            }
            setEnabled(false);
        }
    }

}