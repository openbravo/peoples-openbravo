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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;

/**
 * Encapsulates the logic to translate filter properties and values received from the client to a
 * query, including the order by clause. This generator assumes or logic between the different query
 * parts.
 * 
 * NOTE: this class is not used at the moment.
 * 
 * @author mtaal
 */
public class QueryGeneratorOrLogic {

  public static enum TextMatching {
    startsWith, exact, substring
  }

  private static final Logger log = Logger.getLogger(QueryGeneratorOrLogic.class);

  private static final long serialVersionUID = 1L;

  private Map<String, String> filterParameters = new HashMap<String, String>();
  private List<Object> typedParameters = new ArrayList<Object>();
  private String whereClause = null;
  private Entity entity;
  private boolean doOr = false;

  private TextMatching textMatching = TextMatching.exact;

  public Entity getEntity() {
    return entity;
  }

  public void setEntity(String entityName) {
    this.entity = ModelProvider.getInstance().getEntity(entityName);
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  // compute the aliases, joins
  public void initialize() {

  }

  /**
   * Translates the filter criteria ({@link #addFilterParameter(String, String)}) to a valid HQL
   * where clause (without the 'where' keyword). After calling this method the method
   * {@link #getTypedParameters()} can be called. Note that currently only filtering on string and
   * boolean properties is supported. Also filtering on the identifier of a referenced business
   * object is supported.
   * 
   * @return a valid where clause or an empty string if not set.
   */
  public String getWhereClause() {
    if (whereClause != null) {
      return whereClause;
    }
    Check.isNotNull(entity, "Entity must be set");

    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    simpleDateFormat.setLenient(true);

    final StringBuilder sb = new StringBuilder();
    boolean addAnd = false;
    final StringBuilder orgPart = new StringBuilder();
    final List<Property> propertyDone = new ArrayList<Property>();
    for (String key : filterParameters.keySet()) {
      final String value = filterParameters.get(key);

      if (key.equals(JsonConstants.WHERE_PARAMETER)) {
        // there are cases where null is set as a string
        // handle this
        if (value.equals("null") || value.length() == 0) {
          continue;
        }
        if (addAnd) {
          if (doOr) {
            sb.append(" or ");
          } else {
            sb.append(" and ");
          }
        }
        sb.append(" (" + value + ") ");
        addAnd = true;
        continue;
      }

      // handle the case that we should filter on the accessible organizations
      if (key.equals(JsonConstants.ORG_PARAMETER)) {
        if (entity.isOrganizationEnabled() && value != null && value.length() > 0) {
          final Set<String> orgs = OBContext.getOBContext().getOrganizationStructureProvider()
              .getNaturalTree(value);
          if (orgs.size() > 0) {
            orgPart.append(" organization in (");
            boolean addComma = false;
            for (String org : orgs) {
              if (addComma) {
                orgPart.append(",");
              }
              orgPart.append("'" + org + "'");
              addComma = true;
            }
            orgPart.append(") ");
          }
        }
        continue;
      }

      // determine the property
      final Property property = DalUtil.getPropertyFromPath(getEntity(), key);
      // invalid propname, ignore this one
      // TODO: possibly warn about it
      if (property == null || propertyDone.contains(property)) {
        continue;
      }
      propertyDone.add(property);

      // we know the property and the string representation of the value...
      // do the conversion

      if (addAnd) {
        if (doOr) {
          sb.append(" or ");
        } else {
          sb.append(" and ");
        }
      }
      addAnd = true;

      // filter on the id in this case
      if (!property.isPrimitive()) {
        sb.append(key + ".id = ?");
        typedParameters.add(value);
        continue;
      }

      // get rid of the identifier and replace it with the real property name
      String leftWherePart = key;
      if (key.equals(JsonConstants.IDENTIFIER)) {
        leftWherePart = property.getName();
      } else if (key.endsWith("." + JsonConstants.IDENTIFIER)) {
        final int start = key.indexOf("." + JsonConstants.IDENTIFIER);
        leftWherePart = key.substring(0, start) + "." + property.getName();
      }

      if (String.class == property.getPrimitiveObjectType()) {
        if (textMatching == TextMatching.exact) {
          sb.append(leftWherePart + " = ?");
          typedParameters.add(value);
        } else if (textMatching == TextMatching.startsWith) {
          sb.append("upper(" + leftWherePart + ") like ?");
          typedParameters.add(value.toUpperCase() + "%");
        } else {
          sb.append("upper(" + leftWherePart + ") like ?");
          typedParameters.add("%" + value.toUpperCase() + "%");
        }
      } else if (Boolean.class == property.getPrimitiveObjectType()) {
        sb.append(leftWherePart + " = ?");
        typedParameters.add(new Boolean(value));
      } else if (Date.class.isAssignableFrom(property.getPrimitiveObjectType())) {
        try {
          typedParameters.add(simpleDateFormat.parse(value));
          sb.append(" trunc(" + leftWherePart + ") = trunc(?) ");
        } catch (Exception e) {
          // ignore those errors for now
        }

        // } else if (property.isDate() || property.isDatetime()) {
        // NOTE: dates arrive in the format of the user....
        // sb.append(leftWherePart + " = ?");
        // typedParameters.add(value);
      } else {
        // TODO: support this....
        throw new UnsupportedOperationException("Type " + property.getPrimitiveObjectType()
            + " not yet supported for parameter " + key);
      }
    }

    log.debug("Whereclause for entity " + entity.getName());
    log.debug(sb.toString());
    for (Object param : typedParameters) {
      log.debug(param);
    }
    log.debug("Textmatching " + textMatching);

    if (sb.length() == 0) {
      whereClause = orgPart.length() > 0 ? orgPart.toString() : "";
    } else {
      whereClause = "(" + sb.toString() + ")"
          + (orgPart.length() > 0 ? " and " + orgPart.toString() : "");
    }
    return whereClause;
  }

  /**
   * @return true if one of the filter parameters is the {@link JsonConstants#ORG_PARAMETER}.
   */
  public boolean hasOrganizationParameter() {
    final String value = filterParameters.get(JsonConstants.ORG_PARAMETER);
    return value != null && value.trim().length() > 0;
  }

  /**
   * Add a filter parameter, the method {@link #getWhereClause()} will try to convert the String
   * value to a typed parameter.
   * 
   * @param key
   *          the filter key, can be direct property or a referenced property.
   * @param value
   *          the value as a String
   */
  public void addFilterParameter(String key, String value) {
    // ignore these
    if (value == null) {
      return;
    }
    whereClause = null;
    typedParameters.clear();
    filterParameters.put(key, value);
  }

  public List<Object> getTypedParameters() {
    return typedParameters;
  }

  public TextMatching getTextMatching() {
    return textMatching;
  }

  /**
   * The text matching strategy used. See here for a description:
   * http://www.smartclient.com/docs/7.0rc2/a/b/c/go.html#attr..ComboBoxItem.textMatchStyle
   * 
   * @param textMatchingName
   *          the following values are allowed: startsWith, substring, exact
   */
  public void setTextMatching(TextMatching matchStyle) {
    whereClause = null;
    typedParameters.clear();
    this.textMatching = matchStyle;
  }

  public boolean isDoOr() {
    return doOr;
  }

  public void setDoOr(boolean doOr) {
    this.doOr = doOr;
  }

  // e client.organization.name
  // left join e.client alias_client left join alias_client.organization alias_client_organization
  // private static class Join {
  //
  // private Join nextJoin;
  // private String joinClause;
  // private String whereClauseLeftOperand;
  //
  // private boolean setPath(String ownerAlias, Entity entity, String path) {
  // final String[] pathParts = path.split("\\.");
  // if (!entity.hasProperty(pathParts[0])) {
  // return false;
  // }
  // final String alias = ownerAlias + "_" + pathParts[0];
  // joinClause = " left join " + ownerAlias + "." + pathParts[0] + " " + alias;
  //
  // // different cases
  // // last step in the path
  // if (pathParts.length == 1) {
  // whereClauseLeftOperand = alias;
  // return true;
  // } else if (pathParts.length == 2) {
  // whereClauseLeftOperand = alias + "." + pathParts[1];
  // return true;
  // } else {
  // // need another join
  // final Property property = entity.getProperty(pathParts[0]);
  // if (property.isPrimitive() || property.isOneToMany()) {
  // return false;
  // }
  // final int index = path.indexOf(".");
  // nextJoin = new Join();
  // return nextJoin.setPath(alias, property.getTargetEntity(), path.substring(index + 1));
  // }
  // }
  //
  // public String getJoinClause() {
  // if (nextJoin != null) {
  // return joinClause + " " + nextJoin.getJoinClause();
  // }
  // return joinClause;
  // }
  //
  // public String getWhereClauseLeftOperand() {
  // if (nextJoin != null) {
  // return nextJoin.getWhereClauseLeftOperand();
  // }
  // return whereClauseLeftOperand;
  // }
  // }
}
