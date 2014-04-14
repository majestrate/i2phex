/*
 * Created on 24.05.2004
 *
 */
package phex.test.performance;

import java.io.*;
import java.security.MessageDigest;

import junit.framework.TestCase;

import org.apache.commons.lang.SystemUtils;

import com.bitzi.util.Base32;
import com.bitzi.util.SHA1;

/**
 * Performance test for SHA1. Compare standard Java SHA1 calc performance
 * with bitzi performance and check for equal SHA1 results.
 */
public class TestSHA1 extends TestCase
{
    public TestSHA1(String s)
    {
        super(s);
    }
    
    public void testSHA1()
        throws Exception
    {
        File javahome = SystemUtils.getJavaHome();
        File[] files = javahome.listFiles();
        sha1TestFiles( files );
    }
    
    public void sha1TestFiles( File[] files )
        throws Exception
    {
        for (int j = 0; j < files.length; j++)
        {
            if ( files[j].isFile() )
            {
                String result1 = calcSHA1( files[j], new SHA1());
                String result2 = calcSHA1( files[j],
                    MessageDigest.getInstance( "SHA" ) );
                System.out.println( result1 + " - " + result2 );
                assertTrue( result1.equals( result2 ) );
            }
            else if ( files[j].isDirectory() )
            {
                sha1TestFiles( files[j].listFiles() );
            }
        }
    }
    
    public String calcSHA1( File file, MessageDigest messageDigest )
        throws Exception
    {  
        FileInputStream inStream = new FileInputStream( file );
        

        byte[] buffer = new byte[64 * 1024];
        int length;
        long start = System.currentTimeMillis();
        while ( (length = inStream.read( buffer ) ) != -1 )
        {
            // TODO2 offer two file scan modes
            long start2 = System.currentTimeMillis();
            messageDigest.update( buffer, 0, length );
            long end2 = System.currentTimeMillis();
            Thread.sleep( (end2 - start2) * 2 );
        }
        inStream.close();
        byte[] shaDigest = messageDigest.digest();
        long end = System.currentTimeMillis();
        System.out.println("Digest: " + messageDigest.getClass()
            + " SHA1 time: " + (end - start)
            + " size: " + file.length() );

        return Base32.encode( shaDigest );
    }
}
