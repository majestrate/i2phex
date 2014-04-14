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
 *  $Id: NLogger.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.common.log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

import phex.common.Environment;
import phex.utils.IOUtil;

/**
 * Proxy class for new logging.
 */
public class NLogger
{
    /**
     * Debug log level field to use for configurable logging calls.
     * Preferred method is still to use direct log method calls.
     */
    public static final short LOG_LEVEL_DEBUG = 1;
    
    /**
     * Info log level field to use for configurable logging calls.
     * Preferred method is still to use direct log method calls.
     */
    public static final short LOG_LEVEL_INFO = 2;
    
    /**
     * Debug log level field to use for configurable logging calls.
     * Preferred method is still to use direct log method calls.
     */
    public static final short LOG_LEVEL_WARN = 3;
    
    /**
     * Debug log level field to use for configurable logging calls.
     * Preferred method is still to use direct log method calls.
     */
    public static final short LOG_LEVEL_ERROR = 4;
    
    private static LogFactory factory;
    
    static
    {
        Properties sysProps = System.getProperties();
        // add loaded properties to system properties.
        sysProps.put( "org.apache.commons.logging.Log", "phex.utils.PhexLogger" );
        factory = LogFactory.getFactory( );
        
        // load logging properties
        Properties loggingProperties = new Properties();
        InputStream resIs = null;
        try
        {
            resIs = NLogger.class.getResourceAsStream( "/phex/resources/logging.properties" );
            if ( resIs != null )
            {
                loggingProperties.load( resIs );
            }
        }
        catch ( Throwable th )
        {
            th.printStackTrace();
        }
        finally
        {
            IOUtil.closeQuietly(resIs);
        }
        InputStream fileIs = null;
        try
        {
            File file = Environment.getInstance().getPhexConfigFile( "logging.properties" );
            if ( file.exists() )
            {
                fileIs = new BufferedInputStream( new FileInputStream(file) );
                loggingProperties.load( fileIs );
            }
        }
        catch ( Throwable th )
        {
            th.printStackTrace();
        }
        finally
        {
            IOUtil.closeQuietly(fileIs);
        }
        
        sysProps = System.getProperties();
        // add loaded properties to system properties.
        sysProps.putAll( loggingProperties );
        LogFactory.releaseAll();
        factory = LogFactory.getFactory();
    }

    /**
     * Returns a log instance.
     * @param clazz
     * @return a log instance.
     */
    public static Log getLogInstance( String name )
    {
        try
        {
            return factory.getInstance( name );
        }
        catch ( LogConfigurationException exp )
        {
            Properties sysProps = System.getProperties();
            // add loaded properties to system properties.
            sysProps.put( "org.apache.commons.logging.Log", "phex.utils.PhexLogger" );
            LogFactory.releaseAll();
            factory = LogFactory.getFactory( );
            return factory.getInstance( name );
        }
    }
    
    /**
     * Returns a log instance.
     * @param clazz
     * @return a log instance.
     */
    public static Log getLogInstance( Class<?> clazz )
    {
        try
        {
            return factory.getInstance( clazz );
        }
        catch ( LogConfigurationException exp )
        {
            Properties sysProps = System.getProperties();
            // add loaded properties to system properties.
            sysProps.put( "org.apache.commons.logging.Log", "phex.common.log.PhexLogger" );
            LogFactory.releaseAll();
            factory = LogFactory.getFactory( );
            return factory.getInstance( clazz );
        }
    }

    /**
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public static boolean isDebugEnabled( String name )
    {
        return getLogInstance( name ).isDebugEnabled();
    }
    
    /**
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public static boolean isDebugEnabled( Class<?> clazz )
    {
        return getLogInstance( clazz ).isDebugEnabled();
    }
    
    /**
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public static boolean isInfoEnabled( Class<?> clazz )
    {
        return getLogInstance( clazz ).isInfoEnabled();
    }
    
    /**
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public static boolean isWarnEnabled( String name )
    {
        return getLogInstance( name ).isWarnEnabled();
    }
    
    /**
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public static boolean isWarnEnabled( Class<?> clazz )
    {
        return getLogInstance( clazz ).isWarnEnabled();
    }
    
    /**
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public static boolean isErrorEnabled( Class<?> clazz )
    {
        return getLogInstance( clazz ).isErrorEnabled();
    }
    
    /**
     * Configurable isEnabled call, should only be used in rare cases.
     * Preferred is the direct call.
     */
    public static boolean isEnabled( short logLevel, Class<?> clazz )
    {
        switch (logLevel)
        {
        case LOG_LEVEL_DEBUG:
            return isDebugEnabled( clazz );
        case LOG_LEVEL_INFO:
            return isInfoEnabled( clazz );
        case LOG_LEVEL_WARN:
            return isWarnEnabled( clazz );
        case LOG_LEVEL_ERROR:
            return isErrorEnabled( clazz );
        default:
            throw new IllegalArgumentException( "Unknown log level: " + logLevel );
        }
    }
    
    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object)
     */
    public static void debug( String name, Object message )
    {
        getLogInstance( name ).debug( message );
    }
    
    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object)
     */
    public static void debug( Class<?> clazz, Object message )
    {
        getLogInstance( clazz ).debug( message );
    }

    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
     */
    public static void debug(String name, Object message, Throwable t)
    {
        getLogInstance( name ).debug( message, t );
    }
    
    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
     */
    public static void debug(Class<?> clazz, Object message, Throwable t)
    {
        getLogInstance( clazz ).debug( message, t );
    }
    
    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object)
     */
    public static void info( String name, Object message )
    {
        getLogInstance( name ).info( message );
    }
    
    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object)
     */
    public static void info( Class<?> clazz, Object message )
    {
        getLogInstance( clazz ).info( message );
    }

    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
     */
    public static void info(String name, Object message, Throwable t)
    {
        getLogInstance( name ).info( message, t );
    }
    
    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
     */
    public static void info(Class<?> clazz, Object message, Throwable t)
    {
        getLogInstance( clazz ).info( message, t );
    }
    
    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object)
     */
    public static void warn( String name, Object message )
    {
        getLogInstance( name ).warn( message );
    }

    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
     */
    public static void warn(String name, Object message, Throwable t)
    {
        getLogInstance( name ).warn( message, t );
    }
    
    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
     */
    public static void warn(Class<?> clazz, Object message, Throwable t)
    {
        getLogInstance( clazz ).warn( message, t );
    }
    
    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
     */
    public static void warn(Class<?> clazz, Object message )
    {
        getLogInstance( clazz ).warn( message );
    }

    
    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object)
     */
    public static void error( String name, Object message )
    {
        getLogInstance( name ).error( message );
    }

    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
     */
    public static void error(String name, Object message, Throwable t)
    {
        getLogInstance( name ).error( message, t );
    }
    
    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
     */
    public static void error(Class<?> clazz, Object message)
    {
        getLogInstance( clazz ).error( message );
    }
    
    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
     */
    public static void error(Class<?> clazz, Object message, Throwable t)
    {
        getLogInstance( clazz ).error( message, t );
    }
        
    /**
     * Configurable log call, should only be used in rare cases.
     * Preferred is the direct call.
     */
    public static void log( short logLevel, Class<?> clazz, Object message )
    {
        log( logLevel, clazz, message, null );
    }
    
    /**
     * Configurable log call, should only be used in rare cases.
     * Preferred is the direct call.
     */
    public static void log( short logLevel, Class<?> clazz, Object message, Throwable t)
    {
        switch (logLevel)
        {
        case LOG_LEVEL_DEBUG:
            debug( clazz, message, t );
            break;
        case LOG_LEVEL_INFO:
            info( clazz, message, t );
            break;
        case LOG_LEVEL_WARN:
            warn( clazz, message, t );
            break;
        case LOG_LEVEL_ERROR:
            error( clazz, message, t );
            break;
        default:
            throw new IllegalArgumentException( "Unknown log level: " + logLevel );
        }
    }
}