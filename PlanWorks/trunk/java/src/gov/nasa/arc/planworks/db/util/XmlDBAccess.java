// $Id: XmlDBAccess.java,v 1.1 2003-05-10 01:00:33 taylor Exp $
//
// XmlDBAccess - Interface for XML data base access thru XML:DB API
//
// Will Taylor -- started 07may03
//

package gov.nasa.arc.planworks.db.util;

import java.util.List;

import org.xmldb.api.base.Collection;


public interface XmlDBAccess {

  public void registerDataBase();

  public Collection createCollection( String collectionName);

  public void addXMLFileToCollection( String collectionName,
                                             String filePathname);

  public List queryCollection( String collectionName, String query);

  public List getPartialPlanKeys( String collectionName);

  public List queryAttributeValueOfElements( String query, String attributeName,
                                                    String collectionName);


} // end XmlDBAccess
