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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
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
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.SecurityChecker;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Converts a XML string to an objectgraph with objects. During the XML parse
 * phase this converter will match XML tags with new or existing (in the
 * database) business objects. The matching logic is implemented in the
 * {@link EntityResolver}.
 * <p/>
 * The XMLEntityConverter keeps track of which objects are new, which exist but
 * do not need to be updated or which objects exist but need to be updated.
 * <p/>
 * This converter does not update the database directly. However, it changes the
 * properties of existing objects. This means that a commit after calling the
 * process method on the converter can result in database updates by Hibernate.
 * 
 * @see Entity
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

    private EntityResolver entityResolver;

    private EntityXMLProcessor importProcessor;

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

    // signals that this is an overall client data import
    // in this case the client/organization property is updated through the
    // xml, note that this assumes that the client/organization of the object
    // are present in xml! Also if this option is set then the client and
    // organization in this object are null
    private boolean optionClientImport = false;

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

    /**
     * The main entry point. This method creates a Dom4j Document and then calls
     * {@link #process(Document)}.
     * 
     * @param xml
     *            the xml string
     * @return the list of BaseOBObject present in the root of the xml. This
     *         list contains the to-be-updated, to-be-inserted as well as the
     *         unchanged business objects
     */
    public List<BaseOBObject> process(String xml) {
        try {
            final Document doc = DocumentHelper.parseText(xml);
            return process(doc);
        } catch (final Exception e) {
            throw new EntityXMLException(e);
        }
    }

    /**
     * The main entry point. This method walks through the elements in the root
     * and parses them. The children of a business object (in the xml) are also
     * parsed. Referenced objects are resolved through the
     * {@link EntityResolver}.
     * <p/>
     * After a call to this method the to-be-inserted objects can be retrieved
     * through the {@link #getToInsert()} method and the to-be-updated objects
     * through the {@link #getToUpdate()} method.
     * 
     * @param xml
     *            the xml string
     * @return the list of BaseOBObject present in the root of the xml. This
     *         list contains the to-be-updated, to-be-inserted as well as the
     *         unchanged business objects
     */
    public List<BaseOBObject> process(Document doc) {
        clear();
        getEntityResolver().setClient(getClient());
        getEntityResolver().setOrganization(getOrganization());

        // check that the rootelement is the openbravo one
        final Element rootElement = doc.getRootElement();
        Check.isSameObject(rootElement.getName(), XMLConstants.OB_ROOT_ELEMENT);

        // walk through the elements
        final Set<BaseOBObject> checkDuplicates = new HashSet<BaseOBObject>();
        final List<BaseOBObject> result = new ArrayList<BaseOBObject>();
        for (final Object o : rootElement.elements()) {
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
        // organization

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

            // resolve the entity, using the id, note that
            // resolve will create a new object if none is found
            BaseOBObject bob = resolve(entityName, id, false);

            // should never be null at this point
            Check.isNotNull(bob, "The business object " + entityName + " ("
                    + id + ") can not be resolved");

            // warn/error is logged below if the entity is updated
            // update is prevented below
            final boolean writable = OBContext.getOBContext()
                    .isInAdministratorMode()
                    || SecurityChecker.getInstance().isWritable(bob);

            // do some checks to determine if this one should be updated
            // a referenced instance should not be updated if it is not new
            // note that embedded children are updated but non-embedded children
            // are not updated!
            final boolean preventRealUpdate = !writable
                    || (hasReferenceAttribute && !bob.isNewOBObject());

            final Entity entity = ModelProvider.getInstance().getEntity(
                    obElement.getName());
            boolean updated = false;

            // the onetomany properties are done in a second pass
            final List<Element> oneToManyElements = new ArrayList<Element>();

            // now parse the property elements
            for (final Element childElement : (List<Element>) obElement
                    .elements()) {
                final Property p = entity.getProperty(childElement.getName());
                log.debug(">>> Exporting property " + p.getName());

                // TODO: make this option controlled
                final boolean isNotImportableProperty = p.isTransient(bob)
                        || p.isAuditInfo() || p.isInactive();
                if (isNotImportableProperty) {
                    log.debug("Property " + p
                            + " is inactive, transient or auditinfo, "
                            + "ignoring it");
                    continue;
                }

                // ignore the id properties as they are already set, or should
                // not be set
                if (p.isId()) {
                    continue;
                }

                if (p.isOneToMany()) {
                    oneToManyElements.add(childElement);
                    continue;
                }

                final Object currentValue = bob.get(p.getName());

                // do the primitive values
                if (p.isPrimitive()) {
                    Object newValue = XMLTypeConverter.getInstance().fromXML(
                            p.getPrimitiveType(), childElement.getText());
                    // correct the value
                    newValue = replaceValue(bob, p, newValue);

                    log.debug("Primitive property with value " + newValue);

                    // only update if changed
                    if ((currentValue == null && newValue != null)
                            || (currentValue != null && newValue != null && !currentValue
                                    .equals(newValue))) {
                        log.debug("Value changed setting it");
                        if (!preventRealUpdate) {
                            bob.set(p.getName(), newValue);
                            updated = true;
                        }
                    }
                } else {
                    Check.isTrue(!p.isOneToMany(),
                            "One to many property not allowed here");
                    // never update the org or client through xml!
                    final boolean clientUpdate = bob instanceof ClientEnabled
                            && p.getName().equals(PROPERTY_CLIENT);
                    final boolean orgUpdate = bob instanceof OrganizationEnabled
                            && p.getName().equals(PROPERTY_ORGANIZATION);
                    if (!isOptionClientImport() && currentValue != null
                            && (clientUpdate || orgUpdate)) {
                        continue;
                    }

                    // determine the referenced entity
                    Object newValue;

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
                    newValue = replaceValue(bob, p, newValue);

                    final boolean hasChanged = (currentValue == null && newValue != null)
                            || (currentValue != null && newValue != null && !currentValue
                                    .equals(newValue));
                    if (hasChanged) {
                        log.debug("Setting value " + newValue);
                        if (!preventRealUpdate) {
                            bob.set(p.getName(), newValue);
                            updated = true;
                        }
                    }

                }
            }

            // do the unique constraint matching here
            // if there is a matching object in the db then that one should be
            // used from now on this check can not be done earlier because
            // earlier no properties are set for a new object
            if (bob.isNewOBObject() && entity.getUniqueConstraints().size() > 0) {
                final BaseOBObject otherUniqueObject = entityResolver
                        .findUniqueConstrainedObject(bob);
                if (otherUniqueObject != null) {
                    // now copy the imported values from the bob to
                    // otherUniqueObject
                    for (final Property p : entity.getProperties()) {
                        if (p.isOneToMany()) {
                            // these are done below
                            continue;
                        }
                        final boolean isNotImportableProperty = p
                                .isTransient(bob)
                                || p.isAuditInfo()
                                || p.isInactive()
                                || p.isId();
                        if (isNotImportableProperty) {
                            continue;
                        }
                        // do not change the client or organization of an
                        // existing object
                        if (p.isClientOrOrganization()) {
                            continue;
                        }
                        otherUniqueObject
                                .set(p.getName(), bob.get(p.getName()));
                    }
                    // and replace the bob, because the object from the db
                    // should be used
                    bob = otherUniqueObject;
                }
            }

            // the onetomany properties are imported here because
            // they interfere with uniqueConstraint checking and using
            // another unique object. The entities imported as children nl.
            // can refer to the parent in which the unique object in the db
            // should already be resolved. This is done above.
            for (final Element element : oneToManyElements) {
                final Property p = entity.getProperty(element.getName());
                final Object currentValue = bob.get(p.getName());

                // resolve the content of the list
                final List<BaseOBObject> newValues = new ArrayList<BaseOBObject>();
                for (final Object o : element.elements()) {
                    final Element listElement = (Element) o;
                    newValues.add(processEntityElement(listElement.getName(),
                            listElement, true));
                }
                // get the currentvalue and compare
                final List<BaseOBObject> currentValues = (List<BaseOBObject>) currentValue;

                if (!newValues.equals(currentValues)) {
                    if (!preventRealUpdate) {
                        // TODO: is this efficient? Or will it even work
                        // with hibernate first removing all?
                        currentValues.clear();
                        currentValues.addAll(newValues);
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
            } else if (preventRealUpdate) {
                Check
                        .isTrue(!writable || hasReferenceAttribute
                                && !bob.isNewOBObject(),
                                "This case may only occur for referenced objects which are not new");
                // if the object is referenced then it can not be updated
                if (hasReferenceAttribute && !bob.isNewOBObject()) {
                    warn("Entity "
                            + bob
                            + " ("
                            + bob.getEntity().getTableName()
                            + ") "
                            + " has not been updated because it already exists and "
                            + "it is imported as a reference from another object");
                }
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

                // warn in case of different organization/client
                warnDifferentClientOrg(bob, "Updating");

                log("Updated entity " + bob.getIdentifier() + originalIdStr);

                toUpdate.add(bob);
                checkUpdate.add(bob);
            }

            // do a check that in case of a client/organization import that the
            // client and organization are indeed set
            if (isOptionClientImport()) {
                checkClientOrganizationSet(bob);
            }

            return bob;
        } catch (final Exception e) {
            e.printStackTrace(System.err);
            error("Exception when parsing entity " + entityName + " (" + id
                    + "):" + e.getMessage());
            return null;
        }
    }

    private Object replaceValue(BaseOBObject owner, Property property,
            Object newValue) {
        if (importProcessor == null) {
            return newValue;
        } else {
            return importProcessor.replaceValue(owner, property, newValue);
        }
    }

    protected void checkClientOrganizationSet(BaseOBObject bob) {
        if (bob.getEntity().isClientEnabled()) {
            final ClientEnabled ce = (ClientEnabled) bob;
            if (ce.getClient() == null) {
                error("The client of entity "
                        + bob.getIdentifier()
                        + " is not set. For a client data import the client needs"
                        + " to be set. Check that the xml was created "
                        + "with client/organization property export to true");
            }
        }
        if (bob.getEntity().isOrganizationEnabled()) {
            final OrganizationEnabled oe = (OrganizationEnabled) bob;
            if (oe.getOrganization() == null) {
                error("The organization of entity "
                        + bob.getIdentifier()
                        + " is not set. For a client data import the organization needs"
                        + " to be set. Check that the xml was created "
                        + "with client/organization property export to true");
            }
        }
    }

    protected void warnDifferentClientOrg(BaseOBObject bob, String prefix) {

        // don't need to check as the object retains his client/organization
        if (isOptionClientImport()) {
            return;
        }

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
        if (bob.getEntity().isOrganizationEnabled()) {
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

    /**
     * Returns the objects which exist in the database and will be updated.
     * 
     * @return list of objects which will be updated in the database.
     */
    public List<BaseOBObject> getToUpdate() {
        return toUpdate;
    }

    /**
     * Returns the list of objects which should be inserted in the database
     * 
     * @return the list of new BaseOBObjects which should be inserted in the
     *         database
     */
    public List<BaseOBObject> getToInsert() {
        return toInsert;
    }

    /**
     * The error messages logged during the import process. If no error message
     * exist then null is returned. If error messages exist then the user of
     * this class should not update the database and do a rollback.
     * 
     * @return the logged error messages, null if no error messages are present
     */
    public String getErrorMessages() {
        if (errorMessages.length() == 0) {
            return null;
        }
        return errorMessages.toString();
    }

    /**
     * The warning messages logged during the import process. Warning messages
     * are non-failing messages. The database can be updated if there are
     * warning messages. If no warning message exist then null is returned.
     * 
     * @return the logged warning messages, null if no warning messages are
     *         present
     */
    public String getWarningMessages() {
        if (warningMessages.length() == 0) {
            return null;
        }
        return warningMessages.toString();
    }

    /**
     * The standard log messages logged during the import process. If no log
     * message exist then null is returned.
     * 
     * @return the logged messages, null if no messages are present
     */
    public String getLogMessages() {
        if (logMessages.length() == 0) {
            return null;
        }
        return logMessages.toString();
    }

    /**
     * @return the EntityResolver used by this Converter.
     */
    public EntityResolver getEntityResolver() {

        if (entityResolver == null) {
            entityResolver = EntityResolver.getInstance();
        }
        return entityResolver;
    }

    /**
     * Determines if this a client import. A client import differs from a
     * standard import because it is assumed that all Client/Organization level
     * information is present in the xml and only System objects should be
     * retrieved from the database.
     * 
     * @return the value of the client import option (default is false)
     */
    public boolean isOptionClientImport() {
        return optionClientImport;
    }

    /**
     * Determines if this a client import. A client import differs from a
     * standard import because it is assumed that all Client/Organization level
     * information is present in the xml and only System objects should be
     * retrieved from the database.
     * 
     * @param optionClientImport
     *            sets the value of the client import option (default is false)
     */
    public void setOptionClientImport(boolean optionClientImport) {
        this.optionClientImport = optionClientImport;
    }

    public EntityXMLProcessor getImportProcessor() {
        return importProcessor;
    }

    public void setImportProcessor(EntityXMLProcessor importProcessor) {
        this.importProcessor = importProcessor;
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }
}