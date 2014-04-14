/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2005 Phex Development Group
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
 *  Created on 27.07.2007
 */
package phex.prefs.core;

import java.util.List;

import org.apache.commons.lang.SystemUtils;

import phex.prefs.api.PreferencesFactory;
import phex.prefs.api.Setting;

public class I2PPrefs extends PhexCorePrefs
{
    /**
     * Default inbound tunnel length.
     * Default outbound tunnel length.
     * Default inbound tunnel length variance.
     * Default outbound tunnel length variance.
     * Default inbound tunnel quantity.
     * Default outbound tunnel quantity.
     * Default inbound tunnel backup quantity.
     * Default outbound tunnel backup quantity.
     * Default inbound destination nickname.
     * Default outbound destination nickname.
     * Default I2CP host.
     * Default I2CP port.
     */
    public static final int DEFAULT_INBOUND_LENGTH = 1;
    public static final int DEFAULT_OUTBOUND_LENGTH = 1;
    public static final int DEFAULT_INBOUND_LENGTH_VARIANCE = 1;
    public static final int DEFAULT_OUTBOUND_LENGTH_VARIANCE = 1;
    public static final int DEFAULT_INBOUND_QUANTITY = 2;
    public static final int DEFAULT_OUTBOUND_QUANTITY = 2;
    public static final int DEFAULT_INBOUND_BACKUP_QUANTITY = 0;
    public static final int DEFAULT_OUTBOUND_BACKUP_QUANTITY = 0;
    public static final String DEFAULT_INBOUND_NICKNAME = "i2phex";
    public static final String DEFAULT_OUTBOUND_NICKNAME = "i2phex";
    public static final String DEFAULT_I2CP_HOST = "127.0.0.1";
    public static final int DEFAULT_I2CP_PORT = 7654;
    
    /**
     * Inbound tunnel length.
     * Outbound tunnel length.
     * Inbound tunnel length variance.
     * Outbound tunnel length variance.
     * Inbound tunnel quantity.
     * Outbound tunnel quantity.
     * Inbound tunnel backup quantity.
     * Outbound tunnel backup quantity.
     * Inbound destination nickname.
     * Outbound destination nickname.
     * I2CP host.
     * I2CP port.
     */
    public static final Setting<Integer> InboundLength;
    public static final Setting<Integer> OutboundLength;
    public static final Setting<Integer> InboundLengthVariance;
    public static final Setting<Integer> OutboundLengthVariance;
    public static final Setting<Integer> InboundQuantity;
    public static final Setting<Integer> OutboundQuantity;
    public static final Setting<Integer> InboundBackupQuantity;
    public static final Setting<Integer> OutboundBackupQuantity;
    public static final Setting<String> InboundNickname;
    public static final Setting<String> OutboundNickname;
    public static final Setting<String> I2CPHost;
    public static final Setting<Integer> I2CPPort;
    
    static
    {
        InboundLength = PreferencesFactory.createIntSetting(
            "I2P.InboundLength", DEFAULT_INBOUND_LENGTH, instance );
        OutboundLength = PreferencesFactory.createIntSetting(
            "I2P.OutboundLength", DEFAULT_OUTBOUND_LENGTH, instance );
        InboundLengthVariance = PreferencesFactory.createIntSetting(
            "I2P.InboundLengthVariance", DEFAULT_INBOUND_LENGTH_VARIANCE, instance );
        OutboundLengthVariance = PreferencesFactory.createIntSetting(
            "I2P.OutboundLengthVariance", DEFAULT_OUTBOUND_LENGTH_VARIANCE, instance );
        InboundQuantity = PreferencesFactory.createIntSetting(
            "I2P.InboundQuantity", DEFAULT_INBOUND_QUANTITY, instance );
        OutboundQuantity = PreferencesFactory.createIntSetting(
            "I2P.OutboundQuantity", DEFAULT_OUTBOUND_QUANTITY, instance );
        InboundBackupQuantity = PreferencesFactory.createIntSetting(
            "I2P.InboundBackupQuantity", DEFAULT_INBOUND_BACKUP_QUANTITY, instance );
        OutboundBackupQuantity = PreferencesFactory.createIntSetting(
            "I2P.OutboundBackupQuantity", DEFAULT_OUTBOUND_BACKUP_QUANTITY, instance );
        InboundNickname = PreferencesFactory.createStringSetting(
            "I2P.InboundNickname", DEFAULT_INBOUND_NICKNAME, instance );
        OutboundNickname = PreferencesFactory.createStringSetting(
            "I2P.OutboundNickname", DEFAULT_OUTBOUND_NICKNAME, instance );
        I2CPHost = PreferencesFactory.createStringSetting(
            "I2P.I2CPHost", DEFAULT_I2CP_HOST, instance );
        I2CPPort = PreferencesFactory.createIntSetting(
            "I2P.I2CPPort", DEFAULT_I2CP_PORT, instance );
    }
}
