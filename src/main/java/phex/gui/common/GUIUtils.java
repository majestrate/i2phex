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
 *  $Id: GUIUtils.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.gui.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Keymap;

import phex.gui.common.table.FWTable;
import phex.gui.common.table.FWTableColumn;
import phex.utils.Localizer;
import phex.xml.sax.gui.DGuiSettings;
import phex.xml.sax.gui.DTab;
import phex.xml.sax.gui.DTable;
import phex.xml.sax.gui.DTableColumn;
import phex.xml.sax.gui.DTableColumnList;

public final class GUIUtils
{
    public static final Insets EMPTY_INSETS = new Insets( 0, 0, 0, 0 );
    public static final Insets NARROW_BUTTON_INSETS = new Insets( 1, 1, 1, 3 );
    public static final Border ROLLOVER_BUTTON_BORDER = new RolloverButtonBorder();
    
    private GUIUtils()
    {
    }
    
    /**
     * To ensure that a Keymap sticks with a ComboBoxEditor we need to do a
     * special handling. During a UI update the ComboBox replaces its editor.
     * The new editor will initialize its Keymap during its UI update. After this
     * UI update we are able to set our wanted Keymap on the ComboBoxEditor.
     * Therefore we add a special property change listener to UI updates of the 
     * ComboBox and the ComboBoxEditor. On the occuring UI change event of the
     * ComboBox (during which the editor was replaced), we reassign the property
     * change listener of the ComboBoxEditor. On the occuring UI change event
     * of the ComboBoxEditor (during which the Keymap was initialized) we set
     * our own wanted Keymap.
     * @param keymap the Keymap we like to force on the ComboBoxEditor.
     * @param comboBox the ComboBox with the ComboBoxEditor that should always
     *        use the given Keymap.
     */
    public static void assignKeymapToComboBoxEditor( Keymap keymap, JComboBox comboBox )
    {
        ComboBoxUIChangeListener listener = new ComboBoxUIChangeListener( keymap, comboBox );
        comboBox.addPropertyChangeListener( "UI", listener );
        ComboBoxEditor comboEditor = comboBox.getEditor();
        JTextField editor = ((JTextField)comboEditor.getEditorComponent());
        editor.addPropertyChangeListener( "UI", listener ); 
    }
    
    /**
     * The property change listener used to ensure a Keymap for a ComboBoxEditor
     * of a ComboBox.
     * @see #assignKeymapToComboBoxEditor()
     */
    private static class ComboBoxUIChangeListener implements PropertyChangeListener
    {
        private Keymap keymap;
        private JComboBox comboBox;
        
        public ComboBoxUIChangeListener( Keymap keymap, JComboBox comboBox )
        {
            this.keymap = keymap;
            this.comboBox = comboBox;
        }
        public void propertyChange(PropertyChangeEvent evt)
        {
            if ( !evt.getPropertyName().equals("UI") )
            {
                return;
            }
            
            if ( evt.getSource() == comboBox )
            {
                // during a UI update the comboBox editor changed...
                // reregister property change listener
                ComboBoxEditor comboEditor = comboBox.getEditor();
                JTextField editor = ((JTextField)comboEditor.getEditorComponent());
                editor.addPropertyChangeListener( "UI", this ); 
            }
            else
            {
                // this must be the editor of the comboBox that updates its keymap
                // and UI... reset the keymap...
                ComboBoxEditor comboEditor = comboBox.getEditor();
                JTextField editor = ((JTextField)comboEditor.getEditorComponent());
                editor.setKeymap( keymap );
            }
        }
    }
    
    /**
     * To ensure that a Keymap sticks with a ComboBoxEditor we need to do a
     * special handling. During a UI update the ComboBox replaces its editor.
     * The new editor will initialize its Keymap during its UI update. After this
     * UI update we are able to set our wanted Keymap on the ComboBoxEditor.
     * Therefore we add a special property change listener to UI updates of the 
     * ComboBox and the ComboBoxEditor. On the occuring UI change event of the
     * ComboBox (during which the editor was replaced), we reassign the property
     * change listener of the ComboBoxEditor. On the occuring UI change event
     * of the ComboBoxEditor (during which the Keymap was initialized) we set
     * our own wanted Keymap.
     * @param keymap the Keymap we like to force on the ComboBoxEditor.
     * @param comboBox the ComboBox with the ComboBoxEditor that should always
     *        use the given Keymap.
     */
    public static void assignKeymapToTextField( Keymap keymap, JTextField textField )
    {
        TextFieldUIChangeListener listener = new TextFieldUIChangeListener( keymap, textField );
        textField.addPropertyChangeListener( "UI", listener ); 
    }
    
    /**
     * The property change listener used to ensure a Keymap for a ComboBoxEditor
     * of a ComboBox.
     * @see #assignKeymapToComboBoxEditor()
     */
    private static class TextFieldUIChangeListener implements PropertyChangeListener
    {
        private Keymap keymap;
        private JTextField textField;
        
        public TextFieldUIChangeListener( Keymap keymap, JTextField textField )
        {
            this.keymap = keymap;
            this.textField = textField;
        }
        public void propertyChange(PropertyChangeEvent evt)
        {
            if ( !evt.getPropertyName().equals("UI") )
            {
                return;
            }
                        
            // this must be the editor of the comboBox that updates its keymap
            // and UI... reset the keymap...
            textField.setKeymap( keymap );
        }
    }

    /**
     * Sets the window location in the center relative to the location of
     * relativeWindow.
     */
    public static void setWindowLocationRelativeTo( Window window,
        Window relativeWindow )
    {
        Rectangle windowBounds = window.getBounds();
        Dimension rwSize = relativeWindow.getSize();
        Point rwLoc = relativeWindow.getLocation();

        int dx = rwLoc.x + (( rwSize.width - windowBounds.width ) >> 1 );
        int dy = rwLoc.y + (( rwSize.height - windowBounds.height ) >> 1 );
        Dimension ss = window.getToolkit().getScreenSize();

        if ( dy + windowBounds.height > ss.height)
        {
            dy = ss.height - windowBounds.height;
            dx = rwLoc.x < ( ss.width >> 1 ) ? rwLoc.x + rwSize.width :
                rwLoc.x - windowBounds.width;
        }
        if ( dx + windowBounds.width > ss.width )
        {
            dx = ss.width - windowBounds.width;
        }
        if (dx < 0)
        {
            dx = 0;
        }
        if (dy < 0)
        {
            dy = 0;
        }
        window.setLocation( dx, dy );
    }
    
    ////// Start Phex GUI DElement handling

    public static DTable getDGuiTableByIdentifier( DGuiSettings guiSettings,
        String tableIdentifier )
    {
        if ( guiSettings == null )
        {
            return null;
        }
        DTable dTable;
        Iterator iterator = guiSettings.getTableList().getTableList().iterator();
        while( iterator.hasNext() )
        {
            dTable = (DTable)iterator.next();
            if ( dTable.getTableIdentifier().equals( tableIdentifier ) )
            {
                return dTable;
            }
        }
        return null;
    }

    public static DTab getDGuiTabById( DGuiSettings guiSettings,
        int tabID )
    {
        if ( guiSettings == null )
        {
            return null;
        }
        DTab dTab;
        Iterator iterator = guiSettings.getTabList().iterator();
        while( iterator.hasNext() )
        {
            dTab = (DTab)iterator.next();
            if ( dTab.getTabId() == tabID )
            {
                return dTab;
            }
        }
        return null;
    }
    
    public static void updateTableFromDGuiSettings( DGuiSettings dGuiSettings, FWTable table, 
        String identifier )
    {
        DTable dTable = getDGuiTableByIdentifier( dGuiSettings, identifier );
        updateTableFromDTable( dTable, table );
    }
    
    public static void updateTableFromDTable( DTable dTable, FWTable table )
    {
        if ( dTable == null )
        {
            return;
        }
        DTableColumnList dColumnList = dTable.getTableColumnList();
        if ( dColumnList == null )
        {
            return;
        }
        
        for( DTableColumn dColumn : dColumnList.getSubElementList() )
        {
            Integer colId = Integer.valueOf( dColumn.getColumnID() );
            FWTableColumn column = table.getFWColumn( colId );
            if ( column == null )
            {// column might not be part of model anymore...
             // or is not of managable type
                continue;
            }
            column.setVisible( dColumn.isVisible() );
            column.setPreferredWidth( dColumn.getWidth() );
            if ( column.isVisible() )
            {
                int colIdx = table.getColumnModel().getColumnIndex( colId );
                int visibleIndex = dColumn.getVisibleIndex();
                visibleIndex = Math.min( visibleIndex, table.getColumnCount() - 1 );
                table.moveColumn( colIdx, visibleIndex );
            }
        }        
    }

    
    /**
     * Create the DTable of a FWTable for storage of the table structure.
     * @param table
     * @param tableId
     * @return
     */
    public static DTable createDTable( FWTable table, String tableId )
    {
        DTable dTable = new DTable();
        DTableColumnList dList = createDTableColumnList( table );
        dTable.setTableColumnList( dList );
        dTable.setTableIdentifier( tableId );
        return dTable;
    }
    
    public static DTableColumnList createDTableColumnList( FWTable table )
    {        
        DTableColumnList colList = new DTableColumnList();
        List<DTableColumn> list = colList.getSubElementList();
        List<TableColumn> allColumns = table.getColumns( true );
        
        TableColumnModel model = table.getColumnModel();
        
        // cols need to be sorrted by visible index to be able to build up
        // the table on restart without running into index out of bounds.
        Collections.sort( allColumns, new VisibleTableColumnComparator( model ) );
        for( TableColumn column : allColumns )
        {
            if ( !(column instanceof FWTableColumn) )
            {
                continue;
            }
            FWTableColumn fwColumn = (FWTableColumn)column;
            DTableColumn dColumn = fwColumn.createDGuiTableColumn();
            if ( fwColumn.isVisible() )
            {
                dColumn.setVisibleIndex( model.getColumnIndex( column.getIdentifier() ) );
            }
            list.add( dColumn );
        }
        return colList;
    }
    
    private static class VisibleTableColumnComparator implements Comparator<TableColumn>
    {
        private TableColumnModel model;
        
        public VisibleTableColumnComparator( TableColumnModel model )
        {
            this.model = model;
        }
        
        public int compare( TableColumn col1, TableColumn col2 )
        {
            if ( !(col1 instanceof FWTableColumn) )
            {
                return -1;
            }
            if ( !(col2 instanceof FWTableColumn) )
            {
                return 1;
            }
            FWTableColumn fwCol1 = (FWTableColumn) col1;
            FWTableColumn fwCol2 = (FWTableColumn) col2;
            if ( !fwCol1.isVisible() )
            {
                // if col1 is not visible then col2 is larger (if visible or not)
                return -1;
            }
            if ( !fwCol2.isVisible() )
            {
                // if col2 is not visible then col1 is larger (if visible or not)
                return 1;
            }

            // both cols are visible determine the higher index
            int col1Idx = model.getColumnIndex( col1.getIdentifier() );
            int col2Idx = model.getColumnIndex( col2.getIdentifier() );

            return col1Idx - col2Idx;
        }
    }
    
    ////// End Phex GUI DElement handling

    public static void adjustComboBoxHeight( JComboBox comboBox )
    {
        if ( comboBox == null )
        {
            return;
        }
        Font font = (Font) UIManager.getDefaults().get( "ComboBox.font" );
        if ( font != null )
        {
            Dimension uiSize = comboBox.getUI().getPreferredSize( comboBox );
            FontMetrics fontMetrics = comboBox.getFontMetrics( font );
            int height = fontMetrics.getHeight() + fontMetrics.getDescent() + 3;
            comboBox.setPreferredSize( new Dimension( uiSize.width + 4, height ) );
        }
    }

    public static void adjustTableProgresssBarHeight( JTable table )
    {
        Font progressFont = (Font) UIManager.getDefaults().get( "ProgressBar.font" );
        FontMetrics fontMetrics = table.getFontMetrics( progressFont );
        // no descent used since numbers have no descent...
        int height = fontMetrics.getHeight() + fontMetrics.getDescent();
        table.setRowHeight( height );
    }

    public static void showErrorMessage( String message )
    {
        showErrorMessage( GUIRegistry.getInstance().getMainFrame(), message );
    }

    public static void showErrorMessage( String message, String title )
    {
        showErrorMessage( null, message, title );
    }

    public static void showErrorMessage( Component parent, String message )
    {
        showErrorMessage( parent, message, Localizer.getString( "Error" ) );
    }

    public static void showErrorMessage( Component parent, String message,
        String title )
    {
        if ( parent == null )
        {
            parent = GUIRegistry.getInstance().getMainFrame();
        }
        JOptionPane.showMessageDialog( parent, message,
            title, JOptionPane.ERROR_MESSAGE );
    }

    /**
    *    Center the window on the screen
    *
    *    @param win        The window object to position.
    *    @param offset    The amount to offset from the center of the screen.
    */
    public static void centerWindowOnScreen( Window win )
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension winSize = win.getSize();
        Rectangle rect = new Rectangle(
            (screenSize.width - winSize.width) / 2,
            (screenSize.height - winSize.height) / 2,
            winSize.width, winSize.height );
        win.setBounds(rect);
    }

    // Center Window on screen
    public static void centerAndSizeWindow( Window win, int fraction, int base)
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width  = screenSize.width * fraction / base;
        int height = screenSize.height * fraction / base;
    
        // for debug
        //width = 800;
        //height = 600;
    
        Rectangle rect = new Rectangle( (screenSize.width - width) / 2,
            (screenSize.height - height) / 2, width, height );
        win.setBounds(rect);
    }
    
    public static void updateComponentsUI()
    {
        PhexColors.updateColors();
        MainFrame frame = GUIRegistry.getInstance().getMainFrame();
        if ( frame == null )
        {
            return;
        }
        SwingUtilities.updateComponentTreeUI( frame );

        // go through child windows
        Window[] windows = frame.getOwnedWindows();
        for ( int j = 0; j < windows.length; j++ )
        {
            SwingUtilities.updateComponentTreeUI( windows[j] );
        }
    }
    
    /**
     * Creates a new <code>Color</code> that is a brighter version of this
     * <code>Color</code>. This method is the same implementation
     * java.awt.Color#brighter is usind except it has a configurable factor.
     * The java.awt.Color default facotr is 0.7
     * @return     a new <code>Color</code> object that is  
     *                 a brighter version of this <code>Color</code>.
     * @see        java.awt.Color#darker
     */
    public static Color brighterColor( Color color, double factor ) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        int i = (int)(1.0/(1.0-factor));
        if ( r == 0 && g == 0 && b == 0) {
           return new Color(i, i, i);
        }
        if ( r > 0 && r < i ) r = i;
        if ( g > 0 && g < i ) g = i;
        if ( b > 0 && b < i ) b = i;

        return new Color(Math.min((int)(r/factor), 255),
                         Math.min((int)(g/factor), 255),
                         Math.min((int)(b/factor), 255));
    }

    /**
     * Creates a new <code>Color</code> that is a darker version of this
     * <code>Color</code>. This method is the same implementation
     * java.awt.Color#darker is usind except it has a configurable factor.
     * The java.awt.Color default facotr is 0.7
     * @return  a new <code>Color</code> object that is 
     *                    a darker version of this <code>Color</code>.
     * @see        java.awt.Color#brighter
     */
    public static Color darkerColor( Color color, double factor )
    {
        return new Color(Math.max((int)(color.getRed()  * factor), 0), 
             Math.max((int)(color.getGreen()* factor), 0),
             Math.max((int)(color.getBlue() * factor), 0));
    }
    
    /**
     * The accaptable rage for color brightness difference is 125. 
     * The range for color difference is 500.
     * @param base
     * @param candidates
     * @return
     */
    public static Color getBestColorMatch( Color base, Color[] candidates )
    {
        Color bestMatch = candidates[0];
        int matchValue;
        
        int brightnessDiff = getColorBrightness( base, candidates[0] );
        int colorDiff = getColorDifference( base, candidates[0] );
        matchValue = (brightnessDiff - 125) + (colorDiff - 500);
        
        for ( int i = 1; i < candidates.length; i++ )
        {
            brightnessDiff = getColorBrightness( base, candidates[i] );
            colorDiff = getColorDifference( base, candidates[i] );
            int testVal = (brightnessDiff - 125) + (colorDiff - 500);
            if ( testVal > matchValue )
            {
                matchValue = testVal;
                bestMatch = candidates[i];
            }
        }
        return bestMatch;
    }
    
    /**
     * Calculates the color difference according to W3C rules
     * http://www.w3.org/TR/AERT#color-contrast
     * Color difference is determined by the following formula:
     * (maximum (Red value 1, Red value 2) - minimum (Red value 1, Red value 2)) 
     * + (maximum (Green value 1, Green value 2) - minimum (Green value 1, Green value 2)) 
     * + (maximum (Blue value 1, Blue value 2) - minimum (Blue value 1, Blue value 2))
     * @param col1 the color to calculate the brightness for.
     * @param col2 the color to calculate the brightness for.
     * @return the color brightness difference.
     */
    public static int getColorDifference( Color col1, Color col2 )
    {
        int diff = 
            ( Math.max( col1.getRed(), col2.getRed() ) 
              - Math.min( col1.getRed(), col2.getRed() ) ) 
          + ( Math.max( col1.getGreen(), col2.getGreen() ) 
              - Math.min( col1.getGreen(), col2.getGreen() )) 
          + (Math.max( col1.getBlue(), col2.getBlue() ) 
              - Math.min( col1.getBlue(), col2.getBlue() ));
        return Math.abs( diff );
    }
    
    /**
     * Calculates the color brightness difference according to W3C rules
     * http://www.w3.org/TR/AERT#color-contrast
     * Color brightness is determined by the following formula:
     * ((Red value X 299) + (Green value X 587) + (Blue value X 114)) / 1000
     * Note: This algorithm is taken from a formula for converting RGB values 
     * to YIQ values. This brightness value gives a perceived brightness for a color.
     * @param col1 the color to calculate the brightness for.
     * @param col2 the color to calculate the brightness for.
     * @return the color brightness difference.
     */
    public static int getColorBrightness( Color col1, Color col2 )
    {
        int b1 = ((col1.getRed() * 299) + (col1.getGreen() * 587) + (col1.getBlue() * 114)) / 1000;
        int b2 = ((col2.getRed() * 299) + (col2.getGreen() * 587) + (col2.getBlue() * 114)) / 1000;
        return Math.abs( b1-b2 );
    }
    
    public static void showMainFrame()
    {
        GUIRegistry registry = GUIRegistry.getInstance();
        MainFrame mainFrame = registry.getMainFrame();
        mainFrame.setVisible(true);
        
        DesktopIndicator indicator = registry.getDesktopIndicator();
        if ( indicator != null )
        {
            indicator.hideIndicator();
        }
        if ( mainFrame.getState() != JFrame.NORMAL )
        {
            mainFrame.setState( Frame.NORMAL );
        }
        mainFrame.toFront();
        mainFrame.requestFocus();
    }
    
    public static class RolloverButtonBorder extends AbstractBorder
    {
        private static final Insets INSETS_3 = new Insets( 3, 3, 3, 3 );
        
        private Color controlShadow;
        private Color controlDkShadow;
        private Color controlHighlight;
        private Color control;
        private boolean isShownWhenSelected;
        
        public RolloverButtonBorder()
        {
            this( true );
        }
        
        public RolloverButtonBorder( boolean isShownWhenSelected )
        {
            this.isShownWhenSelected = isShownWhenSelected;
            UIDefaults table = UIManager.getLookAndFeelDefaults();
            controlShadow = table.getColor( "controlShadow" );
            controlDkShadow = table.getColor( "controlDkShadow" );
            controlHighlight = table.getColor( "controlHighlight" );
            control = table.getColor( "control" );
        }

        public void paintBorder( Component c, Graphics g, int x, int y, int w,
            int h )
        {
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();

            if ( !model.isEnabled() ) return;

            if ( model.isRollover() )
            {
                if ( model.isPressed() && model.isArmed() )
                {
                    drawPressed3DBorder( g, x, y, w, h );
                }
                else
                {
                    drawFlush3DBorder( g, x, y, w, h );
                }
            }
            else if ( isShownWhenSelected && model.isSelected() )
            {
                drawDark3DBorder( g, x, y, w, h );
            }
        }
        
        private void drawPressed3DBorder(Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            drawFlush3DBorder(g, 0, 0, w, h);
            g.setColor( controlShadow );
            g.drawLine(1, 1, 1, h - 3);
            g.drawLine(1, 1, w - 3, 1);
            g.translate(-x, -y);
        }
        
        private void drawDark3DBorder(Graphics g, int x, int y, int w, int h) {
            drawFlush3DBorder(g, x, y, w, h);
            g.setColor( control );
            g.drawLine(x+1, y+1, 1, h - 3);
            g.drawLine(y+1, y+1, w - 3, 1);
        }
        
        private void drawFlush3DBorder(Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            g.setColor( controlHighlight );
            drawRect(g, 1, 1, w - 2, h - 2);
            g.drawLine(0, h - 1, 0, h - 1);
            g.drawLine(w - 1, 0, w - 1, 0);
            g.setColor( controlDkShadow );
            drawRect(g, 0, 0, w - 2, h - 2);
            g.translate(-x, -y);
        }
        
        /*
         * An optimized version of Graphics.drawRect.
         */
        private static void drawRect(Graphics g, int x, int y, int w, int h) {
            g.fillRect(x,   y,   w+1, 1);
            g.fillRect(x,   y+1, 1,   h);
            g.fillRect(x+1, y+h, w,   1);
            g.fillRect(x+w, y+1, 1,   h);
        }

        public Insets getBorderInsets( Component c )
        {
            return INSETS_3;
        }

        public Insets getBorderInsets( Component c, Insets newInsets )
        {
            newInsets.top = INSETS_3.top;
            newInsets.left = INSETS_3.left;
            newInsets.bottom = INSETS_3.bottom;
            newInsets.right = INSETS_3.right;
            return newInsets;
        }
    }
}