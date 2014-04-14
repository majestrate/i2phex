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
 *  $Id: GeneralUIPane.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.gui.dialogs.options;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import phex.common.log.NLogger;
import phex.gui.common.*;
import phex.gui.prefs.InterfacePrefs;
import phex.gui.renderer.LAFListCellRenderer;
import phex.gui.renderer.LAFThemeListCellRenderer;
import phex.gui.tabs.search.SearchTab;
import phex.utils.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class GeneralUIPane extends OptionsSettingsPane
{
    private static final String INSTANCIATED_LAF_KEY = "InstanciatedLAF";
    private static final String INSTANCIATED_THEME_KEY = "InstanciatedLAFTheme";
    private static final String LAF_ERROR_KEY = "LAFError";

    private static final String LAF_NOT_SUPPORTED = "LAFNotSupported";
    private static final String LAF_NOT_FOUND = "LAFNotFound";
    private static final String LAF_ACCESS_ERROR = "LAFAccessError";
    private static final String LAF_INSTANTIATION_ERROR = "LAFInstantiationError";
    private static final String THEME_NOT_FOUND = "ThemeNotFound";
    private static final String THEME_ACCESS_ERROR = "ThemeAccessError";
    private static final String THEME_INSTANTIATION_ERROR = "ThemeInstantiationError";

    
    private GUIRegistry guiRegistry;

    private JComboBox lafBox;
    private DefaultComboBoxModel themeModel;
    private JComboBox themeBox;
    private JComboBox iconPackBox;

    private JCheckBox displayTooltipChkbx;
    private JCheckBox showTableHorizontalLinesChkbx;
    private JCheckBox showTableVerticalLinesChkbx;
    private JCheckBox minimizeWhenClosingChkbx;


    public GeneralUIPane()
    {
        super( "General" );
        guiRegistry = GUIRegistry.getInstance();
    }

    /**
     * Override this method if you like to verify inputs before storing them.
     * A input dictionary is given to the pane. It can be used to store values
     * like error flags or prepared values for saving. The dictionary is given
     * to every settings pane checkInput(), displayErrorMessage() and
     * saveAndApplyChanges() method.
     * When the input has been flaged as invalid with the method setInputValid()
     * the method displayErrorMessage() is called directly after return of
     * checkInput() and the focus is given to settings pane.
     * After checking all settings pane without any error the method
     * saveAndApplyChanges() is called for all settings panes to save the
     * changes.
     */
    @Override
    public void checkInput( HashMap<String, Object> inputDic )
    {
        // clear the reference to remove last change...
        inputDic.remove( INSTANCIATED_LAF_KEY );
        inputDic.remove( LAF_ERROR_KEY );
        inputDic.remove( INSTANCIATED_THEME_KEY );

        // verify if LAF is supported on plattform.
        UIManager.LookAndFeelInfo lafInfo =
            ( UIManager.LookAndFeelInfo ) lafBox.getSelectedItem();
        try
        {
            Class lnfClass = Class.forName( lafInfo.getClassName() );
            LookAndFeel laf = (LookAndFeel)lnfClass.newInstance();
            if ( !laf.isSupportedLookAndFeel() )
            {
                inputDic.put( LAF_ERROR_KEY, LAF_NOT_SUPPORTED );
                setInputValid( inputDic, false );
                return;
            }
            // this is a valid instance... safe reference to apply it later.
            inputDic.put( INSTANCIATED_LAF_KEY, laf );
        }
        catch ( ClassNotFoundException exp )
        {
            inputDic.put( LAF_ERROR_KEY, LAF_NOT_FOUND );
            setInputValid( inputDic, false );
            return;
        }
        catch ( IllegalAccessException exp )
        {
            inputDic.put( LAF_ERROR_KEY, LAF_ACCESS_ERROR );
            setInputValid( inputDic, false );
            return;
        }
        catch ( InstantiationException exp )
        {
            inputDic.put( LAF_ERROR_KEY, LAF_INSTANTIATION_ERROR );
            setInputValid( inputDic, false );
            return;
        }
        
        LookAndFeelUtils.ThemeInfo themeInfo = 
            (LookAndFeelUtils.ThemeInfo) themeBox.getSelectedItem();
        LookAndFeelUtils.ThemeInfo currentTheme = LookAndFeelUtils.getCurrentTheme(
            UIManager.getLookAndFeel().getClass().getName() );
        if ( themeInfo != null && currentTheme != null &&
            themeInfo.getClassName().equals( currentTheme.getClassName() ) )
        {// theme has not changed..
            themeInfo = null;
        }
        if ( themeInfo != null )
        {
            try
            {
                Class themeClass = Class.forName( themeInfo.getClassName() );
                Object theme = themeClass.newInstance();
                inputDic.put( INSTANCIATED_THEME_KEY, theme );
            }
            catch ( ClassNotFoundException exp )
            {
                inputDic.put( LAF_ERROR_KEY, THEME_NOT_FOUND );
                setInputValid( inputDic, false );
                return;
            }
            catch ( IllegalAccessException exp )
            {
                inputDic.put( LAF_ERROR_KEY, THEME_ACCESS_ERROR );
                setInputValid( inputDic, false );
                return;
            }
            catch ( InstantiationException exp )
            {
                inputDic.put( LAF_ERROR_KEY, THEME_INSTANTIATION_ERROR );
                setInputValid( inputDic, false );
                return;
            }
        }
        setInputValid( inputDic, true );
    }

    /**
     * When isInputValid() returns a false this method is called.
     * The input dictionary should contain the settings pane specific information
     * of the error.
     * The settings pane should override this method to display a error
     * message. Before calling the method the focus is given to the
     * settings pane.
     */
    @Override
    public void displayErrorMessage( HashMap<String, Object> inputDic )
    {
        Object value = inputDic.get( LAF_ERROR_KEY );
        if ( value != null )
        {
            JOptionPane.showMessageDialog( this, Localizer.getString( (String)value ),
                Localizer.getString( "LAFError" ), JOptionPane.ERROR_MESSAGE  );
        }
    }

    /**
     * Override this method if you like to apply and save changes made on
     * settings pane. To trigger saving of the configuration if any value was
     * changed call triggerConfigSave().
     */
    @Override
    public void saveAndApplyChanges( HashMap<String, Object> inputDic )
    {
        // LAF
        LookAndFeel laf = (LookAndFeel) inputDic.get( INSTANCIATED_LAF_KEY );
        if ( !UIManager.getLookAndFeel().getClass().getName().equals(
            laf.getClass().getName() ) )
        {
            try
            {
                LookAndFeelUtils.setLookAndFeel( laf );
            }
            catch (LookAndFeelFailedException exp)
            {
                JOptionPane.showMessageDialog( 
                    guiRegistry.getMainFrame(),
                    "Error loading Look & Feel " + laf.getName(), "Error", 
                    JOptionPane.ERROR_MESSAGE );
            }
        }
        
        // theme
        Object theme = inputDic.get( INSTANCIATED_THEME_KEY );
        if ( theme != null )
        {
            LookAndFeelUtils.setCurrentTheme( laf.getClass().getName(), theme );
        }
        
        boolean triggerUIUpdate = false;
        
        // icon pack
        String iconPackName = (String) iconPackBox.getSelectedItem();
        if ( !StringUtils.isEmpty( iconPackName ) )
        {
            IconPack currentPack = guiRegistry.getPlafIconPack();
            if ( !iconPackName.equals(  currentPack.getName() ) )
            {
                guiRegistry.setPlafIconPack( IconPack.createIconPack( iconPackName ) );
                triggerUIUpdate = true;
            }
        }

        // TOOLTIP
        boolean tooltip = displayTooltipChkbx.isSelected();
        
        if ( InterfacePrefs.DisplayTooltip.get().booleanValue() != tooltip )
        {
            InterfacePrefs.DisplayTooltip.set( Boolean.valueOf( tooltip ) );
            ToolTipManager.sharedInstance().setEnabled( tooltip );
        }
        
        // minimize on close
        boolean minimizeOnClose = minimizeWhenClosingChkbx.isSelected();
        InterfacePrefs.MinimizeToBackground.set( Boolean.valueOf( minimizeOnClose ) );

        // table lines
        boolean showHorizontalLines = showTableHorizontalLinesChkbx.isSelected();
        if ( guiRegistry.getShowTableHorizontalLines() != showHorizontalLines )
        {
            guiRegistry.setShowTableHorizontalLines( showHorizontalLines );
            triggerUIUpdate = true;
        }
        boolean showVerticalLines = showTableVerticalLinesChkbx.isSelected();
        if ( guiRegistry.getShowTableVerticalLines() != showVerticalLines )
        {
            guiRegistry.setShowTableVerticalLines( showVerticalLines );
            triggerUIUpdate = true;
        }
        if ( triggerUIUpdate )
        {
            GUIUtils.updateComponentsUI();
        }
    }

    /**
     * Called when preparing this settings pane for display the first time. Can
     * be overriden to implement the look of the settings pane.
     */
    @Override
    protected void prepareComponent()
    {
        setLayout( new BorderLayout() );
        
        //JPanel contentPanel = new FormDebugPanel();
        JPanel contentPanel = new JPanel();
        add( contentPanel, BorderLayout.CENTER );
        
        FormLayout layout = new FormLayout(
            "10dlu, d, 2dlu, d, 2dlu:grow", // columns
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 5dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 5dlu, p" ); // 13 rows 
        layout.setRowGroups( new int[][]{{3, 5, 7, 9, 11}} );
        contentPanel.setLayout( layout );
        
        PanelBuilder builder = new PanelBuilder( layout, contentPanel );
        CellConstraints cc = new CellConstraints();

        builder.addSeparator( Localizer.getString( "GeneralUserInterfaceSettings" ),
            cc.xywh( 1, 1, 5, 1 ) );

        // L&F
        JLabel lafLabel = builder.addLabel( Localizer.getString( "LookAndFeel" ),
            cc.xy( 2, 3 ) );
        lafLabel.setToolTipText( Localizer.getString( "TTTLookAndFeel" ) );
        UIManager.LookAndFeelInfo[] lafs = LookAndFeelUtils.getAvailableLAFs();
        lafBox = new JComboBox( lafs );
        lafBox.addItemListener( new LAFItemListener() );
        int currentLAFIndex = determineCurrentLAFIndex( lafs );
        lafBox.setSelectedIndex( currentLAFIndex );
        lafBox.setRenderer( new LAFListCellRenderer() );
        lafBox.setToolTipText( Localizer.getString( "TTTLookAndFeel" ) );
        builder.add( lafBox, cc.xy( 4, 3 ) );
        
        // L&F Theme
        JLabel themeLabel = builder.addLabel( Localizer.getString( "ColorTheme" ),
            cc.xy( 2, 5 ) );
        themeLabel.setToolTipText( Localizer.getString( "TTTColorTheme" ) );
        themeModel = new DefaultComboBoxModel();
        themeBox = new JComboBox( themeModel );
        themeBox.setToolTipText( Localizer.getString( "TTTColorTheme" ) );
        builder.add( themeBox, cc.xy( 4, 5 ) );
        LookAndFeelInfo laf = (UIManager.LookAndFeelInfo)lafBox.getSelectedItem();
        updateThemes( laf );
        themeBox.setRenderer( new LAFThemeListCellRenderer() );
        
        // Icon packs
        JLabel iconPackLabel = builder.addLabel(
            Localizer.getString( "UISettings_IconPack" ), cc.xy( 2, 7 ) );
        iconPackLabel.setToolTipText( Localizer.getString( "UISettings_TTTIconPack" ) );
        DefaultComboBoxModel iconPackModel = new DefaultComboBoxModel( 
            IconPack.getAllIconPackIds().toArray() );
        iconPackBox = new JComboBox( iconPackModel );
        iconPackBox.setToolTipText( Localizer.getString( "UISettings_TTTIconPack" ) );
        iconPackBox.setSelectedItem(
            GUIRegistry.getInstance().getPlafIconPack().getName() );
        builder.add( iconPackBox, cc.xy( 4, 7 ) );
        

        // ToolTip
        displayTooltipChkbx = new JCheckBox(
            Localizer.getString( "DisplayTooltipText" ),
            InterfacePrefs.DisplayTooltip.get().booleanValue() );
        displayTooltipChkbx.setToolTipText( Localizer.getString( 
            "TTTDisplayTooltipText" ) );
        builder.add( displayTooltipChkbx, cc.xywh( 2, 9, 3, 1 ) );

        // table lines...
        showTableHorizontalLinesChkbx = new JCheckBox(
            Localizer.getString( "ShowTableHorizontalLines" ),
            guiRegistry.getShowTableHorizontalLines() );
        showTableHorizontalLinesChkbx.setToolTipText( Localizer.getString(
            "TTTShowTableHorizontalLines") );
        builder.add( showTableHorizontalLinesChkbx, cc.xywh( 2, 11, 3, 1 ) );

        showTableVerticalLinesChkbx = new JCheckBox(
            Localizer.getString( "ShowTableVerticalLines" ),
            guiRegistry.getShowTableVerticalLines() );
        showTableVerticalLinesChkbx.setToolTipText( Localizer.getString(
            "TTTShowTableVerticalLines") );
        builder.add( showTableVerticalLinesChkbx, cc.xywh( 2, 13, 3, 1 ) );
        
        // minimize
        String backgroundText;
        // whether we have sys tray support or not
        if ( GUIRegistry.getInstance().getDesktopIndicator() != null )
        {
            backgroundText = Localizer.getString( "WhenClosingMinToSysTray" );
        }
        else
        {
            backgroundText = Localizer.getString( "WhenClosingMinToBackground" );
        }
        minimizeWhenClosingChkbx = new JCheckBox( backgroundText,
            InterfacePrefs.MinimizeToBackground.get().booleanValue() );
        builder.add( minimizeWhenClosingChkbx, cc.xywh( 2, 15, 3, 1 ) );
        
        JButton clearSearchHistory = new JButton( Localizer.getString( "UISettings_ClearSearchHistory" ) );
        clearSearchHistory.addActionListener( new ActionListener()
                {
                    public void actionPerformed( ActionEvent e )
                    {
                        MainFrame mainFrame = GUIRegistry.getInstance().getMainFrame();
                        SearchTab searchTab = (SearchTab) mainFrame.getTab( MainFrame.SEARCH_TAB_ID );
                        searchTab.clearSearchHistory();
                    }
                });
        builder.add( clearSearchHistory, cc.xywh( 2, 17, 3, 1, "left,center" ) );
    }

    private int determineCurrentLAFIndex( UIManager.LookAndFeelInfo[] lafs )
    {
        LookAndFeel laf = UIManager.getLookAndFeel();
        String lafClassName = laf.getClass().getName();

        for ( int i = 0; i < lafs.length; i++ )
        {
            if ( lafClassName.equals( lafs[i].getClassName() ) )
            {
                return i;
            }
        }
        return -1;
    }
    
    private int determineCurrentThemeIndex( UIManager.LookAndFeelInfo lafs )
    {
        LookAndFeelUtils.ThemeInfo themeInfo = LookAndFeelUtils.getCurrentTheme(
            lafs.getClassName() );
        if ( themeInfo == null )
        {
            return -1;
        }
        return themeModel.getIndexOf( themeInfo );
    }

    
    private void updateThemes( UIManager.LookAndFeelInfo lafInfo )
    {
        LookAndFeelUtils.ThemeInfo[] themes = LookAndFeelUtils.getAvailableThemes(
            lafInfo.getClassName() );
        themeModel = new DefaultComboBoxModel( themes );
        if ( themeBox != null )
        {
            themeBox.setModel( themeModel );
            int currentThemeIndex = determineCurrentThemeIndex( lafInfo );
            themeBox.setSelectedIndex( currentThemeIndex );
        }
    }
    
    private class LAFItemListener implements ItemListener
    {
        public void itemStateChanged( ItemEvent e )
        {
            try
            {
                if ( e.getStateChange() == ItemEvent.SELECTED )
                {
                    UIManager.LookAndFeelInfo lafInfo = 
                        ( UIManager.LookAndFeelInfo ) e.getItem();
                    if ( lafInfo != null )
                    {
                        updateThemes( lafInfo );
                    }
                }
            }
            catch ( Throwable th )
            {
                NLogger.error( LAFItemListener.class, th, th );
            }
        }
    }
}