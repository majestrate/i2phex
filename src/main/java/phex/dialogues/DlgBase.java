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
 *  --- CVS Information ---
 *  $Id: DlgBase.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.dialogues;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.*;

import phex.gui.common.GUIUtils;
import phex.gui.common.MainFrame;

public class DlgBase extends JDialog
{
    JPanel descPanel = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JLabel description = new JLabel();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel centerPanel = new JPanel();
    JPanel filterInputPanel = new JPanel();
    JPanel filterInputPanel2 = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    JTextField mFilterText = new JTextField();
    JButton addFilterButton = new JButton();
    JLabel textDescription = new JLabel();
    JLabel jLabel1 = new JLabel();
    BorderLayout borderLayout3 = new BorderLayout();
    BorderLayout borderLayout5 = new BorderLayout();
    JPanel hostPanel = new JPanel();
    BorderLayout borderLayout6 = new BorderLayout();
    JPanel hostButtonPanel = new JPanel();
    JButton removeFilterButton = new JButton();
    JScrollPane jScrollPane1 = new JScrollPane();
    private DefaultListModel defaultListModel = new DefaultListModel();
    JList results = new JList(defaultListModel);
    JLabel listDescription = new JLabel();
    JPanel buttonPanel = new JPanel();
    JButton ok = new JButton();
    JButton cancel = new JButton();

    private boolean dialogueCancelled;
    private MainFrame	mFrame;
    private Iterator list;


    public DlgBase(MainFrame frame, String filter, Iterator list)
    {
        super(frame, true);
        mFrame = frame;
        mFilterText.setText(filter);
        this.list = list;

        try
        {
          jbInit();
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
    }

    /**
     * Used to design GUI in JBuilder
     */
    public DlgBase()
    {
        try
        {
          jbInit();
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {
        this.getContentPane().setLayout(borderLayout2);

        //description
        descPanel.setLayout(borderLayout1);
        description.setToolTipText("");
        description.setText("Enter dialogue description");
        descPanel.add(description, BorderLayout.CENTER);
        descPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 0));

        // Center panel for holding the fields
        centerPanel.setLayout(borderLayout5);
            centerPanel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(20, 20, 6, 20),
                        BorderFactory.createEtchedBorder()),
                    BorderFactory.createEmptyBorder(16, 20, 20, 20)));


        // The upper panel
        filterInputPanel.setLayout(borderLayout3);
        filterInputPanel2.setLayout(borderLayout4);
        addFilterButton.setToolTipText("");
        addFilterButton.setText("Add");
        textDescription.setToolTipText("");
        textDescription.setText("Enter text description");
        jLabel1.setText(" ");
        filterInputPanel.add(textDescription,  BorderLayout.NORTH);
        filterInputPanel.add(filterInputPanel2,  BorderLayout.CENTER);
        filterInputPanel.add(jLabel1, BorderLayout.SOUTH);
        centerPanel.add(hostPanel,  BorderLayout.CENTER);
        filterInputPanel2.add(mFilterText,  BorderLayout.CENTER);
        filterInputPanel2.add(addFilterButton, BorderLayout.EAST);
        centerPanel.add(filterInputPanel,  BorderLayout.NORTH);

        // The lower panel
        hostPanel.setLayout(borderLayout6);
        removeFilterButton.setText("Remove");
        listDescription.setText("Enter list description");
        jScrollPane1.getViewport().add(results, null);
        hostButtonPanel.add(removeFilterButton, null);
        hostPanel.add(jScrollPane1,  BorderLayout.CENTER);
        hostPanel.add(listDescription, BorderLayout.NORTH);
        hostPanel.add(hostButtonPanel,  BorderLayout.SOUTH);

        // Buttons panel
        buttonPanel.add(ok, null);
        buttonPanel.add(cancel, null);
        ok.setText("OK");
        cancel.setText("Cancel");

        this.getContentPane().add(descPanel, BorderLayout.NORTH);
        this.getContentPane().add(centerPanel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel,  BorderLayout.SOUTH);


        //clicks
        addFilterButton.addActionListener(new java.awt.event.ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            addFilterButton_actionPerformed(e);
          }
        });
        removeFilterButton.addActionListener(new java.awt.event.ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            removeFilterButton_actionPerformed(e);
          }
        });
        ok.addActionListener(new java.awt.event.ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            ok_actionPerformed(e);
          }
        });
        cancel.addActionListener(new java.awt.event.ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            cancel_actionPerformed(e);
          }
        });

        pack();
        GUIUtils.centerWindowOnScreen(this);

        //load the list of ports from the properties file
        loadList();

        mFilterText.selectAll();
        //doesn't work, don't know how
        //TODO, get the focus on the text box
        mFilterText.requestFocus();
    }

    void addFilterButton_actionPerformed(ActionEvent e)
    {
        addFilter();
    }

    void removeFilterButton_actionPerformed(ActionEvent e)
    {
        removeFilter();
    }

    void ok_actionPerformed(ActionEvent e)
    {
        pressOk();
    }

    void cancel_actionPerformed(ActionEvent e)
    {
        pressCancel();
    }

    /**
     * Use the list passed into the constructor to populate the list box
     */
    protected void loadList()
    {
        while (list.hasNext())
        {
            defaultListModel.addElement(list.next());
        }
    }

    //override methods in base class to get buttons to work in another way
    /**
     * Use the text in the filter box to add it to the list
     */
    protected void addFilter()
    {
        defaultListModel.addElement(mFilterText.getText());
        mFilterText.selectAll();
        mFilterText.requestFocus();
    }

    /**
     * Remove selected ports from the list
     */
    protected void removeFilter()
    {
        int[]	indices = results.getSelectedIndices();

        if (indices == null || indices.length == 0)
            return;

        for (int i = indices.length - 1; i >= 0; i--)
        {
            defaultListModel.removeElementAt(indices[i]);
        }
        results.clearSelection();
        mFilterText.requestFocus();
    }

    protected void pressOk()
    {
        dialogueCancelled = false;
        done();
    }

    protected void pressCancel()
    {
        dialogueCancelled = true;
        done();
    }

    protected void done()
    {
        setVisible(false);
        dispose();
    }

    /**
     * Used to check if the cancel button was pressed on the dialogue
     *
     * @return true if the cancel button was pressed
     */
    public boolean getCancel()
    {
        return dialogueCancelled;
    }

    /**
     * Get the ports that are left over in the list after the dialogue has
     * been closed
     *
     * @return an <code>Enumeration</code> containing <code>String</code> objects
     */
    public Enumeration getList()
    {
        return defaultListModel.elements();
    }
}