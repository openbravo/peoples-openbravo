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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.IdentifierProvider;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.SystemInformation;

/**
 * Converts one or more business objects to a XML presentation. There are
 * several options which control the behavior.
 * 
 * @author mtaal
 */

public class EntityXMLConverter implements OBNotSingleton {
    private static final Logger log = Logger
            .getLogger(EntityXMLConverter.class);

    public static EntityXMLConverter newInstance() {
        return OBProvider.getInstance().get(EntityXMLConverter.class);
    }

    // controls if the many-to-one references objects are also included
    private boolean optionIncludeReferenced = false;

    // controls if the children (mostly one-to-many) are also included
    private boolean optionIncludeChildren = false;

    // if children are exported then they can be embedded in the parent's
    // property or placed in the root.
    private boolean optionEmbedChildren = true;

    // controls if the client and organization property are exported to
    private boolean optionExportClientOrganizationReferences = false;

    // if the system attributes (version, timestamp, etc.) are added to
    // to the root element, for testcases it makes sense to not have this
    // to compare previous output results with new output results
    private boolean addSystemAttributes = true;

    // keeps track of which objects still have to be exported
    // and which ones have been considered already
    private List<BaseOBObject> toHandle = new ArrayList<BaseOBObject>();
    private Set<BaseOBObject> consideredForHandling = new HashSet<BaseOBObject>();

    // keeps track which of the objects was added to the export list
    // because it was referenced. In this case an attribute is added
    // to the root element
    private Set<BaseOBObject> referenced = new HashSet<BaseOBObject>();

    private Document document = null;

    // clear internal data to start with a fresh face
    public void clear() {
        document = null;
        referenced.clear();
        toHandle.clear();
        consideredForHandling.clear();
    }

    public String toXML(BaseOBObject obObject) {
        final List<BaseOBObject> bobs = new ArrayList<BaseOBObject>();
        bobs.add(obObject);
        return toXML(bobs);
    }

    public String toXML(Collection<BaseOBObject> bobs) {
        clear();
        process(bobs);
        return XMLUtil.getInstance().toString(getDocument());
    }

    protected void createDocument() {
        if (getDocument() != null) {
            return;
        }
        setDocument(XMLUtil.getInstance().createDomDocument());

        // because a list of objects is exported a root tag is placed
        // around them
        final Element rootElement = XMLUtil.getInstance().addRootElement(
                getDocument(), XMLConstants.OB_ROOT_ELEMENT);
        addSystemAttributes(rootElement);
    }

    public void process(BaseOBObject bob) {
        createDocument();
        // set the export list
        getToHandle().add(bob);
        getConsideredForHandling().add(bob);

        // and do it
        export(getDocument().getRootElement());
    }

    public void process(Collection<BaseOBObject> bobs) {
        createDocument();
        // set the export list
        getToHandle().addAll(bobs);
        getConsideredForHandling().addAll(bobs);

        // and do it
        export(getDocument().getRootElement());
    }

    public String getProcessResult() {
        return XMLUtil.getInstance().toString(getDocument());
    }

    protected void export(Element rootElement) {
        while (getToHandle().size() > 0) {
            final BaseOBObject bob = getToHandle().iterator().next();
            export(bob, rootElement);
            exported(bob);
        }
        getConsideredForHandling().clear();
    }

    protected void export(BaseOBObject obObject, Element rootElement) {
        final String entityName = DalUtil.getEntityName(obObject);
        final Element currentElement = rootElement.addElement(entityName);

        // set the id and identifier attributes
        final Object id = DalUtil.getId(obObject);
        if (id != null) {
            currentElement.addAttribute(XMLConstants.ID_ATTRIBUTE, id
                    .toString());
        }
        currentElement.addAttribute(XMLConstants.IDENTIFIER_ATTRIBUTE,
                IdentifierProvider.getInstance().getIdentifier(obObject));

        // if this object has been added as a referenced object
        // set the reference attribute so that we at import can treat this
        // one differently
        final boolean isCurrentEntityReferenced = getReferenced().contains(
                obObject);
        if (isCurrentEntityReferenced) {
            currentElement.addAttribute(XMLConstants.REFERENCE_ATTRIBUTE,
                    "true");
        }

        // depending on the security only a limited set of
        // properties is exported
        final boolean onlyIdentifierProps = OBContext.getOBContext()
                .getEntityAccessChecker().isDerivedReadable(
                        obObject.getEntity());

        // export each property
        for (final Property p : obObject.getEntity().getProperties()) {
            if (onlyIdentifierProps && !p.isIdentifier()) {
                continue;
            }

            if (p.isClientOrOrganization()
                    && !isOptionExportClientOrganizationReferences()) {
                continue;
            }

            // onetomany is always a child currently
            if (p.isOneToMany()
                    && (!isOptionIncludeChildren() || isCurrentEntityReferenced)) {
                continue;
            }

            // set the tag
            final Element currentPropertyElement = currentElement.addElement(p
                    .getName());

            // add transient attribute
            if (p.isTransient(obObject)) {
                currentPropertyElement.addAttribute(
                        XMLConstants.TRANSIENT_ATTRIBUTE, "true");
            }
            if (p.isAuditInfo()) {
                currentPropertyElement.addAttribute(
                        XMLConstants.TRANSIENT_ATTRIBUTE, "true");
            }
            if (p.isInactive()) {
                currentPropertyElement.addAttribute(
                        XMLConstants.INACTIVE_ATTRIBUTE, "true");
            }

            // get the value
            final Object value = obObject.get(p.getName());

            // will result in an empty tag if null
            if (value == null) {
                continue;
            }

            if (p.isCompositeId()) {
                log
                        .warn("Entity "
                                + obObject.getEntity()
                                + " has compositeid, this is not yet supported in the webservice");
                continue;
            }

            // make a difference between a primitive and a reference
            if (p.isPrimitive()) {
                currentPropertyElement.addText(XMLTypeConverter.getInstance()
                        .toXML(value));
            } else if (p.isOneToMany()) {
                // get all the children and export each child
                final Collection<?> c = (Collection<?>) value;
                for (final Object o : c) {
                    // embed in the parent
                    if (isOptionEmbedChildren()) {
                        export((BaseOBObject) o, currentPropertyElement);
                    } else {
                        // add the child as a tag, the child entityname is
                        // used as the tagname
                        final BaseOBObject child = (BaseOBObject) o;
                        final Element refElement = currentPropertyElement
                                .addElement(DalUtil.getEntityName(child));
                        refElement.addAttribute(XMLConstants.ID_ATTRIBUTE,
                                DalUtil.getId(child).toString());
                        refElement.addAttribute(
                                XMLConstants.IDENTIFIER_ATTRIBUTE,
                                IdentifierProvider.getInstance().getIdentifier(
                                        child));
                        addToExportList((BaseOBObject) o);
                    }
                }
            } else if (!p.isOneToMany()) {
                // add reference attributes
                addReferenceAttributes(currentPropertyElement,
                        (BaseOBObject) value);
                // and also export the object itself if required
                // but do not add auditinfo references
                if (isOptionIncludeReferenced() && !p.isAuditInfo()
                        && !p.isClientOrOrganization()) {
                    addToExportList((BaseOBObject) value);
                }
            }
        }
    }

    private void addReferenceAttributes(Element currentElement,
            BaseOBObject referedObject) {
        if (referedObject == null) {
            return;
        }
        // final Element refElement =
        // currentElement.addElement(REFERENCE_ELEMENT_NAME);
        currentElement.addAttribute(XMLConstants.ID_ATTRIBUTE, DalUtil.getId(
                referedObject).toString());
        currentElement.addAttribute(XMLConstants.ENTITYNAME_ATTRIBUTE, DalUtil
                .getEntityName(referedObject));
        currentElement.addAttribute(XMLConstants.IDENTIFIER_ATTRIBUTE,
                IdentifierProvider.getInstance().getIdentifier(referedObject));
    }

    protected void addToExportList(BaseOBObject bob) {
        // was already exported
        if (getConsideredForHandling().contains(bob)) {
            return;
        }
        getToHandle().add(bob);
        consideredForHandling.add(bob);
        getReferenced().add(bob);
    }

    protected void exported(BaseOBObject bob) {
        Check
                .isTrue(
                        getToHandle().contains(bob),
                        "Exported business object not part of toExport list, it has not yet been removed from it!");
        getToHandle().remove(bob);
    }

    public boolean isOptionIncludeReferenced() {
        return optionIncludeReferenced;
    }

    public void setOptionIncludeReferenced(boolean optionIncludeReferenced) {
        this.optionIncludeReferenced = optionIncludeReferenced;
    }

    public boolean isOptionIncludeChildren() {
        return optionIncludeChildren;
    }

    public void setOptionIncludeChildren(boolean optionIncludeChildren) {
        this.optionIncludeChildren = optionIncludeChildren;
    }

    public boolean isOptionEmbedChildren() {
        return optionEmbedChildren;
    }

    public void setOptionEmbedChildren(boolean optionEmbedChildren) {
        this.optionEmbedChildren = optionEmbedChildren;
    }

    public List<BaseOBObject> getToHandle() {
        return toHandle;
    }

    public Set<BaseOBObject> getReferenced() {
        return referenced;
    }

    public void setReferenced(Set<BaseOBObject> referenced) {
        this.referenced = referenced;
    }

    public Set<BaseOBObject> getConsideredForHandling() {
        return consideredForHandling;
    }

    public void setConsideredForExport(Set<BaseOBObject> consideredForExport) {
        this.consideredForHandling = consideredForExport;
    }

    protected void addSystemAttributes(Element element) {
        if (!isAddSystemAttributes()) {
            return;
        }
        final boolean adminMode = OBContext.getOBContext()
                .isInAdministratorMode();
        try {
            OBContext.getOBContext().setInAdministratorMode(true);
            final List<SystemInformation> sis = OBDal.getInstance()
                    .createCriteria(SystemInformation.class).list();
            Check.isTrue(sis.size() > 0,
                    "There should be at least one SystemInfo record but there are "
                            + sis.size());
            element.addAttribute(XMLConstants.DATE_TIME_ATTRIBUTE, ""
                    + new Date());
            element.addAttribute(XMLConstants.OB_VERSION_ATTRIBUTE, sis.get(0)
                    .getObVersion()
                    + "");
            element.addAttribute(XMLConstants.OB_REVISION_ATTRIBUTE, sis.get(0)
                    .getCodeRevision()
                    + "");
        } finally {
            OBContext.getOBContext().setInAdministratorMode(adminMode);
        }
    }

    public boolean isAddSystemAttributes() {
        return addSystemAttributes;
    }

    public void setAddSystemAttributes(boolean addSystemAttributes) {
        this.addSystemAttributes = addSystemAttributes;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public boolean isOptionExportClientOrganizationReferences() {
        return optionExportClientOrganizationReferences;
    }

    public void setOptionExportClientOrganizationReferences(
            boolean optionExportClientOrganizationReferences) {
        this.optionExportClientOrganizationReferences = optionExportClientOrganizationReferences;
    }
}