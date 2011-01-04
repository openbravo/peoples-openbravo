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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.userinterface.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.criterion.Expression;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.Reference;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.ForeignKeyDomainType;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.util.Check;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.service.datasource.DataSource;
import org.openbravo.service.datasource.DataSourceConstants;
import org.openbravo.service.datasource.DatasourceField;
import org.openbravo.service.json.JsonConstants;

/**
 * Generates the javascript for a selector using parameters passed in as part of the request.
 * 
 * This class has convenience methods to facilitate the template.
 * 
 * @author mtaal
 */
public class SelectorComponent extends BaseTemplateComponent {

  public static final String SELECTOR_ITEM_PARAMETER = "IsSelectorItem";

  private static final String CSSSIZE = "CssSize";
  private static final String ONECELL = "OneCell";
  private static final String TWOCELLS = "TwoCells";
  private static final String THREECELLS = "ThreeCells";
  private static final String FOURCELLS = "FourCells";
  private static final String FIVECELLS = "FiveCells";

  private org.openbravo.userinterface.selector.Selector selector;
  private List<SelectorFieldTrl> selectorFieldTrls;
  private static OutSelectorField IdOutField;
  private static OutSelectorField IdentifierOutField;

  static {
    IdOutField = new OutSelectorField();
    IdOutField.setOutFieldName(JsonConstants.ID);
    IdOutField.setTabFieldName("");

    IdentifierOutField = new OutSelectorField();
    IdentifierOutField.setOutFieldName(JsonConstants.IDENTIFIER);
    IdentifierOutField.setTabFieldName("");
  }

  @Override
  protected Template getComponentTemplate() {
    return getSelector().getObclkerTemplate();
  }

  @Inject
  @ComponentProvider.Qualifier(DataSourceConstants.DS_COMPONENT_TYPE)
  private ComponentProvider componentProvider;

  public boolean isSelectorItem() {
    return hasParameter(SELECTOR_ITEM_PARAMETER);
  }

  public Module getModule() {
    return getSelector().getModule();
  }

  /**
   * Computes the field in the popup which can receive the value entered by the user in the
   * suggestion box, to set the first default filter.
   * 
   * @return the field in the popup to set.
   */
  public String getDefaultPopupFilterField() {
    if (getSelector().getDisplayfield() != null && getSelector().getDisplayfield().isShowingrid()) {
      if (getSelector().getDisplayfield().getProperty() != null) {
        return getSelector().getDisplayfield().getProperty();
      } else {
        return getSelector().getDisplayfield().getObserdsDatasourceField().getName();
      }
    }
    // a very common case, return the first selector field which is part of the
    // identifier
    if (getSelector().getDisplayfield() == null
        || getSelector().getDisplayfield().getProperty().equals(JsonConstants.IDENTIFIER)) {
      final Entity entity = getEntity();
      if (entity != null) {
        for (Property prop : entity.getIdentifierProperties()) {
          for (SelectorField selectorField : getSelector().getOBUISELSelectorFieldList()) {
            if (selectorField.getProperty() != null
                && selectorField.getProperty().equals(prop.getName())) {
              return selectorField.getProperty();
            }
          }
        }
      }
    }
    return JsonConstants.IDENTIFIER;
  }

  public org.openbravo.userinterface.selector.Selector getSelector() {
    if (selector == null) {
      selector = OBDal.getInstance().get(org.openbravo.userinterface.selector.Selector.class,
          getId());
      Check.isNotNull(selector, "No selector found using id " + getId());
      Check.isTrue(selector.isActive(), "Selector " + selector + " is not active anymore");
    }
    return selector;
  }

  public String getColumnName() {
    Check.isTrue(hasParameter(SelectorConstants.PARAM_COLUMN_NAME), "The "
        + SelectorConstants.PARAM_COLUMN_NAME + " parameter must be set");
    return getParameter(SelectorConstants.PARAM_COLUMN_NAME);
  }

  public String getComboReload() {
    if (!hasParameter(SelectorConstants.PARAM_COMBO_RELOAD)) {
      return "null";
    }
    Check.isTrue(hasParameter(SelectorConstants.PARAM_TAB_ID), "The "
        + SelectorConstants.PARAM_TAB_ID + " parameter must be set");
    final String tabId = getParameter(SelectorConstants.PARAM_TAB_ID);
    return "function(name){reloadComboReloads" + tabId + "(name);}";
  }

  public String getDisabled() {
    if (hasParameter(SelectorConstants.PARAM_DISABLED)) {
      return getParameter(SelectorConstants.PARAM_DISABLED);
    }
    return Boolean.FALSE.toString();
  }

  public String getTargetPropertyName() {
    if (hasParameter(SelectorConstants.PARAM_TARGET_PROPERTY_NAME)) {
      return getParameter(SelectorConstants.PARAM_TARGET_PROPERTY_NAME);
    }
    return "";
  }

  public String getValueField() {
    if (getSelector().getValuefield() != null) {
      String valueField = getPropertyOrDataSourceField(getSelector().getValuefield());
      final DomainType domainType = getDomainType(getSelector().getValuefield());
      if (domainType instanceof ForeignKeyDomainType) {
        return valueField + "." + JsonConstants.ID;
      }
      return valueField;
    }

    if (getSelector().getObserdsDatasource() != null) {
      final DataSource dataSource = getSelector().getObserdsDatasource();
      // a complete manual datasource which does not have a table
      // and which has a field defined
      if (dataSource.getTable() == null && !dataSource.getOBSERDSDatasourceFieldList().isEmpty()) {
        final DatasourceField dsField = dataSource.getOBSERDSDatasourceFieldList().get(0);
        return dsField.getName();
      }
    }

    return JsonConstants.ID;
  }

  public String getDisplayField() {
    if (getSelector().getDisplayfield() != null) {
      return getPropertyOrDataSourceField(getSelector().getDisplayfield());
    }

    // try to be intelligent when there is a datasource
    if (getSelector().getObserdsDatasource() != null) {
      final DataSource dataSource = getSelector().getObserdsDatasource();
      // a complete manual datasource which does not have a table
      // and which has a field defined
      if (dataSource.getTable() == null && !dataSource.getOBSERDSDatasourceFieldList().isEmpty()) {
        final DatasourceField dsField = dataSource.getOBSERDSDatasourceFieldList().get(0);
        return dsField.getName();
      }
    }

    // in all other cases use an identifier
    return JsonConstants.IDENTIFIER;
  }

  private String getPropertyOrDataSourceField(SelectorField selectorField) {
    if (selectorField.getProperty() != null) {
      return selectorField.getProperty();
    }
    if (selectorField.getObserdsDatasourceField() != null) {
      return selectorField.getObserdsDatasourceField().getName();
    }
    throw new IllegalStateException("Selectorfield " + selectorField
        + " has a null datasource and a null property");
  }

  private boolean isBoolean(SelectorField selectorField) {
    final DomainType domainType = getDomainType(selectorField);
    if (domainType instanceof PrimitiveDomainType) {
      final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) domainType;
      return boolean.class == primitiveDomainType.getPrimitiveType()
          || Boolean.class == primitiveDomainType.getPrimitiveType();
    }
    return false;
  }

  public String getRequired() {
    if (hasParameter(SelectorConstants.PARAM_REQUIRED)) {
      return getParameter(SelectorConstants.PARAM_REQUIRED);
    }
    return Boolean.FALSE.toString();
  }

  public String getCallOut() {
    if (hasParameter(SelectorConstants.PARAM_CALLOUT)) {
      return getParameter(SelectorConstants.PARAM_CALLOUT);
    }
    return "null";
  }

  public String getShowSelectorGrid() {
    for (SelectorField selectorField : getSelector().getOBUISELSelectorFieldList()) {
      if (selectorField.isShowingrid()) {
        return Boolean.TRUE.toString();
      }
    }
    return Boolean.FALSE.toString();
  }

  public String getWhereClause() {
    return getSafeValue(getSelector().getHQLWhereClause());
  }

  public String getTitle() {
    final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
    String description = null;
    for (SelectorTrl selectorTrl : getSelector().getOBUISELSelectorTrlList()) {
      final String trlLanguageId = (String) DalUtil.getId(selectorTrl.getLanguageID());
      if (trlLanguageId.equals(userLanguageId)) {
        description = selectorTrl.getDescription();
      }
    }
    if (description != null) {
      return description;
    }

    description = getSelector().getDescription();
    if (description == null) {
      return "";
    }
    return description;
  }

  public String getDataSourceJavascript() {
    final String dataSourceId;

    if (getSelector().getObserdsDatasource() != null) {
      dataSourceId = getSelector().getObserdsDatasource().getId();
    } else {
      Check.isNotNull(getSelector().getTable(),
          "Both the datasource and table are null for this selector: " + selector);
      dataSourceId = getSelector().getTable().getName();
    }

    final Map<String, Object> dsParameters = new HashMap<String, Object>(getParameters());
    dsParameters.put(DataSourceConstants.DS_ONLY_GENERATE_CREATESTATEMENT, true);

    final StringBuilder extraProperties = new StringBuilder();
    for (SelectorField selectorField : getSelector().getOBUISELSelectorFieldList()) {
      String fieldName = getPropertyOrDataSourceField(selectorField);

      // handle the case that the field is a foreign key
      // in that case always show the identifier
      final DomainType domainType = getDomainType(selectorField);
      if (domainType instanceof ForeignKeyDomainType) {
        fieldName = fieldName + "." + JsonConstants.IDENTIFIER;
      }

      if (fieldName.contains(".")) {
        if (extraProperties.length() > 0) {
          extraProperties.append(",");
        }
        extraProperties.append(fieldName);
      }
    }
    if (extraProperties.length() > 0) {
      dsParameters.put(JsonConstants.ADDITIONAL_PROPERTIES_PARAMETER, extraProperties.toString());
    }

    final Component component = componentProvider.getComponent(dataSourceId, dsParameters);

    return component.generate();
  }

  public String getNumCols() {
    final String cssSize = getParameter(CSSSIZE);
    if (cssSize == null) {
      return "2";
    }
    if (cssSize.equals(ONECELL)) {
      return "1";
    } else if (cssSize.equals(TWOCELLS)) {
      return "2";
    } else if (cssSize.equals(THREECELLS)) {
      return "3";
    } else if (cssSize.equals(FOURCELLS)) {
      return "4";
    } else if (cssSize.equals(FIVECELLS)) {
      return "5";
    }
    return "2";
  }

  public String getExtraSearchFields() {
    final String displayField = getDisplayField();
    final StringBuilder sb = new StringBuilder();
    for (SelectorField selectorField : getSelector().getOBUISELSelectorFieldList()) {
      if (!selectorField.isActive()) {
        continue;
      }
      String fieldName = getPropertyOrDataSourceField(selectorField);
      if (fieldName.equals(displayField)) {
        continue;
      }
      // prevent booleans as search fields, they don't work
      if (selectorField.isSearchinsuggestionbox() && !isBoolean(selectorField)) {

        // handle the case that the field is a foreign key
        // in that case always show the identifier
        final DomainType domainType = getDomainType(selectorField);
        if (domainType instanceof ForeignKeyDomainType) {
          fieldName = fieldName + "." + JsonConstants.IDENTIFIER;
        }

        if (sb.length() > 0) {
          sb.append(",");
        }
        sb.append("'" + fieldName + "'");
      }
    }
    return sb.toString();
  }

  public List<OutSelectorField> getOutFields() {
    List<OutSelectorField> outFields = new ArrayList<OutSelectorField>();
    final List<SelectorField> sortedFields = new ArrayList<SelectorField>(getSelector()
        .getOBUISELSelectorFieldList());

    final String tabId = getParameter(SelectorConstants.PARAM_TAB_ID);

    Collections.sort(sortedFields, new SelectorFieldComparator());

    outFields.add(SelectorComponent.IdOutField);
    outFields.add(SelectorComponent.IdentifierOutField);

    try {
      OBContext.setAdminMode();
      for (SelectorField selectorField : sortedFields) {
        if (Boolean.TRUE.equals(selectorField.isOutfield())) {
          if (tabId.equals("")) {
            final OutSelectorField outField = new OutSelectorField();
            outField.setOutFieldName(getPropertyOrDataSourceField(selectorField));
            outField.setTabFieldName("");
            outFields.add(outField);
          } else {
            final OBCriteria<Field> obc = OBDal.getInstance().createCriteria(Field.class);
            obc.add(Expression.eq(Field.PROPERTY_OBUISELOUTFIELD, selectorField));
            for (Field associatedField : obc.list()) {
              if (associatedField.getTab().getId().equals(tabId)) {
                final OutSelectorField outField = new OutSelectorField();
                outField.setOutFieldName(getPropertyOrDataSourceField(selectorField));
                outField.setTabFieldName(associatedField.getColumn().getName());
                outFields.add(outField);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace(); // FIXME
    } finally {
      OBContext.restorePreviousMode();
    }
    return outFields;
  }

  public List<LocalSelectorField> getPickListFields() {
    // return the displayfield as the picklist
    final String displayField = getDisplayField();
    final LocalSelectorField localSelectorField = new LocalSelectorField();
    localSelectorField.setName(displayField);
    localSelectorField.setTitle(displayField);
    localSelectorField.setSort(false);
    localSelectorField.setFilter(false);
    return Collections.singletonList(localSelectorField);
  }

  public List<LocalSelectorField> getSelectorGridFields() {
    return getSelectorFields(false, true);
  }

  private List<LocalSelectorField> getSelectorFields(boolean pickList, boolean popupGrid) {
    final List<LocalSelectorField> result = new ArrayList<LocalSelectorField>();

    final List<SelectorField> sortedFields = new ArrayList<SelectorField>(getSelector()
        .getOBUISELSelectorFieldList());
    Collections.sort(sortedFields, new SelectorFieldComparator());

    for (SelectorField selectorField : sortedFields) {
      if (!selectorField.isActive()) {
        continue;
      }
      // if (pickList && !selectorField.isShowinsuggestionbox()) {
      // continue;
      // }
      if (popupGrid && !selectorField.isShowingrid()) {
        continue;
      }
      final LocalSelectorField localSelectorField = new LocalSelectorField();
      String fieldName = getPropertyOrDataSourceField(selectorField);

      // handle the case that the field is a foreign key
      // in that case always show the identifier
      final DomainType domainType = getDomainType(selectorField);
      if (domainType instanceof ForeignKeyDomainType) {
        fieldName = fieldName + "." + JsonConstants.IDENTIFIER;
      }

      localSelectorField.setName(fieldName);
      localSelectorField.setTitle(getTranslatedName(selectorField));
      localSelectorField.setSort(selectorField.isSortable());

      localSelectorField.setFilter(selectorField.isFilterable());
      localSelectorField.setDomainType(domainType);

      // determine format
      // if (selectorField.getProperty() != null) {
      // selectorField.getProperty()
      // }

      result.add(localSelectorField);
    }
    return result;
  }

  private List<SelectorFieldTrl> getTranslatedFields() {
    if (selectorFieldTrls != null) {
      return selectorFieldTrls;
    }

    final String userLanguageId = OBContext.getOBContext().getLanguage().getId();

    final String whereClause = " where " + SelectorFieldTrl.PROPERTY_OBUISELSELECTORFIELD + "."
        + SelectorField.PROPERTY_OBUISELSELECTOR + "=:selector and "
        + SelectorFieldTrl.PROPERTY_LANGUAGEID + ".id=:userLanguageId ";

    final OBQuery<SelectorFieldTrl> qry = OBDal.getInstance().createQuery(SelectorFieldTrl.class,
        whereClause);
    qry.setNamedParameter("selector", getSelector());
    qry.setNamedParameter("userLanguageId", userLanguageId);
    selectorFieldTrls = qry.list();

    return selectorFieldTrls;
  }

  private String getTranslatedName(SelectorField selectorField) {
    final SelectorFieldTrl trl = getTranslation(selectorField);
    if (trl == null) {
      return selectorField.getName();
    }
    return trl.getName();
  }

  private SelectorFieldTrl getTranslation(SelectorField selectorField) {

    final String userLanguageId = OBContext.getOBContext().getLanguage().getId();

    for (SelectorFieldTrl selectorFieldTrl : getTranslatedFields()) {
      if (DalUtil.getId(selectorFieldTrl.getObuiselSelectorField()).equals(selectorField.getId())) {
        final String trlLanguageId = (String) DalUtil.getId(selectorFieldTrl.getLanguageID());
        if (trlLanguageId.equals(userLanguageId)) {
          return selectorFieldTrl;
        }
      }
    }

    return null;
  }

  // Used for create a map Out field - Tab field
  public static class OutSelectorField {
    private String tabFieldName;
    private String outFieldName;

    public String getTabFieldName() {
      return tabFieldName;
    }

    public void setTabFieldName(String tabFieldName) {
      this.tabFieldName = tabFieldName;
    }

    public String getOutFieldName() {
      return outFieldName;
    }

    public void setOutFieldName(String outFieldName) {
      this.outFieldName = outFieldName;
    }

  }

  // used to create picklist and grid fields
  public static class LocalSelectorField {
    private String title;
    private String name;
    private boolean filter;
    private boolean sort;
    private DomainType domainType;

    public DomainType getDomainType() {
      return domainType;
    }

    public void setDomainType(DomainType domainType) {
      this.domainType = domainType;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public List<LocalSelectorFieldProperty> getProperties() {
      final List<LocalSelectorFieldProperty> result = new ArrayList<LocalSelectorFieldProperty>();
      result.add(createLocalSelectorFieldProperty("title", title));
      result.add(createLocalSelectorFieldProperty("name", name));
      result.add(createLocalSelectorFieldProperty("canFilter", filter));
      result.add(createLocalSelectorFieldProperty("canSort", sort));
      if ((domainType instanceof PrimitiveDomainType)) {
        final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) domainType;
        if (Date.class.isAssignableFrom(primitiveDomainType.getPrimitiveType())) {
          result.add(createNonStringLocalSelectorFieldProperty("filterEditorType", "OBDateItem"));
          // TODO: hardcoded width for date
          result.add(createLocalSelectorFieldProperty("width", 100));
        } else if (Boolean.class.isAssignableFrom(primitiveDomainType.getPrimitiveType())) {
          result.add(createNonStringLocalSelectorFieldProperty("filterEditorType", "OBYesNoItem"));
          result.add(createNonStringLocalSelectorFieldProperty("formatCellValue",
              " function(value) { return OB.Utilities.getYesNoDisplayValue(value);}"));
        }
      }
      return result;
    }

    private LocalSelectorFieldProperty createLocalSelectorFieldProperty(String propName,
        Object propValue) {
      LocalSelectorFieldProperty localSelectorFieldProperty = new LocalSelectorFieldProperty();
      localSelectorFieldProperty.setName(propName);
      if (propValue instanceof String) {
        localSelectorFieldProperty.setStringValue((String) propValue);
      } else {
        localSelectorFieldProperty.setValue("" + propValue);
      }
      return localSelectorFieldProperty;
    }

    private LocalSelectorFieldProperty createNonStringLocalSelectorFieldProperty(String propName,
        Object propValue) {
      LocalSelectorFieldProperty localSelectorFieldProperty = new LocalSelectorFieldProperty();
      localSelectorFieldProperty.setName(propName);
      localSelectorFieldProperty.setValue("" + propValue);
      return localSelectorFieldProperty;
    }

    public boolean isFilter() {
      return filter;
    }

    public void setFilter(boolean filter) {
      this.filter = filter;
    }

    public boolean isSort() {
      return sort;
    }

    public void setSort(boolean sort) {
      this.sort = sort;
    }

    public static class LocalSelectorFieldProperty {
      private String name;
      private String value;

      public String getName() {
        return name;
      }

      public void setName(String name) {
        this.name = name;
      }

      public String getValue() {
        return value;
      }

      public void setStringValue(String value) {
        this.value = "'" + value + "'";
      }

      public void setValue(String value) {
        this.value = value;
      }
    }
  }

  /**
   * Compares/sorts SelectorField on the {@link SelectorField#getSortno()} property.
   * 
   * @author mtaal
   */
  private class SelectorFieldComparator implements Comparator<SelectorField> {

    @Override
    public int compare(SelectorField field0, SelectorField field1) {
      return (int) (field0.getSortno() - field1.getSortno());
    }

  }

  private DomainType getDomainType(SelectorField selectorField) {
    if (selectorField.getObuiselSelector().getTable() != null
        && selectorField.getProperty() != null) {
      final String entityName = selectorField.getObuiselSelector().getTable().getName();
      final Entity entity = ModelProvider.getInstance().getEntity(entityName);
      final Property property = DalUtil.getPropertyFromPath(entity, selectorField.getProperty());
      Check.isNotNull(property, "Property " + selectorField.getProperty() + " not found in Entity "
          + entity);
      return property.getDomainType();
    } else if (selectorField.getObserdsDatasourceField().getReference() != null) {
      return getDomainType(selectorField.getObserdsDatasourceField().getReference().getId());
    }
    return null;
  }

  private Entity getEntity() {
    if (getSelector().getTable() != null) {
      final String entityName = getSelector().getTable().getName();
      return ModelProvider.getInstance().getEntity(entityName);
    } else if (getSelector().getObserdsDatasource().getTable() != null) {
      final String entityName = getSelector().getObserdsDatasource().getTable().getName();
      return ModelProvider.getInstance().getEntity(entityName);
    }
    return null;
  }

  private DomainType getDomainType(String referenceId) {
    final Reference reference = ModelProvider.getInstance().getReference(referenceId);
    Check.isNotNull(reference, "No reference found for referenceid " + referenceId);
    return reference.getDomainType();
  }

  // the code below here has to be moved to a utility in core
  // private String getFormat(SelectorField selectorField) {
  // final DomainType domainType = getDomainType(selectorField);
  // if (domainType == null) {
  // return null;
  // }
  // if (!(domainType instanceof PrimitiveDomainType)) {
  // return null;
  // }
  // final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) domainType;
  //
  // final String formatId = primitiveDomainType.getFormatId();
  // if (formatId == null) {
  // return null;
  // }
  //
  // // now get the FormatXML
  // final Document formatXML = OBPropertiesProvider.getInstance().getFormatXMLDocument();
  // // iterate over its nodes to find it
  //
  // }
}
