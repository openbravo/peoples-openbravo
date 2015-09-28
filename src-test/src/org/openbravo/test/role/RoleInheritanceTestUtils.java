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
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.role;

import java.util.Arrays;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.OBUIAPPViewImplementation;
import org.openbravo.client.application.ViewRoleAccess;
import org.openbravo.client.myob.WidgetClass;
import org.openbravo.client.myob.WidgetClassAccess;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.FormAccess;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleInheritance;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.TabAccess;
import org.openbravo.model.ad.access.WindowAccess;
import org.openbravo.model.ad.alert.AlertRecipient;
import org.openbravo.model.ad.alert.AlertRule;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Form;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.common.enterprise.Organization;

public class RoleInheritanceTestUtils {
  public final static String CLIENT_ID = "23C59575B9CF467C9620760EB255B389";
  public final static String ASTERISK_ORG_ID = "0";
  public final static List<String> ACCESS_NAMES = Arrays.asList("ORGANIZATION", "WINDOW", "TAB",
      "REPORT", "FORM", "WIDGET", "VIEW", "PROCESS", "ALERT", "PREFERENCE");

  public static Role createRole(String name, String clientId, String organizationId,
      String userLevel, boolean isManual, boolean isTemplate) {
    final Role role = OBProvider.getInstance().get(Role.class);
    Client client = OBDal.getInstance().get(Client.class, clientId);
    Organization org = OBDal.getInstance().get(Organization.class, organizationId);
    role.setClient(client);
    role.setClientList(clientId);
    role.setOrganization(org);
    role.setOrganizationList(organizationId);
    role.setTemplate(isTemplate);
    role.setManual(isManual);
    role.setName(name);
    role.setUserLevel(userLevel);
    OBDal.getInstance().save(role);
    return role;
  }

  public static void deleteRole(Role role) {
    OBDal.getInstance().remove(role);
  }

  public static void addInheritance(Role role, Role template, Long sequenceNumber) {
    final RoleInheritance inheritance = OBProvider.getInstance().get(RoleInheritance.class);
    inheritance.setClient(role.getClient());
    inheritance.setOrganization(role.getOrganization());
    inheritance.setRole(role);
    inheritance.setInheritFrom(template);
    inheritance.setSequenceNumber(sequenceNumber);
    OBDal.getInstance().save(inheritance);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  public static void removeInheritance(Role role, Role template) {
    final OBCriteria<RoleInheritance> obCriteria = OBDal.getInstance().createCriteria(
        RoleInheritance.class);
    obCriteria.add(Restrictions.eq(RoleInheritance.PROPERTY_ROLE, role));
    obCriteria.add(Restrictions.eq(RoleInheritance.PROPERTY_INHERITFROM, template));
    obCriteria.setMaxResults(1);
    RoleInheritance roleInheritance = (RoleInheritance) obCriteria.uniqueResult();
    OBDal.getInstance().remove(roleInheritance);
    // OBDal.getInstance().flush();
    // OBDal.getInstance().refresh(role);
  }

  public static void addAccess(String type, Role role, String accessName) {
    if ("ORGANIZATION".equals(type)) {
      addOrgAccess(role, accessName, true);
    } else if ("WINDOW".equals(type)) {
      addWindowAccess(role, accessName, true);
    } else if ("TAB".equals(type)) {
      // Create tab access for Business Partner window
      addTabAccess(role, "Business Partner", accessName, true, true);
    } else if ("REPORT".equals(type)) {
      addReportAndProcessAccess(role, accessName);
    } else if ("FORM".equals(type)) {
      addFormAccess(role, accessName);
    } else if ("WIDGET".equals(type)) {
      addWidgetAccess(role, accessName);
    } else if ("VIEW".equals(type)) {
      addViewImplementationAccess(role, accessName);
    } else if ("PROCESS".equals(type)) {
      addProcessDefinitionAccess(role, accessName);
    } else if ("ALERT".equals(type)) {
      addAlertRecipient(role, accessName);
    } else if ("PREFERENCE".equals(type)) {
      addPreference(role, accessName, "");
    }
  }

  public static void removeAccesses(String type, Role role) {
    if ("ALERT".equals(type)) {
      removeAlertRecipients(role);
    } else if ("PREFERENCE".equals(type)) {
      removePreferences(role);
    }
  }

  private static WindowAccess addWindowAccess(Role role, String windowName, boolean editableField) {
    final WindowAccess windowAccess = OBProvider.getInstance().get(WindowAccess.class);
    final OBCriteria<Window> obCriteria = OBDal.getInstance().createCriteria(Window.class);
    obCriteria.add(Restrictions.eq(Window.PROPERTY_NAME, windowName));
    obCriteria.setMaxResults(1);
    windowAccess.setClient(role.getClient());
    windowAccess.setOrganization(role.getOrganization());
    windowAccess.setRole(role);
    windowAccess.setWindow((Window) obCriteria.uniqueResult());
    windowAccess.setEditableField(editableField);
    OBDal.getInstance().save(windowAccess);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
    return windowAccess;
  }

  private static void addTabAccess(Role role, String windowName, String tabName,
      boolean editableField, boolean editableTab) {

    final OBCriteria<Window> windowCriteria = OBDal.getInstance().createCriteria(Window.class);
    windowCriteria.add(Restrictions.eq(Window.PROPERTY_NAME, windowName));
    windowCriteria.setMaxResults(1);
    Window window = (Window) windowCriteria.uniqueResult();

    final OBCriteria<WindowAccess> waCriteria = OBDal.getInstance().createCriteria(
        WindowAccess.class);
    waCriteria.add(Restrictions.eq(WindowAccess.PROPERTY_ROLE, role));
    waCriteria.add(Restrictions.eq(WindowAccess.PROPERTY_WINDOW, window));
    waCriteria.setMaxResults(1);
    WindowAccess wa = (WindowAccess) waCriteria.uniqueResult();
    if (wa == null) {
      // Window access does not exists, create it
      wa = addWindowAccess(role, windowName, editableField);
    }

    final TabAccess tabAccess = OBProvider.getInstance().get(TabAccess.class);
    final OBCriteria<Tab> obCriteria = OBDal.getInstance().createCriteria(Tab.class);
    obCriteria.add(Restrictions.eq(Tab.PROPERTY_NAME, tabName));
    obCriteria.add(Restrictions.eq(Tab.PROPERTY_WINDOW, window));
    obCriteria.setMaxResults(1);
    Tab tab = (Tab) obCriteria.uniqueResult();

    tabAccess.setClient(role.getClient());
    tabAccess.setOrganization(role.getOrganization());
    tabAccess.setWindowAccess(wa);
    tabAccess.setTab(tab);
    tabAccess.setEditableField(editableTab);
    OBDal.getInstance().save(tabAccess);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  private static void addOrgAccess(Role role, String orgName, boolean orgAdmin) {
    final RoleOrganization orgAccess = OBProvider.getInstance().get(RoleOrganization.class);
    final OBCriteria<Organization> obCriteria = OBDal.getInstance().createCriteria(
        Organization.class);
    obCriteria.add(Restrictions.eq(Organization.PROPERTY_NAME, orgName));
    obCriteria.setMaxResults(1);
    orgAccess.setClient(role.getClient());
    orgAccess.setRole(role);
    orgAccess.setOrganization((Organization) obCriteria.uniqueResult());
    orgAccess.setOrgAdmin(orgAdmin);
    OBDal.getInstance().save(orgAccess);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  private static void addReportAndProcessAccess(Role role, String reportName) {
    final org.openbravo.model.ad.access.ProcessAccess processAccess = OBProvider.getInstance().get(
        org.openbravo.model.ad.access.ProcessAccess.class);
    final OBCriteria<Process> obCriteria = OBDal.getInstance().createCriteria(Process.class);
    obCriteria.add(Restrictions.eq(Process.PROPERTY_NAME, reportName));
    obCriteria.setMaxResults(1);
    processAccess.setClient(role.getClient());
    processAccess.setOrganization(role.getOrganization());
    processAccess.setRole(role);
    processAccess.setProcess((Process) obCriteria.uniqueResult());
    OBDal.getInstance().save(processAccess);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  private static void addFormAccess(Role role, String formName) {
    final FormAccess formAccess = OBProvider.getInstance().get(FormAccess.class);
    final OBCriteria<Form> obCriteria = OBDal.getInstance().createCriteria(Form.class);
    obCriteria.add(Restrictions.eq(Form.PROPERTY_NAME, formName));
    obCriteria.setMaxResults(1);
    formAccess.setClient(role.getClient());
    formAccess.setOrganization(role.getOrganization());
    formAccess.setRole(role);
    formAccess.setSpecialForm((Form) obCriteria.uniqueResult());
    OBDal.getInstance().save(formAccess);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  private static void addWidgetAccess(Role role, String widgetTitle) {
    final WidgetClassAccess widgetAccess = OBProvider.getInstance().get(WidgetClassAccess.class);
    final OBCriteria<WidgetClass> obCriteria = OBDal.getInstance()
        .createCriteria(WidgetClass.class);
    obCriteria.add(Restrictions.eq(WidgetClass.PROPERTY_WIDGETTITLE, widgetTitle));
    obCriteria.setMaxResults(1);
    widgetAccess.setClient(role.getClient());
    widgetAccess.setOrganization(role.getOrganization());
    widgetAccess.setRole(role);
    widgetAccess.setWidgetClass((WidgetClass) obCriteria.uniqueResult());
    OBDal.getInstance().save(widgetAccess);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  private static void addViewImplementationAccess(Role role, String viewImplementationName) {
    final ViewRoleAccess viewAccess = OBProvider.getInstance().get(ViewRoleAccess.class);
    final OBCriteria<OBUIAPPViewImplementation> obCriteria = OBDal.getInstance().createCriteria(
        OBUIAPPViewImplementation.class);
    obCriteria
        .add(Restrictions.eq(OBUIAPPViewImplementation.PROPERTY_NAME, viewImplementationName));
    obCriteria.setMaxResults(1);
    viewAccess.setClient(role.getClient());
    viewAccess.setOrganization(role.getOrganization());
    viewAccess.setRole(role);
    viewAccess.setViewImplementation((OBUIAPPViewImplementation) obCriteria.uniqueResult());
    OBDal.getInstance().save(viewAccess);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  private static void addProcessDefinitionAccess(Role role, String processName) {
    final org.openbravo.client.application.ProcessAccess processAccess = OBProvider.getInstance()
        .get(org.openbravo.client.application.ProcessAccess.class);
    final OBCriteria<org.openbravo.client.application.Process> obCriteria = OBDal.getInstance()
        .createCriteria(org.openbravo.client.application.Process.class);
    obCriteria.add(Restrictions.eq(org.openbravo.client.application.Process.PROPERTY_NAME,
        processName));
    obCriteria.setMaxResults(1);
    processAccess.setClient(role.getClient());
    processAccess.setOrganization(role.getOrganization());
    processAccess.setRole(role);
    processAccess.setObuiappProcess((org.openbravo.client.application.Process) obCriteria
        .uniqueResult());
    OBDal.getInstance().save(processAccess);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  private static void addAlertRecipient(Role role, String alertName) {
    final AlertRecipient alertRecipient = OBProvider.getInstance().get(AlertRecipient.class);
    final OBCriteria<AlertRule> obCriteria = OBDal.getInstance().createCriteria(AlertRule.class);
    obCriteria.add(Restrictions.eq(AlertRule.PROPERTY_NAME, alertName));
    obCriteria.setMaxResults(1);
    alertRecipient.setClient(role.getClient());
    alertRecipient.setOrganization(role.getOrganization());
    alertRecipient.setRole(role);
    alertRecipient.setAlertRule((AlertRule) obCriteria.uniqueResult());
    OBDal.getInstance().save(alertRecipient);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  private static void removeAlertRecipients(Role role) {
    final OBCriteria<AlertRecipient> obCriteria = OBDal.getInstance().createCriteria(
        AlertRecipient.class);
    obCriteria.add(Restrictions.eq(AlertRecipient.PROPERTY_ROLE, role));
    for (AlertRecipient ar : obCriteria.list()) {
      OBDal.getInstance().remove(ar);
    }
    OBDal.getInstance().flush();
  }

  private static void addPreference(Role role, String propertyName, String value) {
    final Preference preference = OBProvider.getInstance().get(Preference.class);
    preference.setClient(role.getClient());
    preference.setOrganization(role.getOrganization());
    preference.setVisibleAtRole(role);
    preference.setPropertyList(true);
    preference.setProperty(propertyName);
    preference.setSearchKey(value);
    OBDal.getInstance().save(preference);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(role);
  }

  private static void removePreferences(Role role) {
    final OBCriteria<Preference> obCriteria = OBDal.getInstance().createCriteria(Preference.class);
    obCriteria.add(Restrictions.eq(Preference.PROPERTY_VISIBLEATROLE, role));
    for (Preference p : obCriteria.list()) {
      OBDal.getInstance().remove(p);
    }
    OBDal.getInstance().flush();
  }

  public static String[] getOrderedAccessNames(String type, Role role) {
    if ("ORGANIZATION".equals(type)) {
      return getOrgsFromOrgAccesses(role);
    } else if ("WINDOW".equals(type)) {
      return getWindowsFromWindowAccesses(role);
    } else if ("TAB".equals(type)) {
      // Get tab accesses for Business Partner window
      return getTabFromTabAccesses(role, "Business Partner");
    } else if ("REPORT".equals(type)) {
      return getReportsFromReportAccesses(role);
    } else if ("FORM".equals(type)) {
      return getFormsFromFormAccesses(role);
    } else if ("WIDGET".equals(type)) {
      return getWidgetsFromWidgetAccesses(role);
    } else if ("VIEW".equals(type)) {
      return getViewsFromViewAccesses(role);
    } else if ("PROCESS".equals(type)) {
      return getProcessFromProcessAccesses(role);
    } else if ("ALERT".equals(type)) {
      return getAlertRulesFromAlertRecipients(role);
    } else if ("PREFERENCE".equals(type)) {
      return getPreferences(role);
    }
    return null;
  }

  private static String[] getOrgsFromOrgAccesses(Role role) {
    final OBCriteria<RoleOrganization> obCriteria = OBDal.getInstance().createCriteria(
        RoleOrganization.class);
    obCriteria.add(Restrictions.eq(RoleOrganization.PROPERTY_ROLE, role));
    obCriteria.addOrderBy(
        RoleOrganization.PROPERTY_ORGANIZATION + "." + Organization.PROPERTY_NAME, true);
    List<RoleOrganization> list = obCriteria.list();
    String[] result = new String[list.size() * 2];
    int i = 0;
    for (RoleOrganization ro : list) {
      result[i] = ro.getOrganization().getName();
      result[i + 1] = ro.getInheritedFrom() != null ? (String) DalUtil.getId(ro.getInheritedFrom())
          : "";
      i += 2;
    }
    return result;
  }

  private static String[] getWindowsFromWindowAccesses(Role role) {
    final OBCriteria<WindowAccess> obCriteria = OBDal.getInstance().createCriteria(
        WindowAccess.class);
    obCriteria.add(Restrictions.eq(WindowAccess.PROPERTY_ROLE, role));
    obCriteria.addOrderBy(WindowAccess.PROPERTY_WINDOW + "." + Window.PROPERTY_NAME, true);
    List<WindowAccess> list = obCriteria.list();
    String[] result = new String[list.size() * 2];
    int i = 0;
    for (WindowAccess wa : list) {
      result[i] = wa.getWindow().getName();
      result[i + 1] = wa.getInheritedFrom() != null ? (String) DalUtil.getId(wa.getInheritedFrom())
          : "";
      i += 2;
    }
    return result;
  }

  private static String[] getTabFromTabAccesses(Role role, String windowName) {
    final OBCriteria<Window> windowCriteria = OBDal.getInstance().createCriteria(Window.class);
    windowCriteria.add(Restrictions.eq(Window.PROPERTY_NAME, windowName));
    windowCriteria.setMaxResults(1);
    Window window = (Window) windowCriteria.uniqueResult();

    final OBCriteria<WindowAccess> obCriteria = OBDal.getInstance().createCriteria(
        WindowAccess.class);
    obCriteria.add(Restrictions.eq(WindowAccess.PROPERTY_ROLE, role));
    obCriteria.add(Restrictions.eq(WindowAccess.PROPERTY_WINDOW, window));
    obCriteria.setMaxResults(1);

    final OBCriteria<TabAccess> tabCriteria = OBDal.getInstance().createCriteria(TabAccess.class);
    tabCriteria.add(Restrictions.eq(TabAccess.PROPERTY_WINDOWACCESS,
        (WindowAccess) obCriteria.uniqueResult()));
    tabCriteria.addOrderBy(TabAccess.PROPERTY_TAB + "." + Tab.PROPERTY_NAME, true);
    List<TabAccess> list = tabCriteria.list();
    String[] result = new String[list.size() * 2];
    int i = 0;
    for (TabAccess ta : list) {
      result[i] = ta.getTab().getName();
      result[i + 1] = ta.getInheritedFrom() != null ? (String) DalUtil.getId(ta.getInheritedFrom())
          : "";
      i += 2;
    }
    return result;
  }

  private static String[] getReportsFromReportAccesses(Role role) {
    final OBCriteria<org.openbravo.model.ad.access.ProcessAccess> obCriteria = OBDal.getInstance()
        .createCriteria(org.openbravo.model.ad.access.ProcessAccess.class);
    obCriteria
        .add(Restrictions.eq(org.openbravo.model.ad.access.ProcessAccess.PROPERTY_ROLE, role));
    obCriteria.addOrderBy(org.openbravo.model.ad.access.ProcessAccess.PROPERTY_PROCESS + "."
        + Process.PROPERTY_NAME, true);
    List<org.openbravo.model.ad.access.ProcessAccess> list = obCriteria.list();
    String[] result = new String[list.size() * 2];
    int i = 0;
    for (org.openbravo.model.ad.access.ProcessAccess pa : list) {
      result[i] = pa.getProcess().getName();
      result[i + 1] = pa.getInheritedFrom() != null ? (String) DalUtil.getId(pa.getInheritedFrom())
          : "";
      i += 2;
    }
    return result;
  }

  private static String[] getFormsFromFormAccesses(Role role) {
    final OBCriteria<FormAccess> obCriteria = OBDal.getInstance().createCriteria(FormAccess.class);
    obCriteria.add(Restrictions.eq(FormAccess.PROPERTY_ROLE, role));
    obCriteria.addOrderBy(FormAccess.PROPERTY_SPECIALFORM + "." + Form.PROPERTY_NAME, true);
    List<FormAccess> list = obCriteria.list();
    String[] result = new String[list.size() * 2];
    int i = 0;
    for (FormAccess fa : list) {
      result[i] = fa.getSpecialForm().getName();
      result[i + 1] = fa.getInheritedFrom() != null ? (String) DalUtil.getId(fa.getInheritedFrom())
          : "";
      i += 2;
    }
    return result;
  }

  private static String[] getWidgetsFromWidgetAccesses(Role role) {
    final OBCriteria<WidgetClassAccess> obCriteria = OBDal.getInstance().createCriteria(
        WidgetClassAccess.class);
    obCriteria.add(Restrictions.eq(WidgetClassAccess.PROPERTY_ROLE, role));
    obCriteria.addOrderBy(WidgetClassAccess.PROPERTY_WIDGETCLASS + "."
        + WidgetClass.PROPERTY_WIDGETTITLE, true);
    List<WidgetClassAccess> list = obCriteria.list();
    String[] result = new String[list.size() * 2];
    int i = 0;
    for (WidgetClassAccess wa : list) {
      result[i] = wa.getWidgetClass().getWidgetTitle();
      result[i + 1] = wa.getInheritedFrom() != null ? (String) DalUtil.getId(wa.getInheritedFrom())
          : "";
      i += 2;
    }
    return result;
  }

  private static String[] getViewsFromViewAccesses(Role role) {
    final OBCriteria<ViewRoleAccess> obCriteria = OBDal.getInstance().createCriteria(
        ViewRoleAccess.class);
    obCriteria.add(Restrictions.eq(ViewRoleAccess.PROPERTY_ROLE, role));
    obCriteria.addOrderBy(ViewRoleAccess.PROPERTY_VIEWIMPLEMENTATION + "."
        + OBUIAPPViewImplementation.PROPERTY_NAME, true);
    List<ViewRoleAccess> list = obCriteria.list();
    String[] result = new String[list.size() * 2];
    int i = 0;
    for (ViewRoleAccess va : list) {
      result[i] = va.getViewImplementation().getName();
      result[i + 1] = va.getInheritedFrom() != null ? (String) DalUtil.getId(va.getInheritedFrom())
          : "";
      i += 2;
    }
    return result;
  }

  private static String[] getProcessFromProcessAccesses(Role role) {
    final OBCriteria<org.openbravo.client.application.ProcessAccess> obCriteria = OBDal
        .getInstance().createCriteria(org.openbravo.client.application.ProcessAccess.class);
    obCriteria.add(Restrictions.eq(org.openbravo.client.application.ProcessAccess.PROPERTY_ROLE,
        role));
    obCriteria.addOrderBy(org.openbravo.client.application.ProcessAccess.PROPERTY_OBUIAPPPROCESS
        + "." + org.openbravo.client.application.Process.PROPERTY_NAME, true);
    List<org.openbravo.client.application.ProcessAccess> list = obCriteria.list();
    String[] result = new String[list.size() * 2];
    int i = 0;
    for (org.openbravo.client.application.ProcessAccess pa : list) {
      result[i] = pa.getObuiappProcess().getName();
      result[i + 1] = pa.getInheritedFrom() != null ? (String) DalUtil.getId(pa.getInheritedFrom())
          : "";
      i += 2;
    }
    return result;
  }

  private static String[] getAlertRulesFromAlertRecipients(Role role) {
    final OBCriteria<AlertRecipient> obCriteria = OBDal.getInstance().createCriteria(
        AlertRecipient.class);
    obCriteria.add(Restrictions.eq(AlertRecipient.PROPERTY_ROLE, role));
    obCriteria.addOrderBy(AlertRecipient.PROPERTY_ALERTRULE + "." + AlertRule.PROPERTY_NAME, true);
    List<AlertRecipient> list = obCriteria.list();
    String[] result = new String[list.size() * 2];
    int i = 0;
    for (AlertRecipient ar : list) {
      result[i] = ar.getAlertRule().getName();
      result[i + 1] = ar.getInheritedFrom() != null ? (String) DalUtil.getId(ar.getInheritedFrom())
          : "";
      i += 2;
    }
    return result;
  }

  private static String[] getPreferences(Role role) {
    final OBCriteria<Preference> obCriteria = OBDal.getInstance().createCriteria(Preference.class);
    obCriteria.add(Restrictions.eq(Preference.PROPERTY_VISIBLEATROLE, role));
    obCriteria.addOrderBy(Preference.PROPERTY_PROPERTY, true);
    List<Preference> list = obCriteria.list();
    String[] result = new String[list.size() * 2];
    int i = 0;
    for (Preference p : list) {
      result[i] = p.getProperty();
      result[i + 1] = p.getInheritedFrom() != null ? (String) DalUtil.getId(p.getInheritedFrom())
          : "";
      i += 2;
    }
    return result;
  }
}
