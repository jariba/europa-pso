// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: FileUtils.java,v 1.2 2003-05-15 18:38:45 taylor Exp $
//
// Utilities for JFileChooser 
//
// Will Taylor -- started 08mar02
//

package gov.nasa.arc.planworks.db.util;

import java.io.File;
import java.io.IOException;

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


} // class FileUtils


