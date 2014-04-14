package phex.upload;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import junit.framework.TestCase;
import phex.common.address.DefaultDestAddress;
import phex.common.bandwidth.BandwidthController;
import phex.gui.prefs.InterfacePrefs;
import phex.gui.prefs.PhexGuiPrefs;
import phex.prefs.core.BandwidthPrefs;
import phex.prefs.core.PhexCorePrefs;
import phex.servent.Servent;
import phex.share.ShareFile;
import phex.utils.Localizer;

public class HrrnQueuePerformanceTest extends TestCase
{
    private static final int TOTAL_FILES = 20;
    
    private static final int MAX_QUEUE_LENGTH = 10;
    private static final int MAX_UPLOAD_SLOTS = 3;

    // 2048 - 4096 KB -> 2-4MB
    private static final int SMALL_FILE_MIN = 2048;
    private static final int SMALL_FILE_ADD = 2048;
    
    // 600000 - 1200000 KB -> 600MB-1.2GB
    private static final int LARGE_FILE_MIN = 600000;
    private static final int LARGE_FILE_ADD = 600000;
    
    // 100000 KB/s -> 100MB/s
    private static final int SPEED = 100000;
    private static final int MAX_SPEED_PER_FILE = SPEED / MAX_UPLOAD_SLOTS / 2;
    
    private static Random rand = new Random();
    private UploadManager uploadManager;
    
    
    public HrrnQueuePerformanceTest()
    {
        
    }
    
    public void setUp()
    {
        PhexCorePrefs.init();
        PhexGuiPrefs.init();
        Localizer.initialize( InterfacePrefs.LocaleName.get() );
        Servent servent = Servent.getInstance();
        uploadManager = new UploadManager( servent );
        BandwidthPrefs.MaxUploadBandwidth.set( Integer.valueOf( SPEED ) );
    }
    
    public void testRunFifo()
    {
        System.out.println( "----------FIFO--------------" );
        Provider prov = new Provider( true, TOTAL_FILES );
        Consumer cons = new Consumer( prov );
        prov.start();
        try
        {
            Thread.sleep( 100 );
            cons.start();
            cons.join();
        }
        catch ( InterruptedException exp )
        {
            // TODO Auto-generated catch block
            exp.printStackTrace();
        }
    }
    
    public void testRunHrrn()
    {
        System.out.println( "----------HRRN--------------" );
        Provider prov = new Provider( false, TOTAL_FILES );
        Consumer cons = new Consumer( prov );
        prov.start();
        
        try
        {
            Thread.sleep( 100 );
            cons.start();
            cons.join();
        }
        catch ( InterruptedException exp )
        {
            // TODO Auto-generated catch block
            exp.printStackTrace();
        }
    }
    
    public class Provider extends Thread
    {
        private int fileIndex = 1;
        private int maxUploads;
        private boolean isFifo;
        private List<UploadQueueState> list;
        
        public Provider( boolean isFifo, int maxUploads )
        {
            this.maxUploads = maxUploads;
            this.isFifo = isFifo;
            list = Collections.synchronizedList( new ArrayList<UploadQueueState>() );
        }
        
        public void run()
        {
            while( maxUploads > 0 )
            {
                if ( list.size() < MAX_QUEUE_LENGTH )
                {
                    add();
                    maxUploads --;
                    continue;
                }
                try
                {
                    Thread.sleep( 100 );
                }
                catch ( InterruptedException exp )
                {}
            }
        }
        
        private synchronized void add()
        {
            boolean isBig = rand.nextBoolean();
            long fileSize = isBig ? SMALL_FILE_MIN + rand.nextInt( SMALL_FILE_ADD ) : LARGE_FILE_MIN + rand.nextInt( LARGE_FILE_ADD );
            UploadState us = new UploadState( new DefaultDestAddress( "", 0 ), "", uploadManager );
            ShareFile shareFile = new DummyShareFile( fileIndex++, fileSize );
            UploadQueueState uqs = new UploadQueueState( us, shareFile );
            
            if ( isFifo )
            {
                list.add( uqs );
            }
            else
            {
                list.add( uqs );
                long sortTime = System.currentTimeMillis();
                Collections.sort( list, new HrrnQueueComparator( sortTime ) );
            }
            System.out.println( "Queued (" + list.size() + "): #" + shareFile.getFileIndex() + " - " + shareFile.getFileSize() + "kb" );
        }
        
        public synchronized UploadQueueState pop()
        {
            if ( list.size() == 0 )
            {
                try
                {
                    Thread.sleep( 150 );
                }
                catch ( InterruptedException exp )
                {}
            }
            if ( list.size() > 0 )
            {
                return list.remove( 0 );
            }
            return null;
        }
    }
    
    public class Consumer extends Thread
    {
        private CharArrayWriter writter;
        private List<UploadQueueState> uploads = new ArrayList<UploadQueueState>();
        private Provider provider;
        private BandwidthController bdw = new BandwidthController( "up", 
            BandwidthPrefs.MaxUploadBandwidth.get().intValue() );
        
        public Consumer( Provider provider )
        {
            this.provider = provider;
            writter = new CharArrayWriter();
        }
        
        public void run()
        {
            long transferred = 0;
            long start = System.currentTimeMillis();
            try
            {
                while( true )
                {
                
                    if ( uploads.size() < MAX_UPLOAD_SLOTS )
                    {
                        UploadQueueState pop = provider.pop();
                        if ( pop != null )
                        {
                            uploads.add( pop );
                            long now = System.currentTimeMillis();
                            DummyShareFile shareFile = (DummyShareFile) pop.getLastRequestedFile();
                            shareFile.queueTime = now - pop.getFirstQueueTime();
                            System.out.println( "Uploading: #" + shareFile.getFileIndex() + " - " + 
                                shareFile.queueTime + "ms" );
                            shareFile.trackUp();
                            continue;
                        }
                        else if ( uploads.size() == 0 )
                        {
                            break;
                        }
                    }
                    
                    ListIterator<UploadQueueState> li = uploads.listIterator();
                    while( li.hasNext() )
                    {
                        UploadQueueState s = li.next();
                        DummyShareFile dsf = (DummyShareFile)s.getLastRequestedFile();
                        long left = dsf.getLeftFileSize();
                        int toRequest = (int) Math.min( left, MAX_SPEED_PER_FILE );
                        int av = bdw.getAvailableByteCount( toRequest, false, true );
                        transferred += av;
                        dsf.uploadSize( av );
                        if ( dsf.getLeftFileSize() <= 0 )
                        {
                            long now = System.currentTimeMillis();
                            long uploadTime = Math.max( now - dsf.uploadStart, 1);
                            System.out.println( "Uploaded: #" + dsf.getFileIndex() + " - " + dsf.getFileSize() + "KB - " +
                                uploadTime + "ms - " + dsf.getFileSize()/(uploadTime/1000.0 ) + "KB/s" );
                            li.remove();
                            writter.append( String.valueOf( dsf.getFileSize() ) );
                            writter.append( " " );
                            writter.append( String.valueOf( dsf.queueTime ) );
                            writter.append( " " );
                            writter.append( String.valueOf( uploadTime ) );
                            writter.append( "\n" );
                        }
                    }
                    Thread.yield();
                }
            }
            catch ( IOException exp )
            {
                exp.printStackTrace();
            }
            
            long end = System.currentTimeMillis();
            
            double sec = (end - start)/1000.0;
            System.out.println( "Took: " + sec + "s");
            System.out.println( "Transferred: " + transferred + "KB - " + transferred/sec + "KB/s");
            System.out.println( "====================================================");
            System.out.println( writter.toString() );
            System.out.println( "====================================================");
        }
    }
    
    
    
    private static class DummyShareFile extends ShareFile
    {
        private long queueTime;
        private long uploadStart;
        private long leftFileSize;
        public DummyShareFile( int index, long fileSize )
        {
            super( fileSize );
            setFileIndex( index );
            leftFileSize = fileSize;
        }
        
        public void trackUp()
        {
            uploadStart = System.currentTimeMillis();
        }
        
        public long getLeftFileSize()
        {
            return leftFileSize;
        }
        
        public void uploadSize( long size )
        {
            leftFileSize -= size;
        }
    }
}
