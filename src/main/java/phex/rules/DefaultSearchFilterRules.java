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
 *  Created on 16.11.2005
 *  --- CVS Information ---
 *  $Id: DefaultSearchFilterRules.java 4485 2009-09-21 06:56:10Z ArneBab $
 */
package phex.rules;

import phex.rules.condition.FilenameCondition;
import phex.rules.consequence.FilterFromSearchConsequence;
import phex.rules.consequence.RemoveFromSearchConsequence;
import phex.utils.Localizer;

public class DefaultSearchFilterRules
{
    /**
     * filters adult-files by name
     */
    public static final Rule ADULT_FILTER_RULE;
    
    /**
     * filters file-types mostly used for scam
     */
    public static final Rule SCAM_FILE_FILTER_RULE; 
    
    /**
     * filters known Spam-Files
     */
    public static final Rule SPAM_FILE_FILTER_RULE; 
    
    /**
     * filters really nasty files. 
     */
    public static final Rule NASTY_FILE_FILTER_RULE; 
    
    static
    {
        Rule adultRule = new Rule();
        adultRule.setName( Localizer.getString( "AdultFilterRule_Name" ) );
        adultRule.setId( "PhexAdult" );
        FilenameCondition adultcondition = new FilenameCondition( );
        adultcondition.addTerm( "anal" ).addTerm( "asshole" )
                 .addTerm( "blowjob" ).addTerm( "bondage" )
                 .addTerm( "cock" ).addTerm( "cum" )
                 .addTerm( "cunt" ).addTerm( "facial" )
                 .addTerm( "gangbang" ).addTerm( "hentai" )
                 .addTerm( "incest" ).addTerm( "masturbat" )
                 .addTerm( "penis" ).addTerm( "porn" )
                 .addTerm( "rape" ).addTerm( "slut" )
                 .addTerm( "vagina" ).addTerm( "whore" )
                 .addTerm( "inzest" ).addTerm( "fick" )
                 .addTerm( "xxx" ).addTerm( "fotze" )
                 .addTerm( "schwanz" ).addTerm( "pedo" )
                 .addTerm( "fisting" ).addTerm( "schlampe" )
                 .addTerm( "milfhunter" ).addTerm( "sex" )
                 .addTerm( "dildo" ).addTerm( "execution" )
                 .addTerm( "amateur" ).addTerm( "topless" )
                 .addTerm( "naughty" ).addTerm( "adultery" )
                 .addTerm( "pussy" ).addTerm( "fuck" )
                 .addTerm( "uncensored" ).addTerm( "tits" )
                 .addTerm( "nudist" ).addTerm( "threesome" )
                 .addTerm( "arse" ).addTerm( "booty" )
                 .addTerm( "adult" ).addTerm( "deep throat" )
                 .addTerm( "femdom" ).addTerm( "piss" )
                 .addTerm( "jerk off" ).addTerm( "jerks off" )
                 .addTerm( "nudity" ).addTerm( "suck" )
                 .addTerm( "barely legal" ).addTerm( "shemale" );
        adultRule.addCondition( adultcondition );
        adultRule.addConsequence( FilterFromSearchConsequence.INSTANCE );
        adultRule.setPermanentlyEnabled( false );
        adultRule.setDefaultRule( true );
        ADULT_FILTER_RULE = adultRule;
	
        Rule noscamRule = new Rule();
        noscamRule.setName( Localizer.getString( "ScamFileFilterRule_Name" ) ); 
        noscamRule.setId( "PhexNoScam" ); 
        FilenameCondition scamcondition = new FilenameCondition( ); 
        scamcondition.addTerm( ".asx" ).addTerm( ".wmv" )
            .addTerm( ".wma" ).addTerm( ".asf" ).addTerm(".wvx")
            .addTerm( ".ram" ).addTerm( ".mov" ).addTerm( ".wm" ); 
        noscamRule.addCondition( scamcondition ); 
        noscamRule.addConsequence( FilterFromSearchConsequence.INSTANCE ); 
        noscamRule.setPermanentlyEnabled( false ); 
        noscamRule.setDefaultRule( true ); 
        SCAM_FILE_FILTER_RULE = noscamRule; 
	
        Rule nospamRule = new Rule();
        nospamRule.setName( Localizer.getString( "SpamFileFilterRule_Name" ) ); 
        nospamRule.setId( "PhexNoSpam" ); 
        FilenameCondition spamcondition = new FilenameCondition( ); 
        spamcondition.addTerm( "buylegalmp3.com" ).addTerm( "efreeclub" )
            .addTerm( "------------" ).addTerm( "ifreeclub" ).addTerm( "XPUSS" ); 
        nospamRule.addCondition( spamcondition ); 
        nospamRule.addConsequence( RemoveFromSearchConsequence.INSTANCE ); 
        nospamRule.setPermanentlyEnabled( true ); 
        nospamRule.setDefaultRule( true ); 
        SPAM_FILE_FILTER_RULE = nospamRule;
         
        // Rule to filter out really nasty files. Thanks to a brave user in Gnutellaforums for gathering these! I wouldn't have wanted to ... *shudder* 
        Rule nonastyRule = new Rule();
        nonastyRule.setName( Localizer.getString( "NastyFileFilterRule_Name" ) ); 
        nonastyRule.setId( "PhexNoNasty" ); 
        FilenameCondition nastycondition = new FilenameCondition( ); 
        nastycondition.addTerm("kiddy").addTerm("hussyfan").addTerm("lolitaguy").addTerm("lolia").addTerm("kingpass").addTerm("knabinoj").addTerm("nabot").addTerm("pedo").addTerm("pedofilia").addTerm("underage").addTerm("kidz").addTerm("12yo").addTerm("6yo").addTerm("ls-magazine").addTerm("ls-land").addTerm("lsmagazine").addTerm("bdcompany").addTerm("childsex").addTerm("childfgga").addTerm("ddogprn").addTerm("lhv").addTerm("lh2").addTerm("lkd").addTerm("lpear").addTerm("lpear").addTerm("lolitacastle").addTerm("llb").addTerm("lsv").addTerm("lsbar").addTerm("liluplnet").addTerm("bibcam").addTerm("r@ygold").addTerm("babyhivid").addTerm("child").addTerm("childlver").addTerm("telarium").addTerm("pthc").addTerm("ptsc").addTerm("preteen").addTerm("schoolchild").addTerm("scholkid").addTerm("kdquality").addTerm("jilbait").addTerm("mafiasex").addTerm("babyj").addTerm("fsa").addTerm("kinderficker").addTerm("qaazz").addTerm("guertin").addTerm("kidy").addTerm("darkfeeling"); 
        nonastyRule.addCondition( nastycondition ); 
        nonastyRule.addConsequence( FilterFromSearchConsequence.INSTANCE ); 
        nonastyRule.setPermanentlyEnabled( true ); 
        nonastyRule.setDefaultRule( true ); 
        NASTY_FILE_FILTER_RULE = nonastyRule; 
    }
}
