// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: XmlFilenameFilter.java,v 1.3 2003-06-11 01:02:13 taylor Exp $
//
// JFileChooser filter for XML files
//
// Will Taylor -- started 08mar02
//

package gov.nasa.arc.planworks.db.util;

import java.io.File;
import java.io.FilenameFilter;

  /**
   * XmlFilenameFilter - implements FilenameFilter to find planner files
   * with extension "xml"
   *
   * @version 0.0
   * @author Will Taylor NASA Ames Research Center, Code IC
   */
public class XmlFilenameFilter implements FilenameFilter {

  /**
   * XmlFilenameFilter - constructor
   *
   */
  public XmlFilenameFilter() {
    super();
  }
  
  /**
   * accept - Accept all directories and all .xml files.
   *          implemented method of abstract class FileFilter 
   * @param file - a directory 
   * @param name - a file name
   * @return true, if file is a directory, and name is a file whose extension is ".xml";
   *         false, otherwise
   */
    public boolean accept( File file, String name) {
      // System.err.println( "accept: filename " + name);
      File filename = new File( name);
      return (file.isDirectory() && (! filename.isDirectory()) &&
              XmlFileFilter.XML_EXTENSION.equals
              ( FileUtils.getExtension( filename)));
    }

  /**
   * getDescription - string to describe this filter
   *          implemented method of abstract class FileFilter 
   * @return string to describe this filter
   */
  public String getDescription() {
    return "Partial Plan XML Output Files (*" + XmlFileFilter.XML_EXTENSION_W_DOT + ")";
  }
} // end class XmlFilenameFilter



