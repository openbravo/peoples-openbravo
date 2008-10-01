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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.IdentifierProvider;
import org.openbravo.base.util.Check;
import org.openbravo.base.util.ModelUtil;
import org.openbravo.dal.core.DALUtil;

/**
 * Converts one or more business objects to a XML presentation.
 * 
 * TODO: Support id's with multiple values
 * 
 * 
 * @author mtaal
 */

public class DalToXMLConverter {
  private static final Logger log = Logger.getLogger(DalToXMLConverter.class);
  
  public static final String REFERENCE_TYPE_NAME = "obReference";
  public static final String OB_LIST_ROOT = "OBObjects";
  
  private static DalToXMLConverter instance = new DalToXMLConverter();
  
  public static DalToXMLConverter getInstance() {
    return instance;
  }
  
  public static void setInstance(DalToXMLConverter instance) {
    DalToXMLConverter.instance = instance;
  }
  
  public Document getTypesAsXML() {
    final Document doc = DalWebServiceUtil.getInstance().createDomDocument();
    final Element root = doc.addElement("Types");
    final List<String> entityNames = new ArrayList<String>();
    for (Entity e : ModelProvider.getInstance().getModel()) {
      entityNames.add(e.getName());
    }
    Collections.sort(entityNames);
    
    for (String entityName : entityNames) {
      final Element typeElement = root.addElement("Type");
      typeElement.addAttribute("entityName", entityName);
    }
    return doc;
  }
  
  // Generates the schema, if the
  public Document getSchema() {
    final Document doc = DalWebServiceUtil.getInstance().createDomDocument();
    final Element root = doc.addElement("schema");
    root.addAttribute("xmlns", "http://www.w3.org/1999/XMLSchema");
    root.addAttribute("xmlns:bo", "http://www.openbravo.com");
    
    final List<String> entityNames = new ArrayList<String>();
    for (Entity e : ModelProvider.getInstance().getModel()) {
      entityNames.add(e.getName());
    }
    Collections.sort(entityNames);
    
    final Element multiElement = root.addElement("element");
    multiElement.addAttribute("name", OB_LIST_ROOT);
    final Element complexType = multiElement.addElement("complexType");
    final Element choiceElement = complexType.addElement("choice");
    choiceElement.addAttribute("minOccurs", "1");
    choiceElement.addAttribute("maxOccurs", "unbounded");
    
    for (String entityName : entityNames) {
      
      final Element entityElement = choiceElement.addElement("element");
      entityElement.addAttribute("name", entityName);
      entityElement.addAttribute("type", "ob:" + entityName + "Type");
    }
    
    for (String entityName : entityNames) {
      // add the singlelement
      final Element singleElement = root.addElement("element");
      singleElement.addAttribute("name", entityName);
      singleElement.addAttribute("type", "ob:" + entityName + "Type");
      
      final Element typeElement = root.addElement("complexType");
      typeElement.addAttribute("name", entityName + "Type");
      final Element typeSequenceElement = typeElement.addElement("sequence");
      addPropertyElements(typeSequenceElement, ModelProvider.getInstance().getEntity(entityName));
      typeElement.addElement("attribute").addAttribute("name", "id").addAttribute("type", "string").addAttribute("use", "optional");
      typeElement.addElement("attribute").addAttribute("name", "identifier").addAttribute("type", "string").addAttribute("use", "optional");
      typeElement.addElement("anyAttribute");
    }
    
    addObReferenceType(root);
    
    return doc;
  }
  
  protected void addPropertyElements(Element sequence, Entity e) {
    for (Property p : e.getProperties()) {
      final Element element = sequence.addElement("element");
      
      // this is true in case the primary key is also a foreign Key (for example
      // AD_Client_Info)
      // then two fields are created in the system, one to hold the String id
      // and the other
      // to hold the reference to the parent
      // if (p.isId() && !p.isPrimitive()) {
      // final Element idElement = sequence.addElement("element");
      // idElement.addAttribute("name", "id");
      // idElement.addAttribute("type", "string");
      // final String name = p.getSimpleTypeName().substring(0, 1).toLowerCase()
      // + p.getSimpleTypeName().substring(1);
      // element.addAttribute("name", name);
      // } else {
      
      element.addAttribute("name", p.getName());
      
      if ((p.isPrimitive() && p.isId()) || p.isXMLTransient() || !p.isMandatory()) {
        element.addAttribute("minOccurs", "0");
      } else if (p.isMandatory()) {
        element.addAttribute("minOccurs", "1");
      }
      element.addAttribute("nillable", Boolean.toString(!p.isMandatory()));
      
      // set the type
      if (p.isPrimitive()) {
        element.addAttribute("type", XMLTypeConverter.getInstance().toXMLSchemaType(p.getPrimitiveType()));
      } else {
        element.addAttribute("type", "ob:" + REFERENCE_TYPE_NAME);
      }
    }
  }
  
  protected void addObReferenceType(Element root) {
    final Element complex = root.addElement("complexType");
    complex.addAttribute("name", REFERENCE_TYPE_NAME);
    complex.addElement("attribute").addAttribute("name", "id").addAttribute("type", "string").addAttribute("use", "optional");
    complex.addElement("attribute").addAttribute("name", "entityName").addAttribute("type", "string").addAttribute("use", "optional");
    complex.addElement("attribute").addAttribute("name", "identifier").addAttribute("type", "string").addAttribute("use", "optional");
    // final Element simple = complex.addElement("simpleContent");
    // final Element extension =
    // simple.addElement("extension").addAttribute("base", "string");
    // extension.addElement("attribute").addAttribute("name",
    // "id").addAttribute("type", "string").addAttribute("use", "required");
    // extension.addElement("attribute").addAttribute("name",
    // "entityName").addAttribute("type", "string").addAttribute("use",
    // "optional");
  }
  
  public Document toXML(String entityName, List<BaseOBObject> obObjects) {
    final Document doc = DalWebServiceUtil.getInstance().createDomDocument();
    addRootElement(doc, entityName + "s");
    for (BaseOBObject bob : obObjects) {
      toXML(bob, doc);
    }
    return doc;
  }
  
  protected Element addRootElement(Document doc, String elementName) {
    final Namespace ns = new Namespace("ob", "http://www.openbravo.com");
    final QName qName = new QName(elementName, ns);
    final Element root = doc.addElement(qName);
    root.addNamespace("ob", "http://www.openbravo.com");
    return root;
  }
  
  public Document toXML(BaseOBObject obObject) {
    final Document doc = DalWebServiceUtil.getInstance().createDomDocument();
    toXML(obObject, doc);
    return doc;
  }
  
  // checks if the doc has a rootElement, if so then the obObject
  // is added to it, otherwise a new rootElement is created
  public void toXML(BaseOBObject obObject, Document doc) {
    Check.isNotNull(obObject, "Argument can not be null");
    final Class<?> clz = obObject.getClass();
    final String entityName = ModelUtil.getEntityName(clz);
    final Entity entity = ModelProvider.getInstance().getEntity(entityName);
    
    final Element rootElement;
    if (doc.getRootElement() == null) {
      rootElement = addRootElement(doc, entityName);
    } else {
      final Element docRootElement = doc.getRootElement();
      rootElement = docRootElement.addElement(entityName);
    }
    rootElement.addAttribute("id", DALUtil.getId(obObject).toString());
    rootElement.addAttribute("identifier", IdentifierProvider.getInstance().getIdentifier(obObject));
    
    for (Property p : entity.getProperties()) {
      final Element currentElement = rootElement.addElement(p.getName());
      final Object value = obObject.get(p.getName());
      
      if (p.isCompositeId()) {
        log.warn("Entity " + entity + " has compositeid, this is not yet supported in the webservice");
        continue;
      }
      
      if (p.isPrimitive()) {
        currentElement.addText(XMLTypeConverter.getInstance().toXML(value));
      } else {
        if (p.isId() && !p.isCompositeId()) {
          // special case when an id is also a foreign key, this is handled
          // through two fields, one holding the string the other the reference
          currentElement.addText(XMLTypeConverter.getInstance().toXML(value));
          final String name = p.getSimpleTypeName().substring(0, 1).toLowerCase() + p.getSimpleTypeName().substring(1);
          
          // and add a new element for the actual reference
          final Element refElement = rootElement.addElement(name);
          addReferenceElement(refElement, (BaseOBObject) obObject.get(name));
          
        } else {
          addReferenceElement(currentElement, (BaseOBObject) value);
        }
      }
    }
  }
  
  private void addReferenceElement(Element currentElement, BaseOBObject referedObject) {
    if (referedObject == null) {
      return;
    }
    // final Element refElement =
    // currentElement.addElement(REFERENCE_ELEMENT_NAME);
    currentElement.addAttribute("id", DALUtil.getId(referedObject).toString());
    currentElement.addAttribute("entityName", ModelUtil.getEntityName(referedObject));
    currentElement.addAttribute("identifier", IdentifierProvider.getInstance().getIdentifier(referedObject));
  }
  
}