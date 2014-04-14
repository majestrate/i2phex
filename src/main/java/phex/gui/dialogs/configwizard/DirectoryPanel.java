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
 *  $Id: DirectoryPanel.java 4416 2009-03-22 17:12:33Z ArneBab $
 */
package phex.gui.dialogs.configwizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;

import phex.gui.common.GUIUtils;
import phex.gui.common.HTMLMultiLinePanel;
import phex.prefs.core.DownloadPrefs;
import phex.utils.DirectoryOnlyFileFilter;
import phex.utils.FileUtils;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DirectoryPanel extends JPanel
{
    private ConfigurationWizardDialog parent;
    
    private JTextField incompleteDirectoryTF;
    private JTextField downloadDirectoryTF;
    
    public DirectoryPanel( ConfigurationWizardDialog parent )
    {
        this.parent = parent;
        prepareComponent();
    }
    
    private void prepareComponent()
    {
        FormLayout layout = new FormLayout(
            "10dlu, right:d, 2dlu, d:grow, 2dlu, d, right:d:grow", // columns
            "p, 3dlu, p, 8dlu, p, 3dlu, p, 8dlu, p" );// rows 
        
        setLayout( layout );
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder( layout, this );
        int columnCount = layout.getColumnCount();
        
        builder.addSeparator( Localizer.getString( "ConfigWizard_DirectoryHeader" ),
            cc.xywh( 1, 1, columnCount, 1 ) );
        
        HTMLMultiLinePanel welcomeLines = new HTMLMultiLinePanel(
            Localizer.getString( "ConfigWizard_DirectoryText" ) );
        builder.add( welcomeLines, cc.xywh( 2, 3, columnCount-2, 1 ) );
        
        builder.addLabel( Localizer.getString( "ConfigWizard_Incomplete" ),
            cc.xy( 2, 5 ) );
        incompleteDirectoryTF = new JTextField( DownloadPrefs.IncompleteDirectory.get(), 30 );
        builder.add( incompleteDirectoryTF, cc.xy( 4, 5 ) );
        JButton button = new JButton( Localizer.getString( "ConfigWizard_SetFolder" ) );
        //button.setMargin( noInsets );
        button.addActionListener( new SetIncompleteDirectoryListener() );
        builder.add( button, cc.xy( 6, 5 ) );
        
        builder.addLabel( Localizer.getString( "ConfigWizard_Destination" ),
            cc.xy( 2, 7 ) );
        downloadDirectoryTF = new JTextField( DownloadPrefs.DestinationDirectory.get(), 30 );
        builder.add( downloadDirectoryTF, cc.xy( 4, 7 ) );
        button = new JButton( Localizer.getString( "ConfigWizard_SetFolder" ) );
        //button.setMargin( noInsets );
        button.addActionListener( new SetDownloadDirectoryListener() );
        builder.add( button, cc.xy( 6, 7 ) );
        
        HTMLMultiLinePanel welcomeLines2 = new HTMLMultiLinePanel(
            Localizer.getString( "ConfigWizard_DirectoryText2" ) );        
        builder.add( welcomeLines2, cc.xywh( 2, 9, columnCount-2, 1 ) );
    }
    
    public boolean checkInput()
    {
        String downloadDirPath = downloadDirectoryTF.getText();
        File downloadDir = new File( downloadDirPath );

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
                downloadDirectoryTF.requestFocus();
                downloadDirectoryTF.selectAll();
                GUIUtils.showErrorMessage(
                    Localizer.getFormatedString( "CantCreateDownloadDir", 
                        downloadDirectoryTF.getText() ),
                    Localizer.getString( "DirectoryError" ) );
                return false;
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
                incompleteDirectoryTF.requestFocus();
                incompleteDirectoryTF.selectAll();
                GUIUtils.showErrorMessage(
                    Localizer.getFormatedString( "CantCreateIncompleteDir", 
                        incompleteDirectoryTF.getText() ),
                    Localizer.getString( "DirectoryError" ) );
                return false;
            }
        }
        return true;
    }
    
    public void saveSettings()
    {
        String downloadDirPath = downloadDirectoryTF.getText();
        File downloadDir = new File( downloadDirPath );
        downloadDirPath = downloadDir.getAbsolutePath();
        DownloadPrefs.DestinationDirectory.set( downloadDirPath );
        
        String incompleteDirPath = incompleteDirectoryTF.getText();
        File incompleteDir = new File( incompleteDirPath );
        incompleteDirPath = incompleteDir.getAbsolutePath();
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
            int returnVal = chooser.showDialog( DirectoryPanel.this, null );
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
            int returnVal = chooser.showDialog( DirectoryPanel.this, null );
            if( returnVal == JFileChooser.APPROVE_OPTION )
            {
                String directory = chooser.getSelectedFile().getAbsolutePath();
                incompleteDirectoryTF.setText( directory );
            }
        }
    }
}