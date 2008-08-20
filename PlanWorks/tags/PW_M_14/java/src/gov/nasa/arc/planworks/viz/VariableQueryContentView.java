// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VariableQueryContentView.java,v 1.2 2004-05-13 20:24:08 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 19dec03
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

import gov.nasa.arc.planworks.db.PwVariableQuery;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.QueryResultField;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>VariableQueryContentView</code> - render values of variable object as QueryResultField's
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VariableQueryContentView extends JGoView {

  private List variableList; // element PwVariableQuery
  private String query;
  private VariableQueryHeaderView headerJGoView;
  private ViewableObject viewableObject; // PwPartialPlan or PwPlanning Sequence
  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private QueryResultField keyField;
  private List variableKeyFieldList;  // element QueryResultField

  /**
   * <code>VariableQueryContentView</code> - constructor 
   *
   * @param variableList - <code>List</code> - 
   * @param query - <code>String</code> - 
   * @param headerJGoView - <code>VariableQueryHeaderView</code> - 
   * @param viewableObject - <code>ViewableObject</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public VariableQueryContentView( List variableList, String query,
                          VariableQueryHeaderView headerJGoView, ViewableObject viewableObject,
                          VizView vizView) {
    super();
    this.variableList = variableList;
    this.query = query;
    this.headerJGoView = headerJGoView;
    this.viewableObject = viewableObject;
    this.vizView = vizView;

    variableKeyFieldList = new ArrayList();
    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoDocument = this.getDocument();

    renderVariableQueryContent( query);
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
      renderVariableQueryContent( query);
    } //end run

  } // end class RedrawViewThread

  
  /**
   * <code>getVariableList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getVariableList() {
    return variableList;
  }

  private void renderVariableQueryContent( String query) {
    getDocument().deleteContents();
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 5;
    Iterator variableItr = variableList.iterator();
    int i = 1;
    // System.err.println( "renderVariableQueryContent: variableList " + variableList);
    while (variableItr.hasNext()) {
      x = 0;
      PwVariableQuery variable = (PwVariableQuery) variableItr.next();
      QueryResultField stepNumField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER,
                              variable.getStepNumber().toString(), new Point( x, y),
                              JGoText.ALIGN_RIGHT, bgColor, viewableObject, vizView);
      jGoDocument.addObjectAtTail( stepNumField);
      stepNumField.setSize( (int) headerJGoView.getStepNumNode().getSize().getWidth(),
                            (int) stepNumField.getSize().getHeight());
      x += headerJGoView.getStepNumNode().getSize().getWidth();

      keyField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_OBJECT_KEY_HEADER,
                              variable.getId().toString(),
                              new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( keyField);
      keyField.setSize( (int) headerJGoView.getKeyNode().getSize().getWidth(),
                         (int) keyField.getSize().getHeight());
      variableKeyFieldList.add( keyField);
      x += headerJGoView.getKeyNode().getSize().getWidth();

      String variableName =
        NodeGenerics.trimName( variable.getName(), headerJGoView.getNameNode(), vizView);
      QueryResultField variableNameField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_OBJ_NAME_HEADER,
                              variableName,
                              new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( variableNameField);
      variableNameField.setSize( (int) headerJGoView.getNameNode().getSize().getWidth(),
                         (int) variableNameField.getSize().getHeight());
      x += headerJGoView.getNameNode().getSize().getWidth();

      y += stepNumField.getSize().getHeight();
      i++;
    }
  } // end renderFreeVariables

  /**
   * <code>getVariableKeyField</code>
   *
   * @param lineIndex - <code>int</code> - 
   * @return - <code>QueryResultField</code> - 
   */
  public QueryResultField getVariableKeyField( int lineIndex) {
    return (QueryResultField) variableKeyFieldList.get( lineIndex);
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


} // end class VariableQueryContentView
