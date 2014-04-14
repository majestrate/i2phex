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
 *  $Id: Localizer.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import phex.common.log.NLogger;

/**
 * This class is intended to provide localized strings.
 * 
 * <b>How to store localized ressource bundles</b>
 * Phex will look for resource boundles in the classpath that includes the
 * directory $PHEX/lang.<br>It will look for a file called 'language.list'
 * This file should contain a list of translated locales, one per row, in the format
 * language_COUNTRY e.g. de_DE for german Germany. Also its possible to only provide
 * a single language without country definition e.g. de as a definition for all
 * german speeking countries.<br>
 * Translation files for each local should be named e.g. Lang_de_DE.properties or
 * Lang_de.properties<br>
 * <br>
 * <b>Lookup strategy</b>
 * On startup Phex will try to use the locale defined in its configuration file. If
 * nothing is configured it will use the standard platform locale. With the defined
 * locale e.g. de_DE the $PHEX/lang directory and afterwards the classpath
 * phex.resources is searched for a file called Lang_de_DE.properties, then for
 * a file Lang_de.properties and then for a file Lang.properties. All found files
 * are chained for language key lookup in a ResourceBundle.
 * 
 * To display all available locales in the options menu, Phex will use the file
 * $PHEX/lang/language.list and the internal resource
 * phex/resources/language.list for available locale definitions.
 */
public class Localizer
{
    private static Map<String, String> langKeyMap;
    private static Locale usedLocale;
    private static List<Locale> availableLocales;
    private static DecimalFormatSymbols decimalFormatSymbols;
    private static NumberFormat integerNumberFormat;
    
    public static void initialize( String localeStr )
    {
        setUsedLocale( Locale.US );
        
        Locale locale;
        if ( localeStr == null || localeStr.length() == 0 ||
            ( localeStr.length() != 2 && localeStr.length() != 5 && localeStr.length() != 8) )            
        {// default to en_US
            locale = Locale.US;
        }
        else
        {
	        String lang = localeStr.substring( 0, 2 );
	        String country = "";
	        if ( localeStr.length() >= 5 )
	        {
	            country = localeStr.substring( 3, 5 );
	        }
            String variant = "";
            if ( localeStr.length() == 8 )
            {
                variant = localeStr.substring( 6, 8 );
            }
	        locale = new Locale( lang, country, variant );
        }
        setUsedLocale( locale );
    }

    public static void setUsedLocale(Locale locale)
    {
        usedLocale = locale;
        buildResourceBundle( locale );
        decimalFormatSymbols = new DecimalFormatSymbols( usedLocale );
        integerNumberFormat = NumberFormat.getIntegerInstance( usedLocale );
    }
    
    public static Locale getUsedLocale()
    {
        return usedLocale;
    }
    
    public static DecimalFormatSymbols getDecimalFormatSymbols()
    {
        return decimalFormatSymbols;
    }
    
    public static NumberFormat getIntegerNumberFormat()
    {
        return integerNumberFormat;
    }

    private static void buildResourceBundle(Locale locale)
    {
        // we need to build up the resource bundles backwards to chain correctly.
        ArrayList<String> fileList = new ArrayList<String>();
        StringBuffer buffer = new StringBuffer( "Lang" );
        fileList.add( buffer.toString() );
        String language = locale.getLanguage();
        if ( language.length() > 0 )
        {
            buffer.append( '_' );
            buffer.append( language );
            fileList.add( buffer.toString() );
            String country = locale.getCountry();
            if ( country.length() > 0 )
            {
                buffer.append( '_' );
                buffer.append( country );
                fileList.add( buffer.toString() );
                String variant = locale.getVariant();
                if ( variant.length() > 0 )
                {
                    buffer.append( '_' );
                    buffer.append( variant );
                    fileList.add( buffer.toString() );
                }
            }
        }
        langKeyMap = new HashMap<String, String>();
        HashMap<String, String> tmpMap = new HashMap<String, String>();
        String resourceName;
        int size = fileList.size();
        for (int i = 0; i < size; i++)
        {
            // 1) phex.resources classpath
            resourceName = "/phex/resources/" + fileList.get( i )
                + ".properties";
            tmpMap = loadProperties( resourceName );
            if ( tmpMap != null )
            {
                langKeyMap.putAll( tmpMap );
                NLogger.debug( Localizer.class,
                    "Loaded language map: " + resourceName + "." );
            }
            // 2) e.g. $PHEX/ext
            resourceName = "/" + fileList.get( i ) + ".properties";
            tmpMap = loadProperties( resourceName );
            if ( tmpMap != null )
            {
                langKeyMap.putAll( tmpMap );
                NLogger.debug( Localizer.class,
                    "Loaded language map: " + resourceName + "." );
            }
        }
    }

    private static HashMap<String, String> loadProperties(String name)
    {
        InputStream stream = Localizer.class.getResourceAsStream( name );
        if ( stream == null ) { return null; }
        // make sure it is buffered
        stream = new BufferedInputStream( stream );
        Properties props = new Properties();
        try
        {
            props.load( stream );
            return new HashMap( props );
        }
        catch (IOException exp)
        {
        }
        finally
        {
            IOUtil.closeQuietly( stream );
        }
        return null;
    }

    /**
     * To display all available locales in the options menu, Phex will use the file
     * $PHEX/lang/translations.list and the internal resource
     * phex/resources/translations.list for available locale definitions.
     */
    public static synchronized List<Locale> getAvailableLocales()
    {
        if ( availableLocales != null ) { return availableLocales; }
        availableLocales = new ArrayList<Locale>();
        List<Locale> list = loadLocalList( "/language.list" );
        availableLocales.addAll( list );
        list = loadLocalList( "/phex/resources/language.list" );
        availableLocales.addAll( list );
        return availableLocales;
    }

    private static List<Locale> loadLocalList(String name)
    {
        InputStream stream = Localizer.class.getResourceAsStream( name );
        if ( stream == null ) { return Collections.emptyList(); }
        // make sure it is buffered
        try
        {
            BufferedReader reader = new BufferedReader( new InputStreamReader(
                stream, "ISO-8859-1" ) );
            ArrayList<Locale> list = new ArrayList<Locale>();
            String line;
            Locale locale;
            while (true)
            {
                line = reader.readLine();
                if ( line == null )
                {
                    break;
                }
                line = line.trim();
                if ( line.startsWith( "#" ) 
                  || ( line.length() != 2 && line.length() != 5 && line.length() != 8 ) )
                {
                    continue;
                }
                String lang = line.substring( 0, 2 );
                String country = "";
                if ( line.length() >= 5 )
                {
                    country = line.substring( 3, 5 );
                }
                String variant = "";
                if ( line.length() == 8 )
                {
                    variant = line.substring( 6, 8 );
                }
                locale = new Locale( lang, country, variant );
                list.add( locale );
            }
            return list;
        }
        catch (IOException exp)
        {
            NLogger.error( Localizer.class, exp, exp);
        }
        finally
        {
            IOUtil.closeQuietly( stream );
        }
        return Collections.emptyList();
    }

    /**
     * Returns the actual language text out of the resource boundle.
     * If the key is not defined it returns the key itself and prints an
     * error message on system.err.
     */
    public static String getString(String key)
    {
        String value = langKeyMap.get( key );
        if ( value == null )
        {
            NLogger.error( Localizer.class, "Missing language key: " + key );
            value = key;
        }
        return value;
    }

    /**
     * Returns the first character of the actual language text out of the
     * resource boundle. The method can be usefull for getting mnemonics.
     * If the key is not defined it returns the first char of the key itself and
     * prints an error message on system.err.
     */
    public static char getChar(String key)
    {
        String str = getString( key );
        return str.charAt( 0 );
    }

    /**
     * Returns the actual language text out of the resource boundle and formats
     * it accordingly with the given Object array.
     * If the key is not defined it returns the key itself and print an
     * error message on system.err.
     */
    public static String getFormatedString(String key, Object ... obj)
    {
        String value = null;
        
        String lookupValue = langKeyMap.get( key );
        if ( lookupValue != null )
        {
            value = MessageFormat.format( lookupValue, obj );
        }
        else
        {
            NLogger.info(Localizer.class, "Missing language key: " + key );
            value = key;
        }
        return value;
    }
}