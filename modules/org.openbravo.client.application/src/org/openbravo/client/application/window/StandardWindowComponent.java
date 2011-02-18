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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.ad.ui.WindowTrl;

/**
 * The component which takes care of creating a class for a specific Openbravo window.
 * 
 * @author mtaal
 */
public class StandardWindowComponent extends BaseTemplateComponent {

  private static final String TEMPLATE_ID = "ADD5EF45333C458098286D0E639B3290";

  private Window window;
  private OBViewTab rootTabComponent = null;

  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, TEMPLATE_ID);
  }

  public String generate() {
    final String jsCode = super.generate();
    // System.err.println(jsCode);
    return jsCode;
  }

  public String getTabView() {
    return getRootTabComponent().generate();
  }

  public String getWindowId() {
    return getWindow().getId();
  }

  public Window getWindow() {
    return window;
  }

  public void setWindow(Window window) {
    this.window = window;
  }

  public OBViewTab getRootTabComponent() {
    if (rootTabComponent != null) {
      return rootTabComponent;
    }

    final List<OBViewTab> tempTabs = new ArrayList<OBViewTab>();
    for (Tab tab : getWindow().getADTabList()) {
      if (!tab.isActive()) {
        continue;
      }
      final OBViewTab tabComponent = new OBViewTab();
      tabComponent.setTab(tab);
      tempTabs.add(tabComponent);
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
    int rootCnt = 0;
    for (OBViewTab tabComponent : tempTabs) {
      if (tabComponent.getParentTabComponent() == null) {
        rootCnt++;
      }
    }
    if (rootCnt > 1) {
      // create a new root tab
      rootTabComponent = new OBViewTab();
      final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
      String tabTitle = null;
      for (WindowTrl windowTrl : window.getADWindowTrlList()) {
        final String trlLanguageId = (String) DalUtil.getId(windowTrl.getLanguage());
        if (trlLanguageId.equals(userLanguageId)) {
          tabTitle = windowTrl.getName();
        }
      }
      if (tabTitle == null) {
        tabTitle = window.getName();
      }
      rootTabComponent.setTabTitle(tabTitle);
      for (OBViewTab tabComponent : tempTabs) {
        if (tabComponent.getParentTabComponent() == null) {
          rootTabComponent.addChildTabComponent(tabComponent);
        }
      }
    } else {
      // now get the root tabs
      for (OBViewTab tabComponent : tempTabs) {
        if (tabComponent.getParentTabComponent() == null) {
          if (rootTabComponent != null) {
            throw new IllegalStateException("More than one root tab for window "
                + getWindow().getName() + " " + getWindow().getId());
          }
          rootTabComponent = tabComponent;
        }
      }
    }

    return rootTabComponent;
  }
}
