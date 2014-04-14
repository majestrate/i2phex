package phex.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import junit.framework.TestCase;
import phex.common.bandwidth.BandwidthController;
import phex.io.buffer.ByteBuffer;
import phex.net.connection.Connection;
import phex.utils.DummySocketFacade;

public class TestConnection extends TestCase
{
    
    public void testWriteByteBuffer() throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocate( 20 );
        byte[] data = "Hello World".getBytes();
        buffer.put( data, 0, data.length );
        buffer.flip();
        DummySocketFacade socketFac = new DummySocketFacade( data );
        Connection con = new Connection( socketFac, 
            new BandwidthController( "JUnitText", Long.MAX_VALUE ) );
        con.write( buffer );
        byte[] resultData = socketFac.getOutData();
        assertTrue( Arrays.equals( data, resultData ) );
        assertEquals( buffer.position(), data.length );
        assertEquals( buffer.remaining(), 0 );
    }
    
    public void testReadByteBuffer() throws IOException
    {
        
        String testString = "Hello World";
        byte[] data = testString.getBytes();

        DummySocketFacade socketFac = new DummySocketFacade( data );
        Connection con = new Connection( socketFac, 
            new BandwidthController( "JUnitText", Long.MAX_VALUE ) );
        ByteBuffer buffer = ByteBuffer.allocate( 20 );
        con.read( buffer );
        buffer.flip();
        int size = buffer.remaining();
        assertEquals( data.length, size );
        String resultString = buffer.getString( Charset.forName( "UTF-8" ).newDecoder() );
        assertEquals( testString, resultString );
        assertEquals( buffer.position(), data.length );
        assertEquals( buffer.remaining(), 0 );
    }
}
