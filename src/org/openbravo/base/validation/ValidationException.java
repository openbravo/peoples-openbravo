package org.openbravo.base.validation;

import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Property;

/**
 * Is thrown when an entity is invalid. Prevents that it is logged as this one
 * does not need to be logged.
 * 
 * @author mtaal
 */
public class ValidationException extends OBException {

    private static final long serialVersionUID = 1L;

    private Map<Property, String> msgs = new HashMap<Property, String>();

    public ValidationException() {
        super();
    }

    public void addMessage(Property p, String msg) {
        msgs.put(p, msg);
    }

    public boolean hasMessages() {
        return !msgs.isEmpty();
    }

    @Override
    public String getMessage() {
        if (msgs == null) {
            // during construction
            return "";
        }
        final StringBuffer sb = new StringBuffer();
        for (final Property p : msgs.keySet()) {
            final String msg = msgs.get(p);
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(p.getName() + ": " + msg);
        }
        return sb.toString();
    }
}
