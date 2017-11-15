package org.openbravo.erpCommon.utility;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ISOCurrencyPrecision {
  private static Logger log4j = Logger.getLogger(HttpsUtils.class);
  private static final int DEFAULT_CURRENCY_STANDARD_PRECISION = 2;
  private static final String ISO_4217_XML_FILE_NAME = "ISO_4217.xml";
  private static final String SOURCE_RELATIVE_PATH = "/src/org/openbravo/erpCommon/ad_callouts/";
  private static final String CURRENCY_TAG_NAME = "CcyNtry";
  private static final String CURRENCY_ISO_CODE_TAG_NAME = "Ccy";
  private static final String CURRENCY_PRECISION_TAG_NAME = "CcyMnrUnts";
  private static final String CURRENCY_PRECISION_NOT_ASSIGNED = "N.A.";

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
  public static int getCurrencyPrecisionInISO4217Spec(String paramISOCode) {

    log4j.debug("SL_Currency_StdPrecision - Starting getCurrencyPrecisionInISO4217Spec at: "
        + new Date());

    try {
      // Load the XML file with ISO 4217 Specification
      File iso4217XML = loadISO4217XMLFile();

      if (iso4217XML.exists()) {
        Document doc = createDocumentFromFile(iso4217XML);
        // Get all currency nodes
        NodeList ccyList = doc.getElementsByTagName(CURRENCY_TAG_NAME);
        return getPrecisionForCurrencyFromList(paramISOCode, ccyList);
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

  private static File loadISO4217XMLFile() {
    long t1 = System.currentTimeMillis();
    String obDir = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("source.path");
    File iso4217XML = new File(obDir + SOURCE_RELATIVE_PATH, ISO_4217_XML_FILE_NAME);
    long t2 = System.currentTimeMillis();
    log4j.debug("SL_Currency_StdPrecision - loadISO4217XMLFile took: " + (t2 - t1) + " ms");
    return iso4217XML;
  }

  private static Document createDocumentFromFile(File xmlFile) throws ParserConfigurationException,
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

  private static int getPrecisionForCurrencyFromList(String paramISOCode, NodeList ccyList) {
    // Iterate them to find currency specification for currency iso code param
    for (int currentIndex = 0; currentIndex < ccyList.getLength(); currentIndex++) {
      Node ccyNode = ccyList.item(currentIndex);

      // Checks the current node is an ELEMENT NODE
      if (ccyNode.getNodeType() == Node.ELEMENT_NODE) {

        // Gets the ISO Code and precision of current node
        String ccyIsoCode = getFirstValueWithTagName(ccyNode, CURRENCY_ISO_CODE_TAG_NAME);
        String ccyPrecision = getFirstValueWithTagName(ccyNode, CURRENCY_PRECISION_TAG_NAME);

        if (isSameCurrencyAsParameterAndIsPrecisionDefined(paramISOCode, ccyIsoCode, ccyPrecision)) {
          return Integer.parseInt(ccyPrecision);
        }
      }
    }
    // If no match has been found or no precision defined, return the default value
    return DEFAULT_CURRENCY_STANDARD_PRECISION;
  }

  private static String getFirstValueWithTagName(Node ccyNode, String tagName) {
    Element element = (Element) ccyNode;
    NodeList tags = element.getElementsByTagName(tagName);
    return tags.getLength() > 0 ? tags.item(0).getTextContent() : null;
  }

  private static boolean isSameCurrencyAsParameterAndIsPrecisionDefined(String paramISOCode,
      String ccyIsoCode, String ccyPrecision) {
    return StringUtils.isNotEmpty(ccyIsoCode) && StringUtils.equals(paramISOCode, ccyIsoCode)
        && StringUtils.isNotEmpty(ccyPrecision)
        && !StringUtils.equals(ccyPrecision, CURRENCY_PRECISION_NOT_ASSIGNED);
  }

}
