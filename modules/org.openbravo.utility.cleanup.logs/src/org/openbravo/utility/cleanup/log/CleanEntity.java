package org.openbravo.utility.cleanup.log;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.ProcessLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Qualifier("Default")
public class CleanEntity {
  protected static final String SYSTEM = "0";

  private static final Logger log = LoggerFactory.getLogger(CleanEntity.class);

  public void clean(LogCleanUpConfig config, Client client, Organization org, ProcessLogger bgLogger) {
    Entity entity = ModelProvider.getInstance().getEntityByTableId(
        (String) DalUtil.getId(config.getTable()));

    String hql = "select count(*) from " + entity.getName();

    String where = "";
    if (config.getOlderThan() != 0L) {
      String prop;
      if (config.getColumn() == null) {
        prop = entity.getPropertyByColumnName("created").getName();
      } else {
        prop = entity.getPropertyByColumnName(config.getColumn().getDBColumnName()).getName();
      }
      String since = prop + " < now() - " + config.getOlderThan();
      if (!StringUtils.isEmpty(since)) {
        where = " where " + since;
      }
    }

    if (!StringUtils.isEmpty(config.getHQLWhereClause())) {
      where += StringUtils.isEmpty(where) ? " where " : " and ";
      where += "(" + config.getHQLWhereClause() + ")";
    }

    String clientOrgFilter = getClientOrgFilter(client, org);
    if (!StringUtils.isEmpty(clientOrgFilter)) {
      where += StringUtils.isEmpty(where) ? " where " : " and ";
      where += clientOrgFilter;
    }

    hql += where;

    log.debug("  Query: {}", hql);

    Session s = OBDal.getInstance().getSession();
    Query q = s.createQuery(hql);
    log.debug("    -rows: {}", q.list().get(0));
  }

  protected String getClientOrgFilter(Client client, Organization org) {
    String clientId = (String) DalUtil.getId(client);
    if (SYSTEM.equals(clientId)) {
      return "";
    }

    String filter = "client.id = '" + clientId + "'";

    String orgId = (String) DalUtil.getId(org);
    if (!SYSTEM.equals(orgId)) {
      OrganizationStructureProvider orgTree = OBContext.getOBContext()
          .getOrganizationStructureProvider(clientId);

      String orgFilter = "";
      for (String childOrg : orgTree.getChildTree(orgId, true)) {
        if (!StringUtils.isEmpty(orgFilter)) {
          orgFilter += ", ";
        }
        orgFilter += "'" + childOrg + "'";
      }
      orgFilter = " and organization.id in (" + orgFilter + ")";
      filter += orgFilter;
    }

    return filter;
  }
}
