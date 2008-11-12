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

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.CPStandAlone;
import org.openbravo.exception.PoolNotFoundException;
import org.openbravo.uiTranslation.TranslationHandler;
import org.openbravo.uiTranslation.TranslationUtils;
import org.openbravo.uiTranslation.WindowLabel;

import com.sun.corba.se.pept.transport.Connection;

public class XmlDocument implements XmlComponentValue {
  XmlTemplate xmlTemplate;
  XmlDocument parentXmlDocument;
  Hashtable<String, XmlDocument> hasSubXmlDocuments;  // hashtable of SubXmlDocuments corresponding to SubXmlTemplates
  Hashtable<String, DataValue> hasDataValue;  // hashtable of DataValue
  Hashtable<String, ParameterValue> hasParameterValue; // contains the ParameterValue that not are for the SQL query
  Hashtable<String, LabelValue> hasLabelValue; //contains the label values in the template
  XmlVectorValue xmlVectorValue;  //contains Xml Components before, after and between DataValues and the DataValues
  Hashtable<Object, Object> hasXmlComponentValue; // contains the XmlComponentValues of the document CHX (Change to Hashtable of XmlComponentValue
  public boolean ignoreTranslation = false;
  // it store pairs of XmlComponentTemplate (key), XmlComponentValue (value)
  

  static Logger log4jXmlDocument = Logger.getLogger(XmlDocument.class);

  public XmlDocument(XmlTemplate xmlTemplate, XmlDocument parentXmlDocument) {
    this.xmlTemplate = xmlTemplate;
    this.parentXmlDocument = parentXmlDocument;
    //  }

    //  public void initialize() {

    //hashtable of XmlComponenteValues
    hasXmlComponentValue = new Hashtable<Object, Object>();  //CHX 

    //vector of Subdocuments
    hasSubXmlDocuments = new Hashtable<String, XmlDocument>();
    for (Enumeration<Object> e1 = xmlTemplate.hasSubXmlTemplates.elements() ; e1.hasMoreElements();) {
      XmlTemplate subXmlTemplate = (XmlTemplate)e1.nextElement();
      log4jXmlDocument.debug("Creation of SubXmlTemplate: " + subXmlTemplate.strName);
      XmlDocument subXmlDocument = subXmlTemplate.createXmlDocument(this);
      hasSubXmlDocuments.put(subXmlTemplate.strName, subXmlDocument);
    }

    //  vector of Parameters (not for the SQL query's)
    hasParameterValue = new Hashtable<String, ParameterValue>();
    for (ParameterTemplate parameterTemplate : xmlTemplate.hasParameterTemplate.values()) {
      ParameterValue parameterValue = parameterTemplate.createParameterValue(this);
      parameterValue.strValue = parameterTemplate.strDefault;
      hasParameterValue.put(parameterTemplate.strName, parameterValue);
      log4jXmlDocument.debug("Parameter: " + parameterValue.parameterTemplate.strName + " valor: " + parameterValue.strValue );
    }

    hasLabelValue = new Hashtable<String, LabelValue>();
    for (LabelTemplate labelTemplate : xmlTemplate.hasLabelTemplate.values()) {
      log4jXmlDocument.debug("hasLabelTemplate: " + xmlTemplate.hasLabelTemplate.size());
      LabelValue labelValue = labelTemplate.createLabelValue(this);
      log4jXmlDocument.debug("LabelValue created: " + labelValue.strValue);
      hasLabelValue.put(labelTemplate.strName, labelValue);
      log4jXmlDocument.debug("Label: " + labelValue.labelTemplate.strName);
    }

    //vector of Data
    hasDataValue = new Hashtable<String, DataValue>();
    for (Enumeration<Object> e1 = xmlTemplate.hasDataTemplate.elements() ; e1.hasMoreElements();) {
      DataTemplate dataTemplate = (DataTemplate)e1.nextElement();
      log4jXmlDocument.debug("Creation of Data: " + dataTemplate.strName);
      DataValue dataValue = dataTemplate.createDataValue(this);
      hasDataValue.put(dataTemplate.strName, dataValue);
      dataValue.initialize();
      log4jXmlDocument.debug("End of Data: " + dataValue.dataTemplate.strName);
    }
    

    //parameters of vector of Subdocuments
    log4jXmlDocument.debug("parameters of subdocuments: ");
    for (XmlDocument subXmlDocument : hasSubXmlDocuments.values()) {
      log4jXmlDocument.debug("parameters of subdocument: " + subXmlDocument.xmlTemplate.strName);
      subXmlDocument.setXmlComponentValueParameters();
      subXmlDocument.setXmlComponentValueLabels();
      log4jXmlDocument.debug("parameters of data values of subdocument: " + subXmlDocument.xmlTemplate.strName);
      subXmlDocument.setXmlComponentValueParametersOfDataValues();
    }

    xmlVectorValue = new XmlVectorValue(xmlTemplate.vecXmlVector, this);


    /* it comes from the XmlTemplate clear()
       for (Enumeration e = hasDataTemplate.elements() ; e.hasMoreElements();) {
       DataTemplate elementDataTemplate = (DataTemplate)e.nextElement();
       elementDataTemplate.clear();
       }  //*** see if this loop necessary or if it is here whre the DataTemplate clear is executed
       */

    }

public XmlDocument() {
  hasDataValue = new Hashtable<String, DataValue>();
  hasParameterValue = new Hashtable<String, ParameterValue>();
  hasLabelValue = new Hashtable<String, LabelValue>();
}

private void setXmlComponentValueParameters() {
  for (ParameterValue parameterValue : hasParameterValue.values()) {
    parameterValue.setXmlComponentValue(this);
    log4jXmlDocument.debug("setXmlComponentValue: " + parameterValue.parameterTemplate.strName);
  }
}

private void setXmlComponentValueLabels() {
  log4jXmlDocument.debug("setXmlComponentValueLabels() - hasLabelValues size: " + hasLabelValue.size());
  for (LabelValue labelValue : hasLabelValue.values()) {
    labelValue.setXmlComponentValue(this);
    log4jXmlDocument.debug("setXmlComponentValueLabels() - labelValue.labelTemplate.strName: " + labelValue.labelTemplate.strName);
  }
}

private void setXmlComponentValueParametersOfDataValues() {
  for (DataValue dataValue : hasDataValue.values()) {
    for (Enumeration<Object> e2 = dataValue.vecParameterValue.elements() ; e2.hasMoreElements();) {
      ParameterValue parameter = (ParameterValue)e2.nextElement();
      parameter.setXmlComponentValue(this);
      log4jXmlDocument.debug("setXmlComponentValue of Parameter of DataValue: " + parameter.parameterTemplate.strName);
    }
  }
}

public void setData(String dataName, FieldProvider[] data) {
  DataValue dataValue = hasDataValue.get(dataName);
  if (dataValue == null) {
    log4jXmlDocument.warn("Data: " + dataName + " not found in " + xmlTemplate.strName );
  }
  dataValue.setData(data);
}

public void setDataArray(String dataName, FieldProvider[][] dataArray) {
  DataValue dataValue = hasDataValue.get(dataName);
  if (dataValue == null) {
    log4jXmlDocument.warn("Data: " + dataName + " not found in " + xmlTemplate.strName );
  }
  dataValue.setDataArray(dataArray);
}

// setData for the subXmlDocuments
public void setData(String subXmlDocumentName, String dataName, FieldProvider[] data) {
  log4jXmlDocument.debug("setData FieldProvider[] of dataName: " + dataName + " in subDocument " + subXmlDocumentName + " in " + xmlTemplate.strName );
  XmlDocument xmlDocument = hasSubXmlDocuments.get(xmlTemplate.strName + "&" + subXmlDocumentName);
  if (xmlDocument == null) {
    log4jXmlDocument.warn("Subdocument: " + subXmlDocumentName + " not found in " + xmlTemplate.strName );
  }
  xmlDocument.setData(dataName, data);
}

public void setDataArray(String subXmlDocumentName, String dataName, FieldProvider[][] dataArray) {
  log4jXmlDocument.debug("setData FieldProvider[][] of dataName: " + dataName + " in subDocument " + subXmlDocumentName + " in " + xmlTemplate.strName );
  XmlDocument xmlDocument = hasSubXmlDocuments.get(xmlTemplate.strName + "&" + subXmlDocumentName);
  if (xmlDocument == null) {
    log4jXmlDocument.warn("Subdocument: " + subXmlDocumentName + " not found in " + xmlTemplate.strName );
  }
  xmlDocument.setDataArray(dataName, dataArray);
}

public void setParameter(String strName, String strValue) {
  ParameterValue parameter = hasParameterValue.get(strName);
  if (parameter != null) parameter.setValue(strValue);
  if (parameter!=null && !parameter.parameterTemplate.strName.equals("menu")) {
    log4jXmlDocument.debug("Parameter: " + parameter.parameterTemplate.strName + " valor: " + parameter.print() );
  }
}

public void setLabel(String strName, String strValue) {
  log4jXmlDocument.debug("###setLabel(): trying to setLabel with strName: " + strName);
  if (strName != null && !strName.equals("")) {
    if (hasLabelValue.get(strName) != null) {
      LabelValue label = hasLabelValue.get(strName);
      if (label != null) label.setValue(strValue);
      if (label!=null && !label.labelTemplate.strName.equals("menu")) {
        log4jXmlDocument.debug("###setLabel(): " + label.labelTemplate.strName + " value: " + label.print() );
      }
    }
  }
}

public String print() {
  return print(null);
}

public String print(String strBlank) {
    if (!ignoreTranslation && xmlTemplate != null && xmlTemplate.strName != null) {
      if(log4jXmlDocument.isDebugEnabled()) log4jXmlDocument.debug("Start of print of: "+ xmlTemplate.strName);
      TranslationHandler handler = new TranslationHandler(this);
      handler.setFileName(xmlTemplate.strName.replace("designorg/", "/org/") + ".html");
      
      log4jXmlDocument.debug("print() - xmlTemplate.xmlEngine.fileBaseLocation: " + xmlTemplate.xmlEngine.fileBaseLocation);
      handler.setPropertiesFile(xmlTemplate.xmlEngine.fileBaseLocation + "/../../WEB-INF/Openbravo.properties");
      
      if (hasParameterValue != null && !hasParameterValue.isEmpty()) {
        if (hasParameterValue.get("tabId") != null && hasParameterValue.get("tabId").strValue != null) {
          log4jXmlDocument.debug("print(strBlank) - tabId: " + hasParameterValue.get("tabId").strValue);
          handler.setTabId(hasParameterValue.get("tabId").strValue);
          handler.setXmlDocumentType(TranslationHandler.ADWINDOW);
        }
        if (hasParameterValue.get("processId") != null && hasParameterValue.get("processId").strValue != null && !hasParameterValue.get("processId").strValue.equals("")) {
          //String formType = hasParameterValue.get("trlFormType").strValue;
          //if (formType.equalsIgnoreCase("PROCESS")) 
          log4jXmlDocument.debug("ProcessId: " + hasParameterValue.get("processId").strValue);
          handler.setXmlDocumentType(TranslationHandler.PROCESS);
          handler.setXmlDocumentTypeId(hasParameterValue.get("processId").strValue);
        }
        //CPStandAlone cpStandAlone = new CPStandAlone(xmlTemplate.xmlEngine.fileBaseLocation + "/../../WEB-INF/Openbravo.properties");
        if (hasParameterValue.get("language") != null && hasParameterValue.get("language").strValue != null) {
          log4jXmlDocument.debug("print(strBlank) - language: " + hasParameterValue.get("language").strValue);
          handler.setLanguage(hasParameterValue.get("language").strValue.replace("defaultLang=", "").replace("\"", ""));
        }
        log4jXmlDocument.debug("before running generateTranslations.");
        handler.generateTranslations();
        if (!ignoreTranslation && handler.getFormLabels() != null && !handler.getFormLabels().isEmpty()) {
          xmlVectorValue.setTextMap(handler.getFormLabels());
        }
        if (xmlVectorValue != null) xmlVectorValue.handler = handler;
      } else {
        log4jXmlDocument.debug("print(String strBlank) - properties file not found");
      }
    } else {
      log4jXmlDocument.debug("print(String strBlank) - hasParameterValue is null");
    }
      
  for (DataValue elementDataValue : hasDataValue.values()) {
    if (strBlank != null && !strBlank.equals("")) {
      elementDataValue.executeBlank(strBlank);
    } else {
      elementDataValue.printGenerated();
      //log4jXmlDocument.debug("printGenerated of "+ elementDataValue.dataTemplate.strName + " lng: " + elementDataValue.firstSectionValue.strSection.length());
    }
  }

  StringBuffer strPrint = xmlVectorValue.printStringBuffer();
  //if (strPrint != null) log4jXmlDocument.debug("print(String strBlank) - returning strPrint: " + strPrint);
  //    log4jXmlDocument.debug("XmlDocument: StringBuffer length:" + strPrint.length() + " de " + xmlTemplate.strName);
  /*String strStringPrint = new String(strPrint);
    log4jXmlDocument.debug("XmlDocument: String length:" + strStringPrint.length());*/
  return strPrint.toString();
}

public String printPrevious() {
  return print();
}

public String printSimple() {
  return print();
}

public String printPreviousSimple() {
  return printPrevious();
}

public void connect() {
  for (DataValue elementDataValue : hasDataValue.values()) {
    elementDataValue.connect();
  }
}

}
