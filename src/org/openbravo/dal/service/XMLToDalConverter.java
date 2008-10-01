/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.openbravo.dal.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.OBFactory;
import org.openbravo.dal.core.SessionHandler;

/**
 * Converts XML to a tree of objects which are read from the db and updated or
 * are new objects.
 * 
 * Things to take into account: - references can be internal - both insert and
 * update should be handled for larger object trees - client and organisation
 * should not be changed/imported and for new objects should be set based on the
 * current user The same for created and createdBy
 * 
 * TODO: support delete using a delete type
 * 
 * @author mtaal
 */

public class XMLToDalConverter {
  
  private static final long serialVersionUID = 1L;
  
  private Map<String, Object> objects = new HashMap<String, Object>();
  
  private List<XMLImportMessage> messages = new ArrayList<XMLImportMessage>();
  
  private boolean actionIsDelete = false;
  
  public void importXML(InputStream is) {
    final SAXReader reader = new SAXReader();
    try {
      final Document doc = reader.read(is);
      importXML(doc);
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
  
  public void importXML(String xml) {
    try {
      final Document doc = DocumentHelper.parseText(xml);
      importXML(doc);
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
  
  @SuppressWarnings("unchecked")
  public void importXML(Document doc) {
    // is it a list of ob objects or not
    if (doc.getRootElement().getName().equals(DalToXMLConverter.OB_LIST_ROOT)) {
      importOBObjects(doc.getRootElement().elements());
    } else {
      final List<Element> elements = new ArrayList<Element>();
      elements.add(doc.getRootElement());
      importOBObjects(elements);
    }
  }
  
  public void importOBObjects(List<Element> obElements) {
    collectIdedObjects(obElements);
    
    final List<Object> persistableObjects = new ArrayList<Object>();
    for (Element element : obElements) {
      try {
        Object entityObject;
        // new object
        if (element.attribute("id") == null) {
          entityObject = OBFactory.getInstance().create(element.getName());
        } else { // the object must be already in the id-ed objects
          entityObject = getObject(element.getName(), element.attributeValue("id"));
        }
        setValues(element, entityObject);
        persistableObjects.add(entityObject);
      } catch (Exception e) {
        final XMLImportMessage msg = new XMLImportMessage();
        msg.setError(true);
        msg.setMessage(e.getMessage());
        messages.add(msg);
      }
    }
    if (isActionIsDelete()) {
      for (Object po : persistableObjects) {
        SessionHandler.getInstance().delete(po);
      }
    } else {
      for (Object po : persistableObjects) {
        SessionHandler.getInstance().save(po);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private void setValues(Element obElement, Object object) {
    final BaseOBObject bob = (BaseOBObject) object;
    final Entity e = ModelProvider.getInstance().getEntity(obElement.getName());
    for (Element childElement : (List<Element>) obElement.elements()) {
      final Property p = e.getProperty(childElement.getName());
      
      // TODO: make this option controlled
      if (p.isXMLTransient()) {
        continue;
      }
      
      if (p.isPrimitive()) {
        final Object value = XMLTypeConverter.getInstance().fromXML(p.getPrimitiveType(), childElement.getText());
        bob.set(p.getName(), value);
      } else {
        final Object value;
        if (childElement.attribute("id") == null) {
          value = null;
        } else {
          final String id = childElement.attributeValue("id");
          final String entityName = p.getTargetEntity().getName();
          value = getObject(entityName, id);
        }
        bob.set(p.getName(), value);
      }
      
    }
  }
  
  // collect the objects which have an id, these can be referenced from other
  // objects
  private void collectIdedObjects(List<Element> obElements) {
    // assume that the elements are all obObjects
    for (Element element : obElements) {
      if (element.attribute("id") == null) {
        continue;
      }
      final String entityName = element.getName();
      try {
        final Entity e = ModelProvider.getInstance().getEntity(entityName);
        createReadObject(e.getName(), element.attributeValue("id"));
      } catch (Exception e) {
        final XMLImportMessage msg = new XMLImportMessage();
        msg.setError(true);
        msg.setMessage(e.getMessage());
        messages.add(msg);
      }
    }
  }
  
  // checks if the object is present in the objects list
  // or if not read from the db, if not present there
  // then an exception is thrown
  private Object getObject(String entityName, String id) {
    Object o = objects.get(getKey(entityName, id));
    if (o == null) {
      o = SessionHandler.getInstance().find(entityName, id);
      if (o == null) {
        throw new OBWebServiceException("Object with entityName " + entityName + " and id " + id + " is not present in xml or in the database.");
      }
    }
    return o;
  }
  
  // reads an object from the db if it exists or creates a new one
  private Object createReadObject(String entityName, String id) {
    
    Object result;
    // already found, return it
    if ((result = objects.get(getKey(entityName, id))) != null) {
      return result;
    }
    
    // read from the db
    final Object o = SessionHandler.getInstance().find(entityName, id);
    if (o != null) {
      objects.put(getKey(entityName, id), o);
      return o;
    }
    
    // create new!
    result = OBFactory.getInstance().create(entityName);
    objects.put(getKey(entityName, id), result);
    if (result instanceof BaseOBObject) {
      // TODO: this will probably fail because hibernate can not detect that it
      // is new
      ((BaseOBObject) result).setId(id);
      ((BaseOBObject) result).setNewOBObject(true);
    }
    return result;
  }
  
  private String getKey(String entityName, String id) {
    return entityName + "_" + id;
  }
  
  public boolean isActionIsDelete() {
    return actionIsDelete;
  }
  
  public void setActionIsDelete(boolean actionIsDelete) {
    this.actionIsDelete = actionIsDelete;
  }
  
}