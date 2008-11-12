package org.openbravo.base.util;

import org.openbravo.base.exception.OBException;

/**
 * Unchecked state exception which also logs itself.
 * 
 * @author mtaal
 */
public class CheckException extends OBException {

    private static final long serialVersionUID = 1L;

    public CheckException() {
	super();
    }

    public CheckException(String message, Throwable cause) {
	super(message, cause);
    }

    public CheckException(String message) {
	super(message);
    }

    public CheckException(Throwable cause) {
	super(cause);
    }
}
