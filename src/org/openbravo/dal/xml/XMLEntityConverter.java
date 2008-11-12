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

package org.openbravo.dal.xml;

import static org.openbravo.model.ad.system.Client.PROPERTY_ORGANIZATION;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_CLIENT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.base.util.Check;
import org.openbravo.dal.security.SecurityChecker;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Is responsible for converting an xml string to a tree of business objects.
 * 
 * @author mtaal
 */

public class XMLEntityConverter implements OBNotSingleton {
    // This class should translate the

    private static final Logger log = Logger
	    .getLogger(EntityXMLConverter.class);

    public static XMLEntityConverter newInstance() {
	return OBProvider.getInstance().get(XMLEntityConverter.class);
    }

    private EntityResolver entityResolver = EntityResolver.getInstance();

    // keeps track which instances are part of the xml because they were
    // referenced
    private Set<BaseOBObject> referenced = new HashSet<BaseOBObject>();

    // keeps track which instances changed during the import and need to
    // be updated
    private List<BaseOBObject> toUpdate = new ArrayList<BaseOBObject>();
    private Set<BaseOBObject> checkUpdate = new HashSet<BaseOBObject>();

    // keeps track which instances need to be inserted
    private List<BaseOBObject> toInsert = new ArrayList<BaseOBObject>();
    private Set<BaseOBObject> checkInsert = new HashSet<BaseOBObject>();

    // the client and organization for which the import is done
    private Client client;
    private Organization organization;

    // some error and log messages
    private StringBuilder errorMessages = new StringBuilder();
    private StringBuilder logMessages = new StringBuilder();
    private StringBuilder warningMessages = new StringBuilder();

    // process stops at 20 errors
    private int noOfErrors = 0;

    protected void clear() {
	toUpdate.clear();
	checkUpdate.clear();
	toInsert.clear();
	checkInsert.clear();
	errorMessages = new StringBuilder();
	logMessages = new StringBuilder();
	noOfErrors = 0;
	referenced.clear();
	entityResolver.clear();
    }

    // is the main procedure, parses the root and walks
    // through the entity elements
    public List<BaseOBObject> process(String xml) {
	try {
	    final Document doc = DocumentHelper.parseText(xml);
	    return process(doc);
	} catch (Exception e) {
	    throw new EntityXMLException(e);
	}
    }

    public List<BaseOBObject> process(Document doc) {
	clear();

	entityResolver.setClient(getClient());
	entityResolver.setOrganization(getOrganization());

	// check that the rootelement is the openbravo one
	final Element rootElement = doc.getRootElement();
	Check.isSameObject(rootElement.getName(), XMLConstants.OB_ROOT_ELEMENT);

	// walk through the elements
	final Set<BaseOBObject> checkDuplicates = new HashSet<BaseOBObject>();
	final List<BaseOBObject> result = new ArrayList<BaseOBObject>();
	for (Object o : rootElement.elements()) {
	    final Element element = (Element) o;
	    final BaseOBObject bob = processEntityElement(element.getName(),
		    element, false);
	    // only add it if okay
	    if (bob != null && !checkDuplicates.contains(bob)) {
		result.add(bob);
		checkDuplicates.add(bob);
	    }
	}
	return result;
    }

    // processes a xml tag which denotes an instance of a business object
    @SuppressWarnings("unchecked")
    private BaseOBObject processEntityElement(String entityName,
	    Element obElement, boolean theReferenced) {
	// note: referenced is true for both childs and many-to-one references
	// it is passed to the entityresolver to allow searches in other
	// organisation

	// note id maybe null for new objects
	final String id = obElement.attributeValue(XMLConstants.ID_ATTRIBUTE);
	if (entityName == null) {
	    error("Element " + obElement.getName()
		    + " has no entityname attribute, not processing it");
	    return null;
	}
	try {
	    log.debug("Converting entity " + entityName);
	    final boolean hasReferenceAttribute = obElement
		    .attributeValue(XMLConstants.REFERENCE_ATTRIBUTE) != null;

	    // try to find the entity
	    final BaseOBObject bob = resolve(entityName, id, false);

	    // should never be null at this point!
	    Check.isNotNull(bob, "The business object " + entityName + " ("
		    + id + ") can not be resolved");

	    // warn/error is logged below if the entity is updated
	    // update is prevented below
	    final boolean writable = SecurityChecker.getInstance().isWritable(
		    bob);

	    // do some checks to determine if this one should be updated
	    // a referenced instance should not be updated if it is not new
	    // note that embedded children are updated but non-embedded children
	    // are not updated!
	    final boolean preventRealUpdate = !writable
		    || (hasReferenceAttribute && !bob.isNewOBObject());

	    final Entity entity = ModelProvider.getInstance().getEntity(
		    obElement.getName());
	    boolean updated = false;

	    // now parse the property elements
	    for (Element childElement : (List<Element>) obElement.elements()) {
		final Property p = entity.getProperty(childElement.getName());
		log.debug(">>> Exporting property " + p.getName());

		// TODO: make this option controlled
		if (p.isTransient(bob) || p.isAuditInfo() || p.isInactive()) {
		    log
			    .debug("Property "
				    + p
				    + " is inactive, transient or auditinfo, ignoring it");
		    continue;
		}

		// ignore these as they are already set, or should not be set
		if (p.isId()) {
		    continue;
		}

		final Object currentValue = bob.get(p.getName());

		// do the primitive values
		if (p.isPrimitive()) {
		    final Object newValue = XMLTypeConverter.getInstance()
			    .fromXML(p.getPrimitiveType(),
				    childElement.getText());
		    log.debug("Primitive property with value " + newValue);
		    // only update if changed
		    if ((currentValue == null && newValue != null)
			    || (currentValue != null && newValue != null && !currentValue
				    .equals(newValue))) {
			log.debug("Value changed setting it");
			if (!preventRealUpdate) {
			    bob.set(p.getName(), newValue);
			}
			updated = true;
		    }
		} else if (!p.isOneToMany()) {
		    // never update the org or client through xml!
		    boolean clientUpdate = bob instanceof ClientEnabled
			    && p.getName().equals(PROPERTY_CLIENT);
		    boolean orgUpdate = bob instanceof OrganizationEnabled
			    && p.getName().equals(PROPERTY_ORGANIZATION);
		    if (currentValue != null && (clientUpdate || orgUpdate)) {
			continue;
		    }

		    // determine the referenced entity
		    final Object newValue;

		    // handle null value
		    if (childElement.attribute(XMLConstants.ID_ATTRIBUTE) == null) {
			newValue = null;
		    } else {
			// get the info and resolve the reference
			final String refId = childElement
				.attributeValue(XMLConstants.ID_ATTRIBUTE);
			final String refEntityName = p.getTargetEntity()
				.getName();
			newValue = resolve(refEntityName, refId, true);
		    }

		    // only update if changed
		    if ((currentValue == null && newValue != null)
			    || (currentValue != null && newValue != null && !currentValue
				    .equals(newValue))) {
			log.debug("Setting value " + newValue);
			if (!preventRealUpdate) {
			    bob.set(p.getName(), newValue);
			}
			updated = true;
		    }

		} else {
		    // resolve the content of the list
		    final List<BaseOBObject> newValues = new ArrayList<BaseOBObject>();
		    for (Object o : childElement.elements()) {
			final Element listElement = (Element) o;
			newValues.add(processEntityElement(listElement
				.getName(), listElement, true));
		    }
		    // get the currentvalue and compare
		    final List<BaseOBObject> currentValues = (List<BaseOBObject>) currentValue;

		    if (!newValues.equals(currentValues)) {
			if (!preventRealUpdate) {
			    // TODO: is this efficient? Or will it even work
			    // with
			    // hibernate first removing all?
			    currentValues.clear();
			    currentValues.addAll(newValues);
			}
			updated = true;
		    }
		}
	    }
	    final String originalId = getEntityResolver().getOriginalId(bob);
	    String originalIdStr = "";
	    if (originalId != null) {
		originalIdStr = ", with import id: " + originalId;
	    }

	    if (!writable && updated) {
		if (bob.isNewOBObject()) {
		    warn("Not allowed to create entity: " + bob.getEntityName()
			    + " (import id: " + id + ") "
			    + " because it is not writable");
		    return bob;
		} else {
		    warn("Not updating entity: " + bob.getIdentifier()
			    + " because it is not writable");
		    return bob;
		}
	    } else if (updated && preventRealUpdate) {
		Check
			.isTrue(hasReferenceAttribute && !bob.isNewOBObject(),
				"This case may only occur for referenced objects which are not new");
		// if the object is referenced then it can not be updated
		warn("Entity "
			+ bob.getIdentifier()
			+ " has not been updated because it already exists and "
			+ "it is imported as a reference from another object");
	    } else if (bob.isNewOBObject()) {
		if (!checkInsert.contains(bob)) {
		    warnDifferentClientOrg(bob, "Creating");
		    log("Inserted entity " + bob.getIdentifier()
			    + originalIdStr);
		    toInsert.add(bob);
		    checkInsert.add(bob);
		}
	    } else if (updated && !checkUpdate.contains(bob)) {
		Check.isFalse(bob.isNewOBObject(),
			"May only be here for not-new objects");
		// never update an object which was exported as referenced
		Check.isFalse(hasReferenceAttribute,
			"Referenced objects may not be updated");

		// warn in case of different organisation/client
		warnDifferentClientOrg(bob, "Updating");

		log("Updated entity " + bob.getIdentifier() + originalIdStr);

		toUpdate.add(bob);
		checkUpdate.add(bob);
	    }
	    return bob;
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	    error("Exception when parsing entity " + entityName + " (" + id
		    + "):" + e.getMessage());
	    return null;
	}
    }

    protected void warnDifferentClientOrg(BaseOBObject bob, String prefix) {

	if (bob.getEntity().isClientEnabled()) {
	    final ClientEnabled ce = (ClientEnabled) bob;
	    if (!ce.getClient().getId().equals(getClient().getId())) {
		warn(prefix
			+ " entity "
			+ bob.getIdentifier()
			+ " eventhough it does not belong to the target client "
			+ getClient().getIdentifier() + " but to client "
			+ ce.getClient().getIdentifier());
	    }
	}
	if (bob.getEntity().isOrganisationEnabled()) {
	    final OrganizationEnabled oe = (OrganizationEnabled) bob;
	    if (!oe.getOrganization().getId().equals(getOrganization().getId())) {
		warn(prefix
			+ " entity "
			+ bob.getIdentifier()
			+ " eventhough it does not belong to the target organization "
			+ getOrganization().getIdentifier()
			+ " but to organization "
			+ oe.getOrganization().getIdentifier());
	    }
	}
    }

    protected void warn(String msg) {
	if (warningMessages.length() > 0) {
	    warningMessages.append("\n");
	}
	warningMessages.append(msg);
    }

    protected void log(String msg) {
	if (logMessages.length() > 0) {
	    logMessages.append("\n");
	}
	logMessages.append(msg);
    }

    protected void error(String msg) {
	if (errorMessages.length() > 0) {
	    errorMessages.append("\n");
	}
	errorMessages.append(msg);
	if (noOfErrors++ > 20) {
	    throw new EntityXMLException(
		    "Too many errors, exiting import, error messages:\n"
			    + errorMessages);
	}
    }

    protected BaseOBObject resolve(String entityName, String id,
	    boolean reference) {
	return entityResolver.resolve(entityName, id, reference);
    }

    public Client getClient() {
	return client;
    }

    public void setClient(Client client) {
	this.client = client;
    }

    public Organization getOrganization() {
	return organization;
    }

    public void setOrganization(Organization organization) {
	this.organization = organization;
    }

    public List<BaseOBObject> getToUpdate() {
	return toUpdate;
    }

    public List<BaseOBObject> getToInsert() {
	return toInsert;
    }

    public String getErrorMessages() {
	if (errorMessages.length() == 0) {
	    return null;
	}
	return errorMessages.toString();
    }

    public String getWarningMessages() {
	if (warningMessages.length() == 0) {
	    return null;
	}
	return warningMessages.toString();
    }

    public String getLogMessages() {
	if (logMessages.length() == 0) {
	    return null;
	}
	return logMessages.toString();
    }

    public EntityResolver getEntityResolver() {
	return entityResolver;
    }
}