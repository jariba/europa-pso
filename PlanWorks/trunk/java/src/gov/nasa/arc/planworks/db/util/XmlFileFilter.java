// $Id: XmlFileFilter.java,v 1.1 2003-05-10 01:00:33 taylor Exp $
//
// JFileChooser filter for XML files
//
// Will Taylor -- started 08mar02
//

package gov.nasa.arc.planworks.db.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

  /**
   * XmlFileFilter - extends FileFilter to find planner files
   * with extension "xml"
   *
   * @version 0.0
   * @author Will Taylor NASA Ames Research Center, Code IC
   */
public class XmlFileFilter extends FileFilter {

  /**
   * constant <code>PLAN_EXTENSION</code> - String -
   *
   */
  public static final String XML_EXTENSION = "xml";

  /**
   * XmlFileFilter - constructor
   *
   */
  public XmlFileFilter() {
    super();
  }

  /**
   * accept - Accept all directories and all .xml files.
   *          implemented method of abstract class FileFilter 
   * @param file - a directory or file name
   * @return true, if a directory or file extension is ".xml"
   *         false, otherwise
   */
  public boolean accept( File file) {
    return (file.isDirectory() ||
            XML_EXTENSION.equals( FileUtils.getExtension( file)));
  }

  /**
   * getDescription - string to describe this filter
   *          implemented method of abstract class FileFilter 
   * @return string to describe this filter
   */
  public String getDescription() {
    return "Planner XML Output Files (*.xml)";
  }
} // end class XmlFileFilter

