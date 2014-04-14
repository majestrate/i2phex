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
 *  $Id: GWebCacheListBuilder.java 3682 2007-01-09 15:32:14Z gregork $
 */
package phex.tools;

import java.io.*;
import java.io.InputStream;
import java.net.*;
import java.net.URL;
import java.util.*;
import java.util.ArrayList;

import phex.common.Environment;

/**
 * 
 */
public class GWebCacheListBuilder
{
    private static final String listUrl = "http://www.rodage.net/gnetcache/gcache.php?urlfile=1000";
    private static List<String> dataList;
    
    public static void main( String[] args )
        throws Exception
    {
        dataList = new ArrayList<String>();
        System.setProperty( "http.agent", Environment.getPhexVendor() );
        
        URL url = new URL( listUrl );
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();
        readData(inputStream);
        System.out.println( "Total data read: " + dataList.size() );
        inputStream.close();
        writeToOutputFile( );
    }
    
    private static void readData(InputStream inputStream) throws IOException
    {
        BufferedReader reader = new BufferedReader( new InputStreamReader(
            inputStream ) );
        String line = reader.readLine();
        // the first line might contain an error
        if ( line != null && line.startsWith( "ERROR" ) )
        {
            System.err.println( line );
            return;
        }
        while ( line != null )
        {
            try
            {
                URL url = new URL( line );
                if ( !url.getProtocol().equals( "http" ) )
                {
                    System.err.println( "Skipped " + line );
                    continue;
                }
                dataList.add( line );
            }
            catch ( MalformedURLException exp )
            {//ignore false url
                System.err.println( "Skipped " + line );
            }
            line = reader.readLine();
        }
    }
    
    private static void writeToOutputFile( ) throws IOException
    {
        // write to output file
        BufferedWriter writer = new BufferedWriter(
            new FileWriter( "src/phex/resources/gwebcache.cfg" ) );
        Iterator iterator = dataList.iterator();
        while ( iterator.hasNext() )
        {
            String line = (String)iterator.next();
            writer.write( line + "\n" );
        }
        writer.close();
    }
}
