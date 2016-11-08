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
 * All portions are Copyright (C) 2010-2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.attachment.AttachmentUtils;
import org.openbravo.client.application.attachment.AttachmentWindowComponent;
import org.openbravo.client.application.window.ParameterWindowComponent;
import org.openbravo.client.application.window.StandardWindowComponent;
import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.OBUserException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.FeatureRestriction;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.ad.utility.AttachmentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the view and generates it.
 * 
 * @author mtaal
 */
@RequestScoped
public class ViewComponent extends BaseComponent {
  private static Logger log = LoggerFactory.getLogger(ViewComponent.class);

  @Inject
  private StandardWindowComponent standardWindowComponent;

  @Inject
  private ParameterWindowComponent parameterWindowComponent;

  @Inject
  private AttachmentWindowComponent attachmentWindowComponent;

  @Inject
  private WeldUtils weldUtils;

  @Override
  public String generate() {
    long t = System.currentTimeMillis();
    final String viewId = getParameter("viewId");
    if (viewId == null) {
      throw new IllegalArgumentException("viewId parameter not found, it is mandatory");
    }

    try {
      OBContext.setAdminMode();

      final Window window = OBDal.getInstance().get(Window.class, correctViewId(viewId));

      if (window != null) {
        FeatureRestriction featureRestriction = ActivationKey.getInstance().hasLicenseAccess("MW",
            window.getId());
        if (featureRestriction != FeatureRestriction.NO_RESTRICTION) {
          throw new OBUserException(featureRestriction.toString());
        }
        return generateWindow(window);
      } else if (viewId.startsWith("processDefinition_")) {
        String processId = viewId.substring("processDefinition_".length());
        Process process = OBDal.getInstance().get(Process.class, processId);
        if (process == null) {
          throw new IllegalArgumentException("Not found process definition with ID " + processId);
        }
        return generateProcess(process);
      } else if (viewId.startsWith("attachment_")) {
        return generateAttachment(viewId);
      } else {
        return generateView(viewId);
      }
    } finally {
      OBContext.restorePreviousMode();
      log.debug("View {} generated in {} ms", viewId, System.currentTimeMillis() - t);
    }
  }

  protected String generateWindow(Window window) {
    standardWindowComponent.setWindow(window);
    standardWindowComponent.setParameters(getParameters());
    final String jsCode = standardWindowComponent.generate();
    return jsCode;
  }

  protected String generateView(String viewName) {
    OBUIAPPViewImplementation viewImpDef = getView(viewName);

    final BaseTemplateComponent component;
    if (viewImpDef.getJavaClassName() != null) {
      try {
        @SuppressWarnings("unchecked")
        final Class<BaseTemplateComponent> clz = (Class<BaseTemplateComponent>) OBClassLoader
            .getInstance().loadClass(viewImpDef.getJavaClassName());
        component = weldUtils.getInstance(clz);
      } catch (Exception e) {
        throw new OBException(e);
      }
    } else {
      component = weldUtils.getInstance(BaseTemplateComponent.class);
      if (viewImpDef.getTemplate() == null) {
        throw new IllegalStateException("No class and no template defined for view " + viewName);
      }
    }
    component.setId(viewImpDef.getId());
    component.setComponentTemplate(viewImpDef.getTemplate());
    component.setParameters(getParameters());

    final String jsCode = component.generate();
    return jsCode;
  }

  protected String generateProcess(Process process) {
    parameterWindowComponent.setProcess(process);
    parameterWindowComponent.setParameters(getParameters());
    parameterWindowComponent.setPoup(false);
    return parameterWindowComponent.generate();
  }

  protected String generateAttachment(String viewId) {
    String[] keys = viewId.split(KernelConstants.ID_PREFIX);
    String tabId = keys[1];
    Tab tab = OBDal.getInstance().get(Tab.class, tabId);
    if (tab == null) {
      throw new IllegalArgumentException("Not found process definition with ID " + tabId);
    }
    AttachmentMethod attMethod;
    if (keys.length >= 3) {
      String strAttMethodId = keys[2];
      if (StringUtils.isEmpty(strAttMethodId)) {
        // In case the attachment was created with old attachments.
        attMethod = AttachmentUtils.getDefaultAttachmentMethod();
      } else {
        attMethod = OBDal.getInstance().get(AttachmentMethod.class, strAttMethodId);
      }
    } else {
      // When uploading an attachment ("Add" button) AttachmentMethod is not sent, so there are less
      // than 3 elements in the array
      attMethod = AttachmentUtils.getAttachmentMethod();
    }
    attachmentWindowComponent.initialize(tab, attMethod);
    attachmentWindowComponent.setParameters(getParameters());
    return attachmentWindowComponent.generate();
  }

  private OBUIAPPViewImplementation getView(String viewName) {
    OBCriteria<OBUIAPPViewImplementation> obc = OBDal.getInstance().createCriteria(
        OBUIAPPViewImplementation.class);
    obc.add(Restrictions.or(Restrictions.eq(OBUIAPPViewImplementation.PROPERTY_NAME, viewName),
        Restrictions.eq(OBUIAPPViewImplementation.PROPERTY_ID, viewName)));

    if (obc.list().size() > 0) {
      return obc.list().get(0);
    } else {
      throw new IllegalArgumentException("No view found using id/name " + viewName);
    }
  }

  @Override
  public Module getModule() {
    final String id = getParameter("viewId");
    final Window window = OBDal.getInstance().get(Window.class, correctViewId(id));
    if (window != null) {
      return window.getModule();
    } else if (id.startsWith("processDefinition_")) {
      String processId = id.substring("processDefinition_".length());
      Process process = OBDal.getInstance().get(Process.class, processId);
      if (process == null) {
        throw new IllegalArgumentException("Not found process definition with ID " + processId);
      }
      return process.getModule();
    } else if (id.startsWith("attachment_")) {
      String[] keys = id.split(KernelConstants.ID_PREFIX);
      String tabId = keys[1];
      Tab tab = OBDal.getInstance().get(Tab.class, tabId);
      if (tab == null) {
        throw new IllegalArgumentException("Not found tab with ID " + tabId);
      }
      return tab.getModule();
    } else {
      OBUIAPPViewImplementation view = getView(id);
      if (view != null) {
        return view.getModule();
      } else {
        return super.getModule();
      }
    }
  }

  protected String correctViewId(String viewId) {
    // the case if a window is in development and has a unique making postfix
    // see the StandardWindowComponent.getWindowClientClassName method
    // changes made here should also be done there
    String correctedViewId = (viewId.startsWith(KernelConstants.ID_PREFIX) ? viewId.substring(1)
        : viewId);
    // if in consultants mode, do another conversion
    if (correctedViewId.contains(KernelConstants.ID_PREFIX)) {
      final int index = correctedViewId.indexOf(KernelConstants.ID_PREFIX);
      correctedViewId = correctedViewId.substring(0, index);
    }
    return correctedViewId;
  }

  @Override
  public Object getData() {
    return this;
  }

  @Override
  public String getETag() {
    String etag = super.getETag();

    return etag + "_" + getViewVersionHash();
  }

  /**
   * This function returns the last grid configuration change made into a window at any level (at
   * whole system level or just a for particuar tab or field).
   * 
   * This value is needed for the eTag calculation, so, if there has been any grid configuration
   * change, the eTag should change in order to load again the view definition.
   * 
   * @param window
   *          the window to obtain its last grid configuration change
   * @return a String with the last grid configuration change
   */
  private String getLastGridConfigurationChange(Window window) {
    Date lastModification = new Date(0);

    List<GCSystem> sysConfs = OBDal.getInstance().createQuery(GCSystem.class, "").list();
    if (!sysConfs.isEmpty()) {
      if (lastModification.compareTo(sysConfs.get(0).getUpdated()) < 0) {
        lastModification = sysConfs.get(0).getUpdated();
      }
    }

    String tabHql = "select max(updated) from OBUIAPP_GC_Tab where tab.window.id = :windowId";
    Query qryTabData = OBDal.getInstance().getSession().createQuery(tabHql);
    qryTabData.setParameter("windowId", window.getId());
    Date tabUpdated = (Date) qryTabData.uniqueResult();
    if (tabUpdated != null && lastModification.compareTo(tabUpdated) < 0) {
      lastModification = tabUpdated;
    }

    String fieldHql = "select max(updated) from OBUIAPP_GC_Field where obuiappGcTab.tab.window.id = :windowId";
    Query qryFieldData = OBDal.getInstance().getSession().createQuery(fieldHql);
    qryFieldData.setParameter("windowId", window.getId());
    Date fieldUpdated = (Date) qryFieldData.uniqueResult();
    if (fieldUpdated != null && lastModification.compareTo(fieldUpdated) < 0) {
      lastModification = fieldUpdated;
    }

    return lastModification.toString();
  }

  private synchronized String getViewVersionHash() {
    String viewVersionHash = "";
    String viewVersions = "";
    final String viewId = getParameter("viewId");
    OBContext.setAdminMode();
    try {
      Window window = OBDal.getInstance().get(Window.class, correctViewId(viewId));
      if (window == null) {
        return viewVersionHash;
      }
      for (Tab t : window.getADTabList()) {
        viewVersions += t.getTable().isFullyAudited() + "|";
      }
      viewVersions += getLastGridConfigurationChange(window) + "|";
      viewVersions += getLastSystemPrefrenceChage(window) + "|";
      viewVersionHash = DigestUtils.md5Hex(viewVersions);
    } finally {
      OBContext.restorePreviousMode();
    }
    return viewVersionHash;
  }

  private String getLastSystemPrefrenceChage(Window window) {
    Date lastModification = new Date(0);

    Set<String> preferences = new HashSet<String>();

    Pattern p = Pattern.compile(DynamicExpressionParser.REPLACE_DISPLAY_LOGIC_SERVER_PATTERN);
    for (String displayLogic : getFieldsWithDisplayLogicAtServerLevel(window.getId())) {
      Matcher m = p.matcher(displayLogic);
      while (m.find()) {
        preferences.add(m.group(1));
      }
    }

    Calendar cal = Calendar.getInstance();
    cal.set(9999, 9, 9);
    Date updated = new Date(cal.getTimeInMillis());
    if (!preferences.isEmpty()) {
      updated = getLastUpdated(preferences, updated);
    }
    if (lastModification.compareTo(updated) < 0) {
      lastModification = updated;
    }

    return lastModification.toString();
  }

  @SuppressWarnings("unchecked")
  private List<String> getFieldsWithDisplayLogicAtServerLevel(String windowID) {
    StringBuilder where = new StringBuilder();
    where.append(" select displayLogicEvaluatedInTheServer");
    where.append(" from ADField as f");
    where.append(" where f.displayLogicEvaluatedInTheServer is not null");
    where.append(" and f.tab.id in (select t.id");
    where.append("                  from ADTab t");
    where.append("                  where t.window.id = :windowId)");

    Session session = OBDal.getInstance().getSession();
    Query query = session.createQuery(where.toString());
    query.setParameter("windowId", windowID);

    return (List<String>) query.list();
  }

  private Date getLastUpdated(Set<String> preferenceSet, Date lastUpdatedParam) {

    StringBuilder where = new StringBuilder();
    where.append(" select max(p.updated)");
    where.append(" from ADPreference p");
    where.append(" where p.propertyList = true");
    where.append(" and p.property in :properties");
    where.append(" and p.client.id = '0'");
    where.append(" and p.organization = '0'");
    where.append(" and coalesce(p.visibleAtClient, '0') = '0'");
    where.append(" and coalesce(p.visibleAtOrganization, '0') = '0'");

    Session session = OBDal.getInstance().getSession();
    Query query = session.createQuery(where.toString());
    query.setParameterList("properties", preferenceSet);
    Date lastUpdated = (Date) query.uniqueResult();
    if (lastUpdated == null) {
      lastUpdated = lastUpdatedParam;
    }
    return lastUpdated;

  }
}
