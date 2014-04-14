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
 *  $Id: NewDownloadDialog.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

import phex.common.URN;
import phex.common.log.NLogger;
import phex.download.MagnetData;
import phex.download.swarming.SwarmingManager;
import phex.gui.common.DialogBanner;
import phex.gui.common.GUIRegistry;
import phex.gui.common.GUIUtils;
import phex.gui.common.HTMLMultiLinePanel;
import phex.servent.Servent;
import phex.share.SharedFilesService;
import phex.utils.InternalFileHandler;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 */
public class NewDownloadDialog extends JDialog
{
    public static final int URI_DOWNLOAD = 1;

    public static final int MAGMA_DOWNLOAD = 2;

    public static final int RSS_DOWNLOAD = 3;

    private JTextField uriTF;

    private JTextField magmaTF;

    private JTextField rssTF;

    private JTabbedPane downloadTabPane;

    /**
     * @throws java.awt.HeadlessException
     */
    public NewDownloadDialog() throws HeadlessException
    {
        super(GUIRegistry.getInstance().getMainFrame(), Localizer
            .getString("NewDownload_DialogTitle"), false);
        prepareComponent();
    }

    /**
     * @throws java.awt.HeadlessException
     */
    public NewDownloadDialog(String downloadValue, int type)
        throws HeadlessException
    {
        super(GUIRegistry.getInstance().getMainFrame(), Localizer
            .getString("NewDownload_DialogTitle"), false);
        prepareComponent();

        switch (type)
        {
        case URI_DOWNLOAD:
            uriTF.setText(downloadValue);
            downloadTabPane.setSelectedIndex(0);
            break;
        case MAGMA_DOWNLOAD:
            magmaTF.setText(downloadValue);
            downloadTabPane.setSelectedIndex(1);
            break;
        case RSS_DOWNLOAD:
            rssTF.setText(downloadValue);
            downloadTabPane.setSelectedIndex(2);
            break;
        }

    }

    /**
     * 
     */
    private void prepareComponent()
    {
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                closeDialog();
            }
        });
        CellConstraints cc = new CellConstraints();
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        //JPanel contentPanel = new FormDebugPanel();
        contentPane.add(contentPanel, BorderLayout.CENTER);

        FormLayout layout = new FormLayout("2dlu, fill:d:grow, 2dlu", // columns
            "p, p, 2dlu, p, 6dlu, p, 3dlu, p 6dlu"); //row
        PanelBuilder builder = new PanelBuilder(layout, contentPanel);

        DialogBanner banner = new DialogBanner(Localizer
            .getString("NewDownload_BannerHeader"), Localizer
            .getString("NewDownload_BannerSubHeader"));
        builder.add(banner, cc.xywh(1, 1, 3, 1));

        builder.add(new JSeparator(), cc.xywh(1, 2, 3, 1));

        downloadTabPane = new JTabbedPane();
        JPanel urlPanel = createByUrlPanel();
        downloadTabPane.addTab(Localizer.getString("NewDownload_ByUrl"), urlPanel);
        JPanel magmaPanel = createByMagmaPanel();
        downloadTabPane.addTab(Localizer.getString("NewDownload_ByMagmaFile"),
            magmaPanel);
        JPanel rssPanel = createByRSSPanel();
        downloadTabPane.addTab(Localizer.getString("NewDownload_ByRSSFile"),
            rssPanel);
        builder.add(downloadTabPane, cc.xy(2, 4));

        builder.add(new JSeparator(), cc.xywh(1, 6, 3, 1));

        JButton cancelBtn = new JButton(Localizer.getString("Cancel"));
        cancelBtn.addActionListener(new CancelBtnListener());
        JButton okBtn = new JButton(Localizer.getString("OK"));
        okBtn.addActionListener(new OkBtnListener());
        JPanel btnPanel = ButtonBarFactory.buildOKCancelBar(okBtn, cancelBtn);
        builder.add(btnPanel, cc.xy(2, 8));

        pack();
        setLocationRelativeTo(getParent());
    }

    /**
     * @param cc
     * @param builder
     */
    private JPanel createByUrlPanel()
    {
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout(
            "6dlu, d, 2dlu, d, fill:d:grow, 6dlu", // columns
            "10dlu, p, 10dlu, p, 6dlu, p, 6dlu"); //row
        JPanel panel = new JPanel();
        //JPanel panel = new FormDebugPanel();
        PanelBuilder builder = new PanelBuilder(layout, panel);

        JLabel label = new JLabel(Localizer
            .getString("NewDownload_UrlToDownload"));
        builder.add(label, cc.xy(2, 2));

        uriTF = new JTextField(40);
        builder.add(uriTF, cc.xywh(4, 2, 2, 1));

        builder.addSeparator(Localizer.getString("NewDownload_Examples"), cc
            .xywh(2, 4, 4, 1));

        JPanel examplesPanel = new JPanel();
        builder.add(examplesPanel, cc.xywh(2, 6, 4, 1));
        FormLayout examplesLayout = new FormLayout("2dlu, d", // columns
            "p, 3dlu, p"); //row
        PanelBuilder examplesBuilder = new PanelBuilder(examplesLayout,
            examplesPanel);
        examplesBuilder.addLabel("http://www.host.com/path/file.zip", cc.xy(2,
            1));
        examplesBuilder
            .addLabel("magnet:?xt=urn:sha1:AABBCCDDEEFFGGHHIIJJKKLLMMNNOOPP",
                cc.xy(2, 3));

        return panel;
    }

    /**
     * @param cc
     * @param builder
     */
    private JPanel createByMagmaPanel()
    {
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout(
            "6dlu, d, 2dlu, d, fill:d:grow, 2dlu, d, 6dlu", // columns
            "10dlu, p, 10dlu, p, 6dlu, p, 6dlu"); //row
        //JPanel panel = new FormDebugPanel();
        JPanel panel = new JPanel();
        
//        panel.setFillEntireArea(false);
//        panel.setTileImage(false);
//        panel.setAlignment(FWImagePanel.ALIGN_LEFT);
        
//        TransImageFilter filter = new TransImageFilter( );
//        ImageIcon img = (ImageIcon) GUIRegistry.getInstance().getIconFactory().getIcon("MagmaBackground");
//        ImageProducer prod = new FilteredImageSource(((ImageIcon) img)
//            .getImage().getSource(), filter);
//        Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
//        img = new ImageIcon(grayImage);
//        panel.setImage(img);

        PanelBuilder builder = new PanelBuilder(layout, panel);

        JLabel label = new JLabel(Localizer.getString("NewDownload_MagmaFile"));
        builder.add(label, cc.xy(2, 2));

        magmaTF = new JTextField(30);
        builder.add(magmaTF, cc.xywh(4, 2, 2, 1));
        
        builder.addSeparator(Localizer.getString("NewDownload_CreatingMagma"),
            cc.xywh(2, 4, 7, 1));
        
        HTMLMultiLinePanel magmaHowTo = new HTMLMultiLinePanel( 
            Localizer.getString("NewDownload_MagmaHowTo") )
            {
                @Override
                public Dimension getPreferredSize()
                {
                    return new Dimension( 0, super.getPreferredSize().height );
                }
            };
        builder.add( magmaHowTo, cc.xywh(2, 6, 6, 1) );

        JButton button = new JButton(Localizer.getString("NewDownload_Browse"));
        button.addActionListener(new SetDownlodDirectoryListener());
        builder.add(button, cc.xy(7, 2));

        return panel;
    }/**
     * @param cc
     * @param builder
     */
    private JPanel createByRSSPanel()
    {
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout(
            "6dlu, d, 2dlu, d, fill:d:grow, 2dlu, d, 6dlu", // columns
            "10dlu, p, 10dlu, p, 6dlu, p, 6dlu"); //row
        //JPanel panel = new FormDebugPanel();
        JPanel panel = new JPanel();
        
//        panel.setFillEntireArea(false);
//        panel.setTileImage(false);
//        panel.setAlignment(FWImagePanel.ALIGN_LEFT);
        
//        TransImageFilter filter = new TransImageFilter( );
//        ImageIcon img = (ImageIcon) GUIRegistry.getInstance().getIconFactory().getIcon("RSSBackground");
//        ImageProducer prod = new FilteredImageSource(((ImageIcon) img)
//            .getImage().getSource(), filter);
//        Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
//        img = new ImageIcon(grayImage);
//        panel.setImage(img);

        PanelBuilder builder = new PanelBuilder(layout, panel);

        JLabel label = new JLabel(Localizer.getString("NewDownload_RSSFile"));
        builder.add(label, cc.xy(2, 2));

        rssTF = new JTextField(30);
        builder.add(rssTF, cc.xywh(4, 2, 2, 1));
        
        builder.addSeparator(Localizer.getString("NewDownload_CreatingRSS"),
            cc.xywh(2, 4, 7, 1));
        
        HTMLMultiLinePanel rssHowTo = new HTMLMultiLinePanel( 
            Localizer.getString("NewDownload_RSSHowTo") )
            {
                @Override
                public Dimension getPreferredSize()
                {
                    return new Dimension( 0, super.getPreferredSize().height );
                }
            };
        builder.add( rssHowTo, cc.xywh(2, 6, 6, 1) );

        JButton button = new JButton(Localizer.getString("NewDownload_Browse"));
        button.addActionListener(new SetDownlodDirectoryListener());
        builder.add(button, cc.xy(7, 2));

        return panel;
    }

    private void createNewDownload() throws URIException
    {
        Servent servent = GUIRegistry.getInstance().getServent();
        SwarmingManager swarmingMgr = servent.getDownloadService();
        SharedFilesService shareService = servent.getSharedFilesService();
        
        String uriStr = uriTF.getText().trim();
        if (uriStr.length() == 0)
        {
            return;
        }
        URI uri = new URI( uriStr, true );
        String protocol = uri.getScheme();
        
        // in case this is no magnet we cant determine the file urn and cant
        // check if the download is already running.
        if ( "magnet".equals( protocol ) )
        {
            MagnetData magnetData = MagnetData.parseFromURI( uri );
            URN urn = MagnetData.lookupSHA1URN(magnetData);
            
            if ( swarmingMgr.isURNDownloaded( urn ) )
            {
                GUIUtils.showErrorMessage(
                    Localizer.getString( "NewDownload_AlreadyDownloadingMessage" ),
                    Localizer.getString( "NewDownload_AlreadyDownloadingTitle" ) );
                return;
            }
            if ( shareService.isURNShared(urn) )
            {
                GUIUtils.showErrorMessage(
                    Localizer.getString( "NewDownload_AlreadySharedMessage" ),
                    Localizer.getString( "NewDownload_AlreadySharedTitle" ) );
                return;
            }
        }
        swarmingMgr.addFileToDownload( uri, true );
    }

    private void createNewMagmaDownload()
    {
        String magmaFileName = magmaTF.getText().trim();
        if (magmaFileName.length() == 0)
        {
            return;
        }
        File file = new File(magmaFileName);
        if (!file.exists())
        {
            return;
        }
        InternalFileHandler.magmaReadout( file );
    }
    
    private void createNewRSSDownload()
    {
        String rssFileName = rssTF.getText().trim();
        if (rssFileName.length() == 0)
        {
            return;
        }
        File file = new File(rssFileName);
        if (!file.exists())
        {
            return;
        }
        InternalFileHandler.rssReadout( file );
    }

    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }

    private class SetDownlodDirectoryListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(magmaTF.getText()));
                chooser.setSelectedFile(new File(rssTF.getText()));
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                chooser.setDialogTitle(Localizer
                    .getString("NewDownload_SelectMagmaFile"));
                chooser.setApproveButtonText(Localizer.getString("Select"));
                chooser.setApproveButtonMnemonic(Localizer
                    .getChar("SelectMnemonic"));
                int returnVal = chooser.showDialog(NewDownloadDialog.this, null);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    String directory = chooser.getSelectedFile().getAbsolutePath();
                    magmaTF.setText(directory);
                    rssTF.setText(directory);
                }
            }
            catch ( Throwable th )
            {
                NLogger.error( SetDownlodDirectoryListener.class, th, th );
            }
        }
    }

    private final class OkBtnListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                createNewDownload();
                createNewMagmaDownload();
                createNewRSSDownload();
                closeDialog();
            }
            catch (URIException exp)
            {
                NLogger.error( OkBtnListener.class, exp, exp );
                GUIUtils.showErrorMessage(
                    Localizer.getString( "NewDownload_FailedToCreateDownloadMessage" ),
                    Localizer.getString( "NewDownload_FailedToCreateDownloadTitle" ) );
            }
            catch ( Throwable th )
            {
                NLogger.error( OkBtnListener.class, th, th );
            }
        }
    }

    private final class CancelBtnListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
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
    
    
    static class TransImageFilter extends RGBImageFilter
    {
        public static Icon createTransIcon(Icon icon)
        {
            TransImageFilter filter = new TransImageFilter();
            ImageProducer prod = new FilteredImageSource(((ImageIcon) icon)
                .getImage().getSource(), filter);
            Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
            return new ImageIcon(grayImage);
        }

        /**
         * @param b
         * @param p
         */
        public TransImageFilter()
        {
            canFilterIndexColorModel = true;
        }

        @Override
        public int filterRGB(int x, int y, int rgb)
        {
            rgb = rgb & 0x33FFFFFF; 
            return rgb;
        }

    }
}