// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VizViewRuleView.java,v 1.2 2004-04-15 18:56:28 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 29mar04
//

package gov.nasa.arc.planworks.viz;

import java.awt.FontMetrics;
import java.awt.Point;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.db.PwToken;

/**
 * <code>VizViewRuleView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *               NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VizViewRuleView extends JGoView implements StringViewSetKey {

  private String ruleViewTitle; // key for viewSet hash map
  private VizView vizView;
  private FontMetrics fontMetrics;

  /**
   * <code>VizViewRuleView</code> - constructor 
   *
   * @param ruleViewTitle - <code>String</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public VizViewRuleView( final String ruleViewTitle, final VizView vizView) {
    super();
    this.ruleViewTitle = ruleViewTitle;
    this.vizView = vizView;
    vizView.setRuleView( this);
    this.fontMetrics = vizView.getFontMetrics();
  }

  /**
   * <code>getViewSetKey</code> - implements StringViewSetKey
   *
   * @return - <code>String</code> - 
   */
  public final String getViewSetKey() {
    return ruleViewTitle;
  }

  /**
   * <code>getVizView</code>
   *
   * @return - <code>VizView</code> - 
   */
  public final VizView getVizView() {
    return vizView;
  }

  /**
   * I believe the problem may be related to focus changes in the
   * JDesktopPane.  When focus changes between different JInternalFrames on
   * the desktop it appears that the parent component of the JInternalFrame
   * changes, so momentarily it has no parent.  This causes
   * JGoOverview.removeNotify() to be called which sets the observed JGoView
   * to null:
   *
   * You should subclass JGoOverview and override this method to do nothing.
   * Then make sure these operations are performed when the JInternalFrame
   * holdiong the JGoOverview is closed.
   */
  public void removeNotify() {
    // System.err.println( "VizViewRuleview.removeNotify");
//     removeListeners();
//     super.removeNotify();
  }  

  /**
   * <code>renderRuleText</code>
   *
   * @param token - <code>PwToken</code> - 
   */
  public final void renderRuleText( final PwToken toToken, final PwToken fromToken) { 
    this.getDocument().deleteContents();
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
    // textObject = new JGoText( textLoc, "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
    lineCnt += addText( textObject, xMargin);

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
    // textObject = new JGoText( textLoc, "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
    lineCnt += addText( textObject, xMargin);

    textLoc = new Point( xMargin, lineHeight * lineCnt);
    textObject = new JGoText( textLoc, "Rule:");
    textObject.setBold( true);
    addText( textObject, xMargin);
    lineCnt++;

    textLoc = new Point( xMargin, lineHeight * lineCnt);
    textObject = new JGoText( textLoc, toToken.getModelRule());
    // textObject = new JGoText( textLoc, "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
    addText( textObject, xMargin);
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
    this.getDocument().addObjectAtTail( textObject);
    int numLines = 1 + (int) Math.ceil( SwingUtilities.computeStringWidth
                                        ( fontMetrics, textObject.getText()) /
                                        wrappingWidth);
    // System.err.println( "numLines " + numLines);
    return numLines;
  }


} // end class VizViewRuleView

