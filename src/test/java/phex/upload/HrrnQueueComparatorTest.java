package phex.upload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import phex.common.address.DefaultDestAddress;
import phex.gui.prefs.InterfacePrefs;
import phex.gui.prefs.PhexGuiPrefs;
import phex.prefs.core.PhexCorePrefs;
import phex.servent.Servent;
import phex.share.ShareFile;
import phex.utils.Localizer;

public class HrrnQueueComparatorTest extends TestCase
{
    private UploadManager uploadManager;

    public void setUp()
    {
        PhexCorePrefs.init();
        PhexGuiPrefs.init();
        Localizer.initialize( InterfacePrefs.LocaleName.get() );
        Servent servent = Servent.getInstance();
        uploadManager = new UploadManager( servent );
    }
    
    public void testOrder() throws InterruptedException
    {
        List<UploadQueueState> testList = new ArrayList<UploadQueueState>();
        
        for ( int i = 100; i > 0; i-- )
        {
            UploadQueueState uqs = createUploadQueueState( (i + 1) * 1000000L );
            testList.add( uqs );
            Thread.sleep( 100 );
        }
        Thread.sleep( 10000 );
        long sortTime = System.currentTimeMillis();
        Collections.sort( testList, new HrrnQueueComparator( sortTime ) );
        
        for ( UploadQueueState uqs : testList )
        {
            System.out.println( uqs.getFirstQueueTime() + " - " 
                + uqs.getLastRequestedFile().getFileSize() + " - " 
                + HrrnQueueComparator.calcRatio( uqs, sortTime ) );
        }
        
//        int i = 0;
//        for ( UploadQueueState uqs : testList )
//        {
//            assertEquals( (i + 1) * 1000L, uqs.getLastRequestedFile().getFileSize() );
//            i++;
//        }
    }
    
    private UploadQueueState createUploadQueueState( long fileSize )
    {
        UploadState us = new UploadState( new DefaultDestAddress( "", 0 ), "", uploadManager );
        ShareFile shareFile = new DummyShareFile( fileSize );
        UploadQueueState uqs = new UploadQueueState( us, shareFile );
        return uqs;
    }
    
    private static class DummyShareFile extends ShareFile
    {
        public DummyShareFile( long fileSize )
        {
            super( fileSize );
        }        
    }
}
