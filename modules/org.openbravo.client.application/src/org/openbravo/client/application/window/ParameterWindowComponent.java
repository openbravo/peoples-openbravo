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
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openbravo.client.application.ApplicationConstants;
import org.openbravo.client.application.Process;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.FeatureRestriction;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;

/**
 * The component which takes care of creating a class for a specific paramter window.
 * 
 * @author alostale
 */
public class ParameterWindowComponent extends BaseTemplateComponent {
  private static final Logger log = Logger.getLogger(ParameterWindowComponent.class);
  private static final String DEFAULT_TEMPLATE_ID = "FF80818132F916130132F9357DE10016";

  protected static final Map<String, String> TEMPLATE_MAP = new HashMap<String, String>();

  private Window window;
  private OBViewTab rootTabComponent = null;
  private Boolean inDevelopment = null;
  private String uniqueString = "" + System.currentTimeMillis();
  private List<String> processViews = new ArrayList<String>();
  private Process process;

  @Inject
  private OBViewParameterHandler paramHandler;
  private boolean popup;

  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, DEFAULT_TEMPLATE_ID);
  }

  public String getWindowClientClassName() {
    // see the ViewComponent#correctViewId
    // changes made in this if statement should also be done in that method
    if (isIndevelopment()) {
      return KernelConstants.ID_PREFIX + process.getId() + KernelConstants.ID_PREFIX + uniqueString;
    }
    return KernelConstants.ID_PREFIX + process.getId();
  }

  public void setUniqueString(String uniqueString) {
    this.uniqueString = uniqueString;
  }

  public boolean isIndevelopment() {
    if (inDevelopment != null) {
      return inDevelopment;
    }

    // check window, tabs and fields
    inDevelopment = Boolean.FALSE;
    if (process.getModule().isInDevelopment() && process.getModule().isEnabled()) {
      inDevelopment = Boolean.TRUE;
    }
    // TODO: remove this
    inDevelopment = Boolean.TRUE;

    return inDevelopment;
  }

  public String generate() {
    final String jsCode = super.generate();
    // System.err.println(jsCode);
    return jsCode;
  }

  public String getTabView() {
    return getRootTabComponent().generate();
  }

  public boolean isPopup() {
    return this.popup;
  }

  public void setPoup(boolean popup) {
    this.popup = popup;
  }

  public String getWindowId() {
    return process.getId();
  }

  public String getThreadSafe() {
    // final Boolean value = getWindow().isThreadsafe();
    // if (value != null) {
    // return value.toString();
    // }
    return "true";
  }

  // public Window getWindow() {
  // return window;
  // }
  //
  // public void setWindow(Window window) {
  // this.window = window;
  // }

  public OBViewTab getRootTabComponent() {
    if (rootTabComponent != null) {
      return rootTabComponent;
    }
    processParameter();

    final List<OBViewTab> tempTabs = new ArrayList<OBViewTab>();
    for (Tab tab : window.getADTabList()) {
      // NOTE: grid sequence and field sequence tabs do not have any fields defined!
      if (!tab.isActive()
          || tab.getADFieldList().isEmpty()
          || ActivationKey.getInstance().hasLicencesTabAccess(tab.getId()) != FeatureRestriction.NO_RESTRICTION) {
        continue;
      }
      final OBViewTab tabComponent = createComponent(OBViewTab.class);
      tabComponent.setTab(tab);
      tabComponent.setUniqueString(uniqueString);
      tempTabs.add(tabComponent);
      final String processView = tabComponent.getProcessViews();
      if (!"".equals(processView)) {
        processViews.add(tabComponent.getProcessViews());
      }
    }

    // compute the correct hierarchical structure of the tabs
    for (OBViewTab tabComponent : tempTabs) {
      OBViewTab parentTabComponent = null;
      for (OBViewTab testTabComponent : tempTabs) {
        if (testTabComponent.getTab().getTabLevel() == (tabComponent.getTab().getTabLevel() - 1)
            && testTabComponent.getTab().getSequenceNumber() < tabComponent.getTab()
                .getSequenceNumber()) {
          if (parentTabComponent != null) {
            // if the new potential parent has a higher sequence number then that one is the correct
            // one
            if (parentTabComponent.getTab().getSequenceNumber() < testTabComponent.getTab()
                .getSequenceNumber()) {
              parentTabComponent = testTabComponent;
            }
          } else {
            parentTabComponent = testTabComponent;
          }
        }
      }
      if (parentTabComponent != null) {
        parentTabComponent.addChildTabComponent(tabComponent);
      }
    }

    // handle a special case, multiple root tab components
    // now get the root tabs
    for (OBViewTab tabComponent : tempTabs) {
      if (tabComponent.getParentTabComponent() == null) {
        if (rootTabComponent != null) {
          // warn for a special case, multiple root tab components
          // log.warn("Window " + window.getName() + " " + window.getId()
          // + " has more than on tab on level 0, choosing an arbitrary root tab");
          rootTabComponent.addChildTabComponent(tabComponent);
        } else {
          rootTabComponent = tabComponent;
        }
      }
    }
    if (rootTabComponent != null) {
      rootTabComponent.setRootTab(true);
    }
    return rootTabComponent;
  }

  public List<String> getProcessViews() {
    return processViews;
  }

  public void setProcess(org.openbravo.client.application.Process process) {
    this.process = process;
    paramHandler.setProcess(process);
    paramHandler.setParamWindow(this);
  }

  private void processParameter() {
    for (org.openbravo.client.application.Parameter p : process.getOBUIAPPParameterList()) {
      if (p.getReference().getId().equals(ApplicationConstants.WINDOW_REFERENCE_ID)) {
        if (p.getReferenceSearchKey().getOBUIAPPRefWindowList().size() == 0
            || p.getReferenceSearchKey().getOBUIAPPRefWindowList().get(0).getWindow() == null) {
          // log.error(String.format(AD_DEF_ERROR, p.getId(), "Window", "window"));
          System.out.println("oooo");
        } else {
          this.window = p.getReferenceSearchKey().getOBUIAPPRefWindowList().get(0).getWindow();
        }
        return;
      }
    }
  }

  public OBViewParameterHandler getParamHandler() {
    return paramHandler;
  }
}
