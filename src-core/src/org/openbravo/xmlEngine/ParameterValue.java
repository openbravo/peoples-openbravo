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

import org.openbravo.utils.Replace;

import java.util.Enumeration;

class ParameterValue implements XmlComponentValue {
  String strValue = null;
  ParameterTemplate parameterTemplate;
  XmlComponentValue xmlComponentValue = null;


  public ParameterValue(ParameterTemplate ParameterTemplate, XmlDocument xmlDocument) {
    this.parameterTemplate = ParameterTemplate;
  }

  public void setXmlComponentValue(XmlDocument xmlDocument) {
    if (parameterTemplate.xmlComponentTemplate != null) {
      xmlComponentValue = parameterTemplate.xmlComponentTemplate.createXmlComponentValue(xmlDocument.parentXmlDocument);
    }
  }

  private String replace(String strIni) {
    if (parameterTemplate.vecReplace != null) {
      String strFin = strIni;
      for (Enumeration<ReplaceElement> e = parameterTemplate.vecReplace.elements() ; e.hasMoreElements();) {
        ReplaceElement replaceElement = e.nextElement();
        strFin = Replace.replace(strFin, replaceElement.replaceWhat, replaceElement.replaceWith);
      }
      return strFin;
    } else {
      return strIni;
    }
  }


  public void setValue(String strValue) {
    this.strValue = replace(strValue);
  }

  public String print() {
    if (xmlComponentValue != null) {
      return xmlComponentValue.print();
    } else {
      return strValue;
    }
  }

  public String printPrevious() {
    if (xmlComponentValue != null) {
      return xmlComponentValue.printPrevious();
    } else {
      return strValue;
    }
  }

  public String printSimple() {
    if (xmlComponentValue != null) {
      return xmlComponentValue.printSimple();
    } else {
      return strValue;
    }
  }

  public String printPreviousSimple() {
    if (xmlComponentValue != null) {
      return xmlComponentValue.printPreviousSimple();
    } else {
      return strValue;
    }
  }
}
