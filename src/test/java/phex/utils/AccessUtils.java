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
 *  $Id: AccessUtils.java 3942 2007-09-29 15:41:44Z gregork $
 */
package phex.utils;

import java.lang.reflect.*;

/**
 * This class provides functionality to access class methods and fields with
 * restricted access.
 */
public class AccessUtils
{
    /**
     * Reads the value of a field.
     * @param obj the object to read the field value from. If it is a static field
     *            the object can be a instance of Class.
     */
    public static Object getFieldValue( Object obj, String fieldName )
    {
        try
        {
            Class clazz;
            if ( obj instanceof Class )
            {
                clazz = (Class)obj;
            }
            else
            {
                clazz = obj.getClass();
            }
            Field field = clazz.getDeclaredField( fieldName );
            field.setAccessible( true );
            return field.get( obj );
        }
        catch ( Exception exp )
        {
            exp.printStackTrace();
            throw new RuntimeException( exp.getMessage() );
        }
    }

    /**
     * Reads the value of a field.
     * @param obj the object to read the field value from. If it is a static field
     *            the object can be a instance of Class.
     */
    public static void setFieldValue( Object obj, String fieldName, Object value )
    {
        try
        {
            Class clazz;
            if ( obj instanceof Class )
            {
                clazz = (Class)obj;
            }
            else
            {
                clazz = obj.getClass();
            }
            Field field = clazz.getDeclaredField( fieldName );
            field.setAccessible( true );
            field.set( obj, value );
        }
        catch ( Exception exp )
        {
            exp.printStackTrace();
            throw new RuntimeException( exp.getMessage() );
        }
    }


    public static Object invokeMethod( Object obj, String methodName, Object param )
        throws Throwable
    {
        Object[] params = { param };
        return invokeMethod( obj, methodName, params );
    }

    public static Object invokeMethod(
        Object obj,
        String methodName,
        Object[] params)
        throws Throwable
    {
        Class[] paramTypes = null;
        if ( params != null )
        {
            paramTypes = new Class[params.length];
            for (int i = 0; i < params.length; i++)
            {
                paramTypes[i] = params[i].getClass();
            }
        }
        return invokeMethod( obj, methodName, params, paramTypes );
    }
        
    public static Object invokeMethod(
        Object obj, String methodName, Object[] params,
        Class[] paramTypes )
        throws Throwable
    {
        Class clazz;
        if (obj instanceof Class)
        {
            clazz = (Class) obj;
        }
        else
        {
            clazz = obj.getClass();
        }

        try
        {
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            Object retVal = method.invoke(obj, params);
            return retVal;
        }
        catch (IllegalAccessException exp)
        {
            exp.printStackTrace();
            throw new RuntimeException( exp.getMessage() );
        }
        catch (InvocationTargetException exp)
        {
            throw exp.getTargetException();
        }
        catch (NoSuchMethodException exp)
        {
            exp.printStackTrace();
            throw new RuntimeException( exp.getMessage() );
        }
    }

}