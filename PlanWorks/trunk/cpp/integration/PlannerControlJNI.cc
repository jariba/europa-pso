#include "PlannerControlJNI.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <dlfcn.h>

extern "C"

inline void* &accessPlannerLibHandle() {
  static void* s_plannerLibHandle;
  return s_plannerLibHandle;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
  return JNI_VERSION_1_2;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
}

JNIEXPORT jint JNICALL Java_gov_nasa_arc_planworks_PlannerControlJNI_initPlannerRun 
    (JNIEnv* env, jclass cl, jstring planner_path, jstring model_path, 
                             jstring initial_state_path, jstring dest_path) {

  jint retStatus;
  jclass clazz;
  const char* plannerLibPath;
  const char* modelLibPath;
  const char* initialStatePath;
  const char* destPath;
  void* libHandle;
  const char* error_msg;
  int (*fcn_initModel)(const char*, const char*);

  printf("In Java_gov_nasa_arc_planworks_PlannerControlJNI_initPlannerRun\n");

  /*
   * get full planner and model library names and path to initial state 
   */
  plannerLibPath = env->GetStringUTFChars(planner_path, NULL);
  modelLibPath = env->GetStringUTFChars(model_path, NULL);
  initialStatePath = env->GetStringUTFChars(initial_state_path, NULL);
  destPath = env->GetStringUTFChars(dest_path, NULL);

  printf("Requested planner library file is %s\n", plannerLibPath);
  printf("Requested model library file is %s\n", modelLibPath);
  printf("Requested initial state file is %s\n", initialStatePath);
  printf("Requested destination is %s\n", destPath);
  fflush(stdout);

  //load planner library using full path
  libHandle = dlopen(plannerLibPath, RTLD_LAZY);

  if (!libHandle) {
    error_msg = dlerror();
    printf("Error during dlopen() of %s:\n", plannerLibPath);
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return gov_nasa_arc_planworks_PlannerControlJNI_PLANNER_INITIALLY_INCONSISTANT;
  }
  //save the handle for accessing the planner library later
  accessPlannerLibHandle() = libHandle;

  //locate the 'initModel' function in the library and check for errors
  fcn_initModel = (int (*)(const char*, const char*))dlsym(libHandle, "initModel");
  //printf("Returned from (int (*)(const char*, const char*))dlsym(libHandle, initModel)\n");
  if (!fcn_initModel) {
    error_msg = dlerror();
    printf("dlsym: Error locating initModel:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return gov_nasa_arc_planworks_PlannerControlJNI_PLANNER_INITIALLY_INCONSISTANT;
  }

  // call the initModel function
  try {
    retStatus  = (*fcn_initModel)(modelLibPath, initialStatePath);
    printf("Returned from calling the initModel function\n");
    fflush(stdout);
  }
  catch (...) {
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, "Unexpected exception in initModel()");
    return gov_nasa_arc_planworks_PlannerControlJNI_PLANNER_INITIALLY_INCONSISTANT;
  }

  env->ReleaseStringUTFChars(planner_path, plannerLibPath);
  env->ReleaseStringUTFChars(model_path, modelLibPath);
  env->ReleaseStringUTFChars(initial_state_path, initialStatePath);
  return retStatus;

}

JNIEXPORT jint JNICALL Java_gov_nasa_arc_planworks_PlannerControlJNI_getPlannerStatus (JNIEnv* env, jclass cl) {

  jint retStatus;
  jclass clazz;
  const char* error_msg;
  int (*fcn_getStatus)();
  void* libHandle = accessPlannerLibHandle();
  if (!libHandle) {
    clazz = env->FindClass("java/lang/NullPointerException");
    env->ThrowNew(clazz, "Planner library not loaded");
    return gov_nasa_arc_planworks_PlannerControlJNI_PLANNER_INITIALLY_INCONSISTANT;
  }

  //locate the 'getStatus' function in the library 
  fcn_getStatus = (int (*)())dlsym(libHandle, "getStatus");
  //printf("Returned from (int (*)())dlsym(libHandle, getStatus)\n");
  if (!fcn_getStatus) {
    error_msg = dlerror();
    printf("dlsym: Error locating getStatus:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return gov_nasa_arc_planworks_PlannerControlJNI_PLANNER_INITIALLY_INCONSISTANT;
  }

  // call the getStatus function
  retStatus  = (*fcn_getStatus)();
  printf("Returned from calling the getStatus function\n");
  fflush(stdout);

  return retStatus;
}


JNIEXPORT jint JNICALL Java_gov_nasa_arc_planworks_PlannerControlJNI_writeStep (JNIEnv* env, jclass cl, jint step_num) {

  jint nextPlannerStep;
  jclass clazz;
  const char* error_msg;
  int (*fcn_writeStep)(int);
  void* libHandle = accessPlannerLibHandle();
  if (!libHandle) {
    clazz = env->FindClass("java/lang/NullPointerException");
    env->ThrowNew(clazz, "Planner library not loaded");
    return -1;
  }

  printf("PlannerControlJNI_writeStep %d\n", step_num);
  fflush(stdout);

  //locate the 'writeStep' function in the library 
  fcn_writeStep = (int (*)(int))dlsym(libHandle, "writeStep");
  //printf("Returned from (int (*)(int))dlsym(libHandle, writeStep)\n");
  if (!fcn_writeStep) {
    error_msg = dlerror();
    printf("dlsym: Error locating writeStep:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return -1;
  }

  // call the writeStep function
  nextPlannerStep  = (*fcn_writeStep)(step_num);
  printf("Returned from calling the writeStep function\n");
  fflush(stdout);

  //returns last step completed
  return nextPlannerStep-1;
}


JNIEXPORT jint JNICALL Java_gov_nasa_arc_planworks_PlannerControlJNI_writeNext (JNIEnv* env, jclass cl, jint num_steps) {

  jint nextPlannerStep;
  jclass clazz;
  const char* error_msg;
  int (*fcn_writeNext)(int);
  void* libHandle = accessPlannerLibHandle();
  if (!libHandle) {
    clazz = env->FindClass("java/lang/NullPointerException");
    env->ThrowNew(clazz, "Planner library not loaded");
    return -1;
  }

  printf("PlannerControlJNI_writeNext %d\n", num_steps);
  fflush(stdout);

  //locate the 'writeNext' function in the library 
  fcn_writeNext = (int (*)(int))dlsym(libHandle, "writeNext");
  //printf("Returned from (int (*)(int))dlsym(libHandle, writeNext)\n");
  if (!fcn_writeNext) {
    error_msg = dlerror();
    printf("dlsym: Error locating writeNext:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return -1;
  }

  // call the writeNext function
  nextPlannerStep  = (*fcn_writeNext)(num_steps);
  printf("Returned from calling the writeNext function\n");
  fflush(stdout);

  //returns last step completed
  return nextPlannerStep-1;
}


JNIEXPORT jint JNICALL Java_gov_nasa_arc_planworks_PlannerControlJNI_completePlannerRun (JNIEnv* env, jclass cl) {

  jint lastStepCompleted;
  jclass clazz;
  const char* error_msg;
  int (*fcn_completeRun)();
  void* libHandle = accessPlannerLibHandle();
  if (!libHandle) {
    clazz = env->FindClass("java/lang/NullPointerException");
    env->ThrowNew(clazz, "Planner library not loaded");
    return -1;
  }

  printf("PlannerControlJNI_completePlannerRun\n");
  fflush(stdout);

  //locate the 'completeRun' function in the library 
  fcn_completeRun = (int (*)())dlsym(libHandle, "completeRun");
  //printf("Returned from (int (*)())dlsym(libHandle, completeRun)\n");
  if (!fcn_completeRun) {
    error_msg = dlerror();
    printf("dlsym: Error locating completeRun:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return -1;
  }

  // call the completeRun function
  lastStepCompleted  = (*fcn_completeRun)();
  printf("Returned from calling the completeRun function\n");
  printf("PlannerControlJNI_completePlannerRun: Finished \n");
  fflush(stdout);

  return lastStepCompleted;
}


JNIEXPORT jint JNICALL Java_gov_nasa_arc_planworks_PlannerControlJNI_terminatePlannerRun (JNIEnv* env, jclass cl) {

  jint retStatus;
  jclass clazz;
  const char* error_msg;
  int (*fcn_terminateRun)();
  void* libHandle = accessPlannerLibHandle();
  if (!libHandle) {
    clazz = env->FindClass("java/lang/NullPointerException");
    env->ThrowNew(clazz, "Planner library not loaded");
    return gov_nasa_arc_planworks_PlannerControlJNI_PLANNER_INITIALLY_INCONSISTANT;
  }

  printf("PlannerControlJNI_terminatePlannerRun\n");
  fflush(stdout);

  //locate the 'terminateRun' function in the library 
  fcn_terminateRun = (int (*)())dlsym(libHandle, "terminateRun");
  //printf("Returned from (int (*)())dlsym(libHandle, terminateRun)\n");
  if (!fcn_terminateRun) {
    error_msg = dlerror();
    printf("dlsym: Error locating terminateRun:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return gov_nasa_arc_planworks_PlannerControlJNI_PLANNER_INITIALLY_INCONSISTANT;
  }

  // call the terminateRun function
  retStatus  = (*fcn_terminateRun)();
  printf("Returned from calling the terminateRun function\n");

  printf("PlannerControlJNI_terminatePlannerRun: Model unloaded \n");
  fflush(stdout);

  return retStatus;
}

JNIEXPORT jstring JNICALL Java_gov_nasa_arc_planworks_PlannerControlJNI_getDestinationPath (JNIEnv* env, jclass cl) {

  jstring destPath;
  jclass clazz;
  const char* outputLocation; 
  const char* error_msg;
  const char* (*fcn_getOutputLocation)();
  void* libHandle = accessPlannerLibHandle();
  if (!libHandle) {
    clazz = env->FindClass("java/lang/NullPointerException");
    env->ThrowNew(clazz, "Planner library not loaded");
    return destPath;
  }

  printf("PlannerControlJNI_getDestinationPath\n");
  fflush(stdout);

  //locate the 'getOutputLocation' function in the library 
  fcn_getOutputLocation = (const char* (*)())dlsym(libHandle, "getOutputLocation");
  //printf("Returned from (const char* (*)())dlsym(libHandle, getOutputLocation)\n");
  if (!fcn_getOutputLocation) {
    error_msg = dlerror();
    printf("dlsym: Error locating getOutputLocation:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return destPath;
  }

  // call the getOutputLocation function
  outputLocation  = (*fcn_getOutputLocation)();
  printf("Returned from calling the getOutputLocation function\n");

  destPath = env->NewStringUTF(outputLocation);

  return destPath; 
}


JNIEXPORT jobjectArray JNICALL Java_gov_nasa_arc_planworks_PlannerControlJNI_getTransactionTypes
(JNIEnv * env, jclass cl) {
  jstring utf_str;
  jclass clazz;
  jobjectArray stringArray;
  int numTypes, typeLength;
  const char** types;
  const char* error_msg;
  int (*fcn_getNumTransactions)();
  int (*fcn_getMaxLengthTransactions)();
  const char** (*fcn_getTransactionNameStrs)();
  void* libHandle = accessPlannerLibHandle();
  if (!libHandle) {
    clazz = env->FindClass("java/lang/NullPointerException");
    env->ThrowNew(clazz, "Planner library not loaded");
    return stringArray;
  }

  printf("PlannerControlJNI_getTransactionTypes\n");
  fflush(stdout);

  /*
   * locate the 'getNumTransactions'  'getMaxLengthTransactions' and
   * 'getTransactionNameStrs' functions in the library 
   */
  fcn_getNumTransactions = (int (*)())dlsym(libHandle, "getNumTransactions");
  if (!fcn_getNumTransactions) {
    error_msg = dlerror();
    printf("dlsym: Error locating getNumTransactions:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return stringArray;
  }
  fcn_getMaxLengthTransactions = (int (*)())dlsym(libHandle, "getMaxLengthTransactions");
  if (!fcn_getMaxLengthTransactions) {
    error_msg = dlerror();
    printf("dlsym: Error locating getMaxLengthTransactions:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return stringArray;
  }
  fcn_getTransactionNameStrs = (const char** (*)())dlsym(libHandle, "getTransactionNameStrs");
  if (!fcn_getTransactionNameStrs) {
    error_msg = dlerror();
    printf("dlsym: Error locating getTransactionNameStrs:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return stringArray;
  }

  numTypes  = (*fcn_getNumTransactions)();
  typeLength  = (*fcn_getMaxLengthTransactions)();
  types = (*fcn_getTransactionNameStrs)();

  printf("PlannerControlJNI_getTransactionTypes: found %d types with max length %d\n", numTypes, typeLength);
  fflush(stdout);

  clazz = env->FindClass( "java/lang/String");
  stringArray = env->NewObjectArray( numTypes, clazz, NULL);
  char* buf = new char[typeLength+1];
  for (int i = 0; i < numTypes; i++) {
    sprintf( buf, types[i]);
    utf_str = env->NewStringUTF( buf);
    env->SetObjectArrayElement( stringArray, i, utf_str);
    env->DeleteLocalRef( utf_str);
  }
  delete[] buf;
  return stringArray;
}


JNIEXPORT jintArray JNICALL Java_gov_nasa_arc_planworks_PlannerControlJNI_getTransactionTypeStates
(JNIEnv * env, jclass cl) {
  jboolean isCopy;
  jintArray intArray;
  jclass clazz;
  int numTypes;
  int* filterState;
  const char* error_msg;
  int (*fcn_getNumTransactions)();
  void (*fcn_getTransactionFilterStates)(int*, int);
  void* libHandle = accessPlannerLibHandle();
  if (!libHandle) {
    clazz = env->FindClass("java/lang/NullPointerException");
    env->ThrowNew(clazz, "Planner library not loaded");
    return intArray;
  }

  printf("PlannerControlJNI_getTransactionTypeStates\n");
  fflush(stdout);

  /*
   * locate the 'getNumTransactions'  and 'getTransactionFilterStates'
   * functions in the library 
   */
  fcn_getNumTransactions = (int (*)())dlsym(libHandle, "getNumTransactions");
  if (!fcn_getNumTransactions) {
    error_msg = dlerror();
    printf("dlsym: Error locating getNumTransactions:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return intArray;
  }
  fcn_getTransactionFilterStates = (void (*)(int*, int))dlsym(libHandle, "getTransactionFilterStates");
  if (!fcn_getTransactionFilterStates) {
    error_msg = dlerror();
    printf("dlsym: Error locating getTransactionFilterStates:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return intArray;
  }

  numTypes  = (*fcn_getNumTransactions)();
  filterState = new int[numTypes];
  (*fcn_getTransactionFilterStates)(filterState, numTypes);

  intArray = env->NewIntArray( numTypes);
  jint* intArrayElems = env->GetIntArrayElements( intArray, &isCopy);
  for (int i = 0; i < numTypes; i++) {
    intArrayElems[i] =  filterState[i];
  }
  if (isCopy == JNI_TRUE) {
    env->ReleaseIntArrayElements( intArray, intArrayElems, 0);
  }
  delete[] filterState;
  return intArray;
}


JNIEXPORT void JNICALL Java_gov_nasa_arc_planworks_PlannerControlJNI_setTransactionTypeStates
(JNIEnv * env, jclass cl, jintArray intArray) {
  jboolean isCopy;
  jint size = env->GetArrayLength( intArray);
  jint* intArrayElems = env->GetIntArrayElements( intArray, &isCopy);
  jclass clazz;
  const char* error_msg;
  int numTypes;
  int (*fcn_getNumTransactions)();
  void (*fcn_setTransactionFilterStates)(int*, int);
  void* libHandle = accessPlannerLibHandle();
  if (!libHandle) {
    clazz = env->FindClass("java/lang/NullPointerException");
    env->ThrowNew(clazz, "Planner library not loaded");
    return;
  }

  printf("PlannerControlJNI_setTransactionTypeStates\n");
  fflush(stdout);

  /*
   * locate the 'getNumTransactions'  and 'setTransactionFilterStates'
   * functions in the library 
   */
  fcn_getNumTransactions = (int (*)())dlsym(libHandle, "getNumTransactions");
  if (!fcn_getNumTransactions) {
    error_msg = dlerror();
    printf("dlsym: Error locating getNumTransactions:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return;
  }
  fcn_setTransactionFilterStates = (void (*)(int*, int))dlsym(libHandle, "setTransactionFilterStates");
  if (!fcn_setTransactionFilterStates) {
    error_msg = dlerror();
    printf("dlsym: Error locating setTransactionFilterStates:\n");
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, error_msg);
    return;
  }

  numTypes  = (*fcn_getNumTransactions)();
  if (size != numTypes) {
    clazz = env->FindClass("java/lang/Exception");
    env->ThrowNew(clazz, "Transaction states array is wrong size");
    return;
  }

  int* filterState = new int[numTypes];
  for (int i = 0; i < size; i++) {
    filterState[i] = intArrayElems[i];
  }
  (*fcn_setTransactionFilterStates)(filterState, numTypes);

  if (isCopy == JNI_TRUE) {
    env->ReleaseIntArrayElements( intArray, intArrayElems, 0);
  }
  delete[] filterState;
}

