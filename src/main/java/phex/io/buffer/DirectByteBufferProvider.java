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
 *  $Id: DirectByteBufferProvider.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.io.buffer;

import java.nio.ByteBuffer;
import java.util.*;

import phex.common.Environment;
import phex.common.log.NLogger;

/**
 * TODO find out the usuall requested buffer sizes and provide storage slots for 
 * these sizes.
 */
@Deprecated
public class DirectByteBufferProvider
{
    private static final DirectByteBufferProvider instance;

    private Map<Integer, Set<DirectByteBuffer>> sizeToBufferListMap;

    /**
     * Max use 10MB in buffer cache.
     */
    private static final long MAX_TOTAL_BUFFER_SIZE = 10 * 1024 * 1024;

    static
    {
        instance = new DirectByteBufferProvider();
    }

    private DirectByteBufferProvider()
    {
        sizeToBufferListMap = new LinkedHashMap<Integer, Set<DirectByteBuffer>>();
        
        Environment.getInstance().scheduleTimerTask(new CleanupCheckerTask(),
            CleanupCheckerTask.TIMER_PERIOD, CleanupCheckerTask.TIMER_PERIOD);
    }

    public static DirectByteBuffer requestBuffer(int sizeInBytes)
    {
        return instance.requestDirectByteBuffer(sizeInBytes);
    }

    protected synchronized void releaseDirectByteBuffer(
        DirectByteBuffer directByteBuffer)
    {
        //NLogger.debug( NLoggerNames.GLOBAL, "Releasing DirectByteBuffer:" + directByteBuffer );
        debugDump();
        ByteBuffer buffer = directByteBuffer.getInternalBuffer();
        int kb = (int) Math.ceil(buffer.capacity() / 1024.0);
        Integer key = Integer.valueOf(kb);
        Set<DirectByteBuffer> set = sizeToBufferListMap.get(key);
        if (set == null)
        {
            set = new HashSet<DirectByteBuffer>();
            sizeToBufferListMap.put(key, set);
        }
        set.add(directByteBuffer);
        debugDump();
    }

    private synchronized DirectByteBuffer requestDirectByteBuffer(int capacity)
    {
        //NLogger.debug( NLoggerNames.GLOBAL, "Requesting DirectByteBuffer:" + capacity );
        debugDump();
        
        int kb = (int) Math.ceil(capacity / 1024.0);
        Integer key = Integer.valueOf(kb);
        Set<DirectByteBuffer> set = sizeToBufferListMap.get(key);
        if (set == null)
        {
            set = new HashSet<DirectByteBuffer>();
            sizeToBufferListMap.put(key, set);
        }
        DirectByteBuffer directByteBuffer;
        if (set.isEmpty())
        {
            directByteBuffer = allocateDirectByteBuffer(kb * 1024);
        }
        else
        {
            directByteBuffer = set.iterator().next();
            set.remove(directByteBuffer);
        }
        ByteBuffer buffer = directByteBuffer.getInternalBuffer();
        buffer.clear();
        buffer.limit(capacity);
        
        //NLogger.debug( NLoggerNames.GLOBAL, "Requested DirectByteBuffer:" + directByteBuffer );
        debugDump();
        return directByteBuffer;
    }

    /**
     * @param kb
     * @return
     */
    private synchronized DirectByteBuffer allocateDirectByteBuffer(int capacity)
    {
        ByteBuffer byteBuffer;
        try
        {
            byteBuffer = ByteBuffer.allocateDirect(capacity);
        }
        catch (OutOfMemoryError err)
        {
            clearBuffers();
            System.runFinalization();
            System.gc();
            Thread.yield();
            try
            {
                byteBuffer = ByteBuffer.allocateDirect(capacity);
            }
            catch (OutOfMemoryError err2)
            {
                NLogger.error( DirectByteBufferProvider.class,
                    "Out of memory while trying to allocated direct byte buffer.");
                throw err2;
            }
        }
        DirectByteBuffer directByteBuffer = new DirectByteBuffer(byteBuffer,
            this);
        return directByteBuffer;
    }

    /**
     * Clears available buffers of the provider.
     */
    private synchronized void clearBuffers()
    {
        for ( Set<DirectByteBuffer> set : sizeToBufferListMap.values() )
        {
            set.clear();
        }
    }

    private synchronized void cleanupChecker()
    {
        long bytesUsed = 0;
        Iterator<Integer> iterator = sizeToBufferListMap.keySet().iterator();
        while (iterator.hasNext())
        {
            Integer key = iterator.next();
            Set<DirectByteBuffer> set = sizeToBufferListMap.get(key);

            bytesUsed += key.intValue() * set.size() * 1024L;
        }

        if (bytesUsed > MAX_TOTAL_BUFFER_SIZE)
        {
            performCleanup(bytesUsed - MAX_TOTAL_BUFFER_SIZE);
        }
    }

    private synchronized void performCleanup(long bytesToFree)
    {
        // we remove buffers from sizes with the highest amount of cached buffers
        int mapSize = sizeToBufferListMap.size();
        int highestCount = 0;
        int[] bufferSizeArr = new int[mapSize];
        Set[] bufferSetArr = new Set[mapSize];

        int i = 0;
        Iterator<Integer> listIterator = sizeToBufferListMap.keySet().iterator();
        while (listIterator.hasNext())
        {
            Integer key = listIterator.next();
            Set<DirectByteBuffer> set = sizeToBufferListMap.get(key);
            bufferSizeArr[i] = key.intValue() * 1024;
            bufferSetArr[i] = set;
            int setSize = set.size();
            if (setSize > highestCount)
            {
                highestCount = setSize;
            }
            i++;
        }

        long bytesFreed = 0;
        while (bytesFreed < bytesToFree && highestCount > 0)
        {
            for (i = 0; i < mapSize; i++)
            {
                // if this is the largest buffer list remove 
                // one buffer.
                if (bufferSetArr[i].size() == highestCount)
                {
                    bufferSetArr[i].remove( bufferSetArr[i].iterator().next() );
                    bytesFreed += bufferSizeArr[i];
                }
            }
            highestCount--;
        }
    }
    
    private synchronized void debugDump()
    {
//        Iterator keys = sizeToBufferListMap.keySet().iterator();
//        while ( keys.hasNext() )
//        {
//            Integer key = (Integer) keys.next();
//            System.out.println( "------------------------" );
//            System.out.println( "Key: " + key.intValue() );
//            Set keySet = (Set) sizeToBufferListMap.get(key);
//            Iterator bufferIterator = keySet.iterator();
//            while ( bufferIterator.hasNext() )
//            {
//                DirectByteBuffer buffer = (DirectByteBuffer) bufferIterator.next();
//                System.out.println( buffer );
//            }
//        }
    }

    private class CleanupCheckerTask extends TimerTask
    {
        private static final int TIMER_PERIOD = 10 * 60 * 1000;

        /**
         * @see java.util.TimerTask#run()
         */
        public void run()
        {
            cleanupChecker();
        }

    }
}