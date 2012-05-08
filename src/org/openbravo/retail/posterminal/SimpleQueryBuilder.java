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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import org.openbravo.dal.core.OBContext;

/**
 * An HQL Query builder.
 * 
 * @author adrianromero
 */
public class SimpleQueryBuilder {

  private String hql;

  public SimpleQueryBuilder(String hql) {
    this.hql = hql;

    // :orgCriteria
    // :clientCriteria
    // :activeCriteria
  }

  private static class OrganizationCriteria implements PartBuilder {
    public String getPart() {
  
      StringBuilder orgfilter = new StringBuilder();
  
      final String[] orgs = OBContext.getOBContext().getReadableOrganizations();
  
      if (orgs.length == 0) {
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
  }

  private static class ClientCriteria implements PartBuilder {
    public String getPart() {

      StringBuilder clientfilter = new StringBuilder();

      final String[] clients = OBContext.getOBContext().getReadableClients();

      if (clients.length == 0) {
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
  }

  private static class ReadableCriteria implements PartBuilder {
    private PartBuilder client = new ClientCriteria();
    private PartBuilder org = new OrganizationCriteria();
    private PartBuilder active = new ActiveCriteria();

    public String getPart() {
      return " (" + client.getPart() + " and " + org.getPart() + " and " + active.getPart() + ") ";
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

  public String getHQLQuery() {

    String newhql = hql;

    newhql = replaceAll(newhql, "$clientCriteria", new ClientCriteria());
    newhql = replaceAll(newhql, "$orgCriteria", new OrganizationCriteria());
    newhql = replaceAll(newhql, "$activeCriteria", new ActiveCriteria());
    newhql = replaceAll(newhql, "$readableCriteria", new ReadableCriteria());
    newhql = replaceAll(newhql, "$roleId", new RoleId());
    newhql = replaceAll(newhql, "$userId", new UserId());

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
