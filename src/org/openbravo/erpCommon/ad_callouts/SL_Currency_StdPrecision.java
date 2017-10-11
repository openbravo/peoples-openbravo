/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Callout used to validate that the currency standard precision is correct. This is, validate with
 * the specified currency precision in ISO 4217 Currency codes. If the new precision is higher than
 * expected in the specification, then a warning is shown to user.
 * 
 * @author Mark
 *
 */
public class SL_Currency_StdPrecision extends SimpleCallout {

  private static final int DEFAULT_CURRENCY_STANDARD_PRECISION = 2;
  private static final String ISO_4217_XML_FILE_NAME = "ISO_4217.xml";
  private static final String SOURCE_RELATIVE_PATH = "/src/org/openbravo/erpCommon/ad_callouts/";
  private static final String CURRENCY_TAG_NAME = "CcyNtry";
  private static final String CURRENCY_ISO_CODE_TAG_NAME = "Ccy";
  private static final String CURRENCY_PRECISION_TAG_NAME = "CcyMnrUnts";
  private static final String CURRENCY_PRECISION_NOT_ASSIGNED = "N.A.";

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Parameters
    final String paramStandardPrecision = info.getStringParameter("inpstdprecision");
    final String paramISOCode = info.getStringParameter("inpisoCode");

    if (StringUtils.isNotEmpty(paramStandardPrecision) && StringUtils.isNotEmpty(paramISOCode)) {
      final int stdPrecision = Integer.parseInt(paramStandardPrecision);
      if (stdPrecision < 0) {
        info.addResult("inpstdprecision", DEFAULT_CURRENCY_STANDARD_PRECISION);
        info.showError(Utility.messageBD(this, "CurrencyStdPrecisionNegative",
            info.vars.getLanguage()));
      } else {
        int isoCurrencyPrecision = getCurrencyPrecisionInISO4217Spec(paramISOCode);
        if (stdPrecision > isoCurrencyPrecision) {
          info.showWarning(String.format(
              Utility.messageBD(this, "CurrencyStdPrecisionHigherThanISOSpec",
                  info.vars.getLanguage()), stdPrecision, isoCurrencyPrecision, paramISOCode));
        }
      }
    }
  }

  /**
   * Find defined currency precision for specific ISO Code in ISO 4217 XML specification. If there
   * is no currency defined for ISO Code parameter, then it returns 2 as the default standard
   * precision value.
   * 
   * @param paramISOCode
   *          Currency ISO Code identifier to be found
   * @return Currency Precision in ISO Specifications or default standard precision used in system
   *         for currency standard precision
   */
  private int getCurrencyPrecisionInISO4217Spec(String paramISOCode) {

    log4j.debug("SL_Currency_StdPrecision - Starting getCurrencyPrecisionInISO4217Spec at: "
        + new Date());

    try {
      // Load the XML file with ISO 4217 Specification
      File iso4217XML = loadISO4217XMLFile();

      if (iso4217XML.exists()) {
        Document doc = createDocumentFromFile(iso4217XML);
        // Get all currency nodes
        NodeList ccyList = doc.getElementsByTagName(CURRENCY_TAG_NAME);
        return getPrecissionForCurrencyFromList(paramISOCode, ccyList);
      } else {
        log4j.error("SL_Currency_StdPrecision: No ISO0417 XML file found: " + SOURCE_RELATIVE_PATH
            + ISO_4217_XML_FILE_NAME);
        return DEFAULT_CURRENCY_STANDARD_PRECISION;
      }

    } catch (Exception e) {
      throw new OBException(e.getMessage(), e);
    } finally {
      log4j.debug("SL_Currency_StdPrecision - Ending getCurrencyPrecisionInISO4217Spec at: "
          + new Date());
    }
  }

  private File loadISO4217XMLFile() {
    long t1 = System.currentTimeMillis();
    String obDir = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("source.path");
    File iso4217XML = new File(obDir + SOURCE_RELATIVE_PATH, ISO_4217_XML_FILE_NAME);
    long t2 = System.currentTimeMillis();
    log4j.debug("SL_Currency_StdPrecision - loadISO4217XMLFile took: " + (t2 - t1) + " ms");
    return iso4217XML;
  }

  private Document createDocumentFromFile(File xmlFile) throws ParserConfigurationException,
      SAXException, IOException {
    long t1 = System.currentTimeMillis();
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(xmlFile);
    doc.getDocumentElement().normalize();
    long t2 = System.currentTimeMillis();
    log4j.debug("SL_Currency_StdPrecision - createDocumentFromFile took: " + (t2 - t1) + " ms");
    return doc;
  }

  private int getPrecissionForCurrencyFromList(String paramISOCode, NodeList ccyList) {
    // Iterate them to find currency specification for currency iso code param
    for (int currentIndex = 0; currentIndex < ccyList.getLength(); currentIndex++) {
      Node ccyNode = ccyList.item(currentIndex);

      // Checks the current node is an ELEMENT NODE
      if (ccyNode.getNodeType() == Node.ELEMENT_NODE) {

        // Gets the ISO Code and precision of current node
        String ccyIsoCode = getFirstValueWithTagName(ccyNode, CURRENCY_ISO_CODE_TAG_NAME);
        String ccyPrecision = getFirstValueWithTagName(ccyNode, CURRENCY_PRECISION_TAG_NAME);

        if (isSameCurrencyAsParameterAndIsPrecissionDefined(paramISOCode, ccyIsoCode, ccyPrecision)) {
          return Integer.parseInt(ccyPrecision);
        }
      }
    }
    // If no match has been found or no precision defined, return the default value
    return DEFAULT_CURRENCY_STANDARD_PRECISION;
  }

  private String getFirstValueWithTagName(Node ccyNode, String tagName) {
    Element element = (Element) ccyNode;
    NodeList tags = element.getElementsByTagName(tagName);
    return tags.getLength() > 0 ? tags.item(0).getTextContent() : null;
  }

  private boolean isSameCurrencyAsParameterAndIsPrecissionDefined(String paramISOCode,
      String ccyIsoCode, String ccyPrecision) {
    return StringUtils.isNotEmpty(ccyIsoCode) && StringUtils.equals(paramISOCode, ccyIsoCode)
        && StringUtils.isNotEmpty(ccyPrecision)
        && !StringUtils.equals(ccyPrecision, CURRENCY_PRECISION_NOT_ASSIGNED);
  }
}
