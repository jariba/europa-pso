#! /bin/tcsh -f
# $Id: build-tags-file.csh,v 1.56 2004-02-10 02:35:51 taylor Exp $
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
    ../../../pub/JGo41/com/nwoods/jgo/examples/Diamond.java \
    ../../../pub/JGo41/com/nwoods/jgo/examples/Hexagon.java \
    ../../../pub/JGo41/com/nwoods/jgo/examples/LeftTrapezoid.java \
    ../../../pub/JGo41/com/nwoods/jgo/examples/RightTrapezoid.java \
    src/gov/nasa/arc/planworks/AddSequenceThread.java \
    src/gov/nasa/arc/planworks/CreateSequenceViewThread.java \
    src/gov/nasa/arc/planworks/CreateViewThread.java \
    src/gov/nasa/arc/planworks/DeleteProjectThread.java \
    src/gov/nasa/arc/planworks/DeleteSequenceThread.java \
    src/gov/nasa/arc/planworks/ExecutePlannerThread.java \
    src/gov/nasa/arc/planworks/InstantiateProjectThread.java \
    src/gov/nasa/arc/planworks/NewSequenceThread.java \
    src/gov/nasa/arc/planworks/PlanWorks.java \
    src/gov/nasa/arc/planworks/SequenceViewMenuItem.java \
    src/gov/nasa/arc/planworks/db/DbConstants.java \
    src/gov/nasa/arc/planworks/db/PwConstraint.java \
    src/gov/nasa/arc/planworks/db/PwDBTransaction.java \
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
    src/gov/nasa/arc/planworks/db/PwResource.java \
    src/gov/nasa/arc/planworks/db/PwResourceInstant.java \
    src/gov/nasa/arc/planworks/db/PwResourceTransaction.java \
    src/gov/nasa/arc/planworks/db/PwRule.java \
    src/gov/nasa/arc/planworks/db/PwSlot.java \
    src/gov/nasa/arc/planworks/db/PwTimeline.java \
    src/gov/nasa/arc/planworks/db/PwToken.java \
    src/gov/nasa/arc/planworks/db/PwTokenQuery.java \
    src/gov/nasa/arc/planworks/db/PwTokenRelation.java \
    src/gov/nasa/arc/planworks/db/PwVariable.java \
    src/gov/nasa/arc/planworks/db/PwVariableQuery.java \
    src/gov/nasa/arc/planworks/db/impl/PwConstraintImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwDBTransactionImpl.java \
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
    src/gov/nasa/arc/planworks/db/impl/PwResourceImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwResourceInstantImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwResourceTransactionImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwRuleImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwSlotImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwTimelineImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwTokenImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwTokenQueryImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwTokenRelationImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwVariableImpl.java \
    src/gov/nasa/arc/planworks/db/impl/PwVariableQueryImpl.java \
    src/gov/nasa/arc/planworks/db/util/ContentSpec.java \
    src/gov/nasa/arc/planworks/db/util/FileUtils.java \
    src/gov/nasa/arc/planworks/db/util/MySQLDB.java \
    src/gov/nasa/arc/planworks/db/util/PartialPlanContentSpec.java \
    src/gov/nasa/arc/planworks/db/util/PwSequenceFilenameFilter.java \
    src/gov/nasa/arc/planworks/db/util/PwSQLFilenameFilter.java \
    src/gov/nasa/arc/planworks/db/util/SequenceContentSpec.java \
    src/gov/nasa/arc/planworks/mdi/EmptyDesktopIconUI.java \
    src/gov/nasa/arc/planworks/mdi/MDIDesktopFrame.java \
    src/gov/nasa/arc/planworks/mdi/MDIDesktopPane.java \
    src/gov/nasa/arc/planworks/mdi/MDIDynamicMenuBar.java \
    src/gov/nasa/arc/planworks/mdi/MDIFrame.java \
    src/gov/nasa/arc/planworks/mdi/MDIInternalFrame.java \
    src/gov/nasa/arc/planworks/mdi/MDIMenu.java \
    src/gov/nasa/arc/planworks/mdi/MDIWindowBar.java \
    src/gov/nasa/arc/planworks/mdi/MDIWindowButtonBar.java \
    src/gov/nasa/arc/planworks/mdi/SplashWindow.java \
    src/gov/nasa/arc/planworks/mdi/TileCascader.java \
    src/gov/nasa/arc/planworks/test/BackendTest.java \
    src/gov/nasa/arc/planworks/test/MySQLDBTest.java \
    src/gov/nasa/arc/planworks/test/PlanWorksTest.java \
    src/gov/nasa/arc/planworks/util/Algorithms.java \
    src/gov/nasa/arc/planworks/util/BrowseButton.java \
    src/gov/nasa/arc/planworks/util/ColorMap.java \
    src/gov/nasa/arc/planworks/util/ColorStream.java \
    src/gov/nasa/arc/planworks/util/DirectoryChooser.java \
    src/gov/nasa/arc/planworks/util/DuplicateNameException.java \
    src/gov/nasa/arc/planworks/util/Extent.java \
    src/gov/nasa/arc/planworks/util/FileCopy.java \
    src/gov/nasa/arc/planworks/util/MouseEventOSX.java \
    src/gov/nasa/arc/planworks/util/OneToManyMap.java \
    src/gov/nasa/arc/planworks/util/PlannerCommandLineDialog.java \
    src/gov/nasa/arc/planworks/util/ProjectNameDialog.java \
    src/gov/nasa/arc/planworks/util/ResourceNotFoundException.java \
    src/gov/nasa/arc/planworks/util/StringNameComparator.java \
    src/gov/nasa/arc/planworks/util/UniqueSet.java \
    src/gov/nasa/arc/planworks/util/Utilities.java \
    src/gov/nasa/arc/planworks/util/ViewRenderingException.java \
    src/gov/nasa/arc/planworks/viz/AskQueryObjectKey.java \
    src/gov/nasa/arc/planworks/viz/DBTransactionContentView.java \
    src/gov/nasa/arc/planworks/viz/DBTransactionHeaderView.java \
    src/gov/nasa/arc/planworks/viz/StepContentView.java \
    src/gov/nasa/arc/planworks/viz/StepHeaderView.java \
    src/gov/nasa/arc/planworks/viz/TokenQueryContentView.java \
    src/gov/nasa/arc/planworks/viz/TokenQueryHeaderView.java \
    src/gov/nasa/arc/planworks/viz/VariableQueryContentView.java \
    src/gov/nasa/arc/planworks/viz/VariableQueryHeaderView.java \
    src/gov/nasa/arc/planworks/viz/ViewConstants.java \
    src/gov/nasa/arc/planworks/viz/ViewGenerics.java \
    src/gov/nasa/arc/planworks/viz/VizView.java \
    src/gov/nasa/arc/planworks/viz/VizViewOverview.java \
    src/gov/nasa/arc/planworks/viz/nodes/BasicNodeLink.java \
    src/gov/nasa/arc/planworks/viz/nodes/DBTransactionHeaderNode.java \
    src/gov/nasa/arc/planworks/viz/nodes/ExtendedBasicNodePort.java \
    src/gov/nasa/arc/planworks/viz/nodes/ExtendedBasicNode.java \
    src/gov/nasa/arc/planworks/viz/nodes/HistogramElement.java \
    src/gov/nasa/arc/planworks/viz/nodes/NodeGenerics.java \
    src/gov/nasa/arc/planworks/viz/nodes/QueryResultField.java \
    src/gov/nasa/arc/planworks/viz/nodes/ResourceNameNode.java \
    src/gov/nasa/arc/planworks/viz/nodes/TokenNode.java \
    src/gov/nasa/arc/planworks/viz/nodes/TokenQueryHeaderNode.java \
    src/gov/nasa/arc/planworks/viz/nodes/VariableQueryHeaderNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/AskNodeByKey.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/CreatePartialPlanViewThread.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/PartialPlanView.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/PartialPlanViewMenu.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/PartialPlanViewMenuItem.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/PartialPlanViewSet.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/PartialPlanViewState.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/TimeScaleView.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/constraintNetwork/ConstraintNetwork.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/constraintNetwork/ConstraintNetworkLayout.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/constraintNetwork/ConstraintNetworkTokenNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/constraintNetwork/ConstraintNetworkView.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/constraintNetwork/ConstraintNetworkViewState.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/constraintNetwork/ConstraintNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/constraintNetwork/NewConstraintNetworkLayout.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/constraintNetwork/TokenBoundingBox.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/constraintNetwork/VariableBoundingBox.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/constraintNetwork/VariableNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/dbTransaction/DBTransactionView.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/navigator/ConstraintNavNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/navigator/ModelClassNavNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/navigator/NavigatorView.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/navigator/NavigatorViewLayout.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/navigator/SlotNavNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/navigator/TimelineNavNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/navigator/TokenNavNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/navigator/VariableNavNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/resourceProfile/ResourceProfile.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/resourceProfile/ResourceProfileView.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/resourceProfile/ResourceProfileViewState.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/resourceTransaction/ResourceTransactionSet.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/resourceTransaction/ResourceTransactionView.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/resourceTransaction/ResourceTransactionViewState.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/temporalExtent/TemporalExtentView.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/temporalExtent/TemporalExtentViewState.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/temporalExtent/TemporalNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/temporalExtent/TemporalNodeDurationBridge.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/temporalExtent/TemporalNodeTimeMark.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/temporalExtent/ThickDurationBridge.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/timeline/SlotNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/timeline/TimelineNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/timeline/TimelineTokenNode.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/timeline/TimelineView.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/tokenNetwork/TokenLink.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/tokenNetwork/TokenNetworkLayout.java \
    src/gov/nasa/arc/planworks/viz/partialPlan/tokenNetwork/TokenNetworkView.java \
    src/gov/nasa/arc/planworks/viz/sequence/SequenceView.java \
    src/gov/nasa/arc/planworks/viz/sequence/SequenceViewSet.java \
    src/gov/nasa/arc/planworks/viz/sequence/modelRules/ModelRulesLayout.java \
    src/gov/nasa/arc/planworks/viz/sequence/modelRules/ModelRulesView.java \
    src/gov/nasa/arc/planworks/viz/sequence/modelRules/ParamNode.java \
    src/gov/nasa/arc/planworks/viz/sequence/modelRules/PredicateNode.java \
    src/gov/nasa/arc/planworks/viz/sequence/modelRules/RuleLink.java \
    src/gov/nasa/arc/planworks/viz/sequence/modelRules/RuleNode.java \
    src/gov/nasa/arc/planworks/viz/sequence/sequenceSteps/SequenceStepsView.java \
    src/gov/nasa/arc/planworks/viz/sequence/sequenceSteps/StepElement.java \
    src/gov/nasa/arc/planworks/viz/sequence/sequenceQuery/DBTransactionQueryView.java \
    src/gov/nasa/arc/planworks/viz/sequence/sequenceQuery/StepQueryView.java \
    src/gov/nasa/arc/planworks/viz/sequence/sequenceQuery/TokenQueryView.java \
    src/gov/nasa/arc/planworks/viz/sequence/sequenceQuery/VariableQueryView.java \
    src/gov/nasa/arc/planworks/viz/util/DBTransactionComparatorAscending.java \
    src/gov/nasa/arc/planworks/viz/util/DBTransactionComparatorDescending.java \
    src/gov/nasa/arc/planworks/viz/util/SortStringComparator.java \
    src/gov/nasa/arc/planworks/viz/util/StepButton.java \
    src/gov/nasa/arc/planworks/viz/util/TokenQueryComparatorAscending.java \
    src/gov/nasa/arc/planworks/viz/util/TokenQueryComparatorDescending.java \
    src/gov/nasa/arc/planworks/viz/util/VariableQueryComparatorAscending.java \
    src/gov/nasa/arc/planworks/viz/util/VariableQueryComparatorDescending.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/ContentSpecChecker.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/RedrawNotifier.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/ViewableObject.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/ViewManager.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/ViewSet.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/ViewSetRemover.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/ContentSpecElement.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/ContentSpecGroup.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/ContentSpecWindow.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/GroupBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/KeyEntryBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/LogicComboBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/MergeBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/NegationCheckBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/PredicateBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/PredicateGroupBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/SpecBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/TimeIntervalBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/TimeIntervalGroupBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/TimelineBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/TimelineGroupBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/TokenTypeBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/UniqueKeyBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/partialPlan/UniqueKeyGroupBox.java \
    src/gov/nasa/arc/planworks/viz/viewMgr/contentSpecWindow/sequence/SequenceQueryWindow.java


echo "Writing `pwd`/planWorks.TAGS"
