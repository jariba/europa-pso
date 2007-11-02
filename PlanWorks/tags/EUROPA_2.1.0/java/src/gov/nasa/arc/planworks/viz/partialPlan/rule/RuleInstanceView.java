// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: RuleInstanceView.java,v 1.7 2005-04-26 15:25:26 pdaley Exp $
//
// PlanWorks
//
// Will Taylor -- started 29mar04
//

package gov.nasa.arc.planworks.viz.partialPlan.rule;

import java.awt.BorderLayout;
import java.awt.Point;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwRule;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.StringViewSetKey;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.nodes.RuleInstanceNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>RuleInstanceView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *               NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class RuleInstanceView extends PartialPlanView implements StringViewSetKey {

  private RuleInstanceNode ruleInstanceNode;
  private PwPartialPlan partialPlan;
  private long startTimeMSecs;
  private PartialPlanViewSet viewSet;
  private String viewSetKey;
  private MDIInternalFrame ruleFrame;
  private RuleJGoView jGoView;
  private JGoDocument jGoDocument;

  /**
   * <code>RuleInstanceView</code> - constructor 
   *
   * @param ruleInstanceNode - <code>RuleInstanceNode</code> - 
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewSetKey - <code>String</code> - 
   * @param ruleFrame - <code>MDIInternalFrame</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public RuleInstanceView( final RuleInstanceNode ruleInstanceNode,
                           final ViewableObject partialPlan,
                           final ViewSet viewSet, final String viewSetKey,
                           final MDIInternalFrame ruleFrame, final ViewListener viewListener) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.ruleInstanceNode = ruleInstanceNode;
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.viewSet = (PartialPlanViewSet) viewSet;
    this.viewSetKey = viewSetKey;
    this.ruleFrame = ruleFrame;
    if (viewListener != null) {
      addViewListener( viewListener);
    }

    System.err.println( "Render Rule Instance View ...");
    this.startTimeMSecs = System.currentTimeMillis();
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    jGoView = new RuleJGoView( this);
    jGoView.addViewListener( createViewListener());
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);
    
    // for PWTestHelper.findComponentByName
    this.setName( ruleFrame.getTitle());
    viewName = ViewConstants.RULE_INSTANCE_VIEW;

    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end constructor


//   Runnable runInit = new Runnable() {
//       public final void run() {
//         init();
//       }
//     };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo navigator,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use SwingWorker in constructor
   */
  public final void init() {
    handleEvent( ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for NavigatorView instance to become displayable
    if (! ViewGenerics.displayableWait( RuleInstanceView.this)) {
      closeView( this);
      return;
    }
    this.computeFontMetrics( this);

    jGoDocument = jGoView.getDocument();
    jGoDocument.addDocumentListener( createDocListener());
    renderRuleText();

    MDIInternalFrame contentFilterFrame = viewSet.getContentSpecWindow();
    int contentFilterMaxY = (int) (contentFilterFrame.getLocation().getY() +
                                   contentFilterFrame.getSize().getHeight());
    int delta = Math.min( ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4 *
                          ((PartialPlanViewSet) viewSet).getRuleFrameCnt(),
                          (int) (PlanWorks.getPlanWorks().getSize().getHeight() -
                                 contentFilterMaxY -
                                 (ViewConstants.MDI_FRAME_DECORATION_HEIGHT * 2)));
    ruleFrame.setLocation
      ( ViewConstants.INTERNAL_FRAME_X_DELTA + delta, contentFilterMaxY + delta);

//     Rectangle documentBounds = jGoView.getDocument().computeBounds();
//     jGoView.getDocument().setDocumentSize( (int) documentBounds.getWidth() +
//                                            (ViewConstants.TIMELINE_VIEW_X_INIT * 2),
//                                            (int) documentBounds.getHeight() +
//                                            (ViewConstants.TIMELINE_VIEW_Y_INIT * 2));
    int maxViewWidth = (int) jGoView.getDocumentSize().getWidth();
    int maxViewHeight = (int) jGoView.getDocumentSize().getHeight();

//     ruleFrame.setSize
//       ( maxViewWidth + ViewConstants.MDI_FRAME_DECORATION_WIDTH,
//         maxViewHeight + ViewConstants.MDI_FRAME_DECORATION_HEIGHT);

    expandViewFrame( ruleFrame, maxViewWidth, maxViewHeight);

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    handleEvent( ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init


  /**
   * <code>getRuleInstanceId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public final Integer getRuleInstanceId() {
    return ruleInstanceNode.getRuleInstance().getId();
  }

  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public final JGoDocument getJGoDocument()  {
    return this.jGoDocument;
  }

  /**
   * <code>setStartTimeMSecs</code>
   *
   * @param msecs - <code>long</code> - 
   */
  protected final void setStartTimeMSecs( final long msecs) {
    startTimeMSecs = msecs;
  }

  /**
   * <code>getViewSetKey</code> - implements StringViewSetKey
   *
   * @return - <code>String</code> - 
   */
  public final String getViewSetKey() {
    return viewSetKey;
  }

  private void renderRuleText() {
    PwToken fromToken = ruleInstanceNode.getFromToken();
    List toTokenList = ruleInstanceNode.getToTokenList();
    Collections.sort( toTokenList, new TokenIdComparator());

    jGoView.getDocument().deleteContents();
    int lineHeight = fontMetrics.getHeight();
    int lineCnt = 0, xMargin = 2;
    Point textLoc = new Point( xMargin, lineHeight * lineCnt);
    JGoText textObject = new JGoText( textLoc, "From:");
    textObject.setBold( true);
    int offset = textObject.getWidth();
    addText( textObject, xMargin);

    textLoc = new Point( offset, lineHeight * lineCnt);
    textObject = new JGoText( textLoc, " (key=" + fromToken.getId().toString() + ")");
    addText( textObject, xMargin);
    lineCnt++;

    textLoc = new Point( xMargin, lineHeight * lineCnt);
    textObject = new JGoText( textLoc, fromToken.toString());
    lineCnt += addText( textObject, xMargin);

    Iterator toTokenListItr = toTokenList.iterator();
    while (toTokenListItr.hasNext()) {
      PwToken toToken = (PwToken) toTokenListItr.next();
      textLoc = new Point( xMargin, lineHeight * lineCnt);
      textObject = new JGoText( textLoc, "To:");
      textObject.setBold( true);
      offset = textObject.getWidth();
      addText( textObject, xMargin);

      textLoc = new Point( offset, lineHeight * lineCnt);
      textObject = new JGoText( textLoc, " (key=" + toToken.getId().toString() + ")");
      addText( textObject, xMargin);
      lineCnt++;

      textLoc = new Point( xMargin, lineHeight * lineCnt);
      textObject = new JGoText( textLoc, toToken.toString());
      lineCnt += addText( textObject, xMargin);
    }

    textLoc = new Point( xMargin, lineHeight * lineCnt);
    textObject = new JGoText( textLoc, "---------------------------");
    textObject.setBold( true);
    addText( textObject, xMargin);
    lineCnt++;

    textLoc = new Point( xMargin, lineHeight * lineCnt);
    textObject = new JGoText( textLoc, "Rule:");
    textObject.setBold( true);
    offset = textObject.getWidth();
    addText( textObject, xMargin);

    Integer ruleId = ruleInstanceNode.getRuleInstance().getRuleId();
    textLoc = new Point( offset, lineHeight * lineCnt);
    textObject = new JGoText( textLoc, " (key=" + ruleId.toString() + ")");
    addText( textObject, xMargin);
    lineCnt++;

    PwPartialPlan partialPlan = ruleInstanceNode.getPartialPlanView().getPartialPlan();
    textLoc = new Point( xMargin, lineHeight * lineCnt);
    PwRule rule = partialPlan.getRule( ruleId);
    if ( rule != null ) {
      textObject = new JGoText( textLoc, rule.getText());
      addText( textObject, xMargin);
    } else {
      System.err.println( "Error: Rule text not found for rule " + ruleId );
      System.err.println( "       Check RuleConfigSection of PlanWorks.cfg");
    }
  } // end renderRuleText

  private int addText( final JGoText textObject, final int xMargin) {
    textObject.setResizable( false);
    textObject.setEditable( false);
    textObject.setDraggable( false);
    textObject.setSelectable( false);
    textObject.setAutoResize( true);
    textObject.setMultiline( true);
    textObject.setWrapping( false);
//     textObject.setWrapping( true);
//     int wrappingWidth = (int) this.getExtentSize().getWidth() - xMargin * 2;
//     textObject.setWrappingWidth( wrappingWidth);
    textObject.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoView.getDocument().addObjectAtTail( textObject);
    int numLines = 1;
//     int numLines = 1 + (int) Math.ceil( SwingUtilities.computeStringWidth
//                                         ( fontMetrics, textObject.getText()) /
//                                         wrappingWidth);
    // System.err.println( "numLines " + numLines);
    return numLines;
  }


  private class TokenIdComparator implements Comparator {
    public TokenIdComparator() {
    }
    public final int compare( final Object o1, final Object o2) {
      // System.err.println( "TokenIdComparator " + o1.getClass().getName());
      Integer i1 = ((PwToken) o1).getId();
      Integer i2 = ((PwToken) o2).getId();
      return i1.compareTo( i2);
    }
    public final boolean equals( final Object o1, final Object o2) {
      Integer i1 = ((PwToken) o1).getId();
      Integer i2 = ((PwToken) o2).getId();
      return i1.equals( i2);
    }
  }


  /**
   * <code>RuleJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  public class RuleJGoView extends JGoView {

    private RuleInstanceView ruleInstanceView;

    /**
     * <code>RuleJGoView</code> - constructor 
     *
     * @param ruleInstanceView - <code>RuleInstanceView</code> - 
     */
    public RuleJGoView( final RuleInstanceView ruleInstanceView) {
      super();
      this.ruleInstanceView = ruleInstanceView;
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public final void doBackgroundClick( final int modifiers, final Point docCoords,
                                         final Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {

        // mouseRightPopupMenu( viewCoords);

      }
    } // end doBackgroundClick

  } // end class RuleJGoView


} // end class RuleInstanceView

