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

package org.openbravo.base.model;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;

/**
 * The NamingUtil class is used to create names for the {@link Property
 * properties} of the {@link Entity entities}.
 * 
 * @author iperdomo
 * @author mtaal
 */

class NamingUtil {
    private static final Logger log = Logger.getLogger(NamingUtil.class);
    private static HashMap<String, String> specialPropertyMappings = null;
    private static HashMap<String, String> reservedNames = null;
    private static HashMap<String, String> abbreviations = null;

    static {
        specialPropertyMappings = new HashMap<String, String>();

        try {
            final Properties props = new Properties();
            props.load(NamingUtil.class
                    .getResourceAsStream("column_property_mapping.properties"));
            for (final Object keyObj : props.keySet()) {
                final String key = (String) keyObj;
                specialPropertyMappings.put(key.toLowerCase(), props
                        .getProperty(key));
            }
        } catch (final Exception e) {
            throw new OBException(e);
        }

        abbreviations = new HashMap<String, String>();
        abbreviations.put("acct", "Account");
        abbreviations.put("qty", "Quantity");
        abbreviations.put("amt", "Amount");

        reservedNames = new HashMap<String, String>();
        reservedNames.put("default", "deflt");
        reservedNames.put("import", "imprt");
        reservedNames.put("package", "pkg");
        reservedNames.put("transient", "trnsnt");
        reservedNames.put("case", "cse");
        reservedNames.put("char", "chr");
    }

    static String getSafeJavaName(String name) {
        if (reservedNames.get(name) != null) {
            return reservedNames.get(name);
        }
        return name;
    }

    static String getPropertyMappingName(Property property) {
        if (property.getColumnName() != null
                && !(property.isId() && property.getEntity().getIdProperties()
                        .size() == 1)) {
            final String checkName = (property.getEntity().getTableName() + "." + property
                    .getColumnName()).toLowerCase();
            if (specialPropertyMappings.get(checkName) != null) {
                return specialPropertyMappings.get(checkName);
            }
            if (specialPropertyMappings.get(property.getColumnName()
                    .toLowerCase()) != null) {
                return specialPropertyMappings.get(property.getColumnName()
                        .toLowerCase());
            }
        }
        String mappingName;
        if (property.isPrimitive()) {
            mappingName = property.getColumnName();

            if (property.isId()
                    && property.getEntity().getIdProperties().size() == 1) {
                mappingName = "id";
            }

            // if (property.isBoolean()
            // && mappingName.toLowerCase().startsWith("is")) {
            // String tmp = mappingName.substring(2);
            // boolean duplicated = false;
            // for (Property p : property.getEntity().getProperties()) {
            // if (tmp.equals(p.getColumnName())) {
            // duplicated = true;
            // break;
            // }
            // }
            // if (!duplicated) {
            // mappingName = tmp;
            // }
            // }
        } else {
            if (property.getTargetEntity() == null
                    && property.getColumnName() != null) {
                log.error("Property " + property
                        + " does not have a target entity");
                mappingName = property.getColumnName();
            } else if (property.isOneToMany()) {
                if ((mappingName = getOneToManyCorrectedName(property)) != null) {
                    // nothing to set anyway
                } else if (columnNameSameAsKeyColumn(property
                        .getReferencedProperty())) {
                    mappingName = property.getTargetEntity()
                            .getSimpleClassName()
                            + "List";
                } else {
                    mappingName = property.getReferencedProperty()
                            .getColumnName();
                }
            } else if (columnNameSameAsKeyColumn(property)) {
                mappingName = property.getTargetEntity().getSimpleClassName();
            } else {
                mappingName = property.getColumnName();
            }
        }
        return lowerCaseFirst(correctAbbreviations(camelCaseIt(stripUnderScores(mappingName))));
    }

    // checks if the one-to-many has a correct name
    private static String getOneToManyCorrectedName(Property p) {
        final String checkName = p.getReferencedProperty().getEntity()
                .getTableName()
                + "."
                + p.getReferencedProperty().getColumnName()
                + "."
                + p.getEntity().getTableName();
        return specialPropertyMappings.get(checkName.toLowerCase());
    }

    // checks if the columname is the same as the pk column name of the
    // target entity
    private static boolean columnNameSameAsKeyColumn(Property p) {
        final Entity targetEntity = p.getTargetEntity();
        if (p.getColumnName() == null) {
            return false;
        }
        if (targetEntity == null) {
            return true;
        }
        // more than one pk column, can not handle that here
        if (targetEntity.getIdProperties().size() != 1) {
            return false;
        }
        if (p.getReferencedProperty() == null
                || p.getReferencedProperty().getColumnName() == null) {
            return false;
        }
        return p.getColumnName().equals(
                p.getReferencedProperty().getColumnName());
    }

    private static String stripUnderScores(String mappingName) {
        String localMappingName = mappingName;
        if (localMappingName.toLowerCase().endsWith("_id")) {
            localMappingName = mappingName.substring(0,
                    mappingName.length() - 3);
        }
        final int index = localMappingName.indexOf("_");
        if (index == 1) {
            return localMappingName.substring(2);
        } else if (index == 2) {
            return localMappingName.substring(3);
        }
        return localMappingName;
    }

    private static String camelCaseIt(String mappingName) {
        String localMappingName = mappingName;
        // "CamelCasing"
        int pos = localMappingName.indexOf("_");
        while (pos != -1) {
            final String leftPart = localMappingName.substring(0, pos);
            final String camelLetter = String.valueOf(
                    localMappingName.charAt(pos + 1)).toUpperCase();
            final String rightPart = localMappingName.substring(pos + 2);
            localMappingName = leftPart + camelLetter + rightPart;
            pos = localMappingName.indexOf("_");
        }
        return localMappingName;
    }

    private static String correctAbbreviations(String value) {
        String localValue = value;
        for (final String abbreviation : abbreviations.keySet()) {
            final int index = value.toLowerCase().indexOf(abbreviation);
            if (index == 0) {
                localValue = abbreviations.get(abbreviation)
                        + value.substring(index + abbreviation.length());
            } else if (index != -1) {
                localValue = value.substring(0, index)
                        + abbreviations.get(abbreviation)
                        + value.substring(index + abbreviation.length());
            }
        }
        return localValue;
    }

    private static String lowerCaseFirst(String value) {
        if (value.length() > 1) {
            return value.substring(0, 1).toLowerCase() + value.substring(1);
        }
        return value;
    }
}
