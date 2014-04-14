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
 */
package phex.gui.tabs.upload;

import phex.upload.UploadStatus;
import phex.utils.Localizer;

public final class UploadStatusInfo
{
    // dont allow to create instances
    private UploadStatusInfo()
    {
    }

    /**
     * Returns a localized string for the given status of a download file.
     *
     * @param status the status to get the string representation for.
     * @return the status string representation.
     */
    public static String getUploadStatusString( UploadStatus status )
    {
        switch( status )
        {
            case ACCEPTING_REQUEST:
                return Localizer.getString( "UploadStatus_AcceptingRequest" );
            case HANDSHAKE:
                return Localizer.getString( "UploadStatus_Handshake" );
            case QUEUED:
                return Localizer.getString( "UploadStatus_Queued" );
            case UPLOADING_THEX:
                return Localizer.getString( "UploadStatus_UploadingThex" );
            case UPLOADING_DATA:
                return Localizer.getString( "UploadStatus_UploadingData" );
            case COMPLETED:
                return Localizer.getString( "UploadStatus_Completed" );
            case ABORTED:
                return Localizer.getString( "UploadStatus_Aborted" );
            default:
                Object[] arguments = new Object[1];
                arguments[0] = status;
                return Localizer.getFormatedString( "UnrecognizedStatus",
                    arguments );
        }
    }
}