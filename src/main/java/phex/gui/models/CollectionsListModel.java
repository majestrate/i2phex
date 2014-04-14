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
 *  Created on 19.03.2005
 *  --- CVS Information ---
 *  $Id: CollectionsListModel.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.gui.models;

import java.util.*;

import javax.swing.AbstractListModel;

/**
 *
 */
public class CollectionsListModel extends AbstractListModel implements List
{
    private List delegate;
    
    public CollectionsListModel()
    {
        delegate = new ArrayList();
    }
    
    public CollectionsListModel( List list )
    {
        delegate = list;
    }

    /**
     * @see java.util.Collection#size()
     */
    public int size()
    {
        return delegate.size();
    }

    /**
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }

    /**
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o)
    {
        return delegate.contains(o);
    }

    /**
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator()
    {
        return delegate.iterator();
    }

    /**
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray()
    {
        return delegate.toArray();
    }

    /**
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] a)
    {
        return delegate.toArray(a);
    }

    /**
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(Object obj)
    {     
        int index = delegate.size();
        boolean ret = delegate.add(obj);
        fireIntervalAdded(this, index, index);
        return ret;
    }

    /**
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o)
    {
        int idx = indexOf(o);
        boolean ret = delegate.remove(o);
        if (idx >= 0)
        {
            fireIntervalRemoved(this, idx, idx);
        }
        return ret;
    }

    /**
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c)
    {
        return delegate.containsAll(c);
    }

    /**
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection c)
    {
        int index = delegate.size();
        int count = c.size();
        boolean ret = delegate.addAll(c);
        fireIntervalAdded(this, index, index + count - 1);
        return ret;
    }

    /**
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c)
    {
        return delegate.removeAll(c);
    }

    /**
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection c)
    {
        return delegate.retainAll(c);
    }

    /**
     * @see java.util.Collection#clear()
     */
    public void clear()
    {
        delegate.clear();
    }

    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize()
    {
        return delegate.size();
    }

    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index)
    {
        return delegate.get(index);
    }

    /**
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection c)
    {
        return delegate.addAll(index, c);
    }

    /**
     * @see java.util.List#get(int)
     */
    public Object get(int index)
    {
        return delegate.get(index);
    }

    /**
     * @see java.util.List#set(int, java.lang.Object)
     */
    public Object set(int index, Object element)
    {
        Object obj = delegate.set(index, element);
        fireContentsChanged(this, index, index);
        return obj;
    }

    /**
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, Object element)
    {
        delegate.add(index, element);
    }

    /**
     * @see java.util.List#remove(int)
     */
    public Object remove(int index)
    {
        Object obj = delegate.remove(index);
        fireIntervalRemoved(this, index, index);
        return obj;
    }

    /**
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object o)
    {
        return delegate.indexOf(o);
    }

    /**
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object o)
    {
        return delegate.lastIndexOf(o);
    }

    /**
     * @see java.util.List#listIterator()
     */
    public ListIterator listIterator()
    {
        return delegate.listIterator();
    }

    /**
     * @see java.util.List#listIterator(int)
     */
    public ListIterator listIterator(int index)
    {
        return delegate.listIterator(index);
    }

    /**
     * @see java.util.List#subList(int, int)
     */
    public List subList(int fromIndex, int toIndex)
    {
        return delegate.subList(fromIndex, toIndex);
    }   
}