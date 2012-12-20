/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.service.json.JsonUtils;

/**
 * An HQL Query builder.
 * 
 * @author adrianromero
 */
public class SimpleQueryBuilder {

  private String hql;
  private String client;
  private String org;
  private Date lastUpdated;

  public SimpleQueryBuilder(String hql, String client, String org, Date lastUpdated) {
    this.hql = hql;
    this.client = client;
    this.org = org;
    this.lastUpdated = lastUpdated;
    // :orgCriteria
    // :clientCriteria
    // :activeCriteria
  }

  private static String getClientFilter(Collection<String> clients) {

    StringBuilder clientfilter = new StringBuilder();

    if (clients.size() == 0) {
      clientfilter.append(" (1=1) ");
    } else {
      clientfilter.append(" ($$$$client.id in (");
      boolean comma = false;
      for (String s : clients) {
        if (comma) {
          clientfilter.append(", ");
        } else {
          comma = true;
        }
        clientfilter.append("'");
        clientfilter.append(s);
        clientfilter.append("'");
      }
      clientfilter.append(")) ");
    }

    return clientfilter.toString();
  }

  private static String getOrgFilter(Collection<String> orgs) {

    StringBuilder orgfilter = new StringBuilder();
    if (orgs.isEmpty()) {
      orgfilter.append(" (1=1) ");
    } else {
      orgfilter.append(" ($$$$organization.id in (");
      boolean comma = false;
      for (String s : orgs) {
        if (comma) {
          orgfilter.append(", ");
        } else {
          comma = true;
        }
        orgfilter.append("'");
        orgfilter.append(s);
        orgfilter.append("'");
      }
      orgfilter.append(")) ");
    }
    return orgfilter.toString();
  }

  private static class NaturalOrganizationCriteria implements PartBuilder {

    private String client;
    private String org;

    public NaturalOrganizationCriteria(String client, String org) {
      this.client = client;
      this.org = org;
    }

    public String getPart() {
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(client);
      return getOrgFilter(osp.getNaturalTree(org));
    }
  }

  private static class ChildOrganizationCriteria implements PartBuilder {

    private String client;
    private String org;

    public ChildOrganizationCriteria(String client, String org) {
      this.client = client;
      this.org = org;
    }

    public String getPart() {
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(client);
      return getOrgFilter(osp.getChildTree(org, true));
    }
  }

  private static class ParentOrganizationCriteria implements PartBuilder {

    private String client;
    private String org;

    public ParentOrganizationCriteria(String client, String org) {
      this.client = client;
      this.org = org;
    }

    public String getPart() {
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider(client);
      return getOrgFilter(osp.getParentTree(org, true));
    }
  }

  private static class CurrentOrganizationCriteria implements PartBuilder {
    public String getPart() {
      return "'" + OBContext.getOBContext().getCurrentOrganization().getId() + "'";
    }
  }

  private static class CurrentClientCriteria implements PartBuilder {
    public String getPart() {
      return "'" + OBContext.getOBContext().getCurrentClient().getId() + "'";
    }
  }

  private static class OrganizationCriteria implements PartBuilder {
    public String getPart() {
      return getOrgFilter(Arrays.asList(OBContext.getOBContext().getReadableOrganizations()));
    }
  }

  private static class ClientCriteria implements PartBuilder {
    public String getPart() {
      return getClientFilter(Arrays.asList(OBContext.getOBContext().getReadableClients()));
    }
  }

  private static class ReadableCriteria implements PartBuilder {
    private PartBuilder client = new ClientCriteria();
    private PartBuilder org = new OrganizationCriteria();
    private PartBuilder active = new ActiveCriteria();

    public String getPart() {
      return " (" + client.getPart() + " and " + org.getPart() + " and " + active.getPart() + ") ";
    }
  }

  private static class ReadableClientCriteria implements PartBuilder {
    private PartBuilder client = new ClientCriteria();
    private PartBuilder active = new ActiveCriteria();

    public String getPart() {
      return " (" + client.getPart() + " and " + active.getPart() + ") ";
    }
  }

  private static class ActiveCriteria implements PartBuilder {
    public String getPart() {
      return " ($$$$active = 'Y') ";
    }
  }

  private static class RoleId implements PartBuilder {
    public String getPart() {
      return "'" + OBContext.getOBContext().getRole().getId() + "'";
    }
  }

  private static class UserId implements PartBuilder {
    public String getPart() {
      return "'" + OBContext.getOBContext().getUser().getId() + "'";
    }
  }

  private static class LanguageId implements PartBuilder {
    public String getPart() {
      return "'" + OBContext.getOBContext().getLanguage().getId() + "'";
    }
  }

  private static class IncrementalUpdateCriteria implements PartBuilder {
    Date lastUpdate;
    SimpleDateFormat dateFormat = JsonUtils.createDateTimeFormat();

    public IncrementalUpdateCriteria(Date lastUpdate) {
      this.lastUpdate = lastUpdate;
    }

    public String getPart() {
      String part = "";
      part += " $$$$updated>'"
          + (lastUpdate == null ? dateFormat.format(new Date(0)) : dateFormat.format(lastUpdate))
          + "' ";
      if (lastUpdate == null) {
        part += "OR (1=1) ";
      }
      return part;
    }
  }

  public String getHQLQuery() {

    String newhql = hql;

    newhql = replaceAll(newhql, "$clientCriteria", new ClientCriteria());

    newhql = replaceAll(newhql, "$orgCriteria", new OrganizationCriteria());
    newhql = replaceAll(newhql, "$clientId", new CurrentClientCriteria());
    newhql = replaceAll(newhql, "$orgId", new CurrentOrganizationCriteria());
    newhql = replaceAll(newhql, "$naturalOrgCriteria", new NaturalOrganizationCriteria(client, org));
    newhql = replaceAll(newhql, "$parentOrgCriteria", new ParentOrganizationCriteria(client, org));
    newhql = replaceAll(newhql, "$childOrgCriteria", new ChildOrganizationCriteria(client, org));
    newhql = replaceAll(newhql, "$activeCriteria", new ActiveCriteria());
    newhql = replaceAll(newhql, "$incrementalUpdateCriteria", new IncrementalUpdateCriteria(
        lastUpdated));

    newhql = replaceAll(newhql, "$readableCriteria", new ReadableCriteria());
    newhql = replaceAll(newhql, "$readableClientCriteria", new ReadableClientCriteria());

    newhql = replaceAll(newhql, "$roleId", new RoleId());
    newhql = replaceAll(newhql, "$userId", new UserId());
    newhql = replaceAll(newhql, "$languageId", new LanguageId());

    return newhql;
  }

  private String replaceAll(String s, String search, PartBuilder part) {
    String news = s;
    int i = news.indexOf(search);
    if (i >= 0) {
      String replacement = part.getPart();
      while (i >= 0) {
        int alias = findalias(news, i);
        if (alias >= 0) {
          news = news.substring(0, alias)
              + replacement.replaceAll("\\$\\$\\$\\$", news.substring(alias, i))
              + news.substring(i + search.length());
        } else {
          news = news.substring(0, i) + replacement.replaceAll("\\$\\$\\$\\$", "")
              + news.substring(i + search.length());
        }

        i = news.indexOf(search);
      }
    }
    return news;
  }

  private int findalias(String sentence, int position) {

    int i = position - 1;
    int s = 0;

    while (i > 0) {
      char c = sentence.charAt(i);
      if (s == 0) {
        if (c == '.') {
          s = 1;
        } else {
          return -1;
        }
      } else if (s == 1) {
        if (Character.isLetterOrDigit(c)) {
          s = 2;
        } else {
          return -1;
        }
      } else if (s == 2) {
        if (!Character.isLetterOrDigit(c)) {
          if (Character.isWhitespace(c) || c == ')' || c == '(') {
            return i + 1;
          } else {
            return -1;
          }
        }
      }
      i--;
    }
    return -1;
  }
}
