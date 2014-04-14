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
 */
package phex.gui.dialogs.options;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import phex.utils.Localizer;

/**
 * The base class of all settings panes that represent the right side of the
 * OptionsDialog.
 */
public abstract class OptionsSettingsPane extends JPanel
{
    /**
     * This can be used to flag number format errors in a pane. If the value of
     * the key is the text field itself you can use the method
     * displayNumberFormatError() for easy error messaging.
     */
    protected static final String NUMBER_FORMAT_ERROR_KEY = "NumberFormatError";
    protected static final String IS_INPUT_VALID_KEY = "IsInputValid";
    protected static final String SAVE_CONFIG_TRIGGER = "SaveConfigTrigger";
    protected static final String RESCAN_FILES_TRIGGER = "RescanFilesTrigger";

    private final String treeRepresentation;
    private boolean isPreparedForDisplay;

    public OptionsSettingsPane( String aTreeRepresentation )
    {
        super();
        treeRepresentation = aTreeRepresentation;
        isPreparedForDisplay = false;
    }

    /**
     * Prepares the settings pane for display by adding the child components.
     */
    public void prepareForDisplay()
    {
        if ( !isPreparedForDisplay )
        {
            prepareComponent();
            isPreparedForDisplay = true;
        }
    }

    /**
     * Can be used to check if this pane was ever displayed. If not displayed
     * no validation or saving needs to be done.
     */
    public boolean isSettingsPaneDisplayed()
    {
        // if never prepared then not displayed
        return isPreparedForDisplay;
    }

    /**
     * Called when preparing this settings pane for display the first time. Can
     * be overriden to implement the look of the settings pane.
     */
    protected abstract void prepareComponent();

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
    public void checkInput( HashMap<String, Object> inputDic )
    {
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
    public void displayErrorMessage( HashMap<String, Object> inputDic )
    {
    }

    /**
     * Override this method if you like to apply and save changes made on
     * settings pane. To trigger saving of the configuration if any value was
     * changed call triggerConfigSave().
     */
    public void saveAndApplyChanges( HashMap<String, Object> inputDic )
    {
    }

    /**
     * Returns the not localized tree representation string.
     */
    protected String getOptionTreeRepresentation()
    {
        return treeRepresentation;
    }

    /**
     * To mark if the input in this pane is valid or not every settings pane
     * needs to call this method.
     */
    protected void setInputValid( HashMap<String, Object> inputDic, boolean isValid )
    {
        inputDic.put( IS_INPUT_VALID_KEY, Boolean.valueOf( isValid ) );
    }

    /**
     * To check if the input of a settings pane is valid.
     */
    public boolean isInputValid( HashMap<String, Object> inputDic )
    {
        Boolean status = (Boolean) inputDic.get( IS_INPUT_VALID_KEY );
        return status.booleanValue();
    }

    /**
     * To trigger rescanning of shared files after appling all the changes.
     * This is usefull to trigger only one rescans for all panes.
     */
    public static void triggerSharedFilesRescan( HashMap<String, Object> inputDic )
    {
        if ( !inputDic.containsKey( RESCAN_FILES_TRIGGER ) )
        {
            inputDic.put( RESCAN_FILES_TRIGGER, RESCAN_FILES_TRIGGER );
        }
    }

    /**
     * To check if the saving of the configuration was triggered by any pane.
     */
    public static boolean isSharedFilesRescanTriggered( HashMap<String, Object> inputDic )
    {
        return inputDic.containsKey( RESCAN_FILES_TRIGGER );
    }

    protected void displayNumberFormatError( HashMap<String, Object> inputDic )
    {
        Component comp = (Component)inputDic.get( NUMBER_FORMAT_ERROR_KEY );
        if ( comp == null )
        {
            return;
        }
        comp.requestFocus();
        if ( comp instanceof JTextField )
        {
            ((JTextField)comp).selectAll();
        }
        JOptionPane.showMessageDialog( this,
            Localizer.getString( "WrongNumberFormat" ),
            Localizer.getString( "FormatError" ), JOptionPane.ERROR_MESSAGE  );
    }
}