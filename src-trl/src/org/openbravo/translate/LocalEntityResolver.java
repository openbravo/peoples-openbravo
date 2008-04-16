package org.openbravo.translate;

import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 * Local entiry resolver for known entities. Currently only used for jasperreports DTD
 *
 */
public class LocalEntityResolver implements EntityResolver {

	//TODO Create a system propety for this:
	static final String C_DTD_PATH="../dtds";
	//@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {

		if (systemId.equals("http://jasperreports.sourceforge.net/dtds/jasperreport.dtd")) {
			// return a special input source
			return new InputSource(new FileReader(C_DTD_PATH + "/jasperreport.dtd"));
		} else {
			// Use default behaviour.
			return null;
		}
	}

}
