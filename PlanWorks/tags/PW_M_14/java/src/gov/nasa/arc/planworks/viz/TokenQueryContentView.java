// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenQueryContentView.java,v 1.2 2004-05-13 20:24:08 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17dec03
//

package gov.nasa.arc.planworks.viz;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.db.PwTokenQuery;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.QueryResultField;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>TokenQueryContentView</code> - render values of token object as QueryResultField's
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenQueryContentView extends JGoView {

  private List tokenList; // element PwTokenQuery
  private String query;
  private TokenQueryHeaderView headerJGoView;
  private ViewableObject viewableObject; // PwPartialPlan or PwPlanning Sequence
  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private QueryResultField keyField;
  private List tokenKeyFieldList;  // element QueryResultField

  /**
   * <code>TokenQueryContentView</code> - constructor 
   *
   * @param tokenList - <code>List</code> - 
   * @param query - <code>String</code> - 
   * @param headerJGoView - <code>TokenQueryHeaderView</code> - 
   * @param viewableObject - <code>ViewableObject</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public TokenQueryContentView( List tokenList, String query,
                          TokenQueryHeaderView headerJGoView, ViewableObject viewableObject,
                          VizView vizView) {
    super();
    this.tokenList = tokenList;
    this.query = query;
    this.headerJGoView = headerJGoView;
    this.viewableObject = viewableObject;
    this.vizView = vizView;

    tokenKeyFieldList = new ArrayList();
    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoDocument = this.getDocument();

    renderTokenQueryContent( query);
  }

  /**
   * <code>redraw</code>
   *
   */
  public void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      renderTokenQueryContent( query);
    } //end run

  } // end class RedrawViewThread

  
  /**
   * <code>getTokenList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getTokenList() {
    return tokenList;
  }

  private void renderTokenQueryContent( String query) {
    getDocument().deleteContents();
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 5;
    Iterator tokenItr = tokenList.iterator();
    int i = 1;
    // System.err.println( "renderTokenQueryContent: tokenList " + tokenList);
    while (tokenItr.hasNext()) {
      x = 0;
      PwTokenQuery token = (PwTokenQuery) tokenItr.next();
      QueryResultField stepNumField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER,
                              token.getStepNumber().toString(), new Point( x, y),
                              JGoText.ALIGN_RIGHT, bgColor, viewableObject, vizView);
      jGoDocument.addObjectAtTail( stepNumField);
      stepNumField.setSize( (int) headerJGoView.getStepNumNode().getSize().getWidth(),
                            (int) stepNumField.getSize().getHeight());
      x += headerJGoView.getStepNumNode().getSize().getWidth(); 

      keyField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_OBJECT_KEY_HEADER,
                              token.getId().toString(),
                              new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( keyField);
      keyField.setSize( (int) headerJGoView.getKeyNode().getSize().getWidth(),
                         (int) keyField.getSize().getHeight());
      tokenKeyFieldList.add( keyField);
      x += headerJGoView.getKeyNode().getSize().getWidth();

      String predicateName =
        NodeGenerics.trimName( token.getPredicateName(),
                               headerJGoView.getPredicateNameNode(), vizView);
      QueryResultField predicateNameField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_PREDICATE_HEADER,
                              predicateName,
                              new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( predicateNameField);
      predicateNameField.setSize( (int) headerJGoView.getPredicateNameNode().getSize().getWidth(),
                         (int) predicateNameField.getSize().getHeight());
      x += headerJGoView.getPredicateNameNode().getSize().getWidth();

      y += stepNumField.getSize().getHeight();
      i++;
    }
  } // end renderTokenQueryContent

  /**
   * <code>getTokenKeyField</code>
   *
   * @param lineIndex - <code>int</code> - 
   * @return - <code>QueryResultField</code> - 
   */
  public QueryResultField getTokenKeyField( int lineIndex) {
    return (QueryResultField) tokenKeyFieldList.get( lineIndex);
  }

  /**
   * <code>scrollEntries</code>
   *
   * @param entryIndx - <code>int</code> - 
   */
  public void scrollEntries( int entryIndx) {
    int newPosition = ((int) keyField.getSize().getHeight()) * entryIndx;
    getVerticalScrollBar().setValue( newPosition);
  } // end scrollEntries


} // end class TokenQueryContentView
