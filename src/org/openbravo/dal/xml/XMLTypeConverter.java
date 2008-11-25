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
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.xml;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;

/**
 * Converts primitive types from xml and back.
 * 
 * @author mtaal
 */

public class XMLTypeConverter implements OBSingleton {

    private static XMLTypeConverter instance = new XMLTypeConverter();

    public static XMLTypeConverter getInstance() {
        if (instance == null) {
            instance = OBProvider.getInstance().get(XMLTypeConverter.class);
        }
        return instance;
    }

    public static void setInstance(XMLTypeConverter instance) {
        XMLTypeConverter.instance = instance;
    }

    private final SimpleDateFormat xmlDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.S'Z'");

    public String toXML(Date dt) {
        return xmlDateFormat.format(dt);
    }

    public String toXML(Number number) {
        return number.toString();
    }

    public String toXML(String str) {
        return str;
    }

    public String toXML(Boolean b) {
        return b.toString();
    }

    public String toXML(Object o) {
        if (o == null) {
            return "";
        }
        if (o instanceof Number) {
            return toXML((Number) o);
        }
        if (o instanceof Date) {
            return toXML((Date) o);
        }
        if (o instanceof String) {
            return toXML((String) o);
        }
        if (o instanceof Boolean) {
            return toXML((Boolean) o);
        }
        return o.toString();
        // throw new OBException("Type " + o.getClass().getName() +
        // " not supported");
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T fromXML(Class<T> targetClass, String xml) {
        if (xml.trim().length() == 0) {
            return null;
        }
        try {
            if (Date.class == targetClass) {
                return (T) xmlDateFormat.parse(xml);
            }
            if (Timestamp.class == targetClass) {
                final Date dt = xmlDateFormat.parse(xml);
                return (T) new Timestamp(dt.getTime());
            }
            if (String.class == targetClass) {
                return (T) xml;
            }
            if (BigDecimal.class == targetClass) {
                return (T) new BigDecimal(xml);
            }
            if (Integer.class == targetClass) {
                return (T) new Integer(xml);
            }
            if (Long.class == targetClass) {
                return (T) new Long(xml);
            }
            if (boolean.class == targetClass) {
                return (T) new Boolean(xml);
            }
            if (Boolean.class == targetClass) {
                return (T) new Boolean(xml);
            }
            if (Float.class == targetClass) {
                return (T) new Float(xml);
            }
        } catch (final Exception e) {
            throw new EntityXMLException("Value " + xml
                    + " can not be parsed to an instance of class "
                    + targetClass.getName());
        }
        throw new EntityXMLException("Unsupported target class "
                + targetClass.getName());
    }

    public String toXMLSchemaType(Class<?> targetClass) {
        if (Date.class == targetClass) {
            return "dateTime";
        }
        if (Timestamp.class == targetClass) {
            return "dateTime";
        }
        if (String.class == targetClass) {
            return "string";
        }
        if (BigDecimal.class == targetClass) {
            return "decimal";
        }
        if (Integer.class == targetClass) {
            return "integer";
        }
        if (Long.class == targetClass) {
            return "long";
        }
        if (boolean.class == targetClass) {
            return "boolean";
        }
        if (Boolean.class == targetClass) {
            return "boolean";
        }
        if (Float.class == targetClass) {
            return "float";
        }
        if (Object.class == targetClass) {
            // TODO catch this
            return "OBJECT";
        }
        if (targetClass == null) {
            // TODO catch this
            return "NULL";
        }
        throw new OBException("Unsupported target class "
                + targetClass.getName());
    }
}