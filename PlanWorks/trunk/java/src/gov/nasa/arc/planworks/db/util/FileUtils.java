// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: FileUtils.java,v 1.7 2003-09-11 23:41:12 miatauro Exp $
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
   * @return - <code>boolean</code> - 
   */
  public static boolean validateSequenceDirectory( String sequenceDirectory) {
    // determine sequence's partial plan directories
    List partialPlanDirs = new ArrayList();
    String [] fileNames = new File( sequenceDirectory).list();
//     System.err.println( "validateSequenceDirectory: sequenceDirectory '" +
//                         sequenceDirectory + "' numFiles " + fileNames.length);
    if(fileNames == null) {
      return false;
    }
    for (int i = 0; i < fileNames.length; i++) {
      String fileName = fileNames[i];
      if ((! fileName.equals( "CVS")) &&
          (new File( sequenceDirectory + System.getProperty( "file.separator") +
                     fileName)).isDirectory()) {
//         System.err.println( "Sequence " + sequenceDirectory +
//                            " => partialPlanDirName: " + fileName);
        partialPlanDirs.add( fileName);
      }
    }
    if (partialPlanDirs.size() == 0) {
//       System.err.println( "partialPlanDirs.size() == 0");
      return false;
    }
    // determine existence of the 14 SQL-input files in partial plan directories (steps)
    for (int i = 0, n = partialPlanDirs.size(); i < n; i++) {
      String partialPlanPath = sequenceDirectory + System.getProperty( "file.separator") +
        partialPlanDirs.get( i);
      fileNames = new File(partialPlanPath).list(new PwSQLFilenameFilter());
//       System.err.println( "partialPlanPath " + partialPlanPath + " numFiles " +
//                           fileNames.length);
      if(fileNames.length != DbConstants.NUMBER_OF_PP_FILES) {
        return false;
      }
    }
    return true;
  } // end validateSequenceDirectory

} // class FileUtils


