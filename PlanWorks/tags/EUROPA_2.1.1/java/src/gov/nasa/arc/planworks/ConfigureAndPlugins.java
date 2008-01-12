// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConfigureAndPlugins.java,v 1.14 2005-11-24 00:50:19 miatauro Exp $
//
// PlanWorks
//
// Will Taylor -- started 13july04
//

package gov.nasa.arc.planworks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.List;
import java.util.Map;
import javax.swing.JMenu;

import gov.nasa.arc.planworks.util.JarClassLoader;


/**
 * <code>ConfigureAndPlugins</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *             NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConfigureAndPlugins {

  public static final String CONFIG_FILE_EXT = "config";
  public static final String PROJECT_NAME = "projectName";
  public static final String DEFAULT_PROJECT_NAME = "default";
  public static final String PROJECT_WORKING_DIR = "projectWorkingDir";
  public static final String PROJECT_PLANNER_PATH = "projectPlannerPath";
  public static final String PROJECT_MODEL_NAME = "projectModelName";
  public static final String PROJECT_MODEL_PATH = "projectModelPath";
  public static final String PROJECT_PLANNER_CONFIG_PATH = "projectPlannerConfigPath";
  public static final String PROJECT_MODEL_INIT_STATE_PATH = "projectModelInitStatePath";
  public static final String PROJECT_MODEL_OUTPUT_DEST_DIR = "projectModelOutputDestDir";
  public static final String PROJECT_MODEL_RULE_DELIMITERS = "projectModelRuleDelimiters";
    public static final String PROJECT_HEURISTICS_PATH = "projectHeuristicsPath";
    public static final String PROJECT_SOURCE_PATH = "projectSourcePaths";

  public static final String PLANNER_LIB_NAME_MATCH = ".so"; 
  public static final String PLANNER_CONTROL_JNI_LIB = "libPlannerControlJNI.so"; 
  public static final String MACOSX_PLANNER_LIB_NAME_MATCH = ".dylib"; 
  public static final String MACOSX_PLANNER_CONTROL_JNI_LIB = "libPlannerControlJNI.dylib"; 
  public static List PROJECT_CONFIG_PARAMS;
  public static List PROJECT_PATH_DIR_CONFIG_PARAMS;

  private static final String CONFIGURE_CLASS_NAME = "gov.nasa.arc.planworks.ConfigureAndPlugins";
  private static final String MAP_PLUG_IN_METHOD_NAME = "mapPlugIn";
  private static final String MAP_VIEW_CLASS_NAME = "mapViewClass";
  private static final String MAP_RIGHT_CLICK_FOR_VIEW = "mapRightClickForView";

  public static Map PLUG_IN_JAR_MAP;
  public static Map PLUG_IN_LOADER_MAP;
  public static Map PLUG_IN_CLASS_MAP;

  private static List pluginConfigPathList;
  private static List pluginNameList;


  static {
    PROJECT_CONFIG_PARAMS = new ArrayList();
    PROJECT_CONFIG_PARAMS.add( PROJECT_WORKING_DIR);
    PROJECT_CONFIG_PARAMS.add( PROJECT_PLANNER_PATH);
    PROJECT_CONFIG_PARAMS.add( PROJECT_MODEL_NAME);
    PROJECT_CONFIG_PARAMS.add( PROJECT_MODEL_PATH);
    PROJECT_CONFIG_PARAMS.add( PROJECT_MODEL_INIT_STATE_PATH);
    PROJECT_CONFIG_PARAMS.add( PROJECT_MODEL_OUTPUT_DEST_DIR);
    PROJECT_CONFIG_PARAMS.add( PROJECT_MODEL_RULE_DELIMITERS);
    PROJECT_CONFIG_PARAMS.add(PROJECT_PLANNER_CONFIG_PATH);
    PROJECT_CONFIG_PARAMS.add(PROJECT_HEURISTICS_PATH);
    PROJECT_CONFIG_PARAMS.add(PROJECT_SOURCE_PATH);

    PROJECT_PATH_DIR_CONFIG_PARAMS = new ArrayList( PROJECT_CONFIG_PARAMS);
    int indx = PROJECT_PATH_DIR_CONFIG_PARAMS.indexOf( PROJECT_MODEL_NAME);
    PROJECT_PATH_DIR_CONFIG_PARAMS.remove( indx);
    indx = PROJECT_PATH_DIR_CONFIG_PARAMS.indexOf( PROJECT_MODEL_RULE_DELIMITERS);
    PROJECT_PATH_DIR_CONFIG_PARAMS.remove( indx);
    indx = PROJECT_PATH_DIR_CONFIG_PARAMS.indexOf(PROJECT_SOURCE_PATH);
    PROJECT_PATH_DIR_CONFIG_PARAMS.remove(indx);

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
      System.err.println( "Processing PlanWorks configure file: " +
                          configFile.getCanonicalPath());
    } catch (IOException ioExcep) {
    }
    processPlanWorksConfigFileDoit( configFile, "PlanWorks");

    Iterator configNameItr = pluginNameList.iterator();
    Iterator configPathItr = pluginConfigPathList.iterator();
    while (configPathItr.hasNext()) {
      File anotherConfigFile = new File( (String) configPathItr.next());
      String anotherAppName = (String) configNameItr.next();
      System.err.println( "Processing " + anotherAppName + " configure file: " +
                          anotherConfigFile);
      processPlanWorksConfigFileDoit( anotherConfigFile, anotherAppName);
    }
  } // end processPlanWorksConfigFile

  private static void processPlanWorksConfigFileDoit( File configFile, String applicationName) {
    try {
      BufferedReader in = new BufferedReader( new FileReader( configFile));
      String line = null;
      Class configClass = Class.forName( CONFIGURE_CLASS_NAME);
      String methodName = null;
      Class [] argTypes = null;
      Object [] args = null;
      List liveTokens = null;
      while ((line = in.readLine()) != null) {
        // System.err.println( configFile.getName() + ": '" + line + "'");
        String uncommentedLine = line.split( "//", 2)[0];
        // System.err.println( "uncommentedLine '" + uncommentedLine + "'");
        if (uncommentedLine.length() == 0) {
          continue;
        }
        String [] tokens = uncommentedLine.split( "\\s+"); // one or more spaces or a tab
        liveTokens = new ArrayList();
        for (int i = 0; i < tokens.length; i++) {
          String token = (String) tokens[i];
          // System.err.println( "liveToken '" + token + "'");
          liveTokens.add( token);
        }
        if (liveTokens.size() > 1) {
          methodName = (String) liveTokens.get( 0);
          if (methodName.equals( MAP_PLUG_IN_METHOD_NAME)) {
            argTypes = new Class [] { Class.forName( "java.lang.String"),
                                      Class.forName( "java.lang.String") };
            args = new String [] { (String) liveTokens.get( 1), (String) liveTokens.get( 2) };
          } else if (methodName.equals( MAP_VIEW_CLASS_NAME)) {
            argTypes = new Class [] { Class.forName( "java.lang.String"),
                                      Class.forName( "java.lang.String") };
            Object [] returnArray = collectQuotedNameParts( liveTokens);
            String viewName = (String) returnArray[0];
            int numTokensProcessed = ((Integer) returnArray[1]).intValue();
            liveTokens = liveTokens.subList( numTokensProcessed, liveTokens.size());
            args = new String [] { viewName, (String) liveTokens.get( 0)};
          } else if (methodName.equals( MAP_RIGHT_CLICK_FOR_VIEW)) {
            argTypes = new Class [] { Class.forName( "java.lang.String"),
                                      Class.forName( "java.lang.String"),
                                      Class.forName( "java.lang.String"),
                                      Class.forName( "java.lang.String") };
            Object [] returnArray = collectQuotedNameParts( liveTokens);
            String viewName = (String) returnArray[0];
            int numTokensProcessed = ((Integer) returnArray[1]).intValue();
            liveTokens = liveTokens.subList( numTokensProcessed, liveTokens.size());
            returnArray = collectQuotedNameParts( liveTokens);
            String itemName = (String) returnArray[0];
            numTokensProcessed = ((Integer) returnArray[1]).intValue();
            liveTokens = liveTokens.subList( numTokensProcessed, liveTokens.size());
            args = new String [] { viewName, itemName, (String) liveTokens.get( 0),
                                   (String) liveTokens.get( 1)};


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
  } // end processPlanWorksConfigFileDoit

  private static Object [] collectQuotedNameParts( List liveTokens) {
    Object [] returnArray = new Object [2];
    int numTokensProcessed = 0;
    StringBuffer viewName = new StringBuffer( "");
    Iterator liveTokensItr = liveTokens.iterator();
    boolean isQuotedName = false;
    while (liveTokensItr.hasNext()) {
      String token = (String) liveTokensItr.next();
      numTokensProcessed++;
      if (token.charAt( 0) == '\"') {
        isQuotedName = true;
        viewName.append( token.substring( 1)).append( " ");
      } else if (isQuotedName && (token.charAt( token.length() - 1) == '\"')) {
        viewName.append( token.substring( 0, token.length() - 1));
        break;
      } else if (isQuotedName) {
        viewName.append( token).append( " ");
      }
    }
    returnArray[0] = viewName.toString();
    returnArray[1] = new Integer( numTokensProcessed);
    return returnArray;
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
   * <code>mapRightClickForView</code>
   *
   * @param viewName - <code>String</code> - 
   * @param itemName - <code>String</code> - 
   * @param itemClassName - <code>String</code> - 
   * @param itemMethodName - <code>String</code> - 
   */
  public static void mapRightClickForView( String viewName, String itemName, String itemClassName,
                                           String itemMethodName) {
    // System.err.println( "mapRightClickForView: '" + viewName + "' '" + itemName +
    //                     "' '" + itemClassName + "' '" + itemMethodName);
    List rightClickList = new ArrayList();
    rightClickList.add( itemClassName); 
    rightClickList.add( itemMethodName);
    PlanWorks.VIEW_MOUSE_RIGHT_MAP.put( viewName + ":" + itemName, rightClickList);
  } // end mapRightClickForView

  /**
   * <code>invokeLoadPlugin</code>
   *
   * @param pluginMenuItem - <code>JMenuItem</code> - 
   */
  public static void invokeLoadPlugin( JMenu pluginMenu) {
    boolean resolveIt = true;
    try { 
      Iterator pluginNameItr = PLUG_IN_JAR_MAP.keySet().iterator();
      while (pluginNameItr.hasNext()) {
        String pluginName = (String) pluginNameItr.next();
        JarClassLoader classLoader = (JarClassLoader) PLUG_IN_LOADER_MAP.get( pluginName);
        String pluginClassName = (String) PLUG_IN_CLASS_MAP.get( pluginName);
        Object [] args = new Object [] { pluginMenu };
        Class classObject = classLoader.loadClassAndResolveIt( pluginClassName);
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


  /**
   * <code>processProjectsConfigFile</code>
   *
   * @param configFile - <code>File</code> - 
   */
  public static void processProjectsConfigFile( File configFile) {
    try {
      System.err.println( "Processing Projects configure file: " +
                          configFile.getCanonicalPath());
    } catch (IOException ioExcep) {
    }

    PlanWorks.PROJECT_CONFIG_MAP = processProjectsConfigFileImpl( configFile);
  } // end processProjectsConfigFile

  private static Map processProjectsConfigFileImpl( File configFile) {
    Map configMap = new HashMap();
    try {
      BufferedReader in = new BufferedReader( new FileReader( configFile));
      List liveTokens = null, nameValueList = null;
      String fieldName = "", fieldValue = "", projectName = "";
      String line = null;
      boolean isDefaultProjectFound = false;
      while ((line = in.readLine()) != null) {
        // System.err.println( configFile.getName() + ": '" + line + "'");
        String uncommentedLine = line.split( "//", 2)[0];
        // System.err.println( "uncommentedLine '" + uncommentedLine + "'");
        if (uncommentedLine.length() == 0) {
          continue;
        }
        String [] tokens = uncommentedLine.split( "\\s+"); // one or more spaces or a tab
        liveTokens = new ArrayList();
        for (int i = 0; i < tokens.length; i++) {
          String token = (String) tokens[i];
          //System.err.println( "liveToken '" + token + "'");
          liveTokens.add( token);
        }
        // process name arg pairs
        if (liveTokens.size() > 1) {
          fieldName = (String) liveTokens.get( 0);
          fieldValue = (String) liveTokens.get( 1);
          // System.err.println( "fieldName '" + fieldName + "'");
          // System.err.println( "fieldValue '" + fieldValue + "'");
          if (fieldName.equals( PROJECT_NAME)) {
            if (nameValueList != null) {
              configMap.put( projectName, nameValueList);
            }
            projectName = fieldValue;
            nameValueList = new ArrayList();
            if (fieldValue.equals( DEFAULT_PROJECT_NAME)) {
              isDefaultProjectFound = true;
            }
          } else {
            nameValueList.add( fieldName);
            nameValueList.add( fieldValue);
          }
        }
      }
      in.close();
      configMap.put( projectName, nameValueList);
      if (! isDefaultProjectFound) {
        System.err.println( "processProjectsConfigFileImpl: default project entry (" +
                            "projectName=" + DEFAULT_PROJECT_NAME + ") not found");
        System.exit( -1);
      }
    } catch (FileNotFoundException fnfExcep) {
      System.err.println( "projects configure file '" + configFile + "' not found");
      System.exit( -1);
    } catch (IOException ioExcep) {
      ioExcep.printStackTrace();
      System.exit( -1);
    }
    return configMap;
  } // end processProjectsConfigFileImpl

  /**
   * <code>isProjectInConfigMap</code>
   *
   * @param projectName - <code>String</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean isProjectInConfigMap( String projectName) {
    return (PlanWorks.PROJECT_CONFIG_MAP.get( projectName) != null);
  }

  /**
   * <code>getProjectConfigValue</code>
   *
   * @param configName - <code>String</code> - 
   * @param projectName - <code>String</code> - 
   * @return - <code>String</code> - 
   */
  public static String getProjectConfigValue( String configName, String projectName) {
    //     System.err.println( "getProjectConfigValue: configName '" + configName +
    //                         "' projectName '" + projectName + "'");
    List nameValueList = (List) PlanWorks.PROJECT_CONFIG_MAP.get( projectName);
    List defaultNameValueList = null;
    if (nameValueList == null) {
      // not yet set for this project -- use default
      System.err.println( "getProjectConfigValue: projectName " + projectName +
                          " has no entry in PROJECT_CONFIG_MAP, using projectName " +
                          DEFAULT_PROJECT_NAME);
      nameValueList = (List) PlanWorks.PROJECT_CONFIG_MAP.get( DEFAULT_PROJECT_NAME);
    }
    String retval = getProjectConfigValue( configName, nameValueList);
    if (retval == null) {
      defaultNameValueList = (List) PlanWorks.PROJECT_CONFIG_MAP.get( DEFAULT_PROJECT_NAME);
      retval = getProjectConfigValue( configName, defaultNameValueList);
      if (retval == null) {
        // not in project default of projects.config  -- check in projects.config.template
        Map templateConfigMap =
          processProjectsConfigFileImpl( new File( System.getProperty( "projects.config") +
                                                   ".template"));
        nameValueList = (List) templateConfigMap.get( DEFAULT_PROJECT_NAME);
        retval = getProjectConfigValue( configName, nameValueList);
        if (retval == null) {
          System.err.println( "getProjectConfigValue: config name " + configName +
                              " not found for projectName " + projectName);
          return null;
        } else {
          // put it in project default of projects.config
          defaultNameValueList.add( configName);
          defaultNameValueList.add( retval);
          updateProjectConfigMap( DEFAULT_PROJECT_NAME, defaultNameValueList);
          return retval;
        }
      } else {
        return retval;
     }
    } else {
      return retval;
    }
  } // end getProjectConfigValue

  private static String getProjectConfigValue( String configName, List nameValueList) {
    Iterator nameValueItr = nameValueList.iterator();
    while (nameValueItr.hasNext()) {
      String name = (String) nameValueItr.next();
      if (name.equals( configName)) {
        if (PROJECT_PATH_DIR_CONFIG_PARAMS.contains( name)) {
          try {
            return new File( (String) nameValueItr.next()).getCanonicalPath();
          } catch (IOException ioExcep) {
          }
        } else {
          return (String) nameValueItr.next();
        }
      } else {
         nameValueItr.next();
      }
    }
    return null;
  } // end getProjectConfigValue

  /**
   * <code>completeProjectConfigMap</code>
   *
   * @param nameValueList - <code>List</code> - 
   * @return - <code>List</code> - 
   */
  public static List completeProjectConfigMap(List nameValueList) {
    String plannerPath = null, modelPath = null, modelOutputDestDir = null;
    String modelInitStatePath = null, ruleDelimiters = null;
    String plannerConfigPath = null;
    if (nameValueList.indexOf(PROJECT_PLANNER_PATH) == -1) {
        nameValueList.add( PROJECT_PLANNER_PATH);
        plannerPath = getProjectConfigValue( PROJECT_PLANNER_PATH, DEFAULT_PROJECT_NAME);
        nameValueList.add( plannerPath);
    }
    if (nameValueList.indexOf(PROJECT_MODEL_PATH) == -1) {
        nameValueList.add( PROJECT_MODEL_PATH);
        modelPath = getProjectConfigValue( PROJECT_MODEL_PATH, DEFAULT_PROJECT_NAME);
        nameValueList.add( modelPath);
    }
    if (nameValueList.indexOf(PROJECT_MODEL_OUTPUT_DEST_DIR) == -1) {
        nameValueList.add( PROJECT_MODEL_OUTPUT_DEST_DIR);
        modelOutputDestDir  = getProjectConfigValue( PROJECT_MODEL_OUTPUT_DEST_DIR,
                                                     DEFAULT_PROJECT_NAME);
        nameValueList.add( modelOutputDestDir);
    }
    if (nameValueList.indexOf(PROJECT_MODEL_INIT_STATE_PATH) == -1) {
        nameValueList.add( PROJECT_MODEL_INIT_STATE_PATH);
        modelInitStatePath = getProjectConfigValue( PROJECT_MODEL_INIT_STATE_PATH,
                                                    DEFAULT_PROJECT_NAME);
        nameValueList.add( modelInitStatePath);
    }
    if (nameValueList.indexOf(PROJECT_MODEL_RULE_DELIMITERS) == -1) {
        nameValueList.add( PROJECT_MODEL_RULE_DELIMITERS);
        ruleDelimiters = getProjectConfigValue
          ( PROJECT_MODEL_RULE_DELIMITERS,
            DEFAULT_PROJECT_NAME);
        nameValueList.add( ruleDelimiters);
    }
    if(nameValueList.indexOf(PROJECT_PLANNER_CONFIG_PATH) == -1) {
      nameValueList.add(PROJECT_PLANNER_CONFIG_PATH);
      plannerConfigPath = getProjectConfigValue(PROJECT_PLANNER_CONFIG_PATH,
                                                DEFAULT_PROJECT_NAME);
      nameValueList.add(plannerConfigPath);
    }
    return nameValueList;
  } // end completeProjectConfigMap

  /**
   * <code>updateProjectConfigMap</code>
   *
   * @param projectName - <code>String</code> - 
   * @param nameValueList - <code>List</code> - 
   */
  public static void updateProjectConfigMap( String projectName, List nameValueList) {
//     if (PlanWorks.PROJECT_CONFIG_MAP.get( projectName) != null) {
//       System.err.println("updateProjectConfigMap curr nameValueList len " + projectName + " " +
//                          ((List) PlanWorks.PROJECT_CONFIG_MAP.get( projectName)).size() +
//                          " new len " + nameValueList.size());
//     }
    Iterator nameValueItr = nameValueList.iterator();
    while (nameValueItr.hasNext()) {
      String configName = (String) nameValueItr.next();
      if (PROJECT_CONFIG_PARAMS.contains( configName)) {
        nameValueItr.next();
      } else {
        System.err.println( "updateProjectConfigMap: config name " + configName +
                            " not handled");
        return;
      }
    }
    PlanWorks.PROJECT_CONFIG_MAP.put( projectName, nameValueList);
    writeProjectConfigMap();
  } // end updateProjectConfigMap

  /**
   * <code>writeProjectConfigMap</code>
   *
   */
  public static void writeProjectConfigMap() {
    File configFile = new File( System.getProperty( "projects.config"));
    try {
      System.err.println( "Writing Projects configure file: " + configFile.getCanonicalPath());
    } catch (IOException ioExcep) {
    }
    try {
      BufferedWriter out = new BufferedWriter( new FileWriter( configFile));
      String line = null;
      List keyList = new ArrayList( PlanWorks.PROJECT_CONFIG_MAP.keySet());
      Collections.sort( keyList);
      Iterator keyListItr = keyList.iterator();
      while (keyListItr.hasNext()) {
        out.write( "// ======== project configuration ========");
        out.newLine();
        String key = (String) keyListItr.next();
        List nameValueList = (List) PlanWorks.PROJECT_CONFIG_MAP.get( key);
        line = PROJECT_NAME + " " + key;
        out.write( line);
        out.newLine();
        Iterator nameValueItr = nameValueList.iterator();
        while (nameValueItr.hasNext()) {
          String name = (String) nameValueItr.next();
          String value = (String) nameValueItr.next();
          if (PROJECT_PATH_DIR_CONFIG_PARAMS.contains( name) && value.length() > 0) {
	      //System.err.println("Getting canonical path for '" + value + "'");
            try {
              value = new File( value).getCanonicalPath();
            } catch (IOException ioExcep) {
            }
          }
          line = name + " " + value;
	  //System.err.println("Writing '" + line + "'");
          out.write( line);
          out.newLine();
        }
      }
      out.close();
    } catch (IOException ioExcep) {
      ioExcep.printStackTrace();
      System.exit( -1);
    }
  } // end writeProjectConfigMap

} // end class ConfigureAndPlugins

        







