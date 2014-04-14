/* Created on 18.03.2005 */
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
 *  --- CVS Information ---
 *  $Id: FilterLibraryDialog.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import phex.common.log.NLogger;
import phex.gui.actions.GUIActionPerformer;
import phex.gui.common.DialogBanner;
import phex.gui.common.GUIRegistry;
import phex.gui.models.CollectionsListModel;
import phex.prefs.core.LibraryPrefs;
import phex.prefs.core.PhexCorePrefs;
import phex.utils.Localizer;
import phex.utils.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 */
public class FilterLibraryDialog extends JDialog
{
    private CollectionsListModel filterListModel;
    private JList filterList;

    /**
     * @throws java.awt.HeadlessException
     */
    public FilterLibraryDialog() throws HeadlessException
    {
        super( GUIRegistry.getInstance().getMainFrame(),
            Localizer.getString( "FilterLibraryDialog_DialogTitle" ), false );
        prepareComponent();
    }
    
    /**
     * 
     */
    private void prepareComponent()
    {
        addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing( WindowEvent evt )
                {
                    closeDialog( );
                }
            }
        );
        CellConstraints cc = new CellConstraints();
        Container contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );
        JPanel contentPanel = new JPanel();
        //JPanel contentPanel = new FormDebugPanel();
        contentPane.add( contentPanel, BorderLayout.CENTER );
        
        FormLayout layout = new FormLayout(
            "4dlu, fill:d:grow, 4dlu, d, 4dlu", // columns
            "p, 10dlu, p, 3dlu, p, 3dlu, p, fill:p:grow, 10dlu, p, 6dlu, p, 3dlu, p, 3dlu, p, 10dlu, " + // rows
            "p, 3dlu, p 6dlu" ); // btn rows
        PanelBuilder builder = new PanelBuilder( layout, contentPanel );
        int columnCount = layout.getColumnCount();
        int rowCount = layout.getRowCount();
        
        DialogBanner banner = new DialogBanner( Localizer.getString("FilterLibraryDialog_BannerHeader"),
            Localizer.getString("FilterLibraryDialog_BannerSubHeader") );
        builder.add( banner, cc.xywh( 1, 1, columnCount, 1 ));
        
        filterListModel = new CollectionsListModel();
        filterListModel.addAll( LibraryPrefs.LibraryExclusionRegExList.get() );
        filterList = new JList( filterListModel );
        builder.add( new JScrollPane(filterList), cc.xywh( 2, 3, 1, 6 ));
        
        JButton addBtn = new JButton( Localizer.getString("FilterLibraryDialog_Add") );
        addBtn.addActionListener( new AddBtnListener() );
        builder.add( addBtn, cc.xy( 4, 3 ));
        
        JButton editBtn = new JButton( Localizer.getString("FilterLibraryDialog_Edit") );
        editBtn.addActionListener( new EditBtnListener() );
        builder.add( editBtn, cc.xy( 4, 5 ));
        
        JButton removeBtn = new JButton( Localizer.getString("FilterLibraryDialog_Remove") );
        removeBtn.addActionListener( new RemoveBtnListener() );
        builder.add( removeBtn, cc.xy( 4, 7 ));
        
        builder.addSeparator( Localizer.getString( "FilterLibraryDialog_Examples" ), 
            cc.xywh( 2, 10, 3, 1 ) );
        
        builder.addLabel( Localizer.getString( "FilterLibraryDialog_Example1" ),
            cc.xywh( 2, 12, 3, 1) );
        builder.addLabel( Localizer.getString( "FilterLibraryDialog_Example2" ),
            cc.xywh( 2, 14, 3, 1) );
        builder.addLabel( Localizer.getString( "FilterLibraryDialog_Example3" ),
            cc.xywh( 2, 16, 3, 1) );
        
        
        builder.add( new JSeparator(), cc.xywh( 1, rowCount - 3, columnCount, 1 ) );
        JButton cancelBtn = new JButton( Localizer.getString( "Cancel" ));
        cancelBtn.addActionListener( new CancelBtnListener() );
        JButton okBtn = new JButton( Localizer.getString( "OK" ) );
        okBtn.addActionListener( new OkBtnListener());
        JPanel btnPanel = ButtonBarFactory.buildOKCancelBar( okBtn, cancelBtn);
        builder.add( btnPanel, cc.xywh( 2, rowCount - 1, columnCount - 2, 1 ) );
        
        pack();
        setLocationRelativeTo( getParent() );
    }
    
    private void addFilter( String filter )
    {
        if ( !filterListModel.contains(filter) )
        {
            filterListModel.add( filter );
        }
    }
    
    private void updateFilter( String oldFilter, String newFilter )
    {
        if ( filterListModel.contains( newFilter ) )
        {// new filter already available.. remove old filter
            filterListModel.remove(oldFilter);
        }
        else
        {
            int idx = filterListModel.indexOf(oldFilter);
            filterListModel.set(idx, newFilter);
        }
    }
    
    private void closeDialog( )
    {
        setVisible(false);
        dispose();
    }
    
    private final class AddBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                EditFilterDialog dialog = new EditFilterDialog();
                dialog.setVisible( true );
            }
            catch ( Throwable th )
            {
                NLogger.error( AddBtnListener.class, th, th );
            }
        }
    }
    
    private final class EditBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                String filter = (String)filterList.getSelectedValue();
                if ( StringUtils.isEmpty( filter ) )
                {
                    return;
                }
                EditFilterDialog dialog = new EditFilterDialog( filter );
                dialog.setVisible( true );
            }
            catch ( Throwable th )
            {
                NLogger.error( EditBtnListener.class, th, th );
            }
        }
    }
    
    private final class RemoveBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                int i = filterList.getSelectedIndex();
                filterListModel.remove(i);
            }
            catch ( Throwable th )
            {
                NLogger.error( RemoveBtnListener.class, th, th );
            }
        }
    }
    
    private final class OkBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                // creating a set to make sure we have uniques..
                Set uniqueSet = new LinkedHashSet();
                uniqueSet.addAll( filterListModel );
                LibraryPrefs.LibraryExclusionRegExList.get().clear();
                LibraryPrefs.LibraryExclusionRegExList.get().addAll( uniqueSet );               
                closeDialog();
                PhexCorePrefs.save( false );
                GUIActionPerformer.rescanSharedFiles();
            }
            catch ( Throwable th )
            {
                NLogger.error( OkBtnListener.class, th, th );
            }
        }
    }

    private final class CancelBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                closeDialog();
            }
            catch ( Throwable th )
            {
                NLogger.error( CancelBtnListener.class, th, th );
            }
        }
    }
    
    public class EditFilterDialog extends JDialog
    {
        private JTextField filterField;
        private JLabel errorLabel;
        private String editFilter;
        
        /**
         * @throws java.awt.HeadlessException
         */
        public EditFilterDialog() throws HeadlessException
        {
            super( GUIRegistry.getInstance().getMainFrame(),
                Localizer.getString( "FilterLibraryDialogAdd_DialogTitle" ), false );
            editFilter = null;
            prepareComponent();
        }
        
        /**
         * @throws java.awt.HeadlessException
         */
        public EditFilterDialog(String filter) throws HeadlessException
        {
            super( GUIRegistry.getInstance().getMainFrame(),
                Localizer.getString( "FilterLibraryDialogAdd_DialogTitle" ), false );
            editFilter = filter;
            prepareComponent();
        }
        
        /**
         * 
         */
        private void prepareComponent()
        {
            addWindowListener(new WindowAdapter()
                {
                    @Override
                    public void windowClosing( WindowEvent evt )
                    {
                        closeDialog( );
                    }
                }
            );
            CellConstraints cc = new CellConstraints();
            Container contentPane = getContentPane();
            contentPane.setLayout( new BorderLayout() );
            JPanel contentPanel = new JPanel();
            //JPanel contentPanel = new FormDebugPanel();
            contentPane.add( contentPanel, BorderLayout.CENTER );
            
            FormLayout layout = new FormLayout(
                "4dlu, d, 3dlu, fill:d:grow, 4dlu", // columns
                "p, 10dlu, p, 2dlu, p, 10dlu, p, 6dlu, p, 3dlu, p, 3dlu, p, 10dlu, " + // rows
                "p, 3dlu, p 6dlu" ); // btn rows
            PanelBuilder builder = new PanelBuilder( layout, contentPanel );
            int columnCount = layout.getColumnCount();
            int rowCount = layout.getRowCount();
            
            DialogBanner banner = new DialogBanner( 
                Localizer.getString( "FilterLibraryDialogAdd_BannerHeader"),
                Localizer.getString( "FilterLibraryDialogAdd_BannerSubHeader") );
            builder.add( banner, cc.xywh( 1, 1, columnCount, 1 ));
            
            builder.addLabel( Localizer.getString( "FilterLibraryDialogAdd_Filter" ),
                cc.xy( 2, 3 ) );
            filterField = new JTextField( 40 );
            if ( editFilter != null )
            {
                filterField.setText(editFilter);
            }
            builder.add( filterField, cc.xy( 4, 3 ));
            
            errorLabel = new JLabel( " " );
            builder.add( errorLabel, cc.xy( 4, 5) );
                        
            builder.addSeparator( Localizer.getString( "FilterLibraryDialog_Examples" ), 
                cc.xywh( 2, 7, 3, 1 ) );
            
            builder.addLabel( Localizer.getString( "FilterLibraryDialog_Example1" ),
                cc.xywh( 2, 9, 3, 1) );
            builder.addLabel( Localizer.getString( "FilterLibraryDialog_Example2" ),
                cc.xywh( 2, 11, 3, 1) );
            builder.addLabel( Localizer.getString( "FilterLibraryDialog_Example3" ),
                cc.xywh( 2, 13, 3, 1) );
            
            
            builder.add( new JSeparator(), cc.xywh( 1, rowCount - 3, columnCount, 1 ) );
            JButton cancelBtn = new JButton( Localizer.getString( "Cancel" ));
            cancelBtn.addActionListener( new CancelBtnListener() );
            JButton okBtn = new JButton( Localizer.getString( "OK" ) );
            okBtn.addActionListener( new OkBtnListener());
            JPanel btnPanel = ButtonBarFactory.buildOKCancelBar( okBtn, cancelBtn);
            builder.add( btnPanel, cc.xywh( 2, rowCount - 1, columnCount - 2, 1 ) );
            
            pack();
            setLocationRelativeTo( getParent() );
        }
        
        private void showErrorLabel( String text )
        {
            errorLabel.setText( text );
            errorLabel.setIcon( GUIRegistry.getInstance().getPlafIconPack().getIcon("LibraryFilterDialog.Error") );
        }
        
        private void closeDialog( )
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
                    String filter = filterField.getText();
                    if ( StringUtils.isEmpty(filter) )
                    {
                        closeDialog();
                        return;
                    }
                    
                    boolean isValid = validate( filter );
                    if (isValid)
                    {
                        if ( editFilter == null )
                        {
                            addFilter(filter);
                        }
                        else
                        {
                            updateFilter( editFilter, filter );
                        }
                        closeDialog();
                    }
                    else
                    {
                        showErrorLabel( Localizer.getString( 
                            "FilterLibraryDialogAdd_InvalidRegExp" ));
                    }
                }
                catch ( Throwable th )
                {
                    NLogger.error( OkBtnListener.class, th, th );
                }
            }
            
            private boolean validate( String filter )
            {
                try
                {
                    Pattern pattern = Pattern.compile( filter );
                    return true;
                }
                catch ( PatternSyntaxException exp )
                {
                    return false;
                }
            }
        }

        private final class CancelBtnListener implements ActionListener
        {
            public void actionPerformed( ActionEvent e )
            {
                try
                {
                    closeDialog();
                }
                catch ( Throwable th )
                {
                    NLogger.error( CancelBtnListener.class, th, th );
                }
            }
        }
    }
}
