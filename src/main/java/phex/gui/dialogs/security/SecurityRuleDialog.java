/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  $Id: SecurityRuleDialog.java 4412 2009-03-22 13:40:44Z ArneBab $
 */
package phex.gui.dialogs.security;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.time.DateUtils;

import phex.common.ExpiryDate;
import phex.common.address.AddressUtils;
import phex.common.log.NLogger;
import phex.gui.common.BanneredDialog;
import phex.gui.common.GUIRegistry;
import phex.gui.common.IPTextField;
import phex.gui.common.IntegerTextField;
import phex.security.IpUserSecurityRule;
import phex.security.PhexSecurityManager;
import phex.servent.Servent;
import phex.utils.Localizer;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

public class SecurityRuleDialog extends BanneredDialog
{
    private CloseEventHandler closeEventHandler;
    
    private JTextField descriptionTF;
    private JCheckBox disableRuleCkBx;
    
    private IPTextField ipTF;
    private IntegerTextField cidrTF;
    
    //private JComboBox ruleTypeCBox;
    private JComboBox expiresCBox; 
    
    private JLabel daysLabel;
    private IntegerTextField daysTF;
    private JLabel hoursLabel;
    private IntegerTextField hoursTF;
    private JLabel minutesLabel;
    private IntegerTextField minutesTF;
    
    private JCheckBox isDeletedOnExpiryCkbx; 
    
    private JButton okBtn;
    private JButton cancelBtn;
    
    
    private IpUserSecurityRule securityRule;

    public SecurityRuleDialog( )
    {
        this( null );
    }
    
    public SecurityRuleDialog( IpUserSecurityRule rule )
    {
        super( GUIRegistry.getInstance().getMainFrame(), 
            Localizer.getString( "SecurityRuleDialog_DialogTitle" ), false,
            null, // banner header is set dynamically...
            Localizer.getString( "SecurityRuleDialog_BannerSubHeader" ) );
        
        securityRule = rule;
        if ( securityRule == null )
        {
            setBannerHeaderText( Localizer.getString( 
                "SecurityRuleDialog_BannerHeader_New" ) );
        }
        else
        {
            setBannerHeaderText( Localizer.getString( 
                "SecurityRuleDialog_BannerHeader_Edit" ) );
        }
        
        initContent();
        
        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        pack();
        setLocationRelativeTo( getParent() );
    }
    
    public void customPrefillBanSingleIp( String description, byte[] hostIp )
    {
        descriptionTF.setText( description );
        ipTF.setIPString( AddressUtils.ip2string( hostIp ) );
        cidrTF.setText( "32" );
        disableRuleCkBx.setSelected( false );
        expiresCBox.setSelectedIndex( 1 );
        isDeletedOnExpiryCkbx.setSelected( false );
    }
    
    private void initComponents()
    {
        closeEventHandler = new CloseEventHandler();
        addWindowListener( closeEventHandler );
        
        descriptionTF = new JTextField( 40 );
        disableRuleCkBx = new JCheckBox( Localizer.getString( "SecurityRuleDialog_DisableRule" ) );
        
        ipTF = new IPTextField();
        cidrTF = new IntegerTextField( 2,2 );
        
//        String[] typeArr =
//        {
//            Localizer.getString( "Deny" ),
//            Localizer.getString( "Accept" )
//        };
//        ruleTypeCBox = new JComboBox( typeArr );
        
        String[] expireArr =
        {
            Localizer.getString( "SecurityRuleDialog_Never" ),
            Localizer.getString( "SecurityRuleDialog_EndOfSession" ),
            Localizer.getString( "SecurityRuleDialog_After" )
        };
        expiresCBox = new JComboBox( expireArr );
        expiresCBox.addActionListener( new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    refreshExpiryDisplayState();
                }
            } );
        
        daysTF = new IntegerTextField( 4 );
        hoursTF = new IntegerTextField( 4 );
        minutesTF = new IntegerTextField( 4 );
        
        isDeletedOnExpiryCkbx = new JCheckBox( Localizer.getString(
            "SecurityRuleDialog_DeleteRuleAfterExpiry" ) );
        isDeletedOnExpiryCkbx.setToolTipText( Localizer.getString( "SecurityRuleDialog_TTTDeleteRuleAfterExpiry" ) );

        
        okBtn = new JButton( Localizer.getString( "SecurityRuleDialog_OK" ) );
        okBtn.setDefaultCapable( true );
        okBtn.setRequestFocusEnabled( true );
        okBtn.addActionListener( new OkBtnListener() );
        getRootPane().setDefaultButton( okBtn );
        
        cancelBtn = new JButton( Localizer.getString( "SecurityRuleDialog_Cancel" ) );
        cancelBtn.addActionListener( closeEventHandler );
    }
    
    private void initContent()
    {
        if ( securityRule == null )
        {
            return;
        }
        descriptionTF.setText( securityRule.getDescription() );
        ipTF.setIPString( AddressUtils.ip2string( securityRule.getIp() ) );
        cidrTF.setText( String.valueOf( securityRule.getCidr() ) );

//        boolean isDenyingRule = securityRule.isDenyingRule();
//        if ( isDenyingRule )
//        {
//            ruleTypeCBox.setSelectedIndex( 0 );
//        }
//        else
//        {
//            ruleTypeCBox.setSelectedIndex( 1 );
//        }
        disableRuleCkBx.setSelected( securityRule.isDisabled() );
        ExpiryDate expiryDate = securityRule.getExpiryDate();
        if ( expiryDate.isExpiringNever() )
        {
            expiresCBox.setSelectedIndex( 0 );
        }
        else if ( expiryDate.isExpiringEndOfSession() )
        {
            expiresCBox.setSelectedIndex( 1 );
        }
        else
        {
            expiresCBox.setSelectedIndex( 2 );
            initAfterExpiryDateContent( expiryDate );
        }
        isDeletedOnExpiryCkbx.setSelected( securityRule.isDeletedOnExpiry() );
    }
    
    @Override
    protected JPanel createDialogContentPanel()
    {
        initComponents();
                
        JPanel contentPanel = new JPanel();
        
        FormLayout layout = new FormLayout( "7dlu, d, 3dlu, d, 1dlu, d, 1dlu, d, fill:d:grow" );

        DefaultFormBuilder builder = new DefaultFormBuilder( layout, contentPanel );
        builder.setLeadingColumnOffset( 1 );
        
        
        
        builder.appendSeparator( Localizer.getString( "SecurityRuleDialog_SecurityRule" ) );
        
        builder.append( Localizer.getString( "SecurityRuleDialog_Description" ),
            descriptionTF, 6 );

        builder.append( disableRuleCkBx, 8 );
        
        
        
        builder.appendSeparator( Localizer.getString( "SecurityRuleDialog_NetworkAddress" ) );
        
        builder.append( Localizer.getString( "SecurityRuleDialog_IP_CIDR" ), ipTF );
        
        builder.append( Localizer.getString( "SecurityRuleDialog_IP_CIDR_Separator" ), cidrTF, true );
        
        
        
        builder.appendSeparator( Localizer.getString( "SecurityRuleDialog_Options" ) );
        
        //builder.append( Localizer.getString( "SecurityRuleDialog_ActionType" ),
        //    ruleTypeCBox, 5 );
        
        builder.append( Localizer.getString( "SecurityRuleDialog_Expires" ),
            expiresCBox, 5 );
        
        builder.append( Box.createGlue() );
        builder.append( createTimePanel(), 6 );
        
        builder.append( Box.createGlue() );
        builder.append( isDeletedOnExpiryCkbx, 6  );

        refreshExpiryDisplayState();
        
        return contentPanel;
    }
    
    @Override
    protected JPanel createDialogButtonPanel()
    {
        JPanel btnPanel = ButtonBarFactory.buildOKCancelBar( okBtn, 
            cancelBtn );
        return btnPanel;
    }
    
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
    
    private JPanel createTimePanel()
    {
        JPanel timePanel = new JPanel();
        FormLayout layout = new FormLayout( "d, 1dlu, d, 3dlu, d, 1dlu, d, 3dlu, d, 1dlu, d", "p" );
        PanelBuilder panelBuilder = new PanelBuilder( layout, timePanel );
        
        panelBuilder.add( daysTF );
        panelBuilder.setColumn( 3 );
        daysLabel = panelBuilder.addLabel( Localizer.getString( "SecurityRuleDialog_Days" ) );
        panelBuilder.setColumn( 5 );
        panelBuilder.add( hoursTF );
        panelBuilder.setColumn( 7 );
        hoursLabel = panelBuilder.addLabel( Localizer.getString( "SecurityRuleDialog_Hours" ) );
        panelBuilder.setColumn( 9 );
        panelBuilder.add( minutesTF );
        panelBuilder.setColumn( 11 );
        minutesLabel = panelBuilder.addLabel( Localizer.getString( "SecurityRuleDialog_Minutes" ) );
        
        return timePanel;
    }
    
    private void initAfterExpiryDateContent( ExpiryDate expiryDate )
    {
        long time = expiryDate.getTime();
        long currentTime = System.currentTimeMillis();
        long timeDiff = time - currentTime;
        int days;
        int hours;
        int minutes;
        if ( timeDiff <= 0 )
        {
            days = 0;
            hours = 0;
            minutes = 0;
        }
        else
        {
            days = (int)Math.floor( timeDiff / (double)DateUtils.MILLIS_PER_DAY );
            timeDiff -= days * DateUtils.MILLIS_PER_DAY;
            hours = (int)Math.floor( timeDiff / (double)DateUtils.MILLIS_PER_HOUR );
            timeDiff -= hours * DateUtils.MILLIS_PER_HOUR;
            minutes = (int)Math.ceil( timeDiff / (double)DateUtils.MILLIS_PER_MINUTE );
        }
        daysTF.setText( String.valueOf( days ) );
        hoursTF.setText( String.valueOf( hours ) );
        minutesTF.setText( String.valueOf( minutes ) );
    }
    
    private void refreshExpiryDisplayState()
    {
        if ( Localizer.getString( "SecurityRuleDialog_Never" ).equals(
            expiresCBox.getSelectedItem() ) )
        {
            isDeletedOnExpiryCkbx.setEnabled( false );
            daysTF.setEnabled( false );
            daysLabel.setEnabled( false );
            hoursTF.setEnabled( false );
            hoursLabel.setEnabled( false );
            minutesTF.setEnabled( false );
            minutesLabel.setEnabled( false );
        }
        else if ( Localizer.getString( "SecurityRuleDialog_EndOfSession" ).equals(
            expiresCBox.getSelectedItem() ) )
        {
            isDeletedOnExpiryCkbx.setEnabled( true );
            daysTF.setEnabled( false );
            daysLabel.setEnabled( false );
            hoursTF.setEnabled( false );
            hoursLabel.setEnabled( false );
            minutesTF.setEnabled( false );
            minutesLabel.setEnabled( false );
        }
        else if ( (Localizer.getString( "SecurityRuleDialog_After" ) ).equals(
            expiresCBox.getSelectedItem() ) )
        {
            isDeletedOnExpiryCkbx.setEnabled( true );
            daysTF.setEnabled( true );
            daysLabel.setEnabled( true );
            hoursTF.setEnabled( true );
            hoursLabel.setEnabled( true );
            minutesTF.setEnabled( true );
            minutesLabel.setEnabled( true );
        }
    }
    
    private void validateAndSaveSecurityRule()
    {
        String description = descriptionTF.getText();
        
        Servent servent = GUIRegistry.getInstance().getServent();
        PhexSecurityManager securityMgr = servent.getSecurityService();

        byte[] ip = ipTF.getIP();
        byte cidr = cidrTF.getIntegerValue().byteValue();
        boolean isDisabled = disableRuleCkBx.isSelected();
        ExpiryDate expiryDate;
        switch ( expiresCBox.getSelectedIndex() )
        {
            // never
            case 0:
                expiryDate = ExpiryDate.NEVER_EXPIRY_DATE;
                break;
            // end of session
            case 1:
                expiryDate = ExpiryDate.SESSION_EXPIRY_DATE;
                break;
            // after
            case 2:
                expiryDate = createAfterExpiryDate();
                break;
            default:
                throw new RuntimeException( "Unknown expiry type: " +
                    expiresCBox.getSelectedIndex() );
        }
        boolean isDeletedOnExpiry = isDeletedOnExpiryCkbx.isSelected();
        if ( securityRule == null )
        {
            securityMgr.createIPAccessRule( description, 
                ip, cidr, isDisabled, expiryDate, isDeletedOnExpiry );
        }
        else
        {
            securityMgr.updateIpUserSecurityRule( 
                securityRule, description, ip, cidr, isDisabled, expiryDate, 
                isDeletedOnExpiry );
        }
    }
    
    private ExpiryDate createAfterExpiryDate()
    {
        Integer days = daysTF.getIntegerValue();
        if ( days == null )
        {
            days = Integer.valueOf( 0 );
        }
        Integer hours = hoursTF.getIntegerValue();
        if ( hours == null )
        {
            hours = Integer.valueOf( 0 );
        }
        Integer minutes = minutesTF.getIntegerValue();
        if ( minutes == null )
        {
            minutes = Integer.valueOf( 0 );
        }

        long currentTime = System.currentTimeMillis();
        currentTime += days.intValue() * DateUtils.MILLIS_PER_DAY
            + hours.intValue() * DateUtils.MILLIS_PER_HOUR
            + minutes.intValue() * DateUtils.MILLIS_PER_MINUTE;
        return ExpiryDate.getExpiryDate( currentTime );
    }

    private final class OkBtnListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                validateAndSaveSecurityRule();
                closeDialog();
            }
            catch ( Throwable th )
            {
                NLogger.error( OkBtnListener.class, th, th );
            }
        }
    }
    
    private final class CloseEventHandler extends WindowAdapter implements ActionListener
    {
        @Override
        public void windowClosing(WindowEvent evt)
        {
            closeDialog();
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            closeDialog();
        }
    }
}