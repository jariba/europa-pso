// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceViewMenuItem.java,v 1.2 2004-02-03 19:21:28 miatauro Exp $
//
package gov.nasa.arc.planworks;

import javax.swing.JMenuItem;


public class SequenceViewMenuItem extends JMenuItem {

  private String seqUrl;
  private String sequenceName;

  public SequenceViewMenuItem( final String viewName, final String seqUrl, final String seqName) {
    super( viewName);
    this.seqUrl = seqUrl;
    this.sequenceName = seqName;
  }

  public String getSeqUrl() {
    return seqUrl;
  }

  public String getSequenceName() {
    return sequenceName;
  }

} // end class SequenceViewMenuItem

