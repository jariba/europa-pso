#! /bin/tcsh -f
# $Id: build-tags-file.csh,v 1.4 2003-05-20 18:25:34 taylor Exp $
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
    src/gov/nasa/arc/planworks/db/PwConstraint.java \
    src/gov/nasa/arc/planworks/db/PwDomain.java \
    src/gov/nasa/arc/planworks/db/PwEnumeratedDomain.java \
    src/gov/nasa/arc/planworks/db/PwIntervalDomain.java \
    src/gov/nasa/arc/planworks/db/PwModel.java \
    src/gov/nasa/arc/planworks/db/PwObject.java \
    src/gov/nasa/arc/planworks/db/PwParameter.java \
    src/gov/nasa/arc/planworks/db/PwPartialPlan.java \
    src/gov/nasa/arc/planworks/db/PwPlanningSequence.java \
    src/gov/nasa/arc/planworks/db/PwPredicate.java \
    src/gov/nasa/arc/planworks/db/PwProject.java \
    src/gov/nasa/arc/planworks/db/PwSlot.java \
    src/gov/nasa/arc/planworks/db/PwTimeline.java \
    src/gov/nasa/arc/planworks/db/PwToken.java \
    src/gov/nasa/arc/planworks/db/PwTokenRelation.java \
    src/gov/nasa/arc/planworks/db/PwTransaction.java \
    src/gov/nasa/arc/planworks/db/PwVariable.java \
    src/gov/nasa/arc/planworks/db/impl/PwConstraintImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwDomainImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwEnumeratedDomainImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwIntervalDomainImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwModelImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwObjectImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwParameterImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwPartialPlanImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwPlanningSequenceImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwPredicateImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwProjectImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwSlotImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwTimelineImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwTokenImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwTokenRelationImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwTransactionImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwVariableImpl.java \
    src/gov/nasa/arc/planworks/db/test/ExistTest.java \
    src/gov/nasa/arc/planworks/db/util/FileUtils.java \
    src/gov/nasa/arc/planworks/db/util/XmlDBeXist.java \
    src/gov/nasa/arc/planworks/db/util/XmlFileFilter.java \
    src/gov/nasa/arc/planworks/db/util/XmlFilenameFilter.java \
    src/gov/nasa/arc/planworks/proj/PwProjectMgmt.java \
    src/gov/nasa/arc/planworks/proj/test/PwProjectTest.java \
    src/gov/nasa/arc/planworks/util/ColorMap.java \
    src/gov/nasa/arc/planworks/util/DuplicateNameException.java \
    src/gov/nasa/arc/planworks/util/ResourceNotFoundException.java \
    src/gov/nasa/arc/planworks/viz/nodes/SlotNode.java \
    src/gov/nasa/arc/planworks/viz/nodes/TimelineNode.java \
    src/gov/nasa/arc/planworks/viz/views/VizView.java \
    src/gov/nasa/arc/planworks/viz/views/timeline/TimelineView.java


echo "Writing `pwd`/planWorks.TAGS"
