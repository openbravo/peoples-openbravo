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
package org.openbravo.base.provider;

import java.io.FileInputStream;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.openbravo.base.util.Check;
import org.openbravo.base.util.OBClassLoader;

/**
 * Reads the provider config file and processes it. The provider config file can
 * be used to configure the OBProvider. See the provider config xml file(s) in
 * the WEB-INF directory for examples.
 * 
 * @author mtaal
 */
public class OBProviderConfigReader {

    private static final long serialVersionUID = 1L;

    public void read(String prefix, InputStream is) {
	try {
	    final SAXReader reader = new SAXReader();
	    final Document doc = reader.read(is);
	    process(prefix, doc);
	} catch (final Exception e) {
	    throw new OBProviderException(e);
	}
    }

    public void read(String prefix, String fileLocation) {
	try {
	    final SAXReader reader = new SAXReader();
	    final Document doc = reader.read(new FileInputStream(fileLocation));
	    process(prefix, doc);
	} catch (final Exception e) {
	    throw new OBProviderException(e);
	}
    }

    public void process(String prefix, Document doc) {
	checkName(doc.getRootElement(), "provider");
	for (final Object o : doc.getRootElement().elements()) {
	    final Element elem = (Element) o;
	    checkName(elem, "bean");
	    // now check for three children:
	    final String name = getValue(elem, "name", true);
	    final String clzName = getValue(elem, "class", true);
	    final Class<?> clz = OBClassLoader.getInstance().loadClass(clzName);
	    if (OBModulePrefixRequired.class.isAssignableFrom(clz)
		    && prefix != null && prefix.trim().length() > 0) {
		OBProvider.getInstance().register(prefix + "." + name, clz,
			true);
	    } else {
		OBProvider.getInstance().register(name, clz, true);
	    }
	}
    }

    private String getValue(Element parentElem, String name, boolean mandatory) {
	final Element valueElement = parentElem.element(name);
	if (mandatory) {
	    Check.isNotNull(valueElement, "Element with name " + name
		    + " not found");
	} else if (valueElement == null) {
	    return null;
	}
	return valueElement.getText();
    }

    private void checkName(Element elem, String expectedName) {
	Check.isTrue(elem.getName().equals(expectedName),
		"The element should have the name: " + expectedName
			+ " but is has name " + elem.getName());
    }
}
