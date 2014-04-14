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
 *  $Id: DirectoriesPane.java 4416 2009-03-22 17:12:33Z ArneBab $
 */
package phex.gui.dialogs.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import phex.gui.common.GUIUtils;
import phex.prefs.core.DownloadPrefs;
import phex.utils.DirectoryOnlyFileFilter;
import phex.utils.FileUtils;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DirectoriesPane extends OptionsSettingsPane
{
    private static final String DIRECTORY_ERROR_KEY = "DirectoryErrorKey";
    private static final String DOWNLOAD_DIRECTORY_KEY = "DownloadDirectory";
    private static final String INCOMPLETE_DIRECTORY_KEY = "IncompleteDirectory";

    private static final String INCOMPLETE_DIR_SHARED = "IncompleteDirShared";
    private static final String NO_DIRECTORY_SHARED = "NoDirectoryShared";
    private static final String CANT_CREATE_DOWNLOAD_DIR = "CantCreateDownloadDir";
    private static final String CANT_CREATE_INCOMPLETE_DIR = "CantCreateIncompleteDir";

    private JTextField incompleteDirectoryTF;
    private JTextField downloadDirectoryTF;

    public DirectoriesPane()
    {
        super( "Directories" );
    }

    /**
     * Called when preparing this settings pane for display the first time. Can
     * be overriden to implement the look of the settings pane.
     */
    @Override
    protected void prepareComponent()
    {
        FormLayout layout = new FormLayout(
            "10dlu, right:d, 2dlu, d:grow, 2dlu, d, 2dlu", // columns
            "p, 3dlu, p, 3dlu, p, 9dlu, " + // rows
            "p, 3dlu, p, 3dlu, p, 15dlu:grow, 3dlu, p, 3dlu, p, 3dlu, p" ); 
        layout.setRowGroups( new int[][]{{3, 5, 9, 11, 14, 16, 18}} );
        
        //setLayout( new BorderLayout() );
        //JPanel contentPanel = new FormDebugPanel();
        //add( contentPanel, BorderLayout.CENTER );
        
        //PanelBuilder builder = new PanelBuilder( contentPanel, layout );
        PanelBuilder builder = new PanelBuilder( layout, this );
        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator( Localizer.getString( "DirSettings_DownloadFolders" ),
            cc.xywh( 1, 1, 7, 1 ));
        
        builder.addLabel( Localizer.getString( "DirSettings_Incomplete" ) + ": ",
            cc.xy( 2, 3 ) );
        incompleteDirectoryTF = new JTextField( DownloadPrefs.IncompleteDirectory.get(), 30 );
        builder.add( incompleteDirectoryTF, cc.xy( 4, 3 ) );
        JButton button = new JButton( Localizer.getString( "DirSettings_SetFolder" ) );
        //button.setMargin( noInsets );
        button.addActionListener( new SetIncompleteDirectoryListener() );
        builder.add( button, cc.xy( 6, 3 ) );
        
        builder.addLabel( Localizer.getString( "DirSettings_Completed" ) + ": ",
            cc.xy( 2, 5 ) );
        downloadDirectoryTF = new JTextField( DownloadPrefs.DestinationDirectory.get(), 30 );
        builder.add( downloadDirectoryTF, cc.xy( 4, 5 ) );
        button = new JButton( Localizer.getString( "DirSettings_SetFolder" ) );
        //button.setMargin( noInsets );
        button.addActionListener( new SetDownloadDirectoryListener() );
        builder.add( button, cc.xy( 6, 5 ) );
        
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
    public void checkInput( HashMap<String, Object> inputDic )
    {
        File downloadDir = null;
        String downloadDirPath = downloadDirectoryTF.getText();
        downloadDir = new File( downloadDirPath );

        String incompleteDirPath = incompleteDirectoryTF.getText();
        File incompleteDir = new File( incompleteDirPath );

        if ( !downloadDir.exists() || !downloadDir.isDirectory() )
        {
            try
            {
                FileUtils.forceMkdir( downloadDir );
            }
            catch ( IOException exp )
            {
                inputDic.put( DIRECTORY_ERROR_KEY, CANT_CREATE_DOWNLOAD_DIR );
                setInputValid( inputDic, false );
                return;
            }
        }

        if ( !incompleteDir.exists() || !incompleteDir.isDirectory())
        {
            try
            {
                FileUtils.forceMkdir( incompleteDir );
            }
            catch ( IOException exp )
            {
                inputDic.put( DIRECTORY_ERROR_KEY, CANT_CREATE_INCOMPLETE_DIR );
                setInputValid( inputDic, false );
                return;
            }
        }
        inputDic.put( DOWNLOAD_DIRECTORY_KEY, downloadDir );
        inputDic.put( INCOMPLETE_DIRECTORY_KEY, incompleteDir );
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
    public void displayErrorMessage( HashMap inputDic )
    {
        Object error = inputDic.get( DIRECTORY_ERROR_KEY );
        if ( error == null )
        {// no error
            return;
        }

        if ( error.equals( INCOMPLETE_DIR_SHARED ) )
        {
            incompleteDirectoryTF.requestFocus();
            incompleteDirectoryTF.selectAll();
            GUIUtils.showErrorMessage(
                Localizer.getString( INCOMPLETE_DIR_SHARED ),
                Localizer.getString( "DirectoryError" ) );
        }
        else if ( error.equals( NO_DIRECTORY_SHARED ) )
        {
            GUIUtils.showErrorMessage(
                Localizer.getString( NO_DIRECTORY_SHARED ),
                Localizer.getString( "DirectoryError" ) );
        }
        else if ( error.equals( CANT_CREATE_DOWNLOAD_DIR ) )
        {
            downloadDirectoryTF.requestFocus();
            downloadDirectoryTF.selectAll();
            GUIUtils.showErrorMessage(
                Localizer.getFormatedString( CANT_CREATE_DOWNLOAD_DIR, 
                    downloadDirectoryTF.getText() ),
                Localizer.getString( "DirectoryError" ) );
        }
        else if ( error.equals( CANT_CREATE_INCOMPLETE_DIR ) )
        {
            incompleteDirectoryTF.requestFocus();
            incompleteDirectoryTF.selectAll();
            GUIUtils.showErrorMessage(
                Localizer.getFormatedString( CANT_CREATE_INCOMPLETE_DIR, 
                    incompleteDirectoryTF.getText() ),
                Localizer.getString( "DirectoryError" ) );
        }
    }

    /**
     * Override this method if you like to apply and save changes made on
     * settings pane. To trigger saving of the configuration if any value was
     * changed call triggerConfigSave().
     */
    @Override
    public void saveAndApplyChanges( HashMap inputDic )
    {
        File downloadDir = (File)inputDic.get( DOWNLOAD_DIRECTORY_KEY );
        String downloadDirPath = downloadDir.getAbsolutePath();
        DownloadPrefs.DestinationDirectory.set( downloadDirPath );

        File incompleteDir = (File)inputDic.get( INCOMPLETE_DIRECTORY_KEY );
        String incompleteDirPath = incompleteDir.getAbsolutePath();
        DownloadPrefs.IncompleteDirectory.set(incompleteDirPath);
    }

    private class SetDownloadDirectoryListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile( new File( downloadDirectoryTF.getText() ) );
            chooser.setAcceptAllFileFilterUsed( false );
            chooser.setFileFilter( new DirectoryOnlyFileFilter() );
            chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
            chooser.setMultiSelectionEnabled( false );
            chooser.setDialogTitle(
                Localizer.getString( "SelectDownloadDirectory" ) );
            chooser.setApproveButtonText( Localizer.getString( "Select" ) );
            chooser.setApproveButtonMnemonic(
                Localizer.getChar( "SelectMnemonic" ) );
            int returnVal = chooser.showDialog( DirectoriesPane.this, null );
            if( returnVal == JFileChooser.APPROVE_OPTION )
            {
                String directory = chooser.getSelectedFile().getAbsolutePath();
                downloadDirectoryTF.setText( directory );
            }
        }
    }

    private class SetIncompleteDirectoryListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile( new File( incompleteDirectoryTF.getText() ) );
            chooser.setAcceptAllFileFilterUsed( false );
            chooser.setFileFilter( new DirectoryOnlyFileFilter() );
            chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
            chooser.setMultiSelectionEnabled( false );
            chooser.setDialogTitle(
                Localizer.getString( "SelectIncompleteDirectory" ) );
            chooser.setApproveButtonText( Localizer.getString( "Select" ) );
            chooser.setApproveButtonMnemonic(
                Localizer.getChar( "SelectMnemonic" ) );
            int returnVal = chooser.showDialog( DirectoriesPane.this, null );
            if( returnVal == JFileChooser.APPROVE_OPTION )
            {
                String directory = chooser.getSelectedFile().getAbsolutePath();
                incompleteDirectoryTF.setText( directory );
            }
        }
    }
}