package org.bushe.swing.event;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;

/**
 * Created by IntelliJ IDEA. User: Michael Bushe Date: Sep 3, 2007 Time: 10:07:42 PM To change this template use File |
 * Settings | File Templates.
 */
public class EventBusReferenceQueue extends ReferenceQueue {

   /**
    * Removes the next reference object in this queue, blocking until either one becomes available or the given timeout
    * period expires.
    * <p/>
    * <p> This method does not offer real-time guarantees: It schedules the timeout as if by invoking the {@link
    * Object#wait(long)} method.
    *
    * @param timeout If positive, block for up <code>timeout</code> milliseconds while waiting for a reference to be added
    * to this queue.  If zero, block indefinitely.
    *
    * @return A reference object, if one was available within the specified timeout period, otherwise <code>null</code>
    *
    * @throws IllegalArgumentException If the value of the timeout argument is negative
    * @throws InterruptedException If the timeout wait is interrupted
    */
   public Reference remove(long timeout) throws IllegalArgumentException, InterruptedException {
      Reference r = super.remove(timeout);
      return r;
   }

   /**
    * Removes the next reference object in this queue, blocking until one becomes available.
    *
    * @return A reference object, blocking until one becomes available
    *
    * @throws InterruptedException If the wait is interrupted
    */
   public Reference remove() throws InterruptedException {
      Reference r = super.remove();
      return r;
   }
}
