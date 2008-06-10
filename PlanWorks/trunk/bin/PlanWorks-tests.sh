#!/bin/sh

if [ "${PLANWORKS_HOME}" == "" ] ; then 
	export PLANWORKS_HOME=`pwd`
fi

ismaxscreen=false

java \
  -verbose:gc \
  -Xms192m \
  -Xmx512m \
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
	-Dboolean.isMaxTestScreen=$ismaxscreen \
	-Dant.target.test=true \
	-Dplanworks.test.data.dir=${PLANWORKS_HOME}/src/java/gov/nasa/arc/planworks/test/data \
  -cp build/PlanWorks.jar gov.nasa.arc.planworks.test.PlanWorksTest "PlanWorks (PW_M_23): Plan Visualization [test mode]" $ismaxscreen
