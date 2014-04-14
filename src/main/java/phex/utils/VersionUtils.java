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
 
package phex.utils;

import java.util.StringTokenizer;

import phex.Res;
import phex.common.Environment;
import phex.common.log.NLogger;


public class VersionUtils
{
    private static final int MAJOR_VERSION_NUMBER;
    private static final int MINOR_VERSION_NUMBER;
    private static final int ULTRAPEER_MAJOR_VERSION_NUMBER = 0;
    private static final int ULTRAPEER_MINOR_VERSION_NUMBER = 1;
    
    static
    {
        String version = Res.getStr( "Program.Version" );
        String majorStr, minorStr;
        try
        {
            int firstIdx = version.indexOf(".");
            int secondIdx = version.indexOf( ".", firstIdx + 1 );
            int thirdIdx = version.indexOf( ".", secondIdx + 1 );
            if ( thirdIdx == -1 )
            {
                thirdIdx = version.length();
            }
            majorStr = version.substring( 0, firstIdx );
            minorStr = version.substring( firstIdx + 1, secondIdx );
        }
        catch (NumberFormatException exp)
        {
            NLogger.error( VersionUtils.class, exp, exp );
            majorStr = "3";
            minorStr = "0";
        }
        MAJOR_VERSION_NUMBER = Integer.valueOf( majorStr ).intValue();
        MINOR_VERSION_NUMBER = Integer.valueOf( minorStr ).intValue();
    }
    
    /**
     * Returns the Phex version.
     * @return the Phex version.
     */
    public static String getFullProgramVersion()
    {
        String privateBuild = Environment.getInstance().getProperty( "privatebuild.number" );
        if ( !StringUtils.isEmpty( privateBuild ) )
        {
            return Environment.getInstance().getProperty( "privatebuild.number" ) 
                + ' ' + Res.getStr( "Program.Version" ) + '.'
                + Environment.getInstance().getProperty( "build.number" );
        }
        else
        {
            return Res.getStr( "Program.Version" ) + '.'
                + Environment.getInstance().getProperty( "build.number" );
        }
    }
    
    /**
     * Returns the Phex version.
     * @return the Phex version.
     */
    public static String getProgramVersion()
    {
        String privateBuild = Environment.getInstance().getProperty( "privatebuild.number" );
        if ( !StringUtils.isEmpty( privateBuild ) )
        {
            return Environment.getInstance().getProperty( "privatebuild.number" ) 
                + ' ' + Res.getStr( "Program.Version" );
        }
        else
        {
            return Res.getStr( "Program.Version" );
        }
    }
    
    /**
     * Returns the Phex version.
     * @return the Phex version.
     */
    public static String getBuild()
    {
        return Environment.getInstance().getProperty( "build.number" );
    }

    
    public static int getMajorVersionNumber()
    {
        return MAJOR_VERSION_NUMBER;
    }
    
    public static int getMinorVersionNumber()
    {
        return MINOR_VERSION_NUMBER;
    }
    
    public static int getUltrapeerMajorVersionNumber()
    {
        return ULTRAPEER_MAJOR_VERSION_NUMBER;
    }
    
    public static int getUltrapeerMinorVersionNumber()
    {
        return ULTRAPEER_MINOR_VERSION_NUMBER;
    }

    /**
     * A version is supposed to be seperated by . ( i.e. 1.2.3.4 )
     * returns >0 if version1 is greater then version2
     * returns 0 if both versions are equal
     * returns <0 if version2 is greater then version1
     */
    public static int compare(String version1, String version2)
    {
        int result = 0;
        int[] versionArr1 = tokenizeVersion( version1 );
        int[] versionArr2 = tokenizeVersion( version2 );
        int value1 = 0;
        int value2 = 0;
        for (int i = 0; result == 0 && ( i < versionArr1.length || i < versionArr2.length ); i++)
        {
            if (versionArr1.length > i)
            {
                value1 = versionArr1[ i ];
            }
            else
            {
                value1 = 0;
            }
            if (versionArr2.length > i)
            {
                value2 = versionArr2[ i ];
            }
            else
            {
                value2 = 0;
            }
            result = value1 - value2;
        }
        return result;
    }

    public static int[] tokenizeVersion(String version)
    {
        StringTokenizer tok = new StringTokenizer( version, "." );
        int count = tok.countTokens();
        int[] arr = new int[ count ];

        for (int i = 0; i < count; i++)
        {
            arr[ i ] = Integer.parseInt( tok.nextToken() );
        }
        return arr;
    }

/*   public static void main(String args[])
   {
      System.out.println("Test VersionUtils");

      System.out.println("1.0 , 2.0");
      System.out.println( VersionUtils.compare("1.0", "2.0"));

      System.out.println("2.0 , 1.0");
      System.out.println( VersionUtils.compare("2.0", "1.0"));

      System.out.println("1.0 , 1.0");
      System.out.println( VersionUtils.compare("1.0", "1.0"));

      System.out.println("1.0.1.2 , 1.0.1.3");
      System.out.println( VersionUtils.compare("1.0.1.2", "1.0.1.3"));

      System.out.println("1.2.3.4 , 1.2.4.3");
      System.out.println( VersionUtils.compare("1.2.3.4", "1.2.4.3"));

      System.out.println("3.3.2.1 , 5.0");
      System.out.println( VersionUtils.compare("3.3.2.1", "5.0"));

      System.out.println("5.0 , 3.3.2.1");
      System.out.println( VersionUtils.compare("5.0", "3.3.2.1"));

      System.out.println("2.0 , 2.0.1");
      System.out.println( VersionUtils.compare("2.0", "2.0.1"));

   }*/

}
