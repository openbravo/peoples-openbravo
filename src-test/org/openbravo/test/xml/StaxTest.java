/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.xml;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.helpers.AttributesImpl;

/**
 * Tests various aspects of stax.
 * 
 * @author mtaal
 */

public class StaxTest extends XMLBaseTest {

  public void testStaxRead() throws Exception {
    final String countryXML = getFileContent("country.xml");

    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLStreamReader r = factory.createXMLStreamReader(new StringReader(countryXML));
    try {
      int event = r.getEventType();
      while (true) {
        switch (event) {
        case XMLStreamConstants.START_DOCUMENT:
          System.err.println("Start Document.");
          break;
        case XMLStreamConstants.START_ELEMENT:
          System.err.println("Start Element: " + r.getLocalName());
          for (int i = 0, n = r.getAttributeCount(); i < n; ++i)
            System.err
                .println("Attribute: " + r.getAttributeName(i) + "=" + r.getAttributeValue(i));
          if (r.getLocalName().compareTo("Country") != 0
              && r.getLocalName().compareTo("Openbravo") != 0) {
            // System.err.println("ElementText: " + r.getElementText());
          }

          break;
        case XMLStreamConstants.CHARACTERS:
          if (r.isWhiteSpace())
            break;

          System.err.println("Text: " + r.getText());
          break;
        case XMLStreamConstants.CDATA:
          if (r.isWhiteSpace())
            break;

          System.err.println("CDATA: " + r.getText());
          break;
        case XMLStreamConstants.END_ELEMENT:
          System.err.println("End Element:" + r.getName());
          break;
        case XMLStreamConstants.END_DOCUMENT:
          System.err.println("End Document.");
          break;
        }

        if (!r.hasNext())
          break;

        event = r.next();
      }
    } finally {
      r.close();
    }

  }

  public void testWriteSax() throws Exception {
    StringWriter sw = new StringWriter();
    // XMLOutputFactory factory = XMLOutputFactory.newInstance();
    // XMLStreamWriter writer = factory.createXMLStreamWriter(sw);
    // writer.writeStartDocument("ISO-8859-1", "1.0");
    // writer.writeStartElement("greeting");
    // writer.writeAttribute("id", "g1 &\"");
    // writer.writeCharacters("Hello StAX &");
    // writer.writeEndDocument();
    // writer.flush();
    // writer.close();
    // System.err.println(sw.toString());

    StreamResult streamResult = new StreamResult(sw);
    SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    // SAX2.0 ContentHandler.
    TransformerHandler hd = tf.newTransformerHandler();
    Transformer serializer = hd.getTransformer();
    serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    serializer.setOutputProperty(OutputKeys.VERSION, "1.0");
    serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    serializer.setOutputProperty(OutputKeys.INDENT, "yes");
    hd.setResult(streamResult);
    hd.startDocument();

    AttributesImpl atts = new AttributesImpl();
    // USERS tag.
    hd.startElement("", "", "USERS", atts);
    // USER tags.
    String[] id = { "PWD122", "MX787", "A4Q457&" };
    String[] type = { "customer", "manager", "employee" };
    String[] desc = { "Tim@Home", "Jack&Moud", "John D'oé" };
    for (int i = 0; i < id.length; i++) {
      atts.clear();
      atts.addAttribute("", "", "ID", "CDATA", id[i]);
      atts.addAttribute("", "", "TYPE", "CDATA", type[i]);
      hd.startElement("", "", "USER", atts);
      hd.startElement("", "", "USER2", null);
      hd.characters(desc[i].toCharArray(), 0, desc[i].length());
      hd.endElement("", "", "USER2");
      hd.startElement("", "", "USER3", null);
      hd.endElement("", "", "USER3");
      hd.endElement("", "", "USER");
    }
    hd.endElement("", "", "USERS");
    hd.endDocument();
    System.err.println(sw.toString());
  }
}