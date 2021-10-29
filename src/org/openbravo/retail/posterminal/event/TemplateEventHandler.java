/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at https://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.ui.Element;
import org.openbravo.retail.posterminal.PrintTemplate;
import org.openbravo.retail.posterminal.PrintTemplateSubrep;

/**
 * Validator for {@link PrintTemplate} and {@link PrintTemplateSubrep} consistency.
 * 
 * <p>
 * It checks:
 * <ul>
 * <li>If Template Path field is set, module field must also be set. Template Path defines a URL and
 * it must be deployed as code, so it must be included within a module.
 * <li>One and only one of the {@code exclusiveNotNullProperties} is not null. By default in WebPOS
 * only Template Path property is considered. POS2 includes also Printing Template property. See
 * {@link PrintTemplateValidator#addExclusiveNotNullProperty(String)}
 * </ul>
 * 
 * @see PrintTemplate
 * @see PrintTemplateSubrep
 */
public abstract class TemplateEventHandler extends EntityPersistenceEventObserver {
  @Override
  protected abstract Entity[] getObservedEntities();

  /** Set of properties that will be checked. One and only one of them must be not null. */
  protected abstract Set<String> getExclusiveNotNullProperties();

  public void onInsertOrUpdate(@Observes EntityPersistenceEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    BaseOBObject template = event.getTargetInstance();

    checkPathAndModule(template);
    checkExclusiveNotNullProperties(template);
  }

  private void checkPathAndModule(BaseOBObject template) {
    if (template.get(PrintTemplate.PROPERTY_TEMPLATEPATH) != null
        && template.get(PrintTemplate.PROPERTY_MODULE) == null) {
      throw new OBException(
          OBMessageUtils.getI18NMessage("OBPOS_PrintTemplate_Path_NotNull_Module_Null"));

    }
  }

  private void checkExclusiveNotNullProperties(BaseOBObject template) {
    Set<String> notNullValues = getExclusiveNotNullProperties().stream()
        .filter(p -> template.get(p) != null)
        .collect(Collectors.toSet());

    if (notNullValues.isEmpty()) {
      throw new OBException(OBMessageUtils.getI18NMessage("OBPOS_PrintTemplate_AllNull",
          new String[] { getExclusiveNotNullProperties().stream()
              .map(this::getPropName)
              .collect(Collectors.joining(", ")) }));
    }

    if (notNullValues.size() > 1) {
      throw new OBException(
          OBMessageUtils.getI18NMessage("OBPOS_PrintTemplate_TooManyNotNull", new String[] {
              notNullValues.stream().map(this::getPropName).collect(Collectors.joining(", ")) }));
    }

  }

  private String getPropName(String property) {
    String colId = getObservedEntities()[0].getProperty(property).getColumnId();
    OBContext.setAdminMode();
    try {
      return (String) OBDal.getInstance()
          .get(Column.class, colId)
          .getApplicationElement()
          .get(Element.PROPERTY_NAME, OBContext.getOBContext().getLanguage());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected boolean isValidEvent(EntityPersistenceEvent event) {
    return (event instanceof EntityNewEvent || event instanceof EntityUpdateEvent)
        && super.isValidEvent(event);
  }

  /** {@link PrintTemplate} validator. */
  public static class PrintTemplateValidator extends TemplateEventHandler {
    private static final Entity[] ENTITIES = {
        ModelProvider.getInstance().getEntity(PrintTemplate.class) };
    private static Set<String> exclusiveNotNullProperties = new HashSet<>(
        List.of(PrintTemplate.PROPERTY_TEMPLATEPATH));

    public static void addExclusiveNotNullProperty(String propertyName) {
      exclusiveNotNullProperties.add(propertyName);
    }

    @Override
    protected Entity[] getObservedEntities() {
      return ENTITIES;
    }

    @Override
    protected Set<String> getExclusiveNotNullProperties() {
      return exclusiveNotNullProperties;
    }
  }

  /** {@link PrintTemplateSubrep} validator. */
  public static class PrintTemplateSubReportValidator extends TemplateEventHandler {
    private static final Entity[] ENTITIES = {
        ModelProvider.getInstance().getEntity(PrintTemplateSubrep.class) };
    private static Set<String> exclusiveNotNullProperties = new HashSet<>(
        List.of(PrintTemplateSubrep.PROPERTY_TEMPLATEPATH));

    public static void addExclusiveNotNullProperty(String propertyName) {
      exclusiveNotNullProperties.add(propertyName);
    }

    @Override
    protected Entity[] getObservedEntities() {
      return ENTITIES;
    }

    @Override
    protected Set<String> getExclusiveNotNullProperties() {
      return exclusiveNotNullProperties;
    }

  }
}
