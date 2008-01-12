// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: FileUtils.java,v 1.19 2004-09-24 22:39:58 taylor Exp $
//
// Utilities for JFileChooser 
//
// Will Taylor -- started 08mar02
//

package gov.nasa.arc.planworks.db.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.util.PwSQLFilenameFilter;
import gov.nasa.arc.planworks.util.DirectoryChooser;

/**
 * FileUtils - 
 *
 * @version 0.0
 * @author Will Taylor NASA Ames Research Center, Code IC
 */
public abstract class FileUtils {

  /**
   * getExtension - get the extension of a file.
   *
   * @param file - File -
   * @return extension - String 
   */
  public static String getExtension(final File file) {
    String fileName = file.getName();
    int i = fileName.lastIndexOf( '.');
    if (i > 0 && i < fileName.length() - 1) {
      return fileName.substring( i + 1).toLowerCase();
    } else {
      return null;
    }
  } // end getExtension


  /**
   * <code>getCanonicalPath</code>
   *
   * @param relativePath - <code>String</code> - 
   * @return - <code>String</code> - 
   */
  public static String getCanonicalPath(final String relativePath) {
    File file = new File( relativePath);
    try {
      String canonicalPath = file.getCanonicalPath();
      // System.err.println( "getCanonicalPath: relativePath " + relativePath +
      //                     " canonicalPath " + canonicalPath);
      return canonicalPath;
    } catch (IOException excep) {
      System.err.println( "canonicalPath failed for " + relativePath + ": " + excep);
      System.exit( 0);
    }
    return "";
  } // end canonicalPath

  /**
   * <code>validateMultiSequenceDirectory</code>
   *
   * @param sequenceDirChooser - <code>DirectoryChooser</code> - 
   */
  public static void validateMultiSequenceDirectory(final DirectoryChooser sequenceDirChooser) {
    String currentSelectedDir = sequenceDirChooser.getCurrentDirectory().
      getAbsolutePath();
    File [] selectedFiles = sequenceDirChooser.getSelectedFiles();
    if (selectedFiles.length == 0) {
      // invalid dir -- it will be caught by validateSequenceDirectory
      return;
    } else if (selectedFiles.length > 1) {
      // this is a user multi-choice selection - it will be handled by askSequenceDirectory
      return;
    } else {
      // this is one dir -- it may be a single sequence dir or the parent dir of multiple
      // sequence dirs
      String firstSelectedPath = currentSelectedDir + System.getProperty( "file.separator") +
        selectedFiles[0].getName();
      if (validateSequenceDirectory( firstSelectedPath) == null) {
        //  single sequence dir
        return;
      } else {
        // determine if child dirs are each sequence dirs
        List seqDirs  = new ArrayList();
        String [] fileNames = new File( firstSelectedPath).list();
        if ((fileNames == null) || fileNames.length == 0) {
          // No files or directories in directory -- will be caught by validateSequenceDirectory
          return ;
        }
        int seenFilesCnt = 0, seenDisCnt = 0;
        for (int i = 0; i < fileNames.length; i++) {
          String fileName = fileNames[i];
          if (fileName.equals( "CVS")) {
            // do nothing
          } else {
            if ((new File( firstSelectedPath + System.getProperty( "file.separator") +
                           fileName)).isDirectory()) {
              // System.err.println( "Sequence multi dir " + sequenceMultiDirectory +
              //                    " => seqDirName: " + fileName);
              seqDirs.add( fileName);
            } else {
              seenFilesCnt++;
            }
          }
        }
        if (seenFilesCnt == 0) {
          // parent dir of multiple sequence dirs
          sequenceDirChooser.setCurrentDirectory( new File( firstSelectedPath));
          File [] seqDirArray = new File [seqDirs.size()];
          for (int i = 0, n = seqDirs.size(); i < n; i++) {
            seqDirArray[i] = new File( (String) seqDirs.get( i));
          }
          sequenceDirChooser.setSelectedFiles( seqDirArray);
        }
      }
    }
  } // end validateMultiSequenceDirectory

  /**
   * <code>validateSequenceDirectory</code>
   *
   * @param sequenceDirectory - <code>String</code> - 
   * @return - <code>String</code> - 
   */
  public static String validateSequenceDirectory(final String sequenceDirectory) {
    String msg = null;
    File seqDir = new File( sequenceDirectory);
    if (! seqDir.exists()) {
      msg = sequenceDirectory + "\n    Directory does not exist.";
      System.err.println( msg);
      return msg;
    }
    // determine sequence's partial plan directories
    List partialPlanDirs = new ArrayList();
    String [] fileNames = seqDir.list();
    if(fileNames.length == 0) {
      msg = sequenceDirectory + "\n    No files or directories in directory.";
      System.err.println( msg);
      return msg;
    }
    // System.err.println( "validateSequenceDirectory: sequenceDirectory '" +
    //                      sequenceDirectory + "' numFiles " + fileNames.length);
    int seenSequenceFiles = 0;
    for (int i = 0; i < fileNames.length; i++) {
      String fileName = fileNames[i];
      if ((! fileName.equals( "CVS")) &&
          (new File( sequenceDirectory + System.getProperty( "file.separator") +
                     fileName)).isDirectory()) {
        // System.err.println( "Sequence " + sequenceDirectory +
        //                    " => partialPlanDirName: " + fileName);
        partialPlanDirs.add( fileName);
      }
      for(int j = 0; j < DbConstants.NUMBER_OF_SEQ_FILES; j++) {
        if(fileName.equals(DbConstants.SEQUENCE_FILES[j])) {
          seenSequenceFiles++;
        }
      }
    }
    if (seenSequenceFiles != DbConstants.NUMBER_OF_SEQ_FILES) {
      msg = sequenceDirectory + "\n    " + seenSequenceFiles +
        " sequence files in directory -- " + DbConstants.NUMBER_OF_SEQ_FILES + " are required.";
      System.err.println( msg);
      return msg;
    }
    // allow sequences with no step directories
//     if (partialPlanDirs.size() == 0) {
//       msg = sequenceDirectory + "\n    No partial plans in directory.";
//       System.err.println( msg);
//       return msg;
//     }
    // determine existence of the N SQL-input files in partial plan directories (steps)
    // commented out since it is slow for large sequences, and this check is done
    // when each step is loaded
//     for (int i = 0, n = partialPlanDirs.size(); i < n; i++) {
//       String partialPlanPath = sequenceDirectory + System.getProperty( "file.separator") +
//         partialPlanDirs.get( i);
//       fileNames = new File(partialPlanPath).list(new PwSQLFilenameFilter());
//       if (fileNames.length != DbConstants.NUMBER_OF_PP_FILES) { 
//          msg = partialPlanPath + "\n    Has " + fileNames.length +
//            " files -- " + DbConstants.NUMBER_OF_PP_FILES + " are required.";
//         System.err.println( msg);
//         return msg;
//       }
//     }
    return msg;
  } // end validateSequenceDirectory

  /**
   * <code>deleteDir</code> - http://www.javaalmanac.com/egs/java.io/DeleteDir.html
   *
   * Deletes all files and subdirectories under dir.
   * Returns true if all deletions were successful.
   * If a deletion fails, the method stops attempting to delete and returns false.
   *
   * @param dir - <code>File</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean deleteDir( File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir( new File( dir, children[i]));
        if (! success) {
          return false;
        }
      }
    }
    // The directory is now empty so delete it
    return dir.delete();
  } // end deleteDir


} // class FileUtils


