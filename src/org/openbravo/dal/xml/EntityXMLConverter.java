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
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.IdentifierProvider;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.SystemInformation;

/**
 * Converts one or more business objects to a XML presentation. There are several options which
 * control the behavior. One option is to include referenced objects (or not). For example Currency
 * references Country, if a Currency instance is exported should then also the Country instance be
 * exported. Another option controls if the children of a business object (e.g. the order lines of
 * an order) are exported within the part as a subnode in the xml result. Or that children are not
 * exported or exported in the root of the xml document.
 * 
 * @author mtaal
 */

public class EntityXMLConverter implements OBNotSingleton {
  private static final Logger log = Logger.getLogger(EntityXMLConverter.class);

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

  // should transient info also be exported
  private boolean optionExportTransientInfo = true;

  // should audit info also be exported
  private boolean optionExportAuditInfo = true;

  // controls if the client and organization property are exported to
  private boolean optionExportClientOrganizationReferences = false;

  // only export references which belong to this client
  private Client client;

  // if the system attributes (version, timestamp, etc.) are added to
  // to the root element, for testcases it makes sense to not have this
  // to compare previous output results with new output results
  private boolean addSystemAttributes = true;

  // keeps track of which objects still have to be exported
  // and which ones have been considered already
  private List<BaseOBObject> toHandle = new ArrayList<BaseOBObject>();
  private Set<BaseOBObject> consideredForHandling = new HashSet<BaseOBObject>();

  // the to-be-exported objects passed in explicitly, these will never get a
  // referenced attribute
  private Set<BaseOBObject> originalExportContent = new HashSet<BaseOBObject>();

  // keeps track which of the objects was added to the export list
  // because it was referenced. In this case an attribute is added
  // to the root element
  private Set<BaseOBObject> referenced = new HashSet<BaseOBObject>();

  private Document document = null;

  /**
   * Clear internal data structures, after this call this converter can be used for a new set of
   * objects which need to be exported to a xml representation.
   */
  public void clear() {
    document = null;
    referenced.clear();
    toHandle.clear();
    consideredForHandling.clear();
    originalExportContent.clear();
  }

  /**
   * Converts one business object to xml and returns the resulting xml string.
   * 
   * @param obObject
   *          the object to convert to xml
   * @return the xml representation of obObject
   */
  public String toXML(BaseOBObject obObject) {
    final List<BaseOBObject> bobs = new ArrayList<BaseOBObject>();
    bobs.add(obObject);
    return toXML(bobs);
  }

  /**
   * Converts a collection of business objects to xml.
   * 
   * @param bobs
   *          the collection to convert
   * @return the resulting xml string
   */
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
    final Element rootElement = XMLUtil.getInstance().addRootElement(getDocument(),
        XMLConstants.OB_ROOT_ELEMENT);
    addSystemAttributes(rootElement);
  }

  /**
   * Processes one business object and adds it to the dom4j document which is present in the
   * EntityXMLConverter. After this call the xml can be retrieved by calling the
   * {@link #getDocument()} or the {@link #getProcessResult()} method.
   * 
   * @param bob
   *          the business object to convert to xml (dom4j)
   */
  public void process(BaseOBObject bob) {
    createDocument();
    // set the export list
    getToHandle().add(bob);
    getConsideredForHandling().add(bob);
    originalExportContent.add(bob);

    // and do it
    export(getDocument().getRootElement());
  }

  /**
   * Processes a collection of business objects and adds their xml to the dom4j document which is
   * present in the EntityXMLConverter. After this call the xml can be retrieved by calling the
   * {@link #getDocument()} or the {@link #getProcessResult()} method.
   * 
   * @param bob
   *          the business object to convert to xml (dom4j)
   */
  public void process(Collection<BaseOBObject> bobs) {
    createDocument();
    // set the export list
    getToHandle().addAll(bobs);
    getConsideredForHandling().addAll(bobs);
    originalExportContent.addAll(bobs);

    // and do it
    export(getDocument().getRootElement());
  }

  /**
   * @return the xml String created by formatting the internal dom4j Document
   */
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
      currentElement.addAttribute(XMLConstants.ID_ATTRIBUTE, id.toString());
    }
    currentElement.addAttribute(XMLConstants.IDENTIFIER_ATTRIBUTE, IdentifierProvider.getInstance()
        .getIdentifier(obObject));

    // if this object has been added as a referenced object
    // set the reference attribute so that we at import can treat this
    // one differently
    final boolean isCurrentEntityReferenced = getReferenced().contains(obObject)
        && !originalExportContent.contains(obObject);
    if (isCurrentEntityReferenced) {
      currentElement.addAttribute(XMLConstants.REFERENCE_ATTRIBUTE, "true");
    }

    // depending on the security only a limited set of
    // properties is exported
    final boolean onlyIdentifierProps = OBContext.getOBContext().getEntityAccessChecker()
        .isDerivedReadable(obObject.getEntity());

    // export each property
    for (final Property p : obObject.getEntity().getProperties()) {
      if (onlyIdentifierProps && !p.isIdentifier()) {
        continue;
      }

      if (p.isClientOrOrganization() && !isOptionExportClientOrganizationReferences()) {
        continue;
      }

      // onetomany is always a child currently
      if (p.isOneToMany() && (!isOptionIncludeChildren() || isCurrentEntityReferenced)) {
        continue;
      }

      // note only not-mandatory transient fields are allowed to be
      // not exported, a mandatory field should always be exported
      // auditinfo is mandatory but can be ignored for export
      // as it is always set
      if (p.isAuditInfo() && !isOptionExportAuditInfo()) {
        continue;
      }
      final boolean isTransientField = p.isTransient(obObject);
      if (!p.isMandatory() && isTransientField && !isOptionExportTransientInfo()) {
        continue;
      }

      // set the tag
      final Element currentPropertyElement = currentElement.addElement(p.getName());

      // add transient attribute
      if (p.isTransient(obObject)) {
        currentPropertyElement.addAttribute(XMLConstants.TRANSIENT_ATTRIBUTE, "true");
      }

      if (p.isAuditInfo()) {
        currentPropertyElement.addAttribute(XMLConstants.TRANSIENT_ATTRIBUTE, "true");
      }
      if (p.isInactive()) {
        currentPropertyElement.addAttribute(XMLConstants.INACTIVE_ATTRIBUTE, "true");
      }

      // get the value
      final Object value = obObject.get(p.getName());

      // will result in an empty tag if null
      if (value == null) {
        continue;
      }

      if (p.isCompositeId()) {
        log.warn("Entity " + obObject.getEntity()
            + " has compositeid, this is not yet supported in the webservice");
        continue;
      }

      // make a difference between a primitive and a reference
      if (p.isPrimitive()) {
        currentPropertyElement.addText(XMLTypeConverter.getInstance().toXML(value));
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
            final Element refElement = currentPropertyElement.addElement(DalUtil
                .getEntityName(child));
            refElement.addAttribute(XMLConstants.ID_ATTRIBUTE, DalUtil.getId(child).toString());
            refElement.addAttribute(XMLConstants.IDENTIFIER_ATTRIBUTE, IdentifierProvider
                .getInstance().getIdentifier(child));
            addToExportList((BaseOBObject) o);
          }
        }
      } else if (!p.isOneToMany()) {
        // add reference attributes
        addReferenceAttributes(currentPropertyElement, (BaseOBObject) value);
        // and also export the object itself if required
        // but do not add auditinfo references
        if (isOptionIncludeReferenced() && !p.isAuditInfo() && !p.isClientOrOrganization()) {
          addToExportList((BaseOBObject) value);
        }
      }
    }
  }

  private void addReferenceAttributes(Element currentElement, BaseOBObject referedObject) {
    if (referedObject == null) {
      return;
    }
    // final Element refElement =
    // currentElement.addElement(REFERENCE_ELEMENT_NAME);
    currentElement.addAttribute(XMLConstants.ID_ATTRIBUTE, DalUtil.getId(referedObject).toString());
    currentElement.addAttribute(XMLConstants.ENTITYNAME_ATTRIBUTE, DalUtil
        .getEntityName(referedObject));
    currentElement.addAttribute(XMLConstants.IDENTIFIER_ATTRIBUTE, IdentifierProvider.getInstance()
        .getIdentifier(referedObject));
  }

  protected void addToExportList(BaseOBObject bob) {
    // only export references if belonging to the current client
    if (getClient() != null && bob instanceof ClientEnabled
        && !((ClientEnabled) bob).getClient().getId().equals(getClient().getId())) {
      return;
    }

    // was already exported
    if (getConsideredForHandling().contains(bob)) {
      return;
    }
    getToHandle().add(bob);
    consideredForHandling.add(bob);
    getReferenced().add(bob);
  }

  protected void exported(BaseOBObject bob) {
    Check.isTrue(getToHandle().contains(bob),
        "Exported business object not part of toExport list, it has not yet been removed from it!");
    getToHandle().remove(bob);
  }

  /**
   * Controls if referenced objects (through many-to-one associations) should also be exported (in
   * the root of the xml).
   * 
   * @return true the referenced objects are exported, false (the default) referenced objects are
   *         not exported
   */
  public boolean isOptionIncludeReferenced() {
    return optionIncludeReferenced;
  }

  /**
   * Controls if referenced objects (through many-to-one associations) should also be exported (in
   * the root of the xml).
   * 
   * @param optionIncludeReferenced
   *          set to true the referenced objects are exported, set to false (the default) referenced
   *          objects are not exported
   */
  public void setOptionIncludeReferenced(boolean optionIncludeReferenced) {
    this.optionIncludeReferenced = optionIncludeReferenced;
  }

  /**
   * Controls if children (the one-to-many associations) are exported. If true then the children can
   * be exported embedded in the parent or in the root of the xml. This is controlled by the
   * {@link #isOptionEmbedChildren()} option.
   * 
   * @return true children are exported as well, false (the default) children are not exported
   */
  public boolean isOptionIncludeChildren() {
    return optionIncludeChildren;
  }

  /**
   * Controls if children (the one-to-many associations) are exported. If true then the children can
   * be exported embedded in the parent or in the root of the xml. This is controlled by the
   * {@link #isOptionEmbedChildren()} option.
   * 
   * @param optionIncludeChildren
   *          set to true children are exported as well, set to false (the default) children are not
   *          exported
   */
  public void setOptionIncludeChildren(boolean optionIncludeChildren) {
    this.optionIncludeChildren = optionIncludeChildren;
  }

  /**
   * This option controls if children are exported within the parent or in the root of the xml. The
   * default is embedded (default value is true).
   * 
   * @return true (default) children are embedded in the parent, false children are exported in the
   *         root of the xml
   */
  public boolean isOptionEmbedChildren() {
    return optionEmbedChildren;
  }

  /**
   * This option controls if children are exported within the parent or in the root of the xml. The
   * default is embedded (default value is true).
   * 
   * @return true (default) children are embedded in the parent, false children are exported in the
   *         root of the xml
   */
  public void setOptionEmbedChildren(boolean optionEmbedChildren) {
    this.optionEmbedChildren = optionEmbedChildren;
  }

  private List<BaseOBObject> getToHandle() {
    return toHandle;
  }

  private Set<BaseOBObject> getReferenced() {
    return referenced;
  }

  private Set<BaseOBObject> getConsideredForHandling() {
    return consideredForHandling;
  }

  protected void addSystemAttributes(Element element) {
    if (!isAddSystemAttributes()) {
      return;
    }
    final boolean adminMode = OBContext.getOBContext().isInAdministratorMode();
    try {
      OBContext.getOBContext().setInAdministratorMode(true);
      final List<SystemInformation> sis = OBDal.getInstance().createCriteria(
          SystemInformation.class).list();
      Check.isTrue(sis.size() > 0, "There should be at least one SystemInfo record but there are "
          + sis.size());
      element.addAttribute(XMLConstants.DATE_TIME_ATTRIBUTE, "" + new Date());
      element
          .addAttribute(XMLConstants.OB_VERSION_ATTRIBUTE, sis.get(0).getOpenbravoVersion() + "");
      element.addAttribute(XMLConstants.OB_REVISION_ATTRIBUTE, sis.get(0).getCodeRevision() + "");
    } finally {
      OBContext.getOBContext().setInAdministratorMode(adminMode);
    }
  }

  private boolean isAddSystemAttributes() {
    return addSystemAttributes;
  }

  /**
   * If set to true then the system version and revision are exported as attributes of the root tag.
   * 
   * @param addSystemAttributes
   *          if true (the default) then the Openbravo version and revision are exported in the root
   *          tag.
   */
  public void setAddSystemAttributes(boolean addSystemAttributes) {
    this.addSystemAttributes = addSystemAttributes;
  }

  /**
   * Returns the Dom4j Document which contains the xml. Subsequence calls to
   * {@link #process(BaseOBObject)} or {@link #process(Collection)} will add content to this
   * document.
   * 
   * @return the Dom4j Document containing the xml
   */
  public Document getDocument() {
    return document;
  }

  /**
   * Makes it possible to add exported business objects to an existing Dom4j document.
   * 
   * @param document
   *          the Dom4j document to which the business objects are added
   */
  public void setDocument(Document document) {
    this.document = document;
  }

  /**
   * Controls if the client and organization properties are also exported. The default is false. If
   * this is set to true then the import program should take into account that the
   * client/organization are present in the import xml.
   * 
   * @return if true then the client/organization properties are exported, if false then not
   */
  public boolean isOptionExportClientOrganizationReferences() {
    return optionExportClientOrganizationReferences;
  }

  /**
   * Controls if the client and organization properties are also exported. The default is false. If
   * this is set to true then the import program should take into account that the
   * client/organization are present in the import xml.
   * 
   * @param optionExportClientOrganizationReferences
   *          if set to true then the client/organization properties are exported, if false then not
   */
  public void setOptionExportClientOrganizationReferences(
      boolean optionExportClientOrganizationReferences) {
    this.optionExportClientOrganizationReferences = optionExportClientOrganizationReferences;
  }

  public boolean isOptionExportTransientInfo() {
    return optionExportTransientInfo;
  }

  public void setOptionExportTransientInfo(boolean optionExportTransientInfo) {
    this.optionExportTransientInfo = optionExportTransientInfo;
  }

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public boolean isOptionExportAuditInfo() {
    return optionExportAuditInfo;
  }

  public void setOptionExportAuditInfo(boolean optionExportAuditInfo) {
    this.optionExportAuditInfo = optionExportAuditInfo;
  }
}