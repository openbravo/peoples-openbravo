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

import org.openbravo.data.FieldProvider;

import java.sql.*;

import java.util.Vector;
import java.util.Enumeration;

import org.apache.log4j.Logger ;

/**
  A piece of a XmlDocument with a defined data. This class manages the connection an the query
  if there is not a FieldProvider[]
  */
class DataValue implements XmlComponentValue {
  DataTemplate dataTemplate;
  XmlDocument xmlDocument;
  SectionValue firstSectionValue;
  // field if there are a connection
  Connection connection;
  PreparedStatement preparedStatement;
  ResultSet result;

  Vector<Object> vecSectionValue;
  XmlVectorValue vecDetailValue;  // vector of XmlComponentValues 
  Vector<Object> vecFieldValue;   // vector of FieldValues
  Vector<Object> vecParameterValue;   // vector of ParameterValues for the query
  Vector<Object> vecFunctionValueData;    // vector of functions of structure
  Vector<Object> vecFunctionValueOutSection;    // vector of functions out of a Section, therefore out a Data

  StringBuffer strDetailValue;
  FieldProvider[] data = null;
  int iArray;
  FieldProvider[][] dataArray = null;

  static Logger log4jDataValue = Logger.getLogger(DataValue.class);

  public DataValue(DataTemplate dataTemplate, XmlDocument xmlDocument) {
    this.dataTemplate = dataTemplate;
    this.xmlDocument = xmlDocument;
  }

  public void initialize() {
    //vector of Fields
    vecFieldValue = new Vector<Object>();
    log4jDataValue.debug("vector of Fields");
    for (Enumeration<Object> e1 = dataTemplate.vecFieldTemplate.elements() ; e1.hasMoreElements();) {
      FieldTemplate fieldTemplate = (FieldTemplate)e1.nextElement();
      FieldValue fieldValue = fieldTemplate.createFieldValue(xmlDocument);
      vecFieldValue.addElement(fieldValue);
      log4jDataValue.debug("Field: " + fieldValue.fieldTemplate.name());
    }

    //vector of Functions
    log4jDataValue.debug("vector of Functions");
    vecFunctionValueData = new Vector<Object>();
    vecFunctionValueOutSection = new Vector<Object>();
    for (Enumeration<Object> e1 = dataTemplate.vecFunctionTemplateData.elements() ; e1.hasMoreElements();) {
      FunctionTemplate functionTemplate = (FunctionTemplate)e1.nextElement();
      FunctionValue functionValue = functionTemplate.createFunctionValue(xmlDocument);
      vecFunctionValueData.addElement(functionValue);
      if (functionValue.functionTemplate.fieldName == null) log4jDataValue.debug("Function");
      else log4jDataValue.debug("Function" + functionValue.functionTemplate.fieldName);
    }
    //vector of Functions out of the section
    log4jDataValue.debug("vector of Functions out of the section");
    for (Enumeration<Object> e1 = dataTemplate.vecFunctionTemplateOutSection.elements() ; e1.hasMoreElements();) {
      FunctionTemplate functionTemplate = (FunctionTemplate)e1.nextElement();
      FunctionValue functionValue = functionTemplate.createFunctionValue(xmlDocument);
      vecFunctionValueOutSection.addElement(functionValue);
      log4jDataValue.debug("Function (OutSection): " + functionValue.functionTemplate.fieldName);
    }

    //vector of Sections
    log4jDataValue.debug("vector of Sections");
    vecSectionValue = new Vector<Object>();
    for (Enumeration<Object> e1 = dataTemplate.vecSectionTemplate.elements() ; e1.hasMoreElements();) {
      SectionTemplate sectionTemplate = (SectionTemplate)e1.nextElement();
      SectionValue sectionValue = sectionTemplate.createSectionValue(xmlDocument, this);
      vecSectionValue.addElement(sectionValue);
      if (sectionTemplate.name.equals(dataTemplate.firstSectionTemplate.name)) {
        firstSectionValue = sectionValue;
        log4jDataValue.debug("First Section: " + sectionValue.sectionTemplate.name);
      }
      log4jDataValue.debug("Section: " + sectionValue.sectionTemplate.name);
    }

    log4jDataValue.debug("vector Detail");
    vecDetailValue = new XmlVectorValue(dataTemplate.vecDetailTemplate, xmlDocument);

    vecParameterValue = new Vector<Object>();
    for (Enumeration<Object> e1 = dataTemplate.vecParameterTemplate.elements() ; e1.hasMoreElements();) {
      ParameterTemplate parameterTemplate = (ParameterTemplate)e1.nextElement();
      ParameterValue parameterValue = parameterTemplate.createParameterValue(xmlDocument);
      vecParameterValue.addElement(parameterValue);
      log4jDataValue.debug("Parameter: " + parameterValue.parameterTemplate.strName);
    }

  }

  public void setData(FieldProvider[] data) {
    this.data = data;
  }

  public void setDataArray(FieldProvider[][] dataArray) {
    this.dataArray = dataArray;
    iArray = 0;
  }

  public String printGenerated() {
    if (firstSectionValue == null) {
      return "";
    } else {
      log4jDataValue.debug("Init()");
      init(); // delete the other init()s
      if (data == null && dataArray == null) {
        if (preparedStatement == null) {
          ErrorManagement.error(101, xmlDocument.xmlTemplate.strName + "." + dataTemplate.strName);
          return "";
        }
        query();
        return execute();
      } else {
        if (dataArray != null) { // if there is array set the actual data
          data = dataArray[iArray];
          iArray++;
        }
        return executeArray();
      }
    }
  }

  public String printPrevious() {
    return print();
  }

  public String print() {
    if (firstSectionValue == null) {
      return "";
    } else {
      return new String(firstSectionValue.strSection);
    }
  }

  public String printSimple() {
    return print();
  }

  public String printPreviousSimple() {
    return printPrevious();
  }

  public String execute() {
    int i = 0;
    //    init();
    try {
      while(result.next()) {
        readFields();
        if (i == 0) {
          firstValues();
        } else {
          check();
        }
        acumulate();   //changing the order of these two columns when the AddFunction, SubtractFunction, EqualFunction functions are added
        printDetail();
        i++;
      }
      result.close();
    } catch(SQLException e){
      log4jDataValue.error("SQL error in execute: " + e);
    }
    if (i > 0) {
      firstValues();
      firstSectionValue.close();
    }
    return new String(firstSectionValue.strSection);
  }

  public String executeArray() {
    int i = 0;
    //    init();
    log4jDataValue.info("data.length:" + data.length);
    for (i=0;i<data.length ;i++ ) {
      readFieldsArray(data[i]);
      log4jDataValue.info("data[" + i + "]");
      if (i == 0) {
        firstValues();
      } else {
        check();
      }
      acumulate();   //changing the order of these two columns when the AddFunction, SubtractFunction, EqualFunction functions are added
      printDetail();
    }
    if (data.length >0) {
      firstValues();
      firstSectionValue.close();
    }

    log4jDataValue.info("StringBuffer length:" + firstSectionValue.strSection.length());
    String strStringPrint = new String(firstSectionValue.strSection);
    log4jDataValue.info("String length:" + strStringPrint.length());
    return strStringPrint;

    //    return new String(firstSectionValue.strSection);
  }

  public String executeBlank(String strBlank) {
    init();
    setFieldsBlank(strBlank);
    firstValues();
    acumulate();   //cambiado el orden de estas dos columnas al añadir las funciones AddFunction, SubtractFunction, EqualFunction
    printDetail();
    firstSectionValue.close();
    return new String(firstSectionValue.strSection);
  }

  public void connect() {
    try {
      //        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
      log4jDataValue.info("Loading driver" // of " + strName
          + " Driver: " + dataTemplate.strDriver);
      Class.forName(dataTemplate.strDriver);
      log4jDataValue.info("Driver loaded");
    } catch(ClassNotFoundException e){
      log4jDataValue.error("Class not found: " + e);
    }
    try {
      //      String url="jdbc:odbc:report";
      connection=DriverManager.getConnection(dataTemplate.strURL);
      log4jDataValue.info("connection created");
      if (dataTemplate.strSQL != null) {
        preparedStatement = connection.prepareStatement(dataTemplate.strSQL);
        log4jDataValue.info("preparedStament created");
      }
    } catch(SQLException e){
      log4jDataValue.error("SQL error in connect: " + e);
      return;
    }
  }

  public void closeConnection() {
    try {
      connection.close();
    } catch(SQLException e){
      log4jDataValue.error("SQL error in closeConnection: " + e);
    }
  }

  public void query() {
    try {
      int i = 1;
      //XmlEngineNP: htmlComponent's parameters treatment pendant, when it is a query (SQL)
      for (Enumeration<Object> e = vecParameterValue.elements() ; e.hasMoreElements() ;) {
        ParameterValue parameter = (ParameterValue)e.nextElement();
        if (parameter.parameterTemplate.type == java.sql.Types.INTEGER) {
          log4jDataValue.info("setInt: " + i + " valor: " + Integer.parseInt(parameter.strValue));
          preparedStatement.setInt(i, Integer.parseInt(parameter.strValue));
        } else if (parameter.parameterTemplate.type == java.sql.Types.VARCHAR) {
          String strValue;
          if (parameter.xmlComponentValue != null) {
            log4jDataValue.info("value in xmlComponentValue");
            if (parameter.parameterTemplate.section != null) {
              strValue = parameter.xmlComponentValue.printPrevious();  // detailed in section print printPrevious
            } else {
              strValue = parameter.xmlComponentValue.print();  // detailed in section print printPrevious
            }
          } else {
            log4jDataValue.info("value in strValue");
            strValue = parameter.strValue;
          }
          log4jDataValue.info("setString: " + i + " name: " + parameter.parameterTemplate.strName + " value: " + strValue);
          preparedStatement.setString(i, strValue);
        }
        i++;
      }
      log4jDataValue.info("query execution:");
      result = preparedStatement.executeQuery();
      log4jDataValue.info("query done");
    } catch(SQLException e){
      log4jDataValue.error("SQL error in query: " + dataTemplate.strSQL + "Exception:"+ e);
    }
  }

  private void readFields() {
    for (Enumeration<Object> e = vecFieldValue.elements() ; e.hasMoreElements() ;) {
      FieldValue fieldValue = (FieldValue)e.nextElement();
      fieldValue.read(result);
    }
  }

  private void readFieldsArray(FieldProvider fieldProvider) {
    for (Enumeration<Object> e = vecFieldValue.elements() ; e.hasMoreElements() ;) {
      FieldValue fieldValue = (FieldValue)e.nextElement();
      fieldValue.read(fieldProvider.getField(fieldValue.fieldTemplate.name()));
    }
  }

  private void setFieldsBlank(String strBlank) {
    for (Enumeration<Object> e = vecFieldValue.elements() ; e.hasMoreElements() ;) {
      FieldValue fieldValue = (FieldValue)e.nextElement();
      fieldValue.setBlank(strBlank);
    }
  }

  private void init () {
    log4jDataValue.info("DataValue: init");
    for (Enumeration<Object> e = vecSectionValue.elements() ; e.hasMoreElements() ;) {
      SectionValue section = (SectionValue)e.nextElement();
      log4jDataValue.debug("DataValue: init, section:" + section.sectionTemplate.name);
      section.init();
    }
    firstSectionValue.strSection = new StringBuffer();

    //init for the functions out of StructureSQL
    for (Enumeration<Object> e = vecFunctionValueOutSection.elements() ; e.hasMoreElements() ;) {
      FunctionValue functionInstance = (FunctionValue)e.nextElement();
      functionInstance.init();
    }

  }

  private void firstValues() {
    for (Enumeration<Object> e = vecSectionValue.elements() ; e.hasMoreElements();) {
      SectionValue sectionValue = (SectionValue)e.nextElement();
      if (sectionValue.breakFieldValue != null) {
        sectionValue.breakFieldValue.savePrevious();
      }
    }

    for (Enumeration<Object> e = vecFieldValue.elements() ; e.hasMoreElements() ;) {
      FieldValue fieldValue = (FieldValue)e.nextElement();
      fieldValue.savePrevious();
    }

  }

  private void check () {
    boolean checked = true;
    for (Enumeration<Object> e = vecSectionValue.elements() ; e.hasMoreElements() && checked ;) {
      SectionValue sectionValue = (SectionValue)e.nextElement();
      if (!sectionValue.check()) {
        checked = false;
        sectionValue.close();
      }
    }
  }

  public void printDetail() {
    for (Enumeration<Object> e = vecDetailValue.elements() ; e.hasMoreElements() ;) {
      XmlComponentValue xmlComponentValue = (XmlComponentValue)e.nextElement();
      strDetailValue.append(xmlComponentValue.print());
    }

    //strDetailValue.append("\n");
  }

  public void acumulate() {
    for (Enumeration<Object> e = vecSectionValue.elements() ; e.hasMoreElements() ;) {
      SectionValue sectionValue = (SectionValue)e.nextElement();
      sectionValue.acumulate();
    }

    //Acumulate for the functions out of the DataTemplate
    for (Enumeration<Object> e = vecFunctionValueOutSection.elements() ; e.hasMoreElements() ;) {
      FunctionValue functionInstance = (FunctionValue)e.nextElement();
      functionInstance.acumulate();
    }
  }
}
