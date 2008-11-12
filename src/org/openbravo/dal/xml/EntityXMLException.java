package org.openbravo.dal.xml;

import org.openbravo.base.exception.OBException;

/**
 * Unchecked state exception which also logs itself.
 * 
 * @author mtaal
 */
public class EntityXMLException extends OBException {

    private static final long serialVersionUID = 1L;

    public EntityXMLException() {
	super();
    }

    public EntityXMLException(String message, Throwable cause) {
	super(message, cause);
    }

    public EntityXMLException(String message) {
	super(message);
    }

    public EntityXMLException(Throwable cause) {
	super(cause);
    }
}
