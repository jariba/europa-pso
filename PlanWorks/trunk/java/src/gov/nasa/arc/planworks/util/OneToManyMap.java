package gov.nasa.arc.planworks.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OneToManyMap extends HashMap {
  public OneToManyMap() {
    super();
  }
  public OneToManyMap(int initCapacity) {
    super(initCapacity);
  }
  public OneToManyMap(int initCapacity, float loadFactor) {
    super(initCapacity, loadFactor);
  }
  public OneToManyMap(Map m) {
    super(m);
  }
  public boolean containsValue(Object value) {
    Iterator valueIterator = values().iterator();
    while(valueIterator.hasNext()) {
      if(((List)valueIterator.next()).contains(value)) {
        return true;
      }
    }
    return false;
  }
  public Object getItemAtIndex(Object key, int index) {
    if(!super.containsKey(key)) {
      return null;
    }
    List valueList = (List) super.get(key);
    return valueList.get(index);
  }
  public List put(Object key, List values) {
    List valueList = null;
    if(!super.containsKey(key)) {
      valueList = new ArrayList();
      super.put(key, valueList);
    }
    else {
      valueList = (List) super.get(key);
    }
    valueList.addAll(values);
    return values;
  }
  public Object put(Object key, Object value) {
    List valueList = null;
    if(!super.containsKey(key)) {
      valueList = new ArrayList();
      super.put(key, valueList);
    }
    else {
      valueList = (List) super.get(key);
    }
    valueList.add(value);
    return value;
  }
}
