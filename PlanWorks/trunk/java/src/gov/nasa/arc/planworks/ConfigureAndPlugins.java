// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConfigureAndPlugins.java,v 1.2 2004-07-16 22:54:44 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 13july04
//

package gov.nasa.arc.planworks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JMenu;

import gov.nasa.arc.planworks.util.JarClassLoader;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;


/**
 * <code>ConfigureAndPlugins</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *             NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConfigureAndPlugins {

  public static final String CONFIG_FILE_EXT = "config";

  private static final String CONFIGURE_CLASS_NAME = "gov.nasa.arc.planworks.ConfigureAndPlugins";
  private static final String MAP_PLUG_IN_METHOD_NAME = "mapPlugIn";
  private static final String MAP_VIEW_CLASS_METHOD_NAME = "mapViewClass";

  public static Map PLUG_IN_JAR_MAP;
  public static Map PLUG_IN_LOADER_MAP;
  public static Map PLUG_IN_CLASS_MAP;

  private static List pluginConfigPathList;
  private static List pluginNameList;


  static {
    PLUG_IN_JAR_MAP = new HashMap();
    PLUG_IN_LOADER_MAP = new HashMap();
    PLUG_IN_CLASS_MAP = new HashMap();
    pluginConfigPathList = new ArrayList();
    pluginNameList = new ArrayList();
  }

  private ConfigureAndPlugins() {
  }

  public static void processPlanWorksConfigFile( File configFile) {
    try {
      System.err.println( "Processing PlaWorks configure file: " +
                          configFile.getCanonicalPath());
    } catch (IOException ioExcep) {
    }
    processConfigFile( configFile, "PlanWorks");

    Iterator configNameItr = pluginNameList.iterator();
    Iterator configPathItr = pluginConfigPathList.iterator();
    while (configPathItr.hasNext()) {
      processConfigFile( new File( (String) configPathItr.next()),
                         (String) configNameItr.next());
    }
  } // end processPlanWorksConfigFile

  private static void processConfigFile( File configFile, String applicationName) {
    try {
      BufferedReader in = new BufferedReader( new FileReader( configFile));
      String line = null;
      Class configClass = Class.forName( CONFIGURE_CLASS_NAME);
      String methodName = null;
      Class [] argTypes = null;
      Object [] args = null;
      while ((line = in.readLine()) != null) {
//         System.err.println( configFile.getName() + ": " + line);
        String [] tokens = line.split( "\\s+"); // one or more spaces or a tab
        List liveTokens = new ArrayList();
        for (int i = 0; i < tokens.length; i++) {
          String token = (String) tokens[i];
          if (token.equals( "//")) { // comment
            break;
          }
          liveTokens.add( token);
        }
        if (liveTokens.size() > 1) {
          methodName = (String) liveTokens.get( 0);
          if (methodName.equals( MAP_PLUG_IN_METHOD_NAME)) {
            argTypes = new Class [] { Class.forName( "java.lang.String"),
                                      Class.forName( "java.lang.String") };
            args = new String [] { (String) liveTokens.get( 1), (String) liveTokens.get( 2) };
          } else if (methodName.equals( MAP_VIEW_CLASS_METHOD_NAME)) {
            argTypes = new Class [] { Class.forName( "java.lang.String"),
                                      Class.forName( "java.lang.String") };
            String viewName = collectViewNameParts( liveTokens);
            args = new String [] { viewName, (String) liveTokens.get( liveTokens.size() - 1)};



          } else {
            System.err.println( "configure operation '" + methodName + "' not supported");
            System.exit( -1);
          }
          Method evalMethod = configClass.getMethod( methodName, argTypes);
          evalMethod.invoke( configClass, args);
        }
      }
    } catch (FileNotFoundException fnfExcep) {
      System.err.println( applicationName + " configure file '" + configFile + "' not found");
      System.exit( -1);
    } catch (IOException ioExcep) {
      ioExcep.printStackTrace();
      System.exit( -1);
    } catch (ClassNotFoundException cnfExcep) {
      cnfExcep.printStackTrace();
      System.exit( -1);
    } catch (NoSuchMethodException nsmeExcep) {
      nsmeExcep.printStackTrace();
      System.exit( -1);
    } catch (IllegalAccessException iaeExcep) {
      iaeExcep.printStackTrace();
      System.exit( -1);
    } catch (InvocationTargetException iteExcep) {
      iteExcep.printStackTrace();
      System.exit( -1);
    }
  } // end processConfigFile

  private static String collectViewNameParts( List liveTokens) {
    StringBuffer viewName = new StringBuffer( "");
    Iterator liveTokensItr = liveTokens.iterator();
    boolean isViewName = false;
    while (liveTokensItr.hasNext()) {
      String token = (String) liveTokensItr.next();
      if (token.charAt( 0) == '\"') {
        isViewName = true;
        viewName.append( token.substring( 1)).append( " ");
      } else if (isViewName && (token.charAt( token.length() - 1) == '\"')) {
        viewName.append( token.substring( 0, token.length() - 1));
        break;
      } else if (isViewName) {
        viewName.append( token).append( " ");
      }
    }
    return viewName.toString();
  } // end collectViewNameParts

  /**
   * <code>mapPlugIn</code>
   *
   * @param plugInName - <code>String</code> - 
   * @param jarFilePath - <code>String</code> - 
   */
  public static void mapPlugIn( String plugInName, String jarFilePath) {
    // System.err.println( "mapPlugIn: " + plugInName + " " + jarFilePath);
    PLUG_IN_JAR_MAP.put( plugInName, jarFilePath);
    try {
      File jarFilePathFile = new File( jarFilePath);
      JarFile jarFile = new JarFile( jarFilePathFile);
      Attributes attributes = jarFile.getManifest().getMainAttributes();
      String pluginClassName = attributes.getValue( "Plugin-Class");
      PLUG_IN_CLASS_MAP.put( plugInName, pluginClassName);
      // System.err.println( "JarFile Manifest Plugin-Class: " + pluginClassName);

      JarClassLoader classLoader = new JarClassLoader( new URL( "file://" + jarFilePath));
      PLUG_IN_LOADER_MAP.put( plugInName, classLoader);

      // save plugin config file
      String jarFileName = jarFilePathFile.getName();
      int index = jarFileName.indexOf( ".");
      String pluginConfigPath = jarFilePathFile.getParent() +
        System.getProperty( "file.separator") + jarFileName.substring( 0, index) +
        "." + CONFIG_FILE_EXT;
      // System.err.println( "pluginConfigPath " + pluginConfigPath);
      pluginNameList.add ( plugInName);
      pluginConfigPathList.add( pluginConfigPath);

    } catch (IOException ioExcep) {
      ioExcep.printStackTrace();
      System.exit( -1);
    }
  } // end mapPlugIn

  /**
   * <code>mapViewClass</code>
   *
   * @param viewName - <code>String</code> - 
   * @param viewClassName - <code>String</code> - 
   */
  public static void mapViewClass( String viewName, String viewClassName) {
    // System.err.println( "mapViewClass: " + viewName + " " + viewClassName);
    PlanWorks.VIEW_CLASS_NAME_MAP.put( viewName, viewClassName);
    PlanWorks.PARTIAL_PLAN_VIEW_LIST.add( viewName);
  } // end mapViewClass

  /**
   * <code>invokeLoadPlugin</code>
   *
   * @param pluginMenuItem - <code>JMenuItem</code> - 
   */
  public static void invokeLoadPlugin( JMenu pluginMenu) {
    try { 
      Iterator pluginNameItr = PLUG_IN_JAR_MAP.keySet().iterator();
      while (pluginNameItr.hasNext()) {
        String pluginName = (String) pluginNameItr.next();
        JarClassLoader classLoader = (JarClassLoader) PLUG_IN_LOADER_MAP.get( pluginName);
        String pluginClassName = (String) PLUG_IN_CLASS_MAP.get( pluginName);
        Object [] args = new Object [] { pluginMenu };
        Class classObject = classLoader.loadClass( pluginClassName);
        // System.err.println( "classObject name " + classObject.getName());
        Method method =
          classObject.getMethod( "loadPlugin",
                                 new Class[] { Class.forName( "javax.swing.JMenu") });
	method.setAccessible( true);
        try {
          method.invoke( null, args); // static method
        } catch (IllegalAccessException e) {
          // This should not happen, as we have disabled access checks
        } 
      }
    } catch (ClassNotFoundException cnfExcep) {
      cnfExcep.printStackTrace();
      System.exit( -1);
    } catch (NoSuchMethodException nsmExcep) {
      nsmExcep.printStackTrace();
      System.exit( -1);
    } catch (InvocationTargetException itExcep) {
      itExcep.printStackTrace();
      System.exit( -1);
    }
  } // end invokeLoadPlugin




} // end class ConfigureAndPlugins








