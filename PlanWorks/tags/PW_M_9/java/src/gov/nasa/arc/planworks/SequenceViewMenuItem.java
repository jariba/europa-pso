// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceViewMenuItem.java,v 1.1 2003-10-09 22:07:43 taylor Exp $
//
package gov.nasa.arc.planworks;

import javax.swing.JMenuItem;


public class SequenceViewMenuItem extends JMenuItem {

  private String seqUrl;
  private String sequenceName;

  public SequenceViewMenuItem( String viewName, String seqUrl, String seqName) {
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

