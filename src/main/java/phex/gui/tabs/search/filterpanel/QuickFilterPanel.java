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
 *  --- SVN Information ---
 *  $Id: QuickFilterPanel.java 3932 2007-09-21 13:59:07Z gregork $
 */
package phex.gui.tabs.search.filterpanel;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.text.Keymap;

import phex.common.MediaType;
import phex.gui.common.FWSizeDefComboBox;
import phex.gui.common.GUIUtils;
import phex.gui.common.IntegerTextField;
import phex.gui.renderer.MediaTypeListRenderer;
import phex.gui.tabs.search.SearchResultsDataModel;
import phex.rules.Rule;
import phex.rules.condition.Condition;
import phex.rules.condition.FileSizeCondition;
import phex.rules.condition.FilenameCondition;
import phex.rules.condition.MediaTypeCondition;
import phex.rules.condition.NotCondition;
import phex.rules.condition.OrConcatCondition;
import phex.rules.condition.FileSizeCondition.Range;
import phex.rules.consequence.FilterFromSearchConsequence;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class QuickFilterPanel extends JPanel
{
    private static final String QUICK_FILTER_RULE_NAME = "?<#}PhexQuickFilterRule";
    
    private SearchResultsDataModel currentResultsDataModel;
    private Rule currentQuickFilterRule;
    
    private JTextField withTermsTF;
    private JTextField withoutTermsTF;
    private JComboBox mediaTypeComboBox;
    private JTextField minFileSizeTF;
    private FWSizeDefComboBox minFileSizeUnitComboBox;
    private JTextField maxFileSizeTF;
    private FWSizeDefComboBox maxFileSizeUnitComboBox;
    
    private Timer activateQuickFilterTimer;
    
    public QuickFilterPanel()
    {
        super();
        initializeComponent();
        updateUI();
        
        activateQuickFilterTimer = new Timer( 0, new ActionListener() {
            public void actionPerformed( ActionEvent e )
            {
                activateQuickFilter();
            }
        });
        activateQuickFilterTimer.setRepeats(false);
        activateQuickFilterTimer.setInitialDelay( 500 );
    }

    /**
     * 
     */
    private void initializeComponent()
    {
        setOpaque(false);
        
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout(
            "6dlu, right:d, 2dlu, d, 2dlu, d, 8dlu, right:d, 2dlu, d, 2dlu, d, 6dlu", // columns
            "6dlu, p, 4dlu, p, 4dlu, p, 6dlu," ); // rows
        PanelBuilder panelBuilder = new PanelBuilder( layout, this );
        layout.setColumnGroups( new int[][]{{2, 8},{4, 10},{6,12}} );
        
        JLabel label = panelBuilder.addLabel( Localizer.getString( "SearchTab_WithTerms" ),
            cc.xywh(2, 2, 1, 1) );
        label.setToolTipText( Localizer.getString("SearchTab_TTTWithTerms") );
        
        QuickFilterActivationHandler activationHandler = new QuickFilterActivationHandler();
        
        withTermsTF = new JTextField( 8 );
        withTermsTF.addKeyListener( activationHandler );
        withTermsTF.setToolTipText( Localizer.getString("SearchTab_TTTWithTerms") );
        Keymap keymap = JTextField.addKeymap( "SearchFilterTextEditor", withTermsTF.getKeymap() );
        withTermsTF.setKeymap( keymap );
        GUIUtils.assignKeymapToTextField( keymap, withTermsTF );
        panelBuilder.add( withTermsTF, cc.xywh(4, 2, 3, 1) );
        
        label = panelBuilder.addLabel( Localizer.getString( "SearchTab_WithoutTerms" ),
            cc.xywh(8, 2, 1, 1) );
        label.setToolTipText( Localizer.getString("SearchTab_TTTWithoutTerms") );
        
        withoutTermsTF = new JTextField( 8 );
        withoutTermsTF.addKeyListener( activationHandler );
        withoutTermsTF.setToolTipText( Localizer.getString("SearchTab_TTTWithoutTerms") );
        keymap = JTextField.addKeymap( "SearchFilterTextEditor", withoutTermsTF.getKeymap() );
        withoutTermsTF.setKeymap( keymap );
        GUIUtils.assignKeymapToTextField( keymap, withoutTermsTF );
        panelBuilder.add( withoutTermsTF, cc.xywh(10, 2, 3, 1) );

        label = panelBuilder.addLabel( Localizer.getString( "SearchTab_FileType" ),
            cc.xywh(2, 4, 1, 1) );
        label.setToolTipText( Localizer.getString("SearchTab_TTTFileType") );
        
        mediaTypeComboBox = new JComboBox( MediaType.getAllMediaTypes() );
        mediaTypeComboBox.addItemListener( activationHandler );
        mediaTypeComboBox.setRenderer( new MediaTypeListRenderer() );
        panelBuilder.add( mediaTypeComboBox, cc.xywh(4, 4, 3, 1) );
        
        label = panelBuilder.addLabel( Localizer.getString( "SearchTab_MinFileSize" ),
            cc.xywh(2, 6, 1, 1) );
        label.setToolTipText( Localizer.getString("SearchTab_TTTMinFileSize") );
        
        minFileSizeTF = new IntegerTextField( 9 );
        minFileSizeTF.addKeyListener( activationHandler );
        minFileSizeTF.setToolTipText( Localizer.getString( "SearchTab_TTTMinFileSize" ) );
        keymap = JTextField.getKeymap( "SearchFilterTextEditor" );
        minFileSizeTF.setKeymap( keymap );
        GUIUtils.assignKeymapToTextField( keymap, minFileSizeTF );
        panelBuilder.add( minFileSizeTF, cc.xywh(4, 6, 1, 1) );
        
        minFileSizeUnitComboBox = new FWSizeDefComboBox();
        minFileSizeUnitComboBox.addItemListener( activationHandler );
        minFileSizeUnitComboBox.setToolTipText( Localizer.getString( "SearchTab_TTTMinFileSize" ) );
        panelBuilder.add( minFileSizeUnitComboBox, cc.xywh(6, 6, 1, 1) );

        label = panelBuilder.addLabel( Localizer.getString( "SearchTab_MaxFileSize" ),
            cc.xywh(8, 6, 1, 1) );
        label.setToolTipText( Localizer.getString("SearchTab_TTTMaxFileSize") );
        
        maxFileSizeTF = new IntegerTextField( 9 );
        maxFileSizeTF.addKeyListener( activationHandler );
        maxFileSizeTF.setToolTipText( Localizer.getString( "SearchTab_TTTMaxFileSize" ) );
        keymap = JTextField.getKeymap( "SearchFilterTextEditor" );
        maxFileSizeTF.setKeymap( keymap );
        GUIUtils.assignKeymapToTextField( keymap, maxFileSizeTF );
        panelBuilder.add( maxFileSizeTF, cc.xywh(10, 6, 1, 1) );
        
        maxFileSizeUnitComboBox = new FWSizeDefComboBox( );
        maxFileSizeUnitComboBox.addItemListener( activationHandler );
        maxFileSizeUnitComboBox.setToolTipText( Localizer.getString( "SearchTab_TTTMaxFileSize" ) );
        panelBuilder.add( maxFileSizeUnitComboBox, cc.xywh(12, 6, 1, 1) );
    }
    
    private void activateQuickFilter()
    {
        if ( currentResultsDataModel == null )
        {
            return;
        }
        if ( currentQuickFilterRule == null )
        {
            currentQuickFilterRule = new Rule();
            currentQuickFilterRule.setName(QUICK_FILTER_RULE_NAME);
            currentQuickFilterRule.addConsequence( FilterFromSearchConsequence.INSTANCE );
        }
        currentQuickFilterRule.clearConditions();
        
        OrConcatCondition orCondition = new OrConcatCondition();
        if ( withTermsTF.getText().trim().length() > 0 )
        {
            FilenameCondition cond = new FilenameCondition();
            String terms = withTermsTF.getText();
            StringTokenizer tokenizer = new StringTokenizer( terms, " " );
            while( tokenizer.hasMoreTokens() )
            {
                cond.addTerm( tokenizer.nextToken() );
            }
            orCondition.addCondition( new NotCondition( cond ) );
        }
        if ( withoutTermsTF.getText().trim().length() > 0 )
        {
            FilenameCondition cond = new FilenameCondition();
            String terms = withoutTermsTF.getText();
            StringTokenizer tokenizer = new StringTokenizer( terms, " " );
            while( tokenizer.hasMoreTokens() )
            {
                cond.addTerm( tokenizer.nextToken() );
            }
            orCondition.addCondition( cond );
        }
        
        MediaType mediaType = (MediaType) mediaTypeComboBox.getSelectedItem();
        orCondition.addCondition( new NotCondition( new MediaTypeCondition( mediaType ) ) );
        
        String minSizeStr = minFileSizeTF.getText().trim();
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
        }
        
        String maxSizeStr = maxFileSizeTF.getText().trim();
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
        }
        if ( finalMinSize >= 0 || finalMaxSize >= 0)
        {
            FileSizeCondition fileSizeCond = new FileSizeCondition( finalMinSize,
                finalMaxSize );
            orCondition.addCondition( new NotCondition( fileSizeCond ) );
        }
        
        if ( orCondition.getConditionCount() > 0 )
        {
            currentQuickFilterRule.addCondition( orCondition );
        }
        
        currentResultsDataModel.setQuickFilterRule( currentQuickFilterRule );
    }
    
    public void setDisplayedSearch( SearchResultsDataModel searchResultsDataModel )
    {
        currentResultsDataModel = null;
        currentQuickFilterRule = null;
        Rule quickFilterRule = null;
        if ( searchResultsDataModel != null )
        {
            // get quick filter rule
            quickFilterRule = searchResultsDataModel.getQuickFilterRule();
        }
        
        withTermsTF.setText("");
        withoutTermsTF.setText("");
        mediaTypeComboBox.setSelectedIndex( 0 );
        minFileSizeTF.setText("");
        maxFileSizeTF.setText("");
        if ( quickFilterRule != null )
        {
            List<Condition> conditions = quickFilterRule.getConditions();
            Iterator<Condition> condIterator = conditions.iterator();
            if( condIterator.hasNext() )
            {
                conditions = ((OrConcatCondition)condIterator.next()).getConditions();
                condIterator = conditions.iterator();
            }
            while( condIterator.hasNext() )
            {
                Condition condition = condIterator.next();
                if ( condition instanceof NotCondition )
                {
                    condition = ((NotCondition)condition).getContainedCondition();
                    if ( condition instanceof FilenameCondition )
                    {
                        FilenameCondition filenameCondition = (FilenameCondition) condition;
                        StringBuffer buffer = new StringBuffer();
                        Set<String> terms = filenameCondition.getTerms();
                        if ( terms.size() > 0 )
                        {
                            Iterator<String> iterator = terms.iterator();
                            while ( iterator.hasNext() )
                            {
                                buffer.append( iterator.next() );
                                if ( iterator.hasNext() )
                                {
                                    buffer.append( " " );
                                }
                            }
                        }
                        withTermsTF.setText( buffer.toString() );
                    }
                    else if ( condition instanceof MediaTypeCondition )
                    {
                        MediaTypeCondition mediaTypeCondition = (MediaTypeCondition) condition;
                        Set<MediaType> types = mediaTypeCondition.getTypes();
                        Iterator<MediaType> iterator = types.iterator();
                        if ( iterator.hasNext() )
                        {
                            MediaType type = iterator.next();
                            mediaTypeComboBox.setSelectedItem( type );
                        }
                    }
                    else if ( condition instanceof FileSizeCondition )
                    {
                        FileSizeCondition fileSizeCondition = (FileSizeCondition) condition;
                        Set<Range> ranges = fileSizeCondition.getRanges();
                        Iterator<Range> iterator = ranges.iterator();
                        if ( !iterator.hasNext() )
                        {
                            continue;
                        }
                        Range range = iterator.next();
                        if ( range.min > 0 )
                        {
                            // initialize to bytes
                            minFileSizeUnitComboBox.setSelectedIndex( 0 );
                            FWSizeDefComboBox.SizeDefinition currentDef;
                            long mod;
                            long displayVal = range.min;
                            for ( int i = FWSizeDefComboBox.SIZE_DEFINITIONS.length - 1; i >= 0; i-- )
                            {
                                currentDef = FWSizeDefComboBox.SIZE_DEFINITIONS[ i ];
                                mod = range.min % currentDef.getMultiplier();
                                if ( mod == 0 )
                                {
                                    minFileSizeUnitComboBox.setSelectedIndex( i );
                                    displayVal = range.min / currentDef.getMultiplier();
                                    break;
                                }
                            }
                            minFileSizeTF.setText( String.valueOf( displayVal ) );
                        }
                        if ( range.max > 0 )
                        {
                            // initialize to bytes
                            maxFileSizeUnitComboBox.setSelectedIndex( 0 );
                            long mod;
                            long displayVal = range.max;
                            FWSizeDefComboBox.SizeDefinition currentDef;
                            for ( int i = FWSizeDefComboBox.SIZE_DEFINITIONS.length - 1; i >= 0; i-- )
                            {
                                currentDef = FWSizeDefComboBox.SIZE_DEFINITIONS[ i ];
                                mod = range.max % currentDef.getMultiplier();
                                if ( mod == 0 )
                                {
                                    maxFileSizeUnitComboBox.setSelectedIndex( i );
                                    displayVal = range.min / currentDef.getMultiplier();
                                    break;
                                }
                           }
                            maxFileSizeTF.setText( String.valueOf( displayVal ) );
                        }
                    }
                }
                else if ( condition instanceof FilenameCondition )
                {
                    FilenameCondition filenameCondition = (FilenameCondition) condition;
                    StringBuffer buffer = new StringBuffer();
                    Set<String> terms = filenameCondition.getTerms();
                    if ( terms.size() > 0 )
                    {
                        Iterator<String> iterator = terms.iterator();
                        
                        while ( iterator.hasNext() )
                        {
                            buffer.append( iterator.next() );
                            if ( iterator.hasNext() )
                            {
                                buffer.append( " " );
                            }
                        }
                    }
                    withoutTermsTF.setText( buffer.toString() );
                }
            }
        }
        currentResultsDataModel = searchResultsDataModel;
        currentQuickFilterRule = quickFilterRule;
    }
    
    private class QuickFilterActivationHandler extends KeyAdapter 
        implements ItemListener
    {
        @Override
        public void keyTyped( KeyEvent e )
        {
            activateQuickFilterTimer.restart();
        }

        public void itemStateChanged( ItemEvent e )
        {
            activateQuickFilterTimer.restart();
        }
    }
}
