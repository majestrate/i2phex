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
 *  $Id: FileRescanRunner.java 4365 2009-01-16 11:21:31Z gregork $
 */
package phex.share;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import phex.common.ThreadTracking;
import phex.common.log.NLogger;
import phex.prefs.core.LibraryPrefs;
import phex.xml.sax.share.DSharedFile;
import phex.xml.sax.share.DSharedLibrary;


public class FileRescanRunner implements Runnable
{
    /**
     * if this thread is alive a rescan is running.
     */
    private static Thread rescanThread;
    
    /**
     * Locks access to the rescanThread to prevent threads sleeping in between
     * rescan thread interruption and restart... basically only needed to 
     * sync with the subscription list downloader. 
     */
    private static final Object threadLock = new Object();
    
    private SharedFilesService sharedFilesService;
    
    /**
     * 
     */
    private ArrayList<String> sharedDirectoryFiles;
        
    private List<Pattern> exclusionPatterns;
    
    /**
     * In between storage for shared directories.
     */
    private HashMap<File, SharedDirectory> sharedDirectoryMap;
    private HashSet<SharedDirectory> sharedDirectoryList;
    
    private boolean isInitialRescan;
    private HashMap<String, DSharedFile> sharedFilesCache;

    private FileRescanRunner( SharedFilesService sharedFilesSerivce, 
        boolean isInitialRescan )
    {
        this.isInitialRescan = isInitialRescan;
        this.sharedFilesService = sharedFilesSerivce;
        exclusionPatterns = new ArrayList<Pattern>();
        sharedDirectoryMap = new HashMap<File, SharedDirectory>();
        sharedDirectoryList = new HashSet<SharedDirectory>( 5 );
    }
    
    /**
     * Rescans the shared files. Locking is done in the called methods to
     * have gaps between a rescanning session.
     * You can specify if this is a initial rescan or not. On a initial
     * rescan all SharedFiles are dropped and the stored shared files info are
     * loaded to access urn info.
     */
    public static void rescan( SharedFilesService sharedFilesSerivce,
        boolean isInitialRescan, boolean allowInterrupt )
    {
        synchronized( threadLock )
        {
            if ( allowInterrupt && rescanThread != null && 
                !rescanThread.isInterrupted() && rescanThread.isAlive() )
            {
                NLogger.debug( FileRescanRunner.class,
                    "Interrupting rescan thread." );
                // interrupt running thread to restart rescan...
                rescanThread.interrupt();
                // join interrupted thread and wait till its dead before
                // rescan starts
                try
                {
                    NLogger.debug( FileRescanRunner.class,
                        "Waiting for interrupted rescan thread." );
                    rescanThread.join();
                }
                catch ( InterruptedException exp )
                {
                    NLogger.warn( FileRescanRunner.class, exp, exp );
                }
            }
            if ( rescanThread == null || !rescanThread.isAlive() )
            {
                FileRescanRunner runner = new FileRescanRunner( sharedFilesSerivce,
                    isInitialRescan );
                rescanThread = new Thread( ThreadTracking.rootThreadGroup, runner,
                    "FileRescanRunner-" + Integer.toHexString( runner.hashCode() ) );
                rescanThread.setDaemon( true );
                rescanThread.setPriority( Thread.MIN_PRIORITY );
                rescanThread.start();
            }
        }
    }
    
    /**
     * Syncs an external thread with this rescan thread. The call will return
     * after any pending rescan operations are finished. Though you can't be sure
     * that a rescan might gets triggered shortly after returning from this 
     * method, it helps to sync on the initial rescan during startup.
     */
    public static void sync()
    {
        synchronized( threadLock )
        {
            if ( rescanThread != null && rescanThread.isAlive() )
            {
                try
                {
                    NLogger.debug( FileRescanRunner.class,
                        "Waiting for running rescan thread." );
                    rescanThread.join();
                }
                catch ( InterruptedException exp )
                {
                    NLogger.warn( FileRescanRunner.class, exp, exp );
                }
            }
        }
    }

    public void run()
    {
        NLogger.debug( FileRescanRunner.class,
            "Staring file rescan (Initial: " + isInitialRescan + ")." );

        sharedDirectoryFiles = new ArrayList<String>( LibraryPrefs.SharedDirectoriesSet.get() );
        setExclusionFilter( LibraryPrefs.LibraryExclusionRegExList.get() );

        if ( rescanThread.isInterrupted() )
        {
            return;
        }

        if ( isInitialRescan )
        {
            sharedFilesService.clearSharedFiles();
            if ( rescanThread.isInterrupted() )
            {
                return;
            }
            buildSharedFilesCache();
        }
        else
        {
            removeUnsharedFiles();
        }
        if ( rescanThread.isInterrupted() )
        {
            return;
        }

        try
        {
            sharedFilesService.setCalculationRunnerPause( true );
            HashMap<String, String> scannedDirMap = new HashMap<String, String>();

            for ( String dirStr : sharedDirectoryFiles )
            {
                File dir = new File( dirStr );
                scanDir( dir, scannedDirMap );
                if ( rescanThread.isInterrupted() )
                {
                    return;
                }
            }
            sharedFilesService.updateSharedDirecotries( sharedDirectoryMap, 
                sharedDirectoryList );
            sharedFilesService.triggerSaveSharedFiles();
        }
        finally
        {
            sharedFilesService.setCalculationRunnerPause(false);
        }
    }

    private void buildSharedFilesCache()
    {
        sharedFilesCache = new HashMap<String, DSharedFile>();
        DSharedLibrary library = sharedFilesService.loadSharedLibrary();
        if ( library == null )
        {
            // no library found to load...
            return;
        }
        Iterator<DSharedFile> iterator = library.getSubElementList().iterator();
        while ( iterator.hasNext() && !rescanThread.isInterrupted() )
        {
            DSharedFile cachedFile = iterator.next();
            sharedFilesCache.put( cachedFile.getFileName(), cachedFile );
        }
    }

    /**
     * Scans a directory for files to share.
     * @param dir the directory to scan.
     * @param scannedDirs the directories already scanned. This is used to
     *        keep it from scanning continuously through of symbolically
     *        linked directories in Unix systems. See SF bug #603736
     * @param recursive whether we scan this directory recursive or not
     */
    private void scanDir( File dir, HashMap<String, String> scannedDirMap )
    {
        // verify if dir was already scanned.
        String canonicalPath;
        try
        {
            canonicalPath = dir.getCanonicalPath();
        }
        catch ( IOException exp )
        {
            NLogger.warn( FileRescanRunner.class, exp, exp );
            return;
        }
        if ( scannedDirMap.containsKey( canonicalPath ) )
        {// directory was already scanned...
            return;
        }

        // not scanned... now add it as scanned...
        scannedDirMap.put( canonicalPath, "" );
        
        if ( !dir.exists() )
        {
            return;
        }

        if ( dir.isDirectory() )
        {
            handleScannedDir( dir );
        }

        File[] files = dir.listFiles();

        if ( files == null )
        {
            NLogger.error( FileRescanRunner.class, 
                "'" + dir + "' is not a directory." );
            return;
        }

        for (int j = 0; j < files.length && !rescanThread.isInterrupted(); j++)
        {
            if ( isFileInvalid( files[j] ) )
            {
                continue;
            }

            if ( files[j].isFile() )
            {
                handleScannedFile( files[j] );
            }
            // not recursive
            //else if ( files[j].isDirectory() )
            //{
            //    scanDir( files[j], scannedDirMap );
            //}
        }
    }
    
    private void handleScannedDir( File file )
    {
        if ( rescanThread.isInterrupted() )
        {
            return;
        }
        SharedDirectory sharedDirectory = sharedDirectoryMap.get(file);
        if ( sharedDirectory == null )
        {
            sharedDirectory = new SharedDirectory( file );
            sharedDirectory.setType(SharedDirectory.SHARED_DIRECTORY);
            sharedDirectoryMap.put(file, sharedDirectory);
            sharedDirectoryList.add(sharedDirectory);
        }
        else
        {
            sharedDirectory.setType(SharedDirectory.SHARED_DIRECTORY);
        }
        
        // set parents to partially shared.
        File parent = file.getParentFile();
        while( parent != null )
        {
            if ( parent.isDirectory() )
            {
                sharedDirectory = sharedDirectoryMap.get(parent);
                if ( sharedDirectory == null )
                {
                    sharedDirectory = new SharedDirectory( parent );
                    sharedDirectory.setType(SharedDirectory.UNSHARED_PARENT_DIRECTORY);
                    sharedDirectoryMap.put(parent, sharedDirectory);
                    sharedDirectoryList.add(sharedDirectory);
                }                
            }
            parent = parent.getParentFile();
        }
    }

    private void handleScannedFile( File file )
    {
        ShareFile shareFile;
        if ( isInitialRescan )
        {
            shareFile = new ShareFile( file );
            // Try to find cached file info
            DSharedFile dFile = sharedFilesCache.get( file.getAbsolutePath() );
            if ( dFile != null &&
                 dFile.getLastModified() == file.lastModified() )
            {
                shareFile.updateFromCache( dFile );
                // add the urn to the map to share by urn
                sharedFilesService.addUrn2FileMapping(shareFile);
            }
            else
            {
                sharedFilesService.queueUrnCalculation( shareFile );
                if ( rescanThread.isInterrupted() )
                {
                    return;
                }
            }
            sharedFilesService.addSharedFile( shareFile );
        }
        else
        {
            // try to find file in already existing share
            shareFile = sharedFilesService.getFileByName( file.getAbsolutePath() );
            if ( shareFile == null )
            {// create new file
                shareFile = new ShareFile( file );
                sharedFilesService.queueUrnCalculation(shareFile);
                if ( rescanThread.isInterrupted() )
                {
                    return;
                }
                sharedFilesService.addSharedFile( shareFile );
            }
        }
    }

    private void removeUnsharedFiles()
    {
        List<ShareFile> sharedFiles = sharedFilesService.getSharedFiles();
        int size = sharedFiles.size();
        for (int i = 0; i < size && !rescanThread.isInterrupted(); i++)
        {
            ShareFile shareFile = sharedFiles.get( i );
            File file = shareFile.getSystemFile();
            if (!isInSharedDirectory(file) || !file.exists())
            {
                sharedFilesService.removeSharedFile( shareFile );
            }
        }
    }

    private boolean isInSharedDirectory( File file )
    {
        for( String dir : sharedDirectoryFiles )
        {
            File sharedDirFile = new File( dir );
            if ( file.getParentFile().equals(sharedDirFile) )
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Called when rescanning files
     */
    private void setSharedDirectories( String dirs )
    {
        StringTokenizer tokens = new StringTokenizer(dirs, ";");
        int count = tokens.countTokens();

        sharedDirectoryFiles.clear();
        sharedDirectoryFiles.ensureCapacity(count);

        while (tokens.hasMoreTokens())
        {
            String dir = tokens.nextToken().trim();
            if (!sharedDirectoryFiles.contains(dir))
            {
                sharedDirectoryFiles.add(dir);
            }
        }
    }
    
    private void setExclusionFilter( List<String> exclusionList )
    {
        exclusionPatterns.clear();
        for ( String regExp : exclusionList )
        {
            try
            {
                Pattern pattern = Pattern.compile( regExp );
                exclusionPatterns.add(pattern);
            }
            catch ( PatternSyntaxException exp )
            {
                NLogger.error( FileRescanRunner.class, exp, exp );
            }
        }
    }
    
    /**
     * In case the user is sharing the download directory, skip the
     * download-in-progress files, index files and alias files. Even though
     * the user should not be able to configure the download directory as shared
     * directory since the new option dialog.
     */
    public boolean isFileInvalid(File file)
    {
        // In case the user is sharing the download directory,
        // skip the download-in-progress files.
        if ( file.getName().toLowerCase().endsWith(".dl") )
        {
            return true;
        }
        if ( isExcludedRegExp( file ) )
        {
            return true;
        }
        return false;
    }
    
    /**
     * To match all = .*
     * To match none = (?!pagefile\.sys).*
     * @param file
     * @return
     */
    private boolean isExcludedRegExp( File file )
    {
        // use filter only for files not directories
        if ( file.isDirectory() )
        {
            return true;
        }

        String name = file.getName();
        for( Pattern pattern : exclusionPatterns )
        {
            Matcher m = pattern.matcher( name );
            if ( m.matches() )
            {
                return true;
            }
        }
        return false;
    }
}