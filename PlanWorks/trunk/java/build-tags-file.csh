#! /bin/tcsh -f
# $Id: build-tags-file.csh,v 1.1 2003-05-10 01:00:30 taylor Exp $
#
# build xemacs tags file on unix/linux based systems
#
set etags_flag = -f

if ( "`/bin/uname -s`" == "SunOS" ) then # SUN-OS OR SOLARIS
  if (-f /usr/ucb/hostid) then   # SOLARIS
    set etags_flag = -o
  endif
endif

etags $etags_flag planWorks.TAGS \
    src/gov/nasa/arc/planworks/db/PwModel.java \
    src/gov/nasa/arc/planworks/db/PwPartialPlan.java \
    src/gov/nasa/arc/planworks/db/PwPlanningSequence.java \
    src/gov/nasa/arc/planworks/db/PwProject.java \
    src/gov/nasa/arc/planworks/db/PwTransaction.java \
    src/gov/nasa/arc/planworks/db/impl/Factory.java \
    src/gov/nasa/arc/planworks/db/impl/PwModelImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwPartialPlanImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwPlanningSequenceImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwProjectImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwTransactionImpl.java \
    src/gov/nasa/arc/planworks/db/test/ExistTest.java \
    src/gov/nasa/arc/planworks/db/util/FileUtils.java \
    src/gov/nasa/arc/planworks/db/util/XmlDBAccess.java \
    src/gov/nasa/arc/planworks/db/util/XmlDBeXist.java \
    src/gov/nasa/arc/planworks/db/util/XmlFileFilter.java \
    src/gov/nasa/arc/planworks/db/util/XmlFilenameFilter.java \
    src/gov/nasa/arc/planworks/proj/PwProjectMgmt.java \
    src/gov/nasa/arc/planworks/proj/test/PwProjectTest.java \
    src/gov/nasa/arc/planworks/util/DuplicateNameException.java \
    src/gov/nasa/arc/planworks/util/ResourceNotFoundException.java \


echo "Writing `pwd`/planWorks.TAGS"
