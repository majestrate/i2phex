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
 *  Created on May 17, 2005
 *  --- CVS Information ---
 *  $Id: UdpMsgQueue.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex.udp;


import phex.common.address.DestAddress;
import phex.common.collections.PriorityQueue;
import phex.msg.Message;
import phex.msg.PingMsg;
import phex.msg.PongMsg;

/**
 * This provides a thread safe message queue
 * 
 * @author Madhu
 */
public class UdpMsgQueue
{
    // Dont process more then 15 pings bothways
    private final static int PING_QUEUE_SIZE = 15;
        
    private final static int PONG_QUEUE_SIZE= 20;

    // pongs have more priorities then pings
    private final static int PING_PRIORITY = 0;
    private final static int PONG_PRIORITY = 1;
    
   
    //  priority send queue, we store queue elements in the queue
    private PriorityQueue queue;
    
    public UdpMsgQueue()
    {
        int[] priorities = { PING_QUEUE_SIZE, PONG_QUEUE_SIZE };
        
        queue = new PriorityQueue( priorities );
    }
    
    /**
     * Creates a Queue Element and adds it to the head of the queue
     * @param Msg
     * @param hostAddr
     * @return
     */
    public boolean addMessage( Message Msg, DestAddress hostAddr )
    {
        int priority = -1;
        
        if( Msg == null )
        {
            // we dont add null messages
            return false;
        }
                
        if( Msg instanceof PingMsg )
        {
            priority = PING_PRIORITY;
        }
       
        if( Msg instanceof PongMsg )
        {
            priority = PONG_PRIORITY;
        }
        
        if( priority < 0 )
        {
            // unknown message
            return false;
        }
        
        QueueElement element = new QueueElement( Msg, hostAddr );
        
        synchronized ( queue )
        {
            queue.addToHead( element, priority );
            queue.notify();
        }
               
        return true;
    }
    
    public QueueElement removeMessage()
    {
        QueueElement element = null;
        synchronized ( queue )
        {
            while ( queue.isEmpty() )
            {
                try
                {
                    queue.wait();
                }
                catch ( InterruptedException e )
                {
                    
                }
            }
            element = ( QueueElement )queue.removeMaxPriority();
        }
        return element;
    }
    
    
    public boolean isEmpty()
    {
        synchronized( queue )
        {
        return queue.isEmpty();
        }
    }

    /**
     * Returns true if the queue for the priority is full.
     * @param priority
     * @return
     */
    public boolean isFull( int priority )
    {
        synchronized( queue )
        {
        return queue.isFull( priority );
        }
    }
    
    public class QueueElement
    {
        private Message msg;
        private DestAddress address;
        
        public QueueElement( Message message, DestAddress addr )
        {
            this.msg = message;
            this.address = addr;
        }
        
        public Message getMsg()
        {
            return msg;
        }
        
        public DestAddress getAddress()
        {
            return address;
        }
    }
    
}
