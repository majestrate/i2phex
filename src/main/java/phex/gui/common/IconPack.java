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
 */
package phex.gui.common;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import phex.common.log.NLogger;
import phex.utils.IOUtil;
import phex.utils.Localizer;

/**
 * This class defines an icon pack. It could be a used installed pack or a system
 * internal pack.
 */
public class IconPack
{
    private static final String DEFAULT_ICON_PACK_RESOURCE = "phex.gui.resources.icons.phex.Icons";
    private static final ImageIcon MISSING_IMAGE =
        new ImageIcon( IconPack.class.getResource("/phex/gui/resources/image-failed.gif") );
    private static final String EMPTY_IMAGE_16_NAME = "EMPTY_IMAGE_16";
    public static final ImageIcon EMPTY_IMAGE_16;
    
    private static Map<String,String> iconPackDefinitions;
    
    static
    {
        BufferedImage bufImg = new BufferedImage( 16, 16, BufferedImage.TYPE_INT_ARGB );
        /*int[] rgbArray = new int[16*16];
        Arrays.fill( rgbArray, 0x00ff00 );
        bufImg.setRGB( 0, 0, 16, 16, rgbArray, 0, 16 );*/
        EMPTY_IMAGE_16 = new ImageIcon( bufImg );
    }
    
    /**
     * The name of the icon pack to identify it. Can be null.
     */
    private String name;

    /**
     * The resource bundle of the icon pack.
     */
    private ResourceBundle resource;
    
    /**
     * This table makes sure that each image file is only loaded once and is 
     * afterwards cached. They can be fetched with its associated resource path.
     */
    private Hashtable<String, Image> imageCache;

    public IconPack( String resourceFileName )
    {
        this( null, resourceFileName );
    }
    
    public IconPack( String name, String resourceFileName )
    {
        this();
        this.name = name;
        this.resource = PropertyResourceBundle.getBundle( resourceFileName );
    }
    
    /**
     * Returns the name of the icon pack to identify it. Can be null.
     */
    public String getName()
    {
        return name;
    }
    
    protected IconPack()
    {
        imageCache = new Hashtable<String, Image>();
    }

    /**
     * the method to fetch an icon using the keys defined
     * in Images.properties file.
     */
    public Icon getIcon( String key )
    {
        return new TKIcon( key );
    }
    
    public static IconPack createDefaultIconPack()
    {
        return new IconPack( "Phex 3", DEFAULT_ICON_PACK_RESOURCE );
    }
    
    public static IconPack createIconPack(String name)
    {
        initIconPackDefs();
        String res = iconPackDefinitions.get( name );
        if ( res == null )
        {
            return null;
        }
        return new IconPack( name, res );
    }
    
    public static Set<String> getAllIconPackIds()
    {
        initIconPackDefs();
        return Collections.unmodifiableSet( iconPackDefinitions.keySet() );
    }
    
    private static void initIconPackDefs()
    {
        if ( iconPackDefinitions != null ) { return; }
        iconPackDefinitions = new LinkedHashMap<String, String>();
        Map<String, String> def = loadIconPackDef( "/iconpack.list" );
        iconPackDefinitions.putAll( def );
        def = loadIconPackDef( "/phex/gui/resources/icons/iconpack.list" );
        iconPackDefinitions.putAll( def );
    }
    
    private static Map<String, String> loadIconPackDef(String name)
    {
        InputStream stream = Localizer.class.getResourceAsStream( name );
        if ( stream == null ) { return Collections.emptyMap(); }
        // make sure it is buffered
        try
        {
            BufferedReader reader = new BufferedReader( new InputStreamReader(
                stream, "ISO-8859-1" ) );
            Map<String, String> map = new LinkedHashMap<String, String>();
            String line;
            while (true)
            {
                line = reader.readLine();
                if ( line == null )
                {
                    break;
                }
                line = line.trim();
                if ( line.startsWith( "#" ) )
                {
                    continue;
                }
                
                String[] splitParts = line.split( "=", 2 );
                String packId = splitParts[0].trim();
                String packPath = splitParts[1].trim();
                map.put( packId, packPath );
            }
            return map;
        }
        catch (IOException exp)
        {
            NLogger.error( GUIRegistry.class, exp, exp);
        }
        finally
        {
            IOUtil.closeQuietly( stream );
        }
        return Collections.emptyMap();
    }
    

    /**
     * The class used to define an icon. It wrapps the functionality
     * of only loading a image when required.
     */
    class TKIcon extends ImageIcon
    {
        private String key;
        private boolean loaded;

        TKIcon( String aKey )
        {
            super();
            key = aKey;
            loaded = false;
        }

        /**
         * returns the image (overloaded from ImageIcon)
         */
        @Override
        public Image getImage()
        {
            loadIcon();
            return super.getImage();
        }

        /**
         * paints the icon (overloaded from ImageIcon)
         * if its not already loaded then load it
         */
        @Override
        public void paintIcon( Component c, Graphics g, int x, int y )
        {
            loadIcon();
            super.paintIcon( c, g, x, y );
        }

        /**
         * returns the width of an icon depending on it's offset
         * (overloaded from ImageIcon)
         */
        @Override
        public int getIconWidth()
        {
            loadIcon();
            return super.getIconWidth();
        }

        /**
         * returns the width of an icon depending on it's offset
         * (overloaded from ImageIcon)
         */
        @Override
        public int getIconHeight()
        {
            loadIcon();
            return super.getIconHeight();
        }

        /**
         * loads the icon with its path
         */
        private void loadIcon()
        {
            // watch out that icon is not loaded twice!
            if ( loaded )
            {
                return;
            }

            try
            {
                if ( key != null )
                {
                    String imgURLStr;
                    imgURLStr = resource.getString( key );
                    Image image = imageCache.get( imgURLStr );
                    if ( image == null )
                    {
                        // try to load image.
                        if ( EMPTY_IMAGE_16_NAME.equals( imgURLStr ) )
                        {
                            image = new BufferedImage( 16, 16, BufferedImage.TYPE_INT_ARGB );
                        }
                        else
                        {
                            URL imgURL = null;
                            if ( imgURLStr != null )
                            {
                                imgURL = IconPack.class.getResource( imgURLStr );
                                if ( imgURL != null )
                                {
                                    image = Toolkit.getDefaultToolkit().createImage( imgURL );
                                }
                            }
                        }
                        if ( image != null )
                        {
                            imageCache.put( imgURLStr, image );
                        }
                        else
                        {
                            NLogger.warn( IconPack.class,
                                "Can't find image for key: " + key + " - URL: " + imgURLStr );
                            image = MISSING_IMAGE.getImage();
                        }
                    }
                    
                    super.setImage( image );
                    loaded = true;
                }
            }
            catch ( Exception exp )
            {
                NLogger.error( IconPack.class, exp, exp );
                // TODO integrate gui utilis
                //GUIUtilities.handleException( exp );
            }
        }
    }
}