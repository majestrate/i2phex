/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2006 Phex Development Group
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
 *  $Id: LanguagePane.java 3612 2006-11-17 15:58:21Z gregork $
 */
package phex.gui.dialogs.options;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.*;

import phex.gui.common.GUIRegistry;
import phex.gui.common.HTMLMultiLinePanel;
import phex.gui.common.IconPack;
import phex.gui.prefs.InterfacePrefs;
import phex.gui.renderer.FWListCellRenderer;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 */
public class LanguagePane extends OptionsSettingsPane
{
    private JList languageList;
    
    public LanguagePane()
    {
        super( "LanguageSettings_Language" );
    }
    
    /**
     * Called when preparing this settings pane for display the first time. Can
     * be overriden to implement the look of the settings pane.
     */
    protected void prepareComponent()
    {
        setLayout( new BorderLayout() );
        
        //JPanel contentPanel = new FormDebugPanel();
        JPanel contentPanel = new JPanel();
        add( contentPanel, BorderLayout.CENTER );
        
        FormLayout layout = new FormLayout(
            "10dlu, d, 2dlu:grow", // columns
            "p, 3dlu, p, 2dlu, fill:d:grow, 3dlu, p" ); // rows
        contentPanel.setLayout( layout );
        
        PanelBuilder builder = new PanelBuilder( layout, contentPanel );
        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator( Localizer.getString( "LanguageSettings_LanguageSettings" ), 
            cc.xywh( 1, 1, 3, 1 ) );
        HTMLMultiLinePanel textPanel = new HTMLMultiLinePanel(
            Localizer.getString( "LanguageSettings_SelectYourLanguage") );
        builder.add( textPanel, cc.xy( 2, 3 ) );

        languageList = new JList( 
            Localizer.getAvailableLocales().toArray() );
        languageList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        languageList.setVisibleRowCount( 5 );
        languageList.setCellRenderer( new LanguageListCellRenderer() );
        Font lanugageListFont = getLanugageListFont();
        languageList.setFont( lanugageListFont );
        languageList.setFixedCellHeight( lanugageListFont.getSize() * 2 );
        JScrollPane scrollPane = new JScrollPane( languageList );
        builder.add( scrollPane, cc.xy( 2, 5 ) );

        HTMLMultiLinePanel textPanel2 = new HTMLMultiLinePanel(
            Localizer.getString( "LanguageSettings_RestartNote") );
        builder.add( textPanel2, cc.xy( 2, 7 ) );
        
        Locale locale = Localizer.getUsedLocale();
        languageList.setSelectedValue( locale, true );
    }
    
    @Override
    public void updateUI()
    {
        super.updateUI();
        if ( languageList != null )
        {
            Font lanugageListFont = getLanugageListFont();
            languageList.setFont( lanugageListFont );
            languageList.setFixedCellHeight( lanugageListFont.getSize() * 2 );
        }
    }
    
    private Font getLanugageListFont() 
    {
        return UIManager.getFont("TitledBorder.font").deriveFont( Font.BOLD );
    }
    
    /**
     * Override this method if you like to apply and save changes made on
     * settings pane. To trigger saving of the configuration if any value was
     * changed call triggerConfigSave().
     */
    @Override
    public void saveAndApplyChanges( HashMap inputDic )
    {
        if ( languageList.getSelectedIndex() == -1 )
        {
            return;
        }
        Locale selectedLocale = (Locale)languageList.getSelectedValue();
        String localeStr = selectedLocale.toString();
        if ( !localeStr.equals( InterfacePrefs.LocaleName.get()  ) )
        {
            InterfacePrefs.LocaleName.set( localeStr );
            Localizer.setUsedLocale( selectedLocale );
        }
    }
    
    /**
     *
     */
    public class LanguageListCellRenderer extends FWListCellRenderer
    {
        private IconPack iconFactory;
        
        public LanguageListCellRenderer()
        {
            iconFactory = GUIRegistry.getInstance().getCountryIconPack();
        }
        
        @Override
        public Component getListCellRendererComponent( JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus )
        {
            super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            
            if ( value instanceof Locale )
            {
                Locale locale = (Locale)value;
                String text = locale.getDisplayLanguage(locale);
                String variant = locale.getDisplayVariant( locale );
                if ( variant != null && variant.length() > 0 )
                {
                    text += " / " + variant;
                }
                String country = locale.getDisplayCountry(locale);
                if ( country != null && country.length() > 0 )
                {
                    text += " (" +  country + ")";
                }
                setText( text );
                String countryCode = locale.getCountry();
                Icon icon = null;
                if ( countryCode != null && countryCode.length() > 0 )
                {
                    icon = iconFactory.getIcon( countryCode );
                }
                setIcon( icon );
            }
            return this;
        }
    }
}