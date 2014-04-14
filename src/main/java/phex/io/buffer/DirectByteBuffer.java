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
 *  $Id: DirectByteBuffer.java 4364 2009-01-16 10:56:15Z gregork $
 */
package phex.io.buffer;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import phex.msg.GUID;

/**
 * Represents a nio direct byte buffer provided from the system.
 * A DirectByteBuffer must be requested from the DirectByteBufferProvider and
 * must be released after usage.
 */
@Deprecated
public class DirectByteBuffer
{
    private ByteBuffer buffer;
    private DirectByteBufferProvider provider;
    private String id;

    public DirectByteBuffer(ByteBuffer buffer, DirectByteBufferProvider provider)
    {
        this.buffer = buffer;
        this.provider = provider;
        id = new GUID().toString();
    }
    
    public ByteBuffer getInternalBuffer()
    {
        return buffer;
    }
    
    public void release()
    {
        provider.releaseDirectByteBuffer( this );
    }
    
    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers bytes into this buffer from the given
     * source array.  If there are more bytes to be copied from the array
     * than remain in this buffer, that is, if
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>, then no
     * bytes are transferred and a {@link BufferOverflowException} is
     * thrown.
     *
     * <p> Otherwise, this method copies <tt>length</tt> bytes from the
     * given array into this buffer, starting at the given offset in the array
     * and at the current position of this buffer.  The position of this buffer
     * is then incremented by <tt>length</tt>.
     *
     * <p> In other words, an invocation of this method of the form
     * <tt>dst.put(src,&nbsp;off,&nbsp;len)</tt> has exactly the same effect as
     * the loop
     *
     * <pre>
     *     for (int i = off; i < off + len; i++)
     *         dst.put(a[i]); </pre>
     *
     * except that it first checks that there is sufficient space in this
     * buffer and it is potentially much more efficient. </p>
     *
     * @param  src
     *         The array from which bytes are to be read
     *
     * @param  offset
     *         The offset within the array of the first byte to be read;
     *         must be non-negative and no larger than <tt>array.length</tt>
     *
     * @param  length
     *         The number of bytes to be read from the given array;
     *         must be non-negative and no larger than
     *         <tt>array.length - offset</tt>
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If the preconditions on the <tt>offset</tt> and <tt>length</tt>
     *          parameters do not hold
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     */
    public void put( byte[] src, int off, int len )
    {
        buffer.put( src, off, len );
    }
    
    /**
     * Flips this buffer.  The limit is set to the current position and then
     * the position is set to zero.  If the mark is defined then it is
     * discarded.
     *
     * <p> After a sequence of channel-read or <i>put</i> operations, invoke
     * this method to prepare for a sequence of channel-write or relative
     * <i>get</i> operations.  For example:
     *
     * <blockquote><pre>
     * buf.put(magic);    // Prepend header
     * in.read(buf);      // Read data into rest of buffer
     * buf.flip();        // Flip buffer
     * out.write(buf);    // Write header + data to channel</pre></blockquote>
     *
     * <p> This method is often used in conjunction with the {@link
     * java.nio.ByteBuffer#compact compact} method when transferring data from
     * one place to another.  </p>
     */
    public void flip( )
    {
        buffer.flip( );
    }
    
    /**
     * Clears this buffer.  The position is set to zero, the limit is set to
     * the capacity, and the mark is discarded.
     *
     * <p> Invoke this method before using a sequence of channel-read or
     * <i>put</i> operations to fill this buffer.  For example:
     *
     * <blockquote><pre>
     * buf.clear();     // Prepare buffer for reading
     * in.read(buf);    // Read data</pre></blockquote>
     *
     * <p> This method does not actually erase the data in the buffer, but it
     * is named as if it did because it will most often be used in situations
     * in which that might as well be the case. </p>
     *
     * @return  This buffer
     */
    public void clear( )
    {
        buffer.clear( );
    }
    
    /**
     * Returns this buffer's limit. </p>
     *
     * @return  The limit of this buffer
     */
    public final int limit() 
    {
        return buffer.limit( );
    }
    
    /**
     * Sets this buffer's limit.  If the position is larger than the new limit
     * then it is set to the new limit.  If the mark is defined and larger than
     * the new limit then it is discarded. </p>
     *
     * @param  newLimit
     *         The new limit value; must be non-negative
     *         and no larger than this buffer's capacity
     *
     * @return  This buffer
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on <tt>newLimit</tt> do not hold
     */
    public final void limit(int newLimit) 
    {
        buffer.limit( newLimit );
    }
    
    /**
     * Returns the number of elements between the current position and the
     * limit. </p>
     *
     * @return  The number of elements remaining in this buffer
     */
    public final int remaining() 
    {
        return buffer.remaining();
    }
    
    /**
     * Tells whether there are any elements between the current position and
     * the limit. </p>
     *
     * @return  <tt>true</tt> if, and only if, there is at least one element
     *          remaining in this buffer
     */
    public final boolean hasRemaining() 
    {
        return buffer.hasRemaining();
    }
    
    
    public String toString()
    {
        return "DByteBuffer:[ID:" + id + ", " + super.toString() + "]";
    }
    
    public boolean isEqual( Object obj )
    {
        if ( !(obj instanceof DirectByteBuffer ) )
        {
            return false;
        }
        return id.equals( ((DirectByteBuffer)obj).id );
    }
    
    public int hashCode()
    {
        int subH = id.hashCode();
        int h = 31 * subH + subH;
        return h;
    }
}