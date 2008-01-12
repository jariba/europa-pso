// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceViewMenuItem.java,v 1.4 2004-05-13 20:24:04 taylor Exp $
//
package gov.nasa.arc.planworks;

import javax.swing.JMenuItem;

import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewListener;


/**
 * <code>SequenceViewMenuItem</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                                NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SequenceViewMenuItem extends JMenuItem {

  private String seqUrl;
  private String sequenceName;
  private ViewListener viewListener;

  /**
   * <code>SequenceViewMenuItem</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   * @param seqUrl - <code>String</code> - 
   * @param seqName - <code>String</code> - 
   */
  public SequenceViewMenuItem( final String viewName, final String seqUrl,
                               final String seqName) {
    super( viewName);
    this.seqUrl = seqUrl;
    this.sequenceName = seqName;
    this.viewListener = null;
    this.setToolTipText( Utilities.getUrlLeaf( seqUrl));
  }

  /**
   * <code>SequenceViewMenuItem</code> - constructor - used by PartialPlanViewMenuItem
   *
   * @param viewName - <code>String</code> - 
   * @param seqUrl - <code>String</code> - 
   * @param seqName - <code>String</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public SequenceViewMenuItem( final String viewName, final String seqUrl,
                               final String seqName, final ViewListener viewListener) {
    super( viewName);
    this.seqUrl = seqUrl;
    this.sequenceName = seqName;
    this.viewListener = viewListener;
    this.setToolTipText( Utilities.getUrlLeaf( seqUrl));
  }

  /**
   * <code>getSeqUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public String getSeqUrl() {
    return seqUrl;
  }

  /**
   * <code>getSequenceName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getSequenceName() {
    return sequenceName;
  }

  /**
   * <code>getViewListener</code>
   *
   * @return - <code>ViewListener</code> - 
   */
  public final ViewListener getViewListener() {
    return viewListener;
  }

  /**
   * <code>setViewListener</code>
   *
   * @param listener - <code>ViewListener</code> - 
   */
  public final void setViewListener( ViewListener listener) {
    viewListener = listener;
  }


} // end class SequenceViewMenuItem

