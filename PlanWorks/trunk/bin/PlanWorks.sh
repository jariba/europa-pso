#!/bin/sh

if [ "${EUROPA_HOME}" != "" -a "${PLANWORKS_HOME}" == "" ] ; then
	export PLANWORKS_HOME=${EUROPA_HOME}
	jarfile="${EUROPA_HOME}/lib/PlanWorks.jar"
fi

if [ "${PLANWORKS_HOME}" == "" ] ; then 
	echo "\$PLANWORKS_HOME is undefined, attempting to use ${PWD}"
	export PLANWORKS_HOME=`pwd`
fi

if [ "${jarfile}" == "" ] ; then
	jarfile="${PLANWORKS_HOME}/build/lib/PlanWorks.jar"
fi

ismaxscreen=false

java \
  -verbose:gc \
  -Xms192m \
  -Xmx256m \
  -Dplanworks.root=${PLANWORKS_HOME} \
  -Dplanworks.config=${PLANWORKS_HOME}/config/planworks.config \
  -Dprojects.config=${PLANWORKS_HOME}/config/projects.config \
  -Duser=${USER} \
  -Dos.type=${OSTYPE} \
  -Dspalsh.image=${PLANWORKS_HOME}/res/images \
  -Dant.target.test=false \
  -Dintegration.home=${PLANWORKS_HOME}/src/cpp \
  -Ddb.class=gov.nasa.arc.planworks.db.util.HSQLDB \
  -Dhsqldb.db=${PLANWORKS_HOME}/res/hsql/database \
  -jar ${jarfile} "PlanWorks (PW_M_23): Plan Visualization" $ismaxscreen
