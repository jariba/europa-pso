package gov.nasa.arc.planworks.db.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gov.nasa.arc.planworks.db.PwEntity;

public abstract class PwEntityImpl {
  protected static List getNeighbors(PwEntity entity, List classes, Set ids) {
    List retval = entity.getNeighbors(classes);
    for(Iterator it = retval.iterator(); it.hasNext();) {
      PwEntity ent = (PwEntity) it.next();
      if(!ids.contains(ent.getId()))
        retval.remove(ent);
    }
    return retval;
  }
}
