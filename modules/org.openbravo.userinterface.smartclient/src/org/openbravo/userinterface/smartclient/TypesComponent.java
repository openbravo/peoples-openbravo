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
package org.openbravo.userinterface.smartclient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.EnumerateDomainType;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.model.domaintype.StringDomainType;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Reference;

/**
 * The component responsible for creating Smartclient simple type representations used by other
 * modules.
 * 
 * @author mtaal
 */
public class TypesComponent extends BaseTemplateComponent {

  private List<SimpleType> simpleTypes = null;

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, SmartClientConstants.SC_TYPES_TEMPLATE_ID);
  }

  public List<SimpleType> getSimpleTypes() {
    if (simpleTypes != null) {
      return simpleTypes;
    }
    OBContext.setAdminMode();
    try {
      simpleTypes = new ArrayList<SimpleType>();
      final Map<String, FormatDefinition> formatDefinitions = computeFormatDefinitions();
      final List<Reference> references = OBDal.getInstance().createCriteria(Reference.class).list();
      final List<String> handledDomainTypes = new ArrayList<String>();
      for (Reference reference : references) {
        final DomainType domainType = getDomainType(reference);
        if (handledDomainTypes.contains(domainType.getClass().getName())) {
          continue;
        }
        handledDomainTypes.add(domainType.getClass().getName());
        if (domainType instanceof PrimitiveDomainType) {
          final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) domainType;
          final SimpleType simpleType = new SimpleType();
          simpleTypes.add(simpleType);
          simpleType.setName(domainType.getClass().getName());
          if (domainType instanceof EnumerateDomainType) {
            simpleType.setInheritsFrom("enum");
            simpleType.setEditorType("ComboBoxItem");
          } else {
            setTypeBasedValues(simpleType, primitiveDomainType.getPrimitiveType());
          }
          if (primitiveDomainType.getFormatId() != null) {
            simpleType.setInputFormat(formatDefinitions.get(primitiveDomainType.getFormatId()
                + "Edition"));
            simpleType.setShortDisplayFormat(formatDefinitions.get(primitiveDomainType
                .getFormatId()
                + "Relation"));
            simpleType.setNormalDisplayFormat(formatDefinitions.get(primitiveDomainType
                .getFormatId()
                + "Inform"));
          }
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return simpleTypes;
  }

  public DomainType getDomainType(Reference reference) {
    String modelImplementationClass = getModelImplementationClassName(reference);
    try {
      final Class<?> clz = OBClassLoader.getInstance().loadClass(modelImplementationClass);
      return (DomainType) clz.newInstance();
    } catch (Exception e) {
      throw new OBException("Not able to create domain type for reference " + reference, e);
    }
  }

  public String getModelImplementationClassName(Reference reference) {
    if (reference.getModelImpl() != null) {
      return reference.getModelImpl();
    }
    if (reference.getParentReference() != null && !reference.isBaseReference()
        && reference.getParentReference().isBaseReference()) {
      return reference.getParentReference().getModelImpl();
    }
    // the default
    return StringDomainType.class.getName();
  }

  private Map<String, FormatDefinition> computeFormatDefinitions() {
    final Document doc = OBPropertiesProvider.getInstance().getFormatXMLDocument();
    final Map<String, FormatDefinition> formatDefinitions = new HashMap<String, FormatDefinition>();
    final Element root = doc.getRootElement();
    for (Object object : root.elements()) {
      final Element element = (Element) object;
      final FormatDefinition formatDefinition = new FormatDefinition();

      formatDefinition.setDecimalSymbol(element.attributeValue("decimal"));
      formatDefinition.setFormat(element.attributeValue("formatOutput"));
      formatDefinition.setGroupingSymbol(element.attributeValue("grouping"));
      formatDefinitions.put(element.attributeValue("name"), formatDefinition);
    }

    return formatDefinitions;
  }

  private void setTypeBasedValues(SimpleType simpleType, Class<?> type) {
    if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
      simpleType.setInheritsFrom("boolean");
      simpleType.setEditorType("OBYesNoItem");
      simpleType.setBoolean(true);
    } else if (String.class.isAssignableFrom(type)) {
      simpleType.setInheritsFrom("text");
    } else if (BigDecimal.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type)
        || float.class.isAssignableFrom(type) || Double.class.isAssignableFrom(type)
        || Double.class.isAssignableFrom(type)) {
      simpleType.setInheritsFrom("float");
      simpleType.setEditorType("OBNumberItem");
      simpleType.setNumber(true);
    } else if (BigInteger.class.isAssignableFrom(type) || Long.class.isAssignableFrom(type)
        || Long.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type)
        || int.class.isAssignableFrom(type) || Byte.class.isAssignableFrom(type)
        || byte.class.isAssignableFrom(type) || Short.class.isAssignableFrom(type)
        || short.class.isAssignableFrom(type)) {
      simpleType.setInheritsFrom("integer");
      simpleType.setEditorType("OBNumberItem");
      simpleType.setNumber(true);
    } else if (Timestamp.class.isAssignableFrom(type)) {
      simpleType.setInheritsFrom("datetime");
      simpleType.setEditorType("OBDateTimeItem");
      simpleType.setDateTime(true);
    } else if (Date.class.isAssignableFrom(type)) {
      simpleType.setInheritsFrom("date");
      simpleType.setEditorType("OBDateItem");
      simpleType.setDate(true);
    } else if (type.getName().equals("[B") || type.getName().equals("[Ljava.lang.Byte;")) {
      simpleType.setInheritsFrom("image");
    } else {
      simpleType.setInheritsFrom("text");
    }
  }

  public static class SimpleType {
    private String name;
    private String inheritsFrom;
    private String editorType;
    private boolean isDate;
    private boolean isDateTime;
    private boolean isNumber;
    private boolean isBoolean;
    private FormatDefinition inputFormat;
    private FormatDefinition shortDisplayFormat;
    private FormatDefinition normalDisplayFormat;

    public boolean isShortFormatPresent() {
      return shortDisplayFormat != null;
    }

    public boolean isNormalFormatPresent() {
      return normalDisplayFormat != null;
    }

    public boolean isEditorTypeSet() {
      return editorType != null;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public boolean isDate() {
      return isDate;
    }

    public void setDate(boolean isDate) {
      this.isDate = isDate;
    }

    public boolean isNumber() {
      return isNumber;
    }

    public void setNumber(boolean isNumber) {
      this.isNumber = isNumber;
    }

    public FormatDefinition getInputFormat() {
      return inputFormat;
    }

    public void setInputFormat(FormatDefinition inputFormat) {
      this.inputFormat = inputFormat;
    }

    public FormatDefinition getShortDisplayFormat() {
      return shortDisplayFormat;
    }

    public void setShortDisplayFormat(FormatDefinition shortDisplayFormat) {
      this.shortDisplayFormat = shortDisplayFormat;
    }

    public FormatDefinition getNormalDisplayFormat() {
      return normalDisplayFormat;
    }

    public void setNormalDisplayFormat(FormatDefinition normalDisplayFormat) {
      this.normalDisplayFormat = normalDisplayFormat;
    }

    public String getInheritsFrom() {
      return inheritsFrom;
    }

    public void setInheritsFrom(String inheritsFrom) {
      this.inheritsFrom = inheritsFrom;
    }

    public String getEditorType() {
      return editorType;
    }

    public void setEditorType(String editorType) {
      this.editorType = editorType;
    }

    public boolean isDateTime() {
      return isDateTime;
    }

    public void setDateTime(boolean isDateTime) {
      this.isDateTime = isDateTime;
    }

    public boolean isBoolean() {
      return isBoolean;
    }

    public void setBoolean(boolean isBoolean) {
      this.isBoolean = isBoolean;
    }
  }

  public static class FormatDefinition {
    private String decimalSymbol;
    private String groupingSymbol;
    private String format;

    public String getDecimalSymbol() {
      return decimalSymbol;
    }

    public void setDecimalSymbol(String decimalSymbol) {
      this.decimalSymbol = decimalSymbol;
    }

    public String getGroupingSymbol() {
      return groupingSymbol;
    }

    public void setGroupingSymbol(String groupingSymbol) {
      this.groupingSymbol = groupingSymbol;
    }

    public String getFormat() {
      return format;
    }

    public void setFormat(String format) {
      this.format = format;
    }

  }

}
