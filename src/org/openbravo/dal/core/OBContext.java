/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.openbravo.dal.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.openbravo.base.model.ad.Client;
import org.openbravo.base.model.ad.Language;
import org.openbravo.base.model.ad.Org;
import org.openbravo.base.model.ad.Role;
import org.openbravo.base.model.ad.RoleOrgAccess;
import org.openbravo.base.model.ad.User;
import org.openbravo.base.model.ad.UserRoles;
import org.openbravo.base.util.Check;
import org.openbravo.dal.security.EntityAccessChecker;
import org.openbravo.dal.security.OrganisationStructureProvider;

/**
 * Models the context in which dal actions are executed. Contains the user, the
 * client and the allowed organisations.
 * 
 * It uses the same computation logic as in the Utility.getContext methods to
 * compute the accessible organisations, user organisations and user client.
 * 
 * The OBContext instance is made available to other threads through the static
 * ThreadLocal and the getInstance methods.
 * 
 * Note the getInstance/setInstance and ThreadLocal pattern should be reviewed
 * when using a factory/dependency injection approach.
 * 
 * @author mtaal
 */

public class OBContext {
  private static final Logger log = Logger.getLogger(OBContext.class);
  
  private static final String AD_USERID = "#AD_USER_ID";
  // TODO: maybe use authenticated user
  // private static final String AUTHENTICATED_USER = "#AUTHENTICATED_USER";
  
  private static ThreadLocal<OBContext> instance = new ThreadLocal<OBContext>();
  
  private static String CONTEXT_PARAM = "#OBContext";
  
  // sets the obcontext using the request object, this means that
  // also test cases require the servlet-api.jar
  public static void setOBContext(HttpServletRequest request) {
    
    OBContext context = null;
    if (request.getSession() != null) {
      context = (OBContext) request.getSession().getAttribute(CONTEXT_PARAM);
    }
    
    if (context == null) {
      context = new OBContext();
      request.getSession().setAttribute(CONTEXT_PARAM, context);
    }
    context.setFromRequest(request);
    setOBContext(context);
  }
  
  public static void setOBContext(String userId) {
    final OBContext context = new OBContext();
    context.initialize(userId);
    setOBContext(context);
  }
  
  public static void setOBContext(OBContext obContext) {
    // if (obContext != null && instance.get() != null)
    // throw new ArgumentException("OBContext already set");
    instance.set(obContext);
  }
  
  public static OBContext getOBContext() {
    return instance.get();
  }
  
  private Client currentClient;
  private Org currentOrganisation;
  private Role role;
  private User user;
  private Language language;
  private List<Org> organisationList;
  private String[] readableOrganisations;
  private String[] readableClients;
  private Set<String> writableOrganisations;
  private String userLevel;
  private OrganisationStructureProvider organisationStructureProvider;
  private EntityAccessChecker entityAccessChecker;
  
  // disables security checks
  private boolean inAdministratorMode;
  
  public String getUserLevel() {
    return userLevel;
  }
  
  public void setUserLevel(String userLevel) {
    this.userLevel = userLevel.trim();
  }
  
  public boolean isAdministrator() {
    return getUser().getId().equals("0");
  }
  
  public void setReadableClients(Role role) {
    if (getUserLevel().equals("S")) {
      readableClients = new String[] { "0" };
    } else if (role.getClient().getId().equals("0")) {
      readableClients = new String[] { "0" };
    } else {
      readableClients = new String[] { role.getClient().getId(), "0" };
    }
  }
  
  // writable organisation is determined as follows
  // 1) if the user has level S or C then they can only write in organisation 0
  // 2) in other cases read the organisations from the role
  // only: if user has userlevel O then he/she can not read organisation 0
  // Utility.getContext and LoginUtils for current working
  private void setWritableOrganisations(Role role) {
    writableOrganisations = new HashSet<String>();
    final String localUserLevel = getUserLevel();
    if (localUserLevel.contains("S") || localUserLevel.equals("C")) {
      writableOrganisations.add("0"); // force org *
      return;
    }
    
    final List<Org> os = getOrganisationList(role);
    for (Org o : os) {
      writableOrganisations.add(o.getId());
    }
    
    if (localUserLevel.equals("O")) { // remove *
      writableOrganisations.remove("0");
    }
  }
  
  @SuppressWarnings("unchecked")
  private List<Org> getOrganisationList(Role role) {
    if (organisationList != null) {
      return organisationList;
    }
    final Query qry = SessionHandler.getInstance().createQuery("select o from " + Org.class.getName() + " o, " + RoleOrgAccess.class.getName() + " roa where o.id=roa.org.id and roa.role.id='" + role.getId() + "' and roa.active='Y' and o.active='Y'");
    organisationList = qry.list();
    return organisationList;
  }
  
  private void setReadableOrganisations(Role role) {
    final List<Org> os = getOrganisationList(role);
    final Set<String> readableOrgs = new HashSet<String>();
    for (Org o : os) {
      readableOrgs.addAll(getOrganisationStructureProvider().getNaturalTree(o.getId()));
    }
    readableOrgs.add("0");
    readableOrganisations = new String[readableOrgs.size()];
    int i = 0;
    for (String s : readableOrgs) {
      readableOrganisations[i++] = s;
    }
  }
  
  public Client getCurrentClient() {
    return currentClient;
  }
  
  public void setCurrentClient(Client currentClient) {
    this.currentClient = currentClient;
  }
  
  public void setCurrentOrganisation(Org currentOrganisation) {
    this.currentOrganisation = currentOrganisation;
  }
  
  public Language getLanguage() {
    return language;
  }
  
  public void setLanguage(Language language) {
    this.language = language;
  }
  
  public Org getCurrentOrganisation() {
    return currentOrganisation;
  }
  
  public void setFromRequest(HttpServletRequest request) {
    final String userId = request.getParameter(AD_USERID);
    if (userId == null) {
      return; // not yet set
    }
    initialize(userId);
    
    // final HashMap<String, String> values = new HashMap<String, String>();
    // final HttpSession httpSession = request.getSession();
    // for (Enumeration<String> e = httpSession.getAttributeNames();
    // e.hasMoreElements();) {
    // final String key = e.nextElement();
    // final Object value = httpSession.getAttribute(key);
    // if (key.startsWith("#") && value instanceof String) {
    // values.put(key.toUpperCase(), (String) value);
    // }
    // }
    // set(values);
  }
  
  // sets the context by reading all user information
  public void initialize(String userId) {
    final User u = SessionHandler.getInstance().find(User.class, userId);
    setUser(u);
    Hibernate.initialize(getUser().getClient());
    Hibernate.initialize(getUser().getOrg());
    Hibernate.initialize(getUser().getDefaultAdOrg());
    Hibernate.initialize(getUser().getDefaultAdClient());
    Hibernate.initialize(getUser().getDefaultAdRole());
    Hibernate.initialize(getUser().getDefaultAdLanguage());
    
    organisationStructureProvider = new OrganisationStructureProvider();
    
    // now check if the default role is active, if not another one needs to be
    // selected.
    if (getUser().getDefaultAdRole() != null && getUser().getDefaultAdRole().isActive()) {
      setRole(getUser().getDefaultAdRole());
    } else {
      final UserRoles ur = getOne(UserRoles.class, "select ur from " + UserRoles.class.getName() + " ur where " + " ur.user.id='" + u.getId() + "' and ur.active='Y' and ur.role.active='Y' order by ur.role.id asc");
      Hibernate.initialize(ur.getRole());
      setRole(ur.getRole());
    }
    
    Check.isNotNull(getRole(), "Role may not be null");
    setUserLevel(getRole().getUserlevel());
    
    if (getUser().getDefaultAdOrg() != null && getUser().getDefaultAdOrg().isActive()) {
      setCurrentOrganisation(getUser().getDefaultAdOrg());
    } else {
      final RoleOrgAccess roa = getOne(RoleOrgAccess.class, "select roa from " + RoleOrgAccess.class.getName() + " roa where roa.role.id='" + getRole().getId() + "' and roa.active='Y' and roa.org.active='Y' order by roa.org.id desc");
      Hibernate.initialize(roa.getOrg());
      setCurrentOrganisation(roa.getOrg());
    }
    
    Check.isNotNull(getCurrentOrganisation(), "Organisation may not be null");
    
    // check that the current organisation is actually writable!
    Set<String> writableOrgs = getWritableOrganisations();
    if (!writableOrgs.contains(getCurrentOrganisation().getId())) {
      log.warn("The user " + userId + " does not have write acces to its current organisation repairing that");
      // take the first writableOrganisation
      if (writableOrgs.isEmpty()) {
        log.warn("The user " + userId + " does not have any write acces to any organisation");
      } else {
        setCurrentOrganisation(SessionHandler.getInstance().find(Org.class, writableOrgs.iterator().next()));
      }
    }
    
    if (getUser().getDefaultAdClient() != null && getUser().getDefaultAdClient().isActive()) {
      setCurrentClient(getUser().getDefaultAdClient());
    } else {
      // TODO: should not just the client of the organisation be used?
      // The HttpSecureAppServlet reads the client after the organisation which
      // theoretically can
      // result in a current organisation which does not belong to the client
      Hibernate.initialize(getCurrentOrganisation().getClient());
      setCurrentClient(getCurrentOrganisation().getClient());
    }
    
    Check.isNotNull(getCurrentClient(), "Client may not be null");
    Check.isTrue(getCurrentClient().isActive(), "Current Client " + getCurrentClient().getName() + " is not active!");
    getOrganisationStructureProvider().setClientId(getCurrentClient().getId());
    
    if (getUser().getDefaultAdLanguage() != null && getUser().getDefaultAdLanguage().isActive()) {
      setLanguage(getUser().getDefaultAdLanguage());
    } else {
      final Language l = getOne(Language.class, "select l from " + Language.class.getName() + " l where l.active='Y' and l.baselanguage='Y' order by l.id asc");
      Hibernate.initialize(l);
      setLanguage(l);
    }
    
    Check.isNotNull(getLanguage(), "Language may not be null");
    
    setReadableClients(role);
    
    // initialize some proxys
    Hibernate.initialize(getCurrentOrganisation().getClient());
    Hibernate.initialize(getCurrentClient().getOrg());
    Hibernate.initialize(getRole().getClient());
    Hibernate.initialize(getRole().getOrg());
    Hibernate.initialize(getLanguage().getClient());
    Hibernate.initialize(getLanguage().getOrg());
    
    // TODO: add logging of all context information
  }
  
  // Computes the list of organisations for which the user may write data
  
  @SuppressWarnings("unchecked")
  private <T extends Object> T getOne(Class<T> clz, String qryStr) {
    final Query qry = SessionHandler.getInstance().createQuery(qryStr);
    qry.setMaxResults(1);
    final List<?> result = qry.list();
    Check.isTrue(result.size() == 1, "The query '" + qryStr + "' returned " + result.size() + " results while only 1 result was expected");
    return (T) result.get(0);
  }
  
  public User getUser() {
    return user;
  }
  
  public void setUser(User user) {
    this.user = user;
  }
  
  public Role getRole() {
    return role;
  }
  
  public void setRole(Role role) {
    this.role = role;
  }
  
  public OrganisationStructureProvider getOrganisationStructureProvider() {
    return organisationStructureProvider;
  }
  
  public String[] getReadableOrganisations() {
    if (readableOrganisations == null) {
      setReadableOrganisations(getRole());
    }
    return readableOrganisations;
  }
  
  public Set<String> getWritableOrganisations() {
    if (writableOrganisations == null) {
      setWritableOrganisations(getRole());
    }
    return writableOrganisations;
  }
  
  public String[] getReadableClients() {
    return readableClients;
  }
  
  public EntityAccessChecker getEntityAccessChecker() {
    if (entityAccessChecker == null) {
      entityAccessChecker = new EntityAccessChecker();
      entityAccessChecker.setRoleId(getRole().getId());
      entityAccessChecker.initialize();
    }
    return entityAccessChecker;
  }
  
  public boolean isInAdministratorMode() {
    return inAdministratorMode;
  }
  
  public void setInAdministratorMode(boolean inAdministratorMode) {
    this.inAdministratorMode = inAdministratorMode;
  }
}