package phex.xml.thex;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import phex.common.log.NLogger;

public class TestThexHashTreeSaxHandler extends TestCase
{
    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }
    
    public void testSaxParsing() throws IOException
    {
        InputStream inStream = ClassLoader.getSystemResourceAsStream( "phex/xml/thex/ThexTest.xml" );
        ThexHashTree hashTree = new ThexHashTree();
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try
        {
            SAXParser saxParser = spf.newSAXParser();
        
            saxParser.parse( new InputSource( inStream ),
                new ThexHashTreeSAXHandler(hashTree) );
        }
        catch ( ParserConfigurationException exp )
        {
            NLogger.error( TestThexHashTreeSaxHandler.class, exp, exp );
            throw new IOException( "Parsing Thex HashTree failed." );
        }
        catch ( SAXException exp )
        {
            NLogger.error( TestThexHashTreeSaxHandler.class, exp, exp );
            throw new IOException( "Parsing Thex HashTree failed." );
        }
    }
}
