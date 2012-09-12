/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;

import org.apache.log4j.Logger;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonConstants;

/**
 * @author iperdomo
 * 
 */
@RequestScoped
public class ClientModelComponent extends BaseTemplateComponent {
  private static final String TEMPLATE_ID = "ECC3231352D446F68244260BAA69CA39";

  private static final String ENTITY_PARAM = "entity";
  private static final String MODEL_NAME_PARAM = "modelName";
  private static final String SOURCE_PARAM = "source";

  private static final List<String> FILTERED_PROP_NAMES = new ArrayList<String>();

  private List<Property> filteredProps = null;

  static {
    FILTERED_PROP_NAMES.add("client");
    FILTERED_PROP_NAMES.add("organization");
    FILTERED_PROP_NAMES.add("active");
    FILTERED_PROP_NAMES.add("creationDate");
    FILTERED_PROP_NAMES.add("createdBy");
    FILTERED_PROP_NAMES.add("updated");
    FILTERED_PROP_NAMES.add("updatedBy");

  }

  private static final Logger log = Logger.getLogger(ClientModelComponent.class);

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, TEMPLATE_ID);
  }

  public String getModelName() {
    if ("".equals(getParameter(MODEL_NAME_PARAM))) {
      return getParameter(ENTITY_PARAM);
    }
    return getParameter(MODEL_NAME_PARAM);
  }

  public String getEntityName() {
    return getParameter(ENTITY_PARAM);
  }

  public String getTableName() {
    return ModelProvider.getInstance().getEntity(getParameter(ENTITY_PARAM)).getTableName()
        .toLowerCase();
  }

  public List<Property> getProperties() {
    if (filteredProps != null) {
      return filteredProps;
    }

    filteredProps = new ArrayList<Property>();
    for (Property p : ModelProvider.getInstance().getEntity(getParameter(ENTITY_PARAM))
        .getProperties()) {
      if (FILTERED_PROP_NAMES.contains(p.getName())) {
        continue;
      }
      filteredProps.add(p);
    }
    return filteredProps;
  }

  public String getCreateStatement() {
    StringBuffer sb = new StringBuffer();
    boolean first = true;

    sb.append("CREATE TABLE IF NOT EXISTS ");
    sb.append(getTableName()).append(" (");

    for (Property p : getProperties()) {

      if (p.getColumnName() == null) {
        continue;
      }

      if (!first) {
        sb.append(", ");
      }

      sb.append(p.getColumnName().toLowerCase());

      if ("id".equals(p.getName())) {
        sb.append(" TEXT PRIMARY KEY ");
      } else {
        if (p.isPrimitive() && p.isNumericType()) {
          sb.append(" NUMERIC ");
        } else {
          sb.append(" TEXT ");
        }
      }

      if (first) {
        first = false;
      }
    }
    sb.append(", ").append(JsonConstants.IDENTIFIER).append(" TEXT ");
    sb.append(", _idx NUMERIC").append(")");
    return sb.toString();
  }

  public String getDropStatement() {
    return "DROP TABLE IF EXISTS " + getTableName();
  }

  public String getInsertStatement() {
    StringBuffer sb = new StringBuffer();
    boolean first = true;
    int count = 0;

    sb.append("INSERT INTO ").append(getTableName()).append("(");
    for (Property p : getProperties()) {

      if (p.getColumnName() == null) {
        continue;
      }

      if (!first) {
        sb.append(", ");
      }

      sb.append(p.getColumnName().toLowerCase());

      if (first) {
        first = false;
      }
      count++;
    }

    sb.append(", ").append(JsonConstants.IDENTIFIER);
    sb.append(", _idx");

    sb.append(") ");

    sb.append(" VALUES (");

    first = true;
    for (int i = 0; i < count; i++) {
      if (!first) {
        sb.append(", ");
      }

      sb.append("?");

      if (first) {
        first = false;
      }
    }

    sb.append(", ?, ?"); // _identifier, _idx

    sb.append(")");

    return sb.toString();
  }

  public String getUpdateStatement() {
    return "";
  }

  public String getSource() {
    final String source = getParameter(SOURCE_PARAM);
    if ("".equals(source)) {
      log.warn("Source parameter must be provided");
    }
    return source;
  }

  @Override
  public boolean bypassAuthentication() {
    return true;
  }
}
