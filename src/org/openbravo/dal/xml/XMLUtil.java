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

package org.openbravo.dal.xml;

import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;

/**
 * Utility class for xml processing
 * 
 * @author mtaal
 */

public class XMLUtil implements OBSingleton {

    private static XMLUtil instance;

    public static XMLUtil getInstance() {
        if (instance == null) {
            instance = OBProvider.getInstance().get(XMLUtil.class);
        }
        return instance;
    }

    public static void setInstance(XMLUtil instance) {
        XMLUtil.instance = instance;
    }

    public Document createDomDocument() {
        final Document document = DocumentHelper.createDocument();
        return document;
    }

    public Element addRootElement(Document doc, String elementName) {
        final Namespace ns = new Namespace("ob", "http://www.openbravo.com");
        final QName qName = new QName(elementName, ns);
        final Element root = doc.addElement(qName);
        root.addNamespace("ob", "http://www.openbravo.com");
        return root;
    }

    public String toString(Document document) {
        try {
            final OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            final StringWriter out = new StringWriter();
            final XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            writer.close();
            return out.toString();
        } catch (final Exception e) {
            throw new OBException(e);
        }
    }

}