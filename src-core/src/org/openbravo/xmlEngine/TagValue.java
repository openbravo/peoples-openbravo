/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
package org.openbravo.xmlEngine;

import java.util.Vector;
import java.util.Enumeration;

import org.apache.log4j.Logger ;

class TagValue implements XmlComponentValue {
  protected TagTemplate tagTemplate;
  protected Vector<AttributeItemValue> attributeVectorValue; // vector of AttributeItemValues

  static Logger log4jTagValue = Logger.getLogger(TagValue.class);

  public TagValue(TagTemplate tagTemplate, XmlDocument xmlDocument) {
    this.tagTemplate = tagTemplate;
    attributeVectorValue = new Vector<AttributeItemValue>();
    for (Enumeration<AttributeItemTemplate> e1 = tagTemplate.attributeVectorTemplate.elements() ; e1.hasMoreElements();) {
      AttributeItemTemplate attributeItemTemplate = e1.nextElement();
      AttributeItemValue attributeItemValue = attributeItemTemplate.createAttributeItemValue(xmlDocument);
      attributeVectorValue.addElement(attributeItemValue);
      log4jTagValue.debug("TagValue: " + attributeItemValue.attributeItemTemplate.name);
    }

  }

  public String print() {
    String character = "";
    if (tagTemplate.tag().equals("DIVFO") || tagTemplate.tag().endsWith("_TMP")) return "";
    character = "<" + tagTemplate.tag();
    for (Enumeration<AttributeItemValue> e = attributeVectorValue.elements() ; e.hasMoreElements() ;) {
      AttributeItemValue attribute = e.nextElement();
      if (attribute.attributeItemTemplate.attributeBoolean) {
        String myValue = "";
        try {
          myValue = attribute.valueToCompare.print();
        } catch (Exception ex) {
          myValue = "";
        }
        if (attribute.value.print().equals(myValue)) {
          character += " " + attribute.attributeItemTemplate.name;
        }
      } else {
        character += attribute.print();
      }
    }
    character += ">";
    return character;
  }

  public String printPrevious() {
    if (tagTemplate.tag().equals("DIVFO")) return "";
    String character = "<" + tagTemplate.tag();
    for (Enumeration<AttributeItemValue> e = attributeVectorValue.elements() ; e.hasMoreElements() ;) {
      AttributeItemValue attribute = e.nextElement();
      character = character + attribute.printPrevious();
    }
    character = character + ">";
    return character;
  }

  // in this class don´t have sense the prints Simple
  public String printSimple() {
    return print();
  }

  public String printPreviousSimple() {
    return printPrevious();
  }}
