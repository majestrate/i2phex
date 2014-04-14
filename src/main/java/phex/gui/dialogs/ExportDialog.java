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
 *  $Id: ExportDialog.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.dialogs;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.SystemUtils;

import phex.common.log.NLogger;
import phex.gui.common.BanneredDialog;
import phex.gui.common.GUIRegistry;
import phex.servent.Servent;
import phex.share.ShareFile;
import phex.share.SharedFilesService;
import phex.share.export.ExportEngine;
import phex.utils.FileUtils;
import phex.utils.IOUtil;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 */
public class ExportDialog extends BanneredDialog
{
    private static final int DEFAULT_HTML_INDEX = 0;
    private static final int DEFAULT_MAGMA_YAML_INDEX = 1;
    private static final int DEFAULT_METALINK_XML_INDEX = 2;
    private static final int DEFAULT_RSS_XML_INDEX = 3;

    private CloseEventHandler closeEventHandler;
    
    private JRadioButton standardExport;
    private JComboBox standardExportFormatCB;
    
    private JRadioButton customExport;
    private JTextField customExportFormatTF;
    
    private JRadioButton exportAllFiles;
    private JRadioButton exportSelectedFiles;
    
    private JTextField outputFileTF;
    
    private JCheckBox magnetInclXs;
    private JCheckBox magnetInclFreebase;
    private JButton browseCustomFormat;
    private JButton browseOutFile;

    private JButton okBtn;
    private JButton cancelBtn;
    
    
    private List<ShareFile> shareFileList;

    /**
     * 
     */
    public ExportDialog()
    {
        this( null );
    }
    
    /**
     * 
     */
    public ExportDialog( List<ShareFile> selectionList  )
    {
        super( GUIRegistry.getInstance().getMainFrame(),
            Localizer.getString( "ExportDialog_DialogTitle" ), false,
            Localizer.getString("ExportDialog_BannerHeader"),
            Localizer.getString("ExportDialog_BannerSubHeader") );
        
        shareFileList = selectionList;
        initContent();
        
        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        pack();
        setLocationRelativeTo( getParent() );
    }
    
    private void initContent()
    {
        if ( shareFileList == null || shareFileList.isEmpty() )
        {
            exportAllFiles.setSelected( true );
            exportSelectedFiles.setEnabled( false );
            exportSelectedFiles.setText( Localizer.getFormatedString( 
                "ExportDialog_ExportSelectedFiles", Integer.valueOf( 0 ) ) );
        }
        else
        {
            exportSelectedFiles.setSelected( true );
            exportSelectedFiles.setText( Localizer.getFormatedString( 
                "ExportDialog_ExportSelectedFiles", Integer.valueOf( 
                shareFileList.size() ) ) );
        }
    }
    
    @Override
    protected JPanel createDialogContentPanel()
    {
        initComponents();
                
        JPanel contentPanel = new JPanel();
        
        FormLayout layout = new FormLayout( "7dlu, d, 3dlu, fill:d:grow, 2dlu, d" );

        DefaultFormBuilder builder = new DefaultFormBuilder( layout, contentPanel );
        builder.setLeadingColumnOffset( 1 );
        
        
        // format section
        builder.appendSeparator( Localizer.getString( "ExportDialog_ExportFormat" ) );
        
        builder.append( standardExport );
        builder.append( standardExportFormatCB );
        builder.nextLine();
        builder.append( customExport );
        builder.append( customExportFormatTF );
        builder.append( browseCustomFormat );
        
        // source section
        builder.appendSeparator( Localizer.getString( "ExportDialog_ExportSource" ) );
        
        builder.append( exportAllFiles, 3 );
        builder.nextLine();
        builder.append( exportSelectedFiles, 3 );
        builder.nextLine();
        
        // output section
        builder.appendSeparator( Localizer.getString( "ExportDialog_Output" ) );        
        builder.append( Localizer.getString( "ExportDialog_FileName" ), outputFileTF, browseOutFile );

        
        // option section
        builder.appendSeparator( Localizer.getString( "ExportDialog_Options" ) );
        magnetInclXs = new JCheckBox( Localizer.getString( "ExportDialog_MagnetIncludeXS" ) );
        magnetInclXs.setToolTipText( Localizer.getString( "ExportDialog_TTTMagnetIncludeXS" ) );
        builder.append( magnetInclXs, 3 );
        builder.nextLine();
        builder.append( magnetInclFreebase, 3 );
        
        return contentPanel;
    }
    
    @Override
    protected JPanel createDialogButtonPanel()
    {
        JPanel btnPanel = ButtonBarFactory.buildOKCancelBar( okBtn, 
            cancelBtn );
        return btnPanel;
    }
    
    private void initComponents()
    {
        closeEventHandler = new CloseEventHandler();
        addWindowListener( closeEventHandler );
        
        DefaultComboBoxModel model = new DefaultComboBoxModel( );
        model.insertElementAt( Localizer.getString( "ExportDialog_DefaultHTMLExport" ), DEFAULT_HTML_INDEX );
        model.insertElementAt( Localizer.getString( "ExportDialog_MagmaYAMLExport" ), DEFAULT_MAGMA_YAML_INDEX );
        model.insertElementAt( Localizer.getString( "ExportDialog_MetalinkXMLExport" ), DEFAULT_METALINK_XML_INDEX );
        model.insertElementAt( Localizer.getString( "ExportDialog_RSSXMLExport" ), DEFAULT_RSS_XML_INDEX );
        
        standardExport = new JRadioButton( Localizer.getString( "ExportDialog_StandardExportFormat" ) );
        standardExport.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                replaceFileExtForStandardExport();
            }} );
        
        customExport = new JRadioButton( Localizer.getString( "ExportDialog_CustomExportFormat" ) );
        customExport.setToolTipText( Localizer.getString( "ExportDialog_TTTCustomExportFormat" ) );
        ButtonGroup exportFormatGroup = new ButtonGroup();
        exportFormatGroup.add( standardExport );
        exportFormatGroup.add( customExport );
        standardExport.setSelected( true );
        
        standardExportFormatCB = new JComboBox( model );
        standardExportFormatCB.addActionListener( new ExportTypeListener() );
        
        
        customExportFormatTF = new JTextField( 40 );
        customExportFormatTF.setToolTipText( Localizer.getString( "ExportDialog_TTTCustomExportFormat" ) );
        customExportFormatTF.addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent e)
            {
                customExport.setSelected( true );
            }

            public void keyReleased(KeyEvent e)
            {
                customExport.setSelected( true );
            }

            public void keyTyped(KeyEvent e)
            {
                customExport.setSelected( true );
            }
        } );
        
        browseCustomFormat = new JButton( Localizer.getString( "ExportDialog_Browse" ) );
        browseCustomFormat.addActionListener( new BrowseCustomFileBtnListener());
        
        
        exportAllFiles = new JRadioButton( Localizer.getString( "ExportDialog_ExportAllFiles" ) );
        // text will be set on initContent()
        exportSelectedFiles = new JRadioButton( );
        ButtonGroup exportSourceGroup = new ButtonGroup();
        exportSourceGroup.add( exportAllFiles );
        exportSourceGroup.add( exportSelectedFiles );
        
        
        outputFileTF = new JTextField( 40 );
        File defOutFile = new File( SystemUtils.USER_HOME, "shared_files.html" );
        outputFileTF.setText( defOutFile.getAbsolutePath() );

        browseOutFile = new JButton( Localizer.getString( "ExportDialog_Browse" ) );
        browseOutFile.addActionListener( new BrowseOutFileBtnListener());
        
        magnetInclXs = new JCheckBox( Localizer.getString( "ExportDialog_MagnetIncludeXS" ) );
        magnetInclXs.setToolTipText( Localizer.getString( "ExportDialog_TTTMagnetIncludeXS" ) );
        
        magnetInclFreebase = new JCheckBox( Localizer.getString( "ExportDialog_MagnetIncludeFreebase" ) );
        magnetInclFreebase.setToolTipText( Localizer.getString( "ExportDialog_TTTMagnetIncludeFreebase" ) );
        
        
        // delay setting initial index to ensure all components are available
        standardExportFormatCB.setSelectedIndex( 0 );
        
        
        okBtn = new JButton( Localizer.getString( "OK" ) );
        okBtn.addActionListener( new OkBtnListener());
        
        cancelBtn = new JButton( Localizer.getString( "Cancel" ));
        cancelBtn.addActionListener( closeEventHandler );
    }
    
    private void closeDialog( )
    {
        setVisible( false );
        dispose();
    }
    
    private void replaceFileExtForStandardExport()
    {
        String filename = outputFileTF.getText( );
        String ext = FileUtils.getFileExtension(filename);
        
        int idx = standardExportFormatCB.getSelectedIndex();
        switch ( idx )
        {
        case DEFAULT_HTML_INDEX:
            if ( !(ext.equals("htm") || ext.equals("html")) )
            {
                filename = FileUtils.replaceFileExtension( filename, "html" );
            }
            break;
        case DEFAULT_MAGMA_YAML_INDEX:
            if ( !ext.equals("magma") )
            {
                filename = FileUtils.replaceFileExtension( filename, "magma" );
            }
            break;
        case DEFAULT_METALINK_XML_INDEX:
            if ( !ext.equals("metalink") )
            {
                filename = FileUtils.replaceFileExtension( filename, "metalink" );
            }
            break;
        case DEFAULT_RSS_XML_INDEX:
            if ( !ext.equals("xml") )
            {
                filename = FileUtils.replaceFileExtension( filename, "xml" );
            }
            break;
        }
        outputFileTF.setText( filename );
    }
    
    /**
     * @param absolutePath
     * @return
     */
    private String ensureStandardExportFileExtension( String filename )
    {
        String ext = FileUtils.getFileExtension(filename);
        
        int idx = standardExportFormatCB.getSelectedIndex();
        switch ( idx )
        {
        case DEFAULT_HTML_INDEX:
            if ( !(ext.equals("htm") || ext.equals("html")) )
            {
                filename = filename + ".html";
            }
            break;
        case DEFAULT_MAGMA_YAML_INDEX:
            if ( !ext.equals("magma") )
            {
                filename = filename + ".magma";
            }
            break;
        case DEFAULT_METALINK_XML_INDEX:
            if ( !ext.equals("metalink") )
            {
                filename = filename + ".metalink";
            }
            break;
        case DEFAULT_RSS_XML_INDEX:
            if ( !ext.equals("xml") )
            {
                filename = filename + ".rss.xml";
            }
            break;
        }            
        return filename;
    }
    
    private void startExport()
    {
        String outFileName = outputFileTF.getText();
        File file = new File( outFileName );
        InputStream inStream = null;
        OutputStream outStream = null;
        try
        {
            outStream = new BufferedOutputStream( new FileOutputStream( file ) );
            if ( standardExport.isSelected() )
            {
                int idx = standardExportFormatCB.getSelectedIndex();
                switch ( idx )
                {
                case DEFAULT_HTML_INDEX:
                    inStream = ClassLoader.getSystemResourceAsStream(
                        "phex/resources/defaultSharedFilesHTMLExport.xsl" );
                    break;
                case DEFAULT_MAGMA_YAML_INDEX:
                    inStream = ClassLoader.getSystemResourceAsStream(
                        "phex/resources/magmaSharedFilesYAMLExport.xsl" );
                    break;
                case DEFAULT_METALINK_XML_INDEX:
                    inStream = ClassLoader.getSystemResourceAsStream(
                        "phex/resources/metalinkSharedFilesXMLExport.xsl" );
                    break;
                case DEFAULT_RSS_XML_INDEX:
                    inStream = ClassLoader.getSystemResourceAsStream(
                        "phex/resources/rssSharedFilesXMLExport.xsl" );
                    break;
                }
            }
            else if ( customExport.isSelected() )
            {
                String styleFileName = customExportFormatTF.getText();
                File styleFile = new File( styleFileName );
                inStream = new BufferedInputStream( new FileInputStream( styleFile ) );
            }
            else 
            {
                return;
            }
            
            Map<String, String> exportOptions = new HashMap<String, String>();
            if ( magnetInclXs.isSelected() )
            {
                exportOptions.put( ExportEngine.USE_MAGNET_URL_WITH_XS, "true" );
            }
            if ( magnetInclFreebase.isSelected() )
            {
                exportOptions.put( ExportEngine.USE_MAGNET_URL_WITH_FREEBASE, "true" );
            }
            
            List<ShareFile> exportData;
            if ( exportAllFiles.isSelected() )
            {
                SharedFilesService sharedFilesService = GUIRegistry.getInstance().getServent().getSharedFilesService();
                exportData = sharedFilesService.getSharedFiles();
            }
            else
            {
                exportData = shareFileList;
            }
            
            ExportEngine exportEngine = new ExportEngine( GUIRegistry.getInstance().getServent().getLocalAddress(), 
                inStream, outStream, exportData, exportOptions );
            exportEngine.startExport();
        }
        catch ( IOException exp )
        {
            NLogger.error( ExportDialog.class, exp, exp );
        }
        finally
        {
            IOUtil.closeQuietly(inStream);
            IOUtil.closeQuietly(outStream);
        }
    }
    
    private final class OkBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                startExport();
                closeDialog();
            }
            catch ( Throwable th )
            {
                NLogger.error( OkBtnListener.class, th, th );
            }
        }
    }
    
    private final class BrowseOutFileBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                if ( SystemUtils.IS_OS_MAC_OSX )
                {
                    FileDialog dia = new FileDialog( GUIRegistry.getInstance().getMainFrame(),
                        Localizer.getString( "ExportDialog_SelectOutputFile" ), FileDialog.SAVE );
                    dia.setVisible( true );
                    String filename = dia.getDirectory() + dia.getFile();
                    if ( standardExport.isSelected() )
                    {
                        filename = ensureStandardExportFileExtension( filename );
                    }
                    
                    outputFileTF.setText( filename );
                }
                else
                {
                    JFileChooser chooser = new JFileChooser( );
                    chooser.setDialogTitle(Localizer.getString( "ExportDialog_SelectOutputFile" ));
                    chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
                    chooser.setMultiSelectionEnabled(false);
                    int rc = chooser.showSaveDialog( ExportDialog.this );
                    if ( rc == JFileChooser.APPROVE_OPTION )
                    {
                        File file = chooser.getSelectedFile();
                        String filename = file.getAbsolutePath();
                        if ( standardExport.isSelected() )
                        {
                            filename = ensureStandardExportFileExtension( filename );
                        }
                        outputFileTF.setText( filename );
                    }
                }
            }
            catch ( Throwable th )
            {
                NLogger.error( BrowseOutFileBtnListener.class, th, th );
            }
        }
    }
    
    private final class BrowseCustomFileBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                customExport.setSelected(true);
                if ( SystemUtils.IS_OS_MAC_OSX )
                {
                    FileDialog dia = new FileDialog( GUIRegistry.getInstance().getMainFrame(),
                        Localizer.getString( "ExportDialog_SelectCustomStyleFile" ), FileDialog.LOAD );
                    dia.setVisible( true );
                    customExportFormatTF.setText( dia.getDirectory() + dia.getFile() );
                }
                else
                {
                    JFileChooser chooser = new JFileChooser( );
                    chooser.setDialogTitle(Localizer.getString( "ExportDialog_SelectCustomStyleFile" ));
                    chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setFileFilter( new FileFilter() {
                        @Override
                        public boolean accept(File file)
                        {
                            return file.isDirectory() || FileUtils.getFileExtension(file).equalsIgnoreCase("XSL");
                        }
    
                        @Override
                        public String getDescription()
                        {
                            return "XSL-Stylesheet";
                        }} );
                    int rc = chooser.showOpenDialog( ExportDialog.this );
                    if ( rc == JFileChooser.APPROVE_OPTION )
                    {
                        File file = chooser.getSelectedFile();
                        customExportFormatTF.setText( file.getAbsolutePath() );
                    }
                }
            }
            catch ( Throwable th )
            {
                NLogger.error( BrowseCustomFileBtnListener.class, th, th );
            }
        }
    }
    
    private final class ExportTypeListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                standardExport.setSelected( true );
                replaceFileExtForStandardExport();
            }
            catch ( Throwable th )
            {
                NLogger.error( ExportTypeListener.class, th, th );
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
}