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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.application.personalization;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.UIPersonalization;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Handles personalization settings, stores them and retrieves them, taking into account priority
 * order.
 * 
 */
@RequestScoped
public class PersonalizationHandler {
  private static final Logger log4j = Logger.getLogger(PersonalizationHandler.class);

  /**
   * Returns all the personalization settings in an object keyed by tabid. The current client, org,
   * role and user are taken into account to find the correct personalization entry. If no
   * personalization record is present then null is returned for that specific tab.
   * 
   * @param window
   *          the window for which the personalization settings are to be returned.
   * @return the personalization settings in a json object for a window.
   */
  public JSONObject getPersonalizationForWindow(Window window) {
    OBContext.setAdminMode(false);
    try {
      final JSONObject result = new JSONObject();
      for (Tab tab : window.getADTabList()) {
        final UIPersonalization uiPersonalization = getPersonalizationForTab(tab);
        if (uiPersonalization == null || uiPersonalization.getValue() == null) {
          result.put(tab.getId(), (Object) null);
        } else {
          final JSONObject persJSON = new JSONObject(uiPersonalization.getValue());
          // if on user level then allow delete
          if (uiPersonalization.getUser() != null) {
            persJSON.put("canDelete", true);
          }
          persJSON.put("personalizationId", uiPersonalization.getId());
          result.put(tab.getId(), persJSON);
        }
      }
      return result;
    } catch (Exception e) {
      throw new OBException("Exception when getting personalization settings for window " + window,
          e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns all the personalization settings for a tab. The current client, org, role and user are
   * taken into account to find the correct personalization entry. If no personalization entries are
   * present then null is returned.
   * 
   * @param tab
   *          the tab for which the personalization settings are to be returned.
   * @return the personalization settings in a json object for this tab.
   */
  public UIPersonalization getPersonalizationForTab(Tab tab) {
    OBContext.setAdminMode(false);
    try {
      return getPersonalization(OBContext.getOBContext().getCurrentClient().getId(), OBContext
          .getOBContext().getCurrentOrganization().getId(), OBContext.getOBContext().getRole()
          .getId(), OBContext.getOBContext().getUser().getId(), tab.getId(), false);
    } catch (Exception e) {
      throw new OBException("Exception when getting personalization settings for tab " + tab, e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public UIPersonalization getPersonalization(String clientId, String orgId, String roleId,
      String userId, String tabId, boolean exactMatch) {
    OBContext.setAdminMode(false);
    try {
      final List<UIPersonalization> pers = getPersonalizations(clientId, orgId, userId, roleId,
          tabId, exactMatch);
      if (pers.isEmpty()) {
        return null;
      }
      if (exactMatch) {
        if (pers.size() > 0) {
          log4j.warn("There are is more than one ui personalization record "
              + "for a certain exact match, ignoring it, just picking the first one: "
              + pers.get(0));
        }
        return pers.get(0);
      }

      // find the best match
      UIPersonalization selectedUIPersonalization = null;
      List<String> parentTree = null;
      if (orgId != null) {
        parentTree = OBContext.getOBContext().getOrganizationStructureProvider(clientId)
            .getParentList(orgId, true);
      }
      for (UIPersonalization uiPersonalization : pers) {
        // select the highest priority or raise exception in case of conflict
        if (selectedUIPersonalization == null) {
          selectedUIPersonalization = uiPersonalization;
          continue;
        }
        int higherPriority = isHigherPriority(selectedUIPersonalization, uiPersonalization,
            parentTree);
        switch (higherPriority) {
        case 1:
          // do nothing, selected one has higher priority
          break;
        case 2:
          selectedUIPersonalization = uiPersonalization;
          break;
        default:
          // conflict ignore
          break;
        }
      }
      return selectedUIPersonalization;
    } catch (Exception e) {
      // TODO: add param values to message
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Stores the personalization json object for a certain level, if there is no current record then
   * a new one is created and persisted. If the pers
   * 
   * @param persId
   *          if a specific personalization id is set then the system updates that record and
   *          ignores the other parameters.
   * @param clientId
   *          the client, maybe null
   * @param orgId
   *          the organization id, maybe null
   * @param roleId
   *          the role id, maybe null
   * @param userId
   *          the user id, maybe null
   * @param tabId
   *          the tab id, may not be null
   * @param target
   *          the personalization target, is either form or grid
   * @param value
   *          the value, a json string
   * @return the persisted record
   */
  public UIPersonalization storePersonalization(String persId, String clientId, String orgId,
      String roleId, String userId, String tabId, String target, String value) {
    OBContext.setAdminMode(false);
    try {
      UIPersonalization uiPersonalization;
      if (persId != null) {
        uiPersonalization = OBDal.getInstance().get(UIPersonalization.class, persId);
        if (uiPersonalization == null) {
          throw new IllegalArgumentException("UI Personalization with id " + persId + " not found");
        }
      } else {
        uiPersonalization = getPersonalization(clientId, orgId, roleId, userId, tabId, true);
      }

      if (uiPersonalization == null) {
        uiPersonalization = OBProvider.getInstance().get(UIPersonalization.class);
        uiPersonalization.setClient(OBDal.getInstance().get(Client.class, "0"));
        uiPersonalization.setOrganization(OBDal.getInstance().get(Organization.class, "0"));

        if (clientId != null) {
          uiPersonalization.setVisibleAtClient(OBDal.getInstance().get(Client.class, clientId));
          // also store it in that client
          uiPersonalization.setClient(uiPersonalization.getVisibleAtClient());
        }
        if (orgId != null) {
          uiPersonalization.setVisibleAtOrganization(OBDal.getInstance().get(Organization.class,
              orgId));
        }

        if (roleId != null) {
          uiPersonalization.setVisibleAtRole(OBDal.getInstance().get(Role.class, roleId));
        }

        if (userId != null) {
          uiPersonalization.setUser(OBDal.getInstance().get(User.class, userId));
        }
        uiPersonalization.setTab(OBDal.getInstance().get(Tab.class, tabId));
      }
      final JSONObject jsonValue = new JSONObject(value);
      JSONObject jsonObject;
      if (uiPersonalization.getValue() != null) {
        jsonObject = new JSONObject(uiPersonalization.getValue());
      } else {
        jsonObject = new JSONObject();
      }
      jsonObject.put(target, (Object) jsonValue);
      uiPersonalization.setValue(jsonObject.toString());
      OBDal.getInstance().save(uiPersonalization);
      return uiPersonalization;
    } catch (Exception e) {
      // TODO: add param values to message
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static List<UIPersonalization> getPersonalizations(String clientId, String orgId,
      String userId, String roleId, String tabId, boolean exactMatch) {

    List<Object> parameters = new ArrayList<Object>();
    StringBuilder hql = new StringBuilder();
    hql.append(" as p ");
    hql.append(" where ");
    if (exactMatch) {
      if (clientId != null) {
        hql.append(" p.visibleAtClient.id = ? ");
        parameters.add(clientId);
      } else {
        hql.append(" p.visibleAtClient is null");
      }

      if (orgId != null) {
        hql.append(" and p.visibleAtOrganization.id = ? ");
        parameters.add(orgId);
      } else {
        hql.append(" and p.visibleAtOrganization is null ");
      }

      if (userId != null) {
        hql.append(" and p.user.id = ? ");
        parameters.add(userId);
      } else {
        hql.append(" and p.user is null ");
      }

      if (roleId != null) {
        hql.append(" and p.visibleAtRole.id = ? ");
        parameters.add(roleId);
      } else {
        hql.append(" and p.visibleAtRole is null");
      }
    } else {
      if (clientId != null) {
        hql.append(" (p.visibleAtClient.id = ? or ");
        parameters.add(clientId);
      } else {
        hql.append(" (");
      }
      hql.append(" coalesce(p.visibleAtClient, '0')='0') ");

      if (roleId != null) {
        hql.append(" and   (p.visibleAtRole.id = ? or ");
        parameters.add(roleId);
      } else {
        hql.append(" and (");
      }
      hql.append(" p.visibleAtRole is null) ");

      // note orgId != null is handled below
      if (orgId == null) {
        hql.append(" and (coalesce(p.visibleAtOrganization, '0')='0'))");
      }

      if (userId != null) {
        hql.append("  and (p.user.id = ? or ");
        parameters.add(userId);
      } else {
        hql.append(" and (");
      }
      hql.append(" p.user is null) ");
    }

    hql.append(" and  p.tab.id = ? ");
    parameters.add(tabId);

    OBQuery<UIPersonalization> qPers = OBDal.getInstance().createQuery(UIPersonalization.class,
        hql.toString());
    qPers.setParameters(parameters);
    List<UIPersonalization> personalizations = qPers.list();

    if (orgId != null && !exactMatch) {
      // Remove from list organization that are not visible
      final Organization org = OBDal.getInstance().get(Organization.class, orgId);
      List<String> parentTree = OBContext.getOBContext()
          .getOrganizationStructureProvider((String) DalUtil.getId(org.getClient()))
          .getParentList(orgId, true);
      List<UIPersonalization> auxPersonalizations = new ArrayList<UIPersonalization>();
      for (UIPersonalization pers : personalizations) {
        if (pers.getVisibleAtOrganization() == null
            || parentTree.contains(pers.getVisibleAtOrganization().getId())) {
          auxPersonalizations.add(pers);
        }
      }
      return auxPersonalizations;
    } else {
      return personalizations;
    }
  }

  /**
   * Determines which of the 2 personalizations has higher visibility priority.
   * 
   * @param pers1
   *          First personalization to compare
   * @param pers2
   *          Second personalization to compare
   * @param parentTree
   *          Parent tree of organizations including the current one, used to assign more priority
   *          to organizations nearer in the tree.
   * @return <ul>
   *         <li>1 in case pers1 is more visible than pers2
   *         <li>2 in case pers2 is more visible than pers1
   *         <li>0 in case of conflict (both have identical visibility and value)
   *         </ul>
   */
  private static int isHigherPriority(UIPersonalization pers1, UIPersonalization pers2,
      List<String> parentTree) {
    // Check priority by client
    if ((pers2.getVisibleAtClient() == null || pers2.getVisibleAtClient().getId().equals("0"))
        && pers1.getVisibleAtClient() != null && !pers1.getVisibleAtClient().getId().equals("0")) {
      return 1;
    }

    // Check priority by organization
    Organization org1 = pers1.getVisibleAtOrganization();
    Organization org2 = pers2.getVisibleAtOrganization();
    if (org1 != null && org2 == null) {
      return 1;
    }

    if ((org1 == null && org2 != null)) {
      return 2;
    }

    if (org1 != null && org2 != null) {
      int depth1 = parentTree.indexOf(org1.getId());
      int depth2 = parentTree.indexOf(org2.getId());

      if (depth1 < depth2) {
        return 1;
      } else if (depth1 > depth2) {
        return 2;
      }
    }

    // Check priority by user
    if (pers1.getUser() != null && pers2.getUser() == null) {
      return 1;
    }

    if (pers1.getUser() == null && pers2.getUser() != null) {
      return 2;
    }

    // Check priority by role
    if (pers1.getVisibleAtRole() != null && pers2.getVisibleAtRole() == null) {
      return 1;
    }

    if (pers1.getVisibleAtRole() == null && pers2.getVisibleAtRole() != null) {
      return 2;
    }

    // Actual conflict
    return 0;
  }
}
