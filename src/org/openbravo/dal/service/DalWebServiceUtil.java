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
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.openbravo.base.exception.OBException;

/**
 * Utility class.
 * 
 * @author mtaal
 */

public class DalWebServiceUtil {
  
  private static DalWebServiceUtil instance = new DalWebServiceUtil();
  
  public static DalWebServiceUtil getInstance() {
    return instance;
  }
  
  public static void setInstance(DalWebServiceUtil instance) {
    DalWebServiceUtil.instance = instance;
  }
  
  public Document createDomDocument() {
    final Document document = DocumentHelper.createDocument();
    return document;
  }
  
  public String toString(Document document) {
    try {
      final OutputFormat format = OutputFormat.createPrettyPrint();
      format.setEncoding("UTF-8");
      final StringWriter out = new StringWriter();
      XMLWriter writer = new XMLWriter(out, format);
      writer.write(document);
      writer.close();
      return out.toString();
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
  
  public Document applyTemplate(Document sourceDocument, String template) {
    try {
      final TransformerFactory factory = TransformerFactory.newInstance();
      final InputStream is = this.getClass().getResourceAsStream(template);
      final Transformer transformer = factory.newTransformer(new StreamSource(is));
      final DocumentSource source = new DocumentSource(sourceDocument);
      final DocumentResult result = new DocumentResult();
      transformer.transform(source, result);
      
      return result.getDocument();
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
}