// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: FileUtils.java,v 1.11 2003-11-25 01:40:38 taylor Exp $
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

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.util.PwSQLFilenameFilter;

/**
 * FileUtils - 
 *
 * @version 0.0
 * @author Will Taylor NASA Ames Research Center, Code IC
 */
public class FileUtils {

  /**
   * getExtension - get the extension of a file.
   *
   * @param file - File -
   * @return extension - String 
   */
  public static String getExtension( File file) {
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
  public static String getCanonicalPath( String relativePath) {
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
   * <code>validateSequenceDirectory</code>
   *
   * @param sequenceDirectory - <code>String</code> - 
   * @return - <code>String</code> - 
   */
  public static String validateSequenceDirectory( String sequenceDirectory) {
    // determine sequence's partial plan directories
    List partialPlanDirs = new ArrayList();
    String [] fileNames = new File( sequenceDirectory).list();
    String msg = null;
    if(fileNames.length == 0) {
      msg = sequenceDirectory + "\n\n    No files in directory.";
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
      if(fileName.equals(DbConstants.SEQ_PP_STATS) || fileName.equals(DbConstants.SEQ_FILE) ||
         fileName.equals(DbConstants.SEQ_TRANSACTIONS)) {
        seenSequenceFiles++;
      }
    }
    if (seenSequenceFiles != DbConstants.NUMBER_OF_SEQ_FILES) {
      msg = sequenceDirectory + "\n\n    " + seenSequenceFiles +
        " sequence files in directory -- " + DbConstants.NUMBER_OF_SEQ_FILES +
        " are required.";
      System.err.println( msg);
      return msg;
    }
    if (partialPlanDirs.size() == 0) {
      msg = sequenceDirectory + "\n\n    No partial plans in directory.";
      System.err.println( msg);
      return msg;
    }
    // determine existence of the N SQL-input files in partial plan directories (steps)
    for (int i = 0, n = partialPlanDirs.size(); i < n; i++) {
      String partialPlanPath = sequenceDirectory + System.getProperty( "file.separator") +
        partialPlanDirs.get( i);
      fileNames = new File(partialPlanPath).list(new PwSQLFilenameFilter());
      if (fileNames.length != DbConstants.NUMBER_OF_PP_FILES) { 
         msg = partialPlanPath + "\n\n    Has " + fileNames.length +
           " files -- " + DbConstants.NUMBER_OF_PP_FILES + " are required.";
        System.err.println( msg);
        return msg;
      }
    }
    return msg;
  } // end validateSequenceDirectory

} // class FileUtils


