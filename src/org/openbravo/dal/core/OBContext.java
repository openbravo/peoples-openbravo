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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.Check;
import org.openbravo.dal.security.EntityAccessChecker;
import org.openbravo.dal.security.OrganisationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.common.enterprise.Organization;

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

public class OBContext implements OBNotSingleton {
    private static final Logger log = Logger.getLogger(OBContext.class);

    // private static final String AD_USERID = "#AD_USER_ID";
    // TODO: maybe use authenticated user
    private static final String AUTHENTICATED_USER = "#AUTHENTICATED_USER";
    private static final String ROLE = "#AD_Role_ID";
    private static final String CLIENT = "#AD_Client_ID";
    private static final String ORG = "#AD_Org_ID";

    private static ThreadLocal<OBContext> instance = new ThreadLocal<OBContext>();

    private static String CONTEXT_PARAM = "#OBContext";

    // sets the obcontext using the request object, this means that
    // also test cases require the servlet-api.jar
    public static synchronized void setOBContext(HttpServletRequest request) {

	OBContext context = null;
	if (request.getSession() != null) {
	    context = (OBContext) request.getSession().getAttribute(
		    CONTEXT_PARAM);
	}

	if (context == null) {
	    context = new OBContext();
	    if (context.setFromRequest(request)) {
		request.getSession().setAttribute(CONTEXT_PARAM, context);
		setOBContext(context);
	    }
	} else {
	    if (!context.isInSync(request)) {
		context.setFromRequest(request);
	    }
	    setOBContext(context);
	}
    }

    public static void setOBContext(String userId) {
	final OBContext context = OBProvider.getInstance().get(OBContext.class);
	setOBContext((OBContext) null);
	context.initialize(userId);
	setOBContext(context);
    }

    public static OBContext createOBContext(String userId) {
	final OBContext context = new OBContext();
	setOBContext((OBContext) null);
	context.initialize(userId);
	return context;
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
    private Organization currentOrganisation;
    private Role role;
    private User user;
    private Language language;
    private List<Organization> organisationList;
    private String[] readableOrganisations;
    private String[] readableClients;
    private Set<String> writableOrganisations;
    private String userLevel;
    private Map<String, OrganisationStructureProvider> organisationStructureProviderByClient;
    private EntityAccessChecker entityAccessChecker;

    // disables security checks
    private boolean inAdministratorMode;
    private boolean prevAdminMode;
    // the "0" user is the administrator
    private boolean isAdministrator;
    private boolean isInitialized = false;

    private Set<String> additionalWritableOrganisations = new HashSet<String>();

    public String getUserLevel() {
	return userLevel;
    }

    public void setUserLevel(String userLevel) {
	this.userLevel = userLevel.trim();
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
    // 1) if the user has level S or C then they can only write in organisation
    // 0
    // 2) in other cases read the organisations from the role
    // only: if user has userlevel O then he/she can not read organisation 0
    // Utility.getContext and LoginUtils for current working
    private void setWritableOrganisations(Role role) {
	writableOrganisations = new HashSet<String>();
	final String localUserLevel = getUserLevel();
	if (localUserLevel.contains("S") || localUserLevel.equals("C")) {
	    writableOrganisations.add("0"); // force org *
	}

	final List<Organization> os = getOrganisationList(role);
	for (final Organization o : os) {
	    writableOrganisations.add(o.getId());
	}

	if (localUserLevel.equals("O")) { // remove *
	    writableOrganisations.remove("0");
	}
	writableOrganisations.addAll(additionalWritableOrganisations);
    }

    @SuppressWarnings("unchecked")
    private List<Organization> getOrganisationList(Role thisRole) {
	if (organisationList != null) {
	    return organisationList;
	}
	final Query qry = SessionHandler.getInstance().createQuery(
		"select o from " + Organization.class.getName() + " o, "
			+ RoleOrganization.class.getName() + " roa where o."
			+ Organization.PROPERTY_ID + "=roa."
			+ RoleOrganization.PROPERTY_ORGANIZATION + "."
			+ Organization.PROPERTY_ID + " and roa."
			+ RoleOrganization.PROPERTY_ROLE + "."
			+ Organization.PROPERTY_ID + "='" + thisRole.getId()
			+ "' and roa." + RoleOrganization.PROPERTY_ISACTIVE
			+ "='Y' and o." + Organization.PROPERTY_ISACTIVE
			+ "='Y'");
	organisationList = qry.list();
	for (final String orgId : additionalWritableOrganisations) {
	    final Organization org = OBDal.getInstance().get(
		    Organization.class, orgId);
	    if (!organisationList.contains(org)) {
		organisationList.add(org);
	    }
	}
	return organisationList;
    }

    @SuppressWarnings("unchecked")
    private List<Organization> getOrganisations(Client client) {
	final Query qry = SessionHandler.getInstance().createQuery(
		"select o from " + Organization.class.getName() + " o where "
			+ "o." + Organization.PROPERTY_CLIENT + "=? and o."
			+ Organization.PROPERTY_ISACTIVE + "='Y'");
	qry.setParameter(0, client);
	organisationList = qry.list();
	return organisationList;
    }

    private void setReadableOrganisations(Role role) {
	final List<Organization> os = getOrganisationList(role);
	final Set<String> readableOrgs = new HashSet<String>();
	for (final Organization o : os) {
	    readableOrgs.addAll(getOrganisationStructureProvider()
		    .getNaturalTree(o.getId()));
	    // if zero is an organisation then add them all!
	    if (o.getId().equals("0")) {
		for (final Organization org : getOrganisations(getCurrentClient())) {
		    readableOrgs.add(org.getId());
		}
	    }
	}
	readableOrgs.add("0");
	readableOrganisations = new String[readableOrgs.size()];
	int i = 0;
	for (final String s : readableOrgs) {
	    readableOrganisations[i++] = s;
	}
    }

    public Client getCurrentClient() {
	return currentClient;
    }

    public void setCurrentClient(Client currentClient) {
	this.currentClient = currentClient;
    }

    public void setCurrentOrganisation(Organization currentOrganisation) {
	this.currentOrganisation = currentOrganisation;
    }

    public Language getLanguage() {
	return language;
    }

    public void setLanguage(Language language) {
	this.language = language;
    }

    public Organization getCurrentOrganisation() {
	return currentOrganisation;
    }

    public void removeWritableOrganisation(String orgId) {
	additionalWritableOrganisations.remove(orgId);
    }

    public void addWritableOrganisation(String orgId) {
	additionalWritableOrganisations.add(orgId);
	// nullify will be recomputed at first occasion
	organisationList = null;
	readableOrganisations = null;
	writableOrganisations = null;
    }

    public boolean setFromRequest(HttpServletRequest request) {
	String userId = null;
	for (final Enumeration<?> e = request.getSession().getAttributeNames(); e
		.hasMoreElements();) {
	    final String name = (String) e.nextElement();
	    if (name.equalsIgnoreCase(AUTHENTICATED_USER)) {
		userId = (String) request.getSession().getAttribute(name);
		break;
	    }
	}
	if (userId == null) {
	    return false; // not yet set
	}
	return initialize(userId, getSessionValue(request, ROLE),
		getSessionValue(request, CLIENT), getSessionValue(request, ORG));
    }

    // sets the context by reading all user information
    public boolean initialize(String userId) {
	return initialize(userId, null, null, null);
    }

    // sets the context by reading all user information
    public boolean initialize(String userId, String roleId, String clientId,
	    String orgId) {
	final User u = SessionHandler.getInstance().find(User.class, userId);
	if (u == null) {
	    return false;
	}
	setInitialized(false);
	setInAdministratorMode(true);
	try {
	    setUser(u);
	    Hibernate.initialize(getUser().getClient());
	    Hibernate.initialize(getUser().getOrganization());
	    Hibernate.initialize(getUser().getDefaultAdOrg());
	    Hibernate.initialize(getUser().getDefaultAdClient());
	    Hibernate.initialize(getUser().getDefaultAdRole());
	    Hibernate.initialize(getUser().getDefaultAdLanguage());

	    organisationStructureProviderByClient = new HashMap<String, OrganisationStructureProvider>();

	    // first take the passed role, if any
	    // now check if the default role is active, if not another one needs
	    // to be
	    // selected.
	    if (roleId != null) {
		final Role r = getOne(Role.class, "select r from "
			+ Role.class.getName() + " r where " + " r."
			+ Role.PROPERTY_ID + "='" + roleId + "'");
		setRole(r);
	    } else if (getUser().getDefaultAdRole() != null
		    && getUser().getDefaultAdRole().isActive()) {
		setRole(getUser().getDefaultAdRole());
	    } else {

		final UserRoles ur = getOne(UserRoles.class, "select ur from "
			+ UserRoles.class.getName() + " ur where " + " ur."
			+ UserRoles.PROPERTY_USER + "." + User.PROPERTY_ID
			+ "='" + u.getId() + "' and ur."
			+ UserRoles.PROPERTY_ISACTIVE + "='Y' and ur."
			+ UserRoles.PROPERTY_ROLE + "."
			+ Role.PROPERTY_ISACTIVE + "='Y' order by ur."
			+ UserRoles.PROPERTY_ROLE + "." + Role.PROPERTY_ID
			+ " asc", false);
		Check.isNotNull(ur,
			"There are no valid and active user roles for user with id: "
				+ u.getId());
		Hibernate.initialize(ur.getRole());
		setRole(ur.getRole());
	    }

	    Check.isNotNull(getRole(), "Role may not be null");
	    setUserLevel(getRole().getUserLevel());

	    if (orgId != null) {
		final Organization o = getOne(Organization.class,
			"select r from " + Organization.class.getName()
				+ " r where " + " r."
				+ Organization.PROPERTY_ID + "='" + orgId + "'");
		setCurrentOrganisation(o);
	    } else if (getUser().getDefaultAdOrg() != null
		    && getUser().getDefaultAdOrg().isActive()) {
		setCurrentOrganisation(getUser().getDefaultAdOrg());
	    } else {
		final RoleOrganization roa = getOne(RoleOrganization.class,
			"select roa from " + RoleOrganization.class.getName()
				+ " roa where roa."
				+ RoleOrganization.PROPERTY_ORGANIZATION + "."
				+ Organization.PROPERTY_ID + "='"
				+ getRole().getId() + "' and roa."
				+ RoleOrganization.PROPERTY_ISACTIVE
				+ "='Y' and roa."
				+ RoleOrganization.PROPERTY_ORGANIZATION + "."
				+ Organization.PROPERTY_ISACTIVE
				+ "='Y' order by roa."
				+ RoleOrganization.PROPERTY_ORGANIZATION + "."
				+ Organization.PROPERTY_ID + " desc");
		Hibernate.initialize(roa.getOrganization());
		setCurrentOrganisation(roa.getOrganization());

		// if no client id then use the client of the role
		if (clientId == null) {
		    clientId = roa.getClient().getId();
		}
	    }

	    Check.isNotNull(getCurrentOrganisation(),
		    "Organisation may not be null");

	    // check that the current organisation is actually writable!
	    final Set<String> writableOrgs = getWritableOrganisations();
	    if (!writableOrgs.contains(getCurrentOrganisation().getId())) {
		log
			.warn("The user "
				+ userId
				+ " does not have write acces to its current organisation repairing that");
		// take the first writableOrganisation
		if (writableOrgs.isEmpty()) {
		    log
			    .warn("The user "
				    + userId
				    + " does not have any write acces to any organisation");
		} else {
		    setCurrentOrganisation(SessionHandler.getInstance().find(
			    Organization.class, writableOrgs.iterator().next()));
		}
	    }

	    if (clientId != null) {
		final Client c = getOne(Client.class, "select r from "
			+ Client.class.getName() + " r where " + " r."
			+ Client.PROPERTY_ID + "='" + clientId + "'");
		setCurrentClient(c);
	    } else if (getUser().getDefaultAdClient() != null
		    && getUser().getDefaultAdClient().isActive()) {
		setCurrentClient(getUser().getDefaultAdClient());
	    } else {
		// The HttpSecureAppServlet reads the client after the
		// organisation
		// which
		// theoretically can
		// result in a current organisation which does not belong to the
		// client
		// other comment, use the client of the organisation
		Hibernate.initialize(getCurrentOrganisation().getClient());
		setCurrentClient(getCurrentOrganisation().getClient());
	    }

	    Check.isNotNull(getCurrentClient(), "Client may not be null");
	    Check.isTrue(getCurrentClient().isActive(), "Current Client "
		    + getCurrentClient().getName() + " is not active!");

	    if (getUser().getDefaultAdLanguage() != null
		    && getUser().getDefaultAdLanguage().isActive()) {
		setLanguage(getUser().getDefaultAdLanguage());
	    } else {
		final Language l = getOne(Language.class, "select l from "
			+ Language.class.getName() + " l where l."
			+ Language.PROPERTY_ISACTIVE + "='Y' order by l."
			+ Language.PROPERTY_ID + " asc");
		Hibernate.initialize(l);
		setLanguage(l);
	    }

	    Check.isNotNull(getLanguage(), "Language may not be null");

	    setReadableClients(role);

	    // initialize some proxys
	    Hibernate.initialize(getCurrentOrganisation().getClient());
	    Hibernate.initialize(getCurrentClient().getOrganization());
	    Hibernate.initialize(getRole().getClient());
	    Hibernate.initialize(getRole().getOrganization());
	    Hibernate.initialize(getLanguage().getClient());
	    Hibernate.initialize(getLanguage().getOrganization());

	    // TODO: add logging of all context information
	} finally {
	    setInAdministratorMode(false);
	    setInitialized(true);
	}
	return true;
    }

    private <T extends Object> T getOne(Class<T> clz, String qryStr) {
	return getOne(clz, qryStr, true);
    }

    @SuppressWarnings("unchecked")
    private <T extends Object> T getOne(Class<T> clz, String qryStr,
	    boolean doCheck) {
	final Query qry = SessionHandler.getInstance().createQuery(qryStr);
	qry.setMaxResults(1);
	final List<?> result = qry.list();
	if (doCheck) {
	    Check.isTrue(result.size() == 1, "The query '" + qryStr
		    + "' returned " + result.size()
		    + " results while only 1 result was expected");
	}
	if (result.size() == 0) {
	    return null;
	}
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
	isAdministrator = ((String) DalUtil.getId(role)).equals("0");
	this.role = role;
    }

    public OrganisationStructureProvider getOrganisationStructureProvider() {
	return getOrganisationStructureProvider(getCurrentClient().getId());
    }

    public OrganisationStructureProvider getOrganisationStructureProvider(
	    String clientId) {
	OrganisationStructureProvider orgProvider = organisationStructureProviderByClient
		.get(clientId);

	// create one
	if (orgProvider == null) {
	    orgProvider = OBProvider.getInstance().get(
		    OrganisationStructureProvider.class);
	    orgProvider.setClientId(clientId);
	    organisationStructureProviderByClient.put(clientId, orgProvider);
	}
	return orgProvider;
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
	    entityAccessChecker = OBProvider.getInstance().get(
		    EntityAccessChecker.class);
	    // use the DalUtil.getId because it does not resolve hibernate
	    // proxies
	    entityAccessChecker.setRoleId((String) DalUtil.getId(getRole()));
	    entityAccessChecker.setObContext(this);
	    entityAccessChecker.initialize();
	}
	return entityAccessChecker;
    }

    public boolean isInAdministratorMode() {
	return inAdministratorMode || isAdministrator;
    }

    public void setInAdministratorMode(boolean inAdministratorMode) {
	if (this.inAdministratorMode != inAdministratorMode) {
	    prevAdminMode = isInAdministratorMode();
	}
	this.inAdministratorMode = inAdministratorMode;
    }

    public void restorePreviousAdminMode() {
	setInAdministratorMode(prevAdminMode);
    }

    public boolean isInitialized() {
	return isInitialized;
    }

    public void setInitialized(boolean isInitialized) {
	this.isInitialized = isInitialized;
    }

    private boolean isInSync(HttpServletRequest request) {
	if (unequal(request, AUTHENTICATED_USER, getUser())) {
	    return false;
	}
	if (unequal(request, ROLE, getRole())) {
	    return false;
	}
	if (unequal(request, CLIENT, getCurrentClient())) {
	    return false;
	}
	if (unequal(request, ORG, getCurrentOrganisation())) {
	    return false;
	}
	return true;
    }

    private boolean unequal(HttpServletRequest request, String param,
	    BaseOBObject bob) {
	if (bob == null) {
	    return true;
	}
	final String sessionValue = getSessionValue(request, param);
	if (sessionValue == null) {
	    return false;
	}
	return !bob.getId().equals(sessionValue);
    }

    private String getSessionValue(HttpServletRequest request, String param) {
	return (String) request.getSession().getAttribute(param.toUpperCase());
    }

}