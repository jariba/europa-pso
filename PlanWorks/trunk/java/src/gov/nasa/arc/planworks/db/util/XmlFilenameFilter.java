// $Id: XmlFilenameFilter.java,v 1.1 2003-05-10 01:00:33 taylor Exp $
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
   * constant <code>PLAN_EXTENSION</code> - String -
   *
   */
  public static final String XML_EXTENSION = "xml";

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
    return "Partial Plan XML Output Files (*.xml)";
  }
} // end class XmlFilenameFilter



