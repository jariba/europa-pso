// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ExistTest.java,v 1.6 2003-06-12 23:49:46 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 06May03
//         derived from skunkworks/planViz/java/src/.../PlanViz.java
//

package gov.nasa.arc.planworks.db.test;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.db.util.XmlDBeXist;
import gov.nasa.arc.planworks.db.util.XmlFilenameFilter;


/**
 * <code>ExistTest</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ExistTest {

  /**
   * constant <code>PLUS_INFINITY</code> - Integer max value
   *
   */
  public static final int PLUS_INFINITY = Integer.MAX_VALUE;
  /**
   * constant <code>MINUS_INFINITY</code> - Integer min value
   *
   */
  public static final int MINUS_INFINITY = Integer.MIN_VALUE;

  // writeTime( int seconds)
  private static final DateFormat SEC_FORMAT =
    new SimpleDateFormat( ":ss");
  private static final DateFormat MIN_SEC_FORMAT =
    new SimpleDateFormat( ":mm:ss");
  private static final DateFormat HR_MIN_SEC_FORMAT =
    new SimpleDateFormat( "HH:mm:ss");
  private static final DateFormat DAY_HR_MIN_SEC_FORMAT =
    new SimpleDateFormat( "DDD:HH:mm:ss");
  // writeTime( long millisecs)
  private static final DateFormat MSEC_MSEC_FORMAT =
    new SimpleDateFormat( "0.SSS");
  private static final DateFormat MSEC_SEC_FORMAT =
    new SimpleDateFormat( "ss.SSS");
  private static final DateFormat MSEC_MIN_SEC_FORMAT =
    new SimpleDateFormat( "mm:ss.SSS");
  private static final DateFormat MSEC_HR_MIN_SEC_FORMAT =
    new SimpleDateFormat( "HH:mm:ss.SSS");
  private static final DateFormat MSEC_DAY_HR_MIN_SEC_FORMAT =
    new SimpleDateFormat( "DDD:HH:mm:ss.SSS");
  private static final int MILLSECS_PER_SECOND = 1000;
  private static final int SECONDS_PER_MINUTE = 60;
  private static final int MINUTES_PER_HOUR = 60;
  private static final int HOURS_PER_DAY = 24;

  private static ExistTest existTest;
  private static String planWorksRoot;
  private static String userName;
  private static String osType;

  // XML XPath - eXist-0.9.1
  private static String xmlFilesDirectory;
  private static String [] xmlFileNames;
  private static String userCollectionName; // e.g. /wtaylor
  private static String collectionName; // e.g. /wtaylor/test/monkey (xml files directory)


  /**
   * <code>ExistTest</code> - constructor 
   *
   */
  public ExistTest() {

    connectToExistDataBase();

    xmlFileNames = new File( xmlFilesDirectory).list( new XmlFilenameFilter());

    collectionName = loadPlannerXmlFiles();

    accessPlannerXmlFiles( collectionName);

  } // end constructor

  private final void connectToExistDataBase() {
    // connect to eXist XPath data base
    long startTimeMSecs = (new Date()).getTime();

    XmlDBeXist.INSTANCE.registerDataBase();
    // create userName collection in data base, if needed
    XmlDBeXist.INSTANCE.createCollection( userCollectionName);

    long stopTimeMSecs = (new Date()).getTime();
    String timeString = "Register Data Base \n   ... elapsed time: " +
      writeTime( (stopTimeMSecs - startTimeMSecs)) + " seconds.";
    System.err.println( timeString);
  } // end connectToExistDataBase


  private String loadPlannerXmlFiles() {
    int index = xmlFilesDirectory.toString().
      lastIndexOf( System.getProperty( "file.separator"));
    if (index == -1) {
      System.err.println( "collectionName cannot be parsed " +
                          xmlFilesDirectory.toString());
      return "";
    }
    index = xmlFilesDirectory.toString().substring( 0, index - 1).
      lastIndexOf( System.getProperty( "file.separator"));
    String collectionName = userCollectionName + xmlFilesDirectory.substring( index);
    System.err.println( "xmlFiles ");
    int count = 0;
    for (int i = 0, n = xmlFileNames.length; i < n; i++) {
      StringBuffer pathStrBuf = new StringBuffer( xmlFilesDirectory);
      pathStrBuf.append( System.getProperty( "file.separator")).
        append( (String) xmlFileNames[i]);
      System.err.println( "Load " + pathStrBuf.toString());
      long startLoadTimeMSecs = (new Date()).getTime();

      XmlDBeXist.INSTANCE.addXMLFileToCollection( collectionName,
                                                  pathStrBuf.toString());

      long stopLoadTimeMSecs = (new Date()).getTime();
      String loadTimeString = "   ... elapsed time: " +
        writeTime( (stopLoadTimeMSecs - startLoadTimeMSecs)) + " seconds.";
      System.err.println( loadTimeString);
      count++;
    }
    return collectionName;
  } // end loadPlannerXmlFiles

  private void accessPlannerXmlFiles( String collectionName) {
    if (collectionName.indexOf( "test") >= 0) {
      List partialPlanKeys = XmlDBeXist.INSTANCE.queryPartialPlanKeys( collectionName);
      Iterator keysIterator = partialPlanKeys.iterator();
      while (keysIterator.hasNext()) {
        System.err.println( "PartialPlanKey: " + (String) keysIterator.next());
      }
    } else {
      System.err.println( "accessPlannerXmlFiles: only 'PlanWorks/xml/test/' allowed");
      System.exit( 0);
    }
  } // end accessPlannerXmlFiles


  /**
   * writeTime (milliSeconds)
   *
   * @param milliSeconds - long - 
   * @return formattedTime - String - (ddd:hh:mm:ss.SS)
   */
  public static String writeTime( long milliSeconds) {
    // format time string with leading zeros
    String formattedTime = "";
    String timeString = "";
    char format = 'S';
    Date date;
    int seconds = (int) milliSeconds / MILLSECS_PER_SECOND;
    int milliSecs = (int) milliSeconds % MILLSECS_PER_SECOND;
    int manyMinutes = seconds / SECONDS_PER_MINUTE;
    int secs = seconds % SECONDS_PER_MINUTE;
    int manyHours = manyMinutes / MINUTES_PER_HOUR;
    int minutes = manyMinutes % MINUTES_PER_HOUR;
    int days = manyHours / HOURS_PER_DAY;
    int hours = manyHours % HOURS_PER_DAY;

//     System.err.println( "days " + days + " hours " + hours + " minutes " +
//                         minutes + " secs " + secs + "milliSecs " + milliSecs);
    if ((seconds == 0) && (minutes == 0) && (hours == 0) && (days == 0)) {
      timeString = "0." + Integer.toString( milliSecs);
      format = 'S';
    } else if ((minutes == 0) && (hours == 0) && (days == 0)) {
      timeString = Integer.toString( secs) + "." + Integer.toString( milliSecs);
      format = 's';
    } else if ((hours == 0) && (days == 0)) {
      timeString = Integer.toString( minutes) + ":" + Integer.toString( secs) +
        "." + Integer.toString( milliSecs);
      format = 'm';
    } else if (days == 0) {
      timeString = Integer.toString( hours) + ":" + Integer.toString( minutes) +
        ":" + Integer.toString( secs) + "." + Integer.toString( milliSecs);
      format = 'h';
    } else {
      timeString = Integer.toString( days) + ":" + Integer.toString( hours) +
        ":" + Integer.toString( minutes) + ":" + Integer.toString( secs) +
        "." + Integer.toString( milliSecs);
      format = 'd';
    }
    try {
      switch (format) {
      case 'S': 
        date = MSEC_MSEC_FORMAT.parse( timeString);
        formattedTime = MSEC_MSEC_FORMAT.format( date);
        break;
      case 's': 
        date = MSEC_SEC_FORMAT.parse( timeString);
        formattedTime = MSEC_SEC_FORMAT.format( date);
        break;
      case 'm': 
        date = MSEC_MIN_SEC_FORMAT.parse( timeString);
        formattedTime = MSEC_MIN_SEC_FORMAT.format( date);
        break;
      case 'h': 
        date = MSEC_HR_MIN_SEC_FORMAT.parse( timeString);
        formattedTime = MSEC_HR_MIN_SEC_FORMAT.format( date);
        break;
      case 'd': 
        date = MSEC_DAY_HR_MIN_SEC_FORMAT.parse( timeString);
        formattedTime = MSEC_DAY_HR_MIN_SEC_FORMAT.format( date);
        break;
      }
    } catch (ParseException pe) {
      System.err.println( "ParseException " + pe);
    }
    return formattedTime;
  } // end writeTime( long milliSeconds)


  private static void processArguments( String[] args) {
    // input args - defaults
    xmlFilesDirectory = "";
    String pathname = "";
    for (int argc = 0; argc < args.length; argc++) {
      System.err.println( "argc " + argc + " " + args[argc]);
      if (argc == 0) {
        // linux | solaris | darwin (MacOSX)
        osType = args[argc];
      } else if (argc == 1) {
        pathname = args[argc];
        if (! pathname.equals( "null")) {
           xmlFilesDirectory = FileUtils.getCanonicalPath( pathname);
          System.err.println( "xmlFilesDirectory: " + xmlFilesDirectory);
        }
      } else {
        System.err.println( "argument '" + args[argc] + "' not handled");
        System.exit( 0);
      }
    }
  } // end processArguments

  
  /**
   * <code>main</code>
   *
   * @param args - <code>String[]</code> - 
   */
  public static void main( String[] args) {

    processArguments( args);

    // planWorksRoot = getEnvVar( "PLANWORKS_ROOT");
    planWorksRoot = System.getProperty( "planworks.root");
    // userName = getEnvVar( "USER");
    userName = System.getProperty( "user");
    userCollectionName = System.getProperty( "file.separator") + userName;

    existTest = new ExistTest();

  } // end main

} // end class ExistTest

