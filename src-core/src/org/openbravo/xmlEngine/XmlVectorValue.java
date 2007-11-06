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

public class XmlVectorValue extends Vector<Object> {

  private static final long serialVersionUID = 1L;
  static Logger log4jXmlVectorValue = Logger.getLogger(XmlVectorValue.class);

  public XmlVectorValue(XmlVectorTemplate xmlVectorTemplate, XmlDocument xmlDocument) {
    for (Enumeration<Object> e = xmlVectorTemplate.elements() ; e.hasMoreElements() ;) {
      XmlComponentTemplate xmlComponentTemplate = (XmlComponentTemplate)e.nextElement();
      log4jXmlVectorValue.debug("Adding XmlComponentTemplate");
      addElement(xmlComponentTemplate.createXmlComponentValue(xmlDocument));
    }

  }

  StringBuffer printStringBuffer() {
    StringBuffer str = new StringBuffer();
    for (Enumeration<Object> e = elements() ; e.hasMoreElements() ;) {
      XmlComponentValue xmlComponentValue = (XmlComponentValue)e.nextElement();
      str.append(xmlComponentValue.print());
      //      log4jXmlVectorValue.debug("Añadido XmlComponentValue, longitud actual:" + str.length());
    }
    //str.append("\n");
    return str;
  }

  String print() {
    return printStringBuffer().toString();
  }

  StringBuffer printPreviousStringBuffer() {
    StringBuffer str = new StringBuffer();
    for (Enumeration<Object> e = elements() ; e.hasMoreElements() ;) {
      XmlComponentValue xmlComponentValue = (XmlComponentValue)e.nextElement();
      str.append(xmlComponentValue.printPrevious());
      //      log4jXmlVectorValue.debug("Añadido XmlComponentValue, longitud actual:" + str.length());
    }
    //str.append("\n");
    return str;
  }

  String printPrevious() {
    return printStringBuffer().toString();
  }
}
