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
 *  $Id: PhexEventTopics.java 4362 2009-01-16 10:27:18Z gregork $
 */
package phex.event;

public interface PhexEventTopics
{
    public String Net_Favorites = "phex:net/favorites";
    public String Net_Hosts = "phex:net/hosts";
    public String Net_ConnectionStatus = "phex:net/connectionStatus";
    
    public String Download_File = "phex:download/file";
    public String Download_File_Completed = "phex:download/file/completed";
    public String Download_Candidate = "phex:download/candidate";
    public String Download_Candidate_Status = "phex:download/candidate/status";
    
    public String Upload_State = "phex:upload/state";
    public String Share_Update = "phex:share/update";
     
    public String Incoming_Uri = "phex:incoming/uri";
    public String Incoming_Magma = "phex:incoming/magma";
    public String Incoming_Rss = "phex:incoming/rss";
    
    public String Search_Update = "phex:search/update";
    public String Search_Data = "phex:search/data";
    public String Search_Monitor_Results = "phex:search/monitor/results";
    
    public String Query_Monitor = "phex:query/monitor";
    
    public String Chat_Update = "phex:chat/update";
    
    public String Security_Rule = "phex:security/rule";
    
    public String Servent_GnutellaNetwork = "phex:servent/gnutellaNetwork";
    public String Servent_OnlineStatus = "phex:servent/onlineStatus";
    public String Servent_LocalAddress = "phex:servent/localAddress";
}