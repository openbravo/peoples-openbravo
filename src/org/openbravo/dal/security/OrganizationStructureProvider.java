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
 * All portions are Copyright (C) 2008-2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hibernate.SQLQuery;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a tree of organizations to compute the accessible organizations for the current
 * organizations of a user. Is used to check if references from one object to another are correct
 * from an organization structure perspective.
 * <p/>
 * For example a city refers to a country then: an organization of the country (the refered object)
 * must be in the natural tree of the organization of the city (the referee).
 * 
 * @author mtaal
 */

public class OrganizationStructureProvider implements OBNotSingleton {
  final static Logger log = LoggerFactory.getLogger(OrganizationStructureProvider.class);

  private boolean isInitialized = false;
  private Map<String, OrgNode> orgNodes;
  private static final String AD_ORG_TABLE_ID = "155";
  private String clientId;

  /**
   * Set initialized to false and recompute the organization structures
   */
  public void reInitialize() {
    isInitialized = false;
    initialize();
  }

  private synchronized void initialize() {
    if (isInitialized) {
      return;
    }

    if (getClientId() == null) {
      setClientId(OBContext.getOBContext().getCurrentClient().getId());
    }

    String sql = "select n.node_id, n.parent_id, o.isready, ot.islegalentity, ot.isbusinessunit, ot.istransactionsallowed, ot.isacctlegalentity  \n"
        + "  from ad_tree t, ad_treenode n, ad_org o, ad_orgtype ot\n"
        + " where n.node_id = o.ad_org_id \n"
        + "   and o.ad_orgtype_id = ot.ad_orgtype_id \n"
        + "   and n.ad_tree_id = t.ad_tree_id\n"
        + "   and t.ad_table_id = :tableId\n"
        + "   and t.ad_client_id = :clientId";

    // read all trees of all clients, bypass DAL to prevent security checks
    SQLQuery qry = OBDal.getInstance().getSession().createSQLQuery(sql);
    qry.setParameter("clientId", getClientId());
    qry.setParameter("tableId", AD_ORG_TABLE_ID);

    @SuppressWarnings("unchecked")
    List<Object[]> treeNodes = qry.list();

    orgNodes = new HashMap<>(treeNodes.size());

    for (Object[] nodeDef : treeNodes) {
      final OrgNode on = new OrgNode();
      String nodeId = (String) nodeDef[0];
      on.setTreeNodeData(nodeDef);
      orgNodes.put(nodeId, on);
    }

    List<OrgNode> nodes = new ArrayList<>(orgNodes.values());
    for (final OrgNode on : nodes) {
      on.resolve(nodes);
    }

    isInitialized = true;
  }

  /**
   * Returns the natural tree of an organization.
   * 
   * @param orgId
   *          the id of the organization for which the natural tree is determined.
   * @return the natural tree of the organization.
   */
  public Set<String> getNaturalTree(String orgId) {
    initialize();
    long t = System.currentTimeMillis();
    OrgNode node = orgNodes.get(orgId);
    if (node == null) {
      return new HashSet<>(Arrays.asList(orgId));
    } else {
      Set<String> result = new HashSet<>(getParentTree(orgId, true));
      result.addAll(getChildTree(orgId, false));
      log.info("getNaturalTree {} - {} ms", orgId, System.currentTimeMillis() - t);
      return result;
    }
  }

  /**
   * Checks if an organization (org2) is in the natural tree of another organization (org1).
   * 
   * @param org1
   *          the natural tree of this organization is used to check if org2 is present
   * @param org2
   *          the organization checked in the natural tree of org1
   * @return true if org2 is in the natural tree of org1, false otherwise
   */
  public boolean isInNaturalTree(Organization org1, Organization org2) {
    initialize();
    final String id1 = org1.getId();
    final String id2 = org2.getId();

    // org 0 is in everyones natural tree, and the other way around
    if (id2 != null && id2.equals("0")) {
      return true;
    }
    if (id1 != null && id1.equals("0")) {
      return true;
    }

    final Set<String> ids = getNaturalTree(id1);
    Check.isNotNull(ids, "Organization with id " + id1
        + " does not have a computed natural tree, does this organization exist?");
    return ids.contains(id2);
  }

  /**
   * Returns the parent organization tree of an organization.
   * 
   * @param orgId
   *          the id of the organization for which the parent organization tree is determined.
   * @param includeOrg
   *          if true, returns also the given organization as part of the tree
   * @return the parent organization tree of the organization.
   */
  public Set<String> getParentTree(String orgId, boolean includeOrg) {
    initialize();
    String parentOrg = getParentOrg(orgId);
    Set<String> result = new HashSet<String>();

    if (includeOrg) {
      result.add(orgId);
    }

    while (parentOrg != null) {
      result.add(parentOrg);
      parentOrg = getParentOrg(parentOrg);
    }
    return result;
  }

  /**
   * Returns an ordered list of parents of an organization. The parents are listed from the
   * organization and up (so parent before grand parent).
   * 
   * @param orgId
   *          the id of the organization for which the parent organization tree is determined.
   * @param includeOrg
   *          if true, returns also the given organization as part of the tree
   * @return the parent organization tree of the organization.
   */
  public List<String> getParentList(String orgId, boolean includeOrg) {
    initialize();
    String parentOrg = this.getParentOrg(orgId);
    List<String> result = new ArrayList<String>();

    if (includeOrg) {
      result.add(orgId);
    }

    while (parentOrg != null) {
      result.add(parentOrg);
      parentOrg = this.getParentOrg(parentOrg);
    }
    return result;
  }

  /**
   * Returns the parent organization of an organization.
   * 
   * @param orgId
   *          the id of the organization for which the parent organization is determined.
   * @return the parent organization.
   */
  public String getParentOrg(String orgId) {
    initialize();

    OrgNode node = orgNodes.get(orgId);
    return node == null ? null : node.getParentNodeId();
  }

  /**
   * Returns the parent organization of an organization.
   * 
   * @param org
   *          the organization for which the parent organization is determined.
   * @return the parent organization.
   */
  public Organization getParentOrg(Organization org) {
    String parentOrgId = getParentOrg(org.getId());
    return parentOrgId == null ? null : OBDal.getInstance().get(Organization.class, parentOrgId);
  }

  /**
   * Returns the child organization tree of an organization.
   * 
   * @param orgId
   *          the id of the organization for which the child organization tree is determined.
   * @param includeOrg
   *          if true, returns also the given organization as part of the tree
   * @return the child organization tree of the organization.
   */
  public Set<String> getChildTree(String orgId, boolean includeOrg) {
    initialize();

    OrgNode node = orgNodes.get(orgId);
    Set<String> result = new HashSet<>();
    if (includeOrg) {
      result.add(orgId);
    }

    if (node == null) {
      reInitialize();

      node = orgNodes.get(orgId);
      if (node == null) {
        return result;
      }
    }

    Set<String> childOrg = getChildOrg(orgId);

    for (String co : childOrg) {
      result.add(co);
      childOrg = getChildTree(co, false);
      result.addAll(childOrg);
    }
    return result;
  }

  /**
   * Returns the child organizations of an organization.
   * 
   * @param orgId
   *          the id of the organization for which the child organizations are determined.
   * @return the child organizations
   */
  public Set<String> getChildOrg(String orgId) {
    initialize();

    OrgNode node = orgNodes.get(orgId);
    if (node == null) {
      reInitialize();

      node = orgNodes.get(orgId);
      if (node == null) {
        return new HashSet<String>(0);
      }
    }

    Set<String> os = new HashSet<String>(node.getChildren().size());
    for (String child : node.getChildren()) {
      os.add(child);
    }
    return os;
  }

  class OrgNode {
    private String nodeId;
    private String parentNodeId;
    private boolean isReady;
    private boolean isLegalEntity;
    private boolean isBusinessUnit;
    private boolean isTransactionsAllowed;
    private boolean isAcctLegalEntity;

    private List<String> children = new ArrayList<>();

    void addChild(String childId) {
      children.add(childId);
    }

    public void setTreeNodeData(Object[] nodeDef) {
      nodeId = (String) nodeDef[0];
      parentNodeId = (String) nodeDef[1];
      isReady = Objects.equals('Y', nodeDef[2]);// "Y".equals(nodeDef[2]);
      isLegalEntity = Objects.equals('Y', nodeDef[3]); // "Y".equals(nodeDef[3]);
      isBusinessUnit = Objects.equals('Y', nodeDef[4]); // "Y".equals(nodeDef[4]);
      isTransactionsAllowed = Objects.equals('Y', nodeDef[5]); // "Y".equals(nodeDef[5]);
      isAcctLegalEntity = Objects.equals('Y', nodeDef[6]);// "Y".equals(nodeDef[6]);
    }

    public void resolve(List<OrgNode> nodes) {
      if (parentNodeId == null) {
        return;
      }
      for (final OrgNode on : nodes) {
        if (on.getNodeId().equals(parentNodeId)) {
          on.addChild(nodeId);
          break;
        }
      }
    }

    public String getNodeId() {
      return nodeId;
    }

    public String getParentNodeId() {
      return parentNodeId;
    }

    public List<String> getChildren() {
      return children;
    }
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /*
   * Returns the legal entities of the client.
   */
  public List<Organization> getLegalEntitiesList() {
    return getLegalEntitiesListForSelectedClient(clientId);
  }

  /*
   * Returns the legal entities of the selected client.
   */
  public List<Organization> getLegalEntitiesListForSelectedClient(String paramClientId) {
    StringBuffer where = new StringBuffer();
    where.append(" as org");
    where.append(" join org." + Organization.PROPERTY_ORGANIZATIONTYPE + " as orgType");
    where.append(" where org." + Organization.PROPERTY_CLIENT + ".id = :client");
    where.append("   and orgType." + OrganizationType.PROPERTY_LEGALENTITY + " = true");
    OBQuery<Organization> orgQry = OBDal.getInstance().createQuery(Organization.class,
        where.toString());
    orgQry.setFilterOnReadableClients(false);
    orgQry.setFilterOnReadableOrganization(false);
    orgQry.setNamedParameter("client", paramClientId);
    return orgQry.list();
  }

  /**
   * Returns the legal entity of the given organization
   * 
   * @param org
   *          organization to get its legal entity
   * @return legal entity (with or without accounting) organization or null if not found
   */
  public Organization getLegalEntity(final Organization org) {
    // Admin mode needed to get the Organization type.
    OBContext.setAdminMode(true);
    try {
      for (final String orgId : getParentList(org.getId(), true)) {
        final Organization parentOrg = OBDal.getInstance().get(Organization.class, orgId);
        if (parentOrg.getOrganizationType().isLegalEntity()) {
          return parentOrg;
        }
      }
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the list of legal entities that are children of the given organization
   * 
   * @param org
   *          organization to get its child legal entities
   * @return legal entity (with or without accounting) organization or null if not found
   */
  public List<Organization> getChildLegalEntitesList(final Organization org) {
    // Admin mode needed to get the Organization type.
    OBContext.setAdminMode(true);
    List<Organization> childLegalEntitiesList = new ArrayList<Organization>();
    try {
      for (final String orgId : getChildTree(org.getId(), false)) {
        final Organization childOrg = OBDal.getInstance().get(Organization.class, orgId);
        if (childOrg.getOrganizationType().isLegalEntity()) {
          childLegalEntitiesList.add(childOrg);
        }
      }
      return childLegalEntitiesList;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the legal entity or Business Unit of the given organization
   * 
   * @param org
   *          organization to get its legal entity or business unit
   * @return legal entity (with or without accounting) organization or null if not found
   */
  public Organization getLegalEntityOrBusinessUnit(final Organization org) {
    // Admin mode needed to get the Organization type.
    OBContext.setAdminMode(true);
    try {
      for (final String orgId : getParentList(org.getId(), true)) {
        final Organization parentOrg = OBDal.getInstance().get(Organization.class, orgId);
        if (parentOrg.getOrganizationType().isLegalEntity()
            || parentOrg.getOrganizationType().isBusinessUnit()) {
          return parentOrg;
        }
      }
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the organization that is period control allowed for the org Organization. If no
   * organization is found, it returns NULL.
   * 
   * @param org
   *          Organization to get its period control allowed organization.
   */
  public Organization getPeriodControlAllowedOrganization(final Organization org) {
    // Admin mode needed to get the Organization type.
    OBContext.setAdminMode(true);
    try {
      if (org.isAllowPeriodControl()) {
        return org;
      }
      for (final String orgId : getParentList(org.getId(), false)) {
        final Organization parentOrg = OBDal.getInstance().get(Organization.class, orgId);
        if (parentOrg.isAllowPeriodControl()) {
          return parentOrg;
        }
      }
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}