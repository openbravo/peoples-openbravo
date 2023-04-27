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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.cache.TimeInvalidatedCache;
import org.openbravo.client.application.attachment.AttachmentUtils.AttachmentType;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.SecurityChecker;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.model.ad.utility.ReprintableDocument;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Centralizes the {@link ReprintableDocument} Management. Any action to manage reprintable
 * documents in Openbravo should be done through this class.
 */
@ApplicationScoped
public class ReprintableDocumentManager {

  private TimeInvalidatedCache<String, String> methodsOfAttachmentConfigs;
  private TimeInvalidatedCache<String, Boolean> reprintDocumentsConfiguration;

  /**
   * Supported formats for a {@link ReprintableDocument}
   */
  public enum Format {
    XML, PDF;
  }

  @PostConstruct
  private void init() {
    methodsOfAttachmentConfigs = TimeInvalidatedCache.newBuilder()
        .name("Methods of Attachment Configs")
        .expireAfterDuration(Duration.ofMinutes(10))
        .build(this::getAttachmentMethod);
    reprintDocumentsConfiguration = TimeInvalidatedCache.newBuilder()
        .name("Reprint Document Org Configs")
        .expireAfterDuration(Duration.ofMinutes(10))
        .build(this::getIsReprintDocumentEnabled);
  }

  private String getAttachmentMethod(String attachmentConfigurationId) {
    //@formatter:off
    String hql = "select c.attachmentMethod.value" +
                 "  from AttachmentConfig c" +
                 " where c.id = :id";
    //@formatter:on
    return OBDal.getInstance()
        .getSession()
        .createQuery(hql, String.class)
        .setParameter("id", attachmentConfigurationId)
        .uniqueResult();
  }

  private Boolean getIsReprintDocumentEnabled(String orgId) {
    return OBContext.getOBContext()
        .getOrganizationStructureProvider(OBContext.getOBContext().getCurrentClient().getId())
        .getParentList(orgId, true)
        .stream()
        .map(org -> OBDal.getInstance().get(Organization.class, org).getReprintDocuments())
        .filter(setting -> !StringUtils.isBlank(setting))
        .findFirst()
        .map("enabled"::equals)
        .orElse(false);
  }

  /**
   * Creates a new ReprintableDocument and uploads its data as an attachment
   * 
   * @param documentData
   *          An InputStream with the document data. This method is in charge of closing it when
   *          finish its execution.
   * @param format
   *          The format of document
   * @param sourceDocument
   *          The document used as source data for the ReprintableDocument
   *
   * @throws OBSecurityException
   *           if the write access to the source document is not granted in the current context
   *           because in such case is not allowed to create a ReprintableDocument linked to the
   *           source document.
   * @throws OBException
   *           if there is not a valid attachment configuration for the ReprintableDocument or is
   *           not possible to find a handler for the attachment method defined in the attachment
   *           configuration found
   */
  public ReprintableDocument upload(InputStream documentData, Format format,
      SourceDocument sourceDocument) {
    ReprintableDocument document = createReprintableDocument(format, sourceDocument);
    ReprintableDocumentAttachHandler handler = getHandler(document);

    try (documentData) {
      handler.upload(document, documentData);
    } catch (Exception ex) {
      throw new OBException("Error uploading reprintable document", ex);
    }
    return document;
  }

  /**
   * Retrieves the data of a ReprintableDocument linked to the provided document
   *
   * @param sourceDocument
   *          The document used as source data for the ReprintableDocument
   *
   * @return an InputStream with the document data. Code invoking this method is also responsible of
   *         closing this InputStream.
   *
   * @throws OBSecurityException
   *           if the read access to the source document is not granted in the current context
   *           because in such case is not allowed to access to the ReprintableDocument linked to
   *           the source document.
   * @throws OBException
   *           if it is not possible to find a handler for the attachment method defined in the
   *           ReprintableDocument attachment configuration
   */
  public InputStream download(SourceDocument sourceDocument) throws IOException {
    ReprintableDocument reprintableDocument = findReprintableDocument(sourceDocument);
    ReprintableDocumentAttachHandler handler = getHandler(reprintableDocument);

    return handler.download(reprintableDocument);
  }

  /**
   * Checks whether documents reprinting is enabled for a given organization which is determined by
   * the value returned with {@link Organization#getReprintDocuments()}. Note that in case that
   * value is not defined (null) for the given organization, it is taken from the closest
   * organization in the parent organization tree that has a value defined (not null).
   *
   * @param orgId
   *          The ID of the organization to check
   *
   * @return true if document reprinting is enabled for the given organization or false otherwise
   */
  public boolean isReprintDocumentsEnabled(String orgId) {
    return reprintDocumentsConfiguration.get(orgId);
  }

  private ReprintableDocument createReprintableDocument(Format format,
      SourceDocument sourceDocument) {
    AttachmentConfig config = AttachmentUtils.getAttachmentConfig(AttachmentType.RD);
    if (config == null) {
      throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoValidAttachConfig"));
    }
    BaseOBObject sourceDocumentBOB = sourceDocument.getBOB();

    SecurityChecker.getInstance().checkWriteAccess(sourceDocumentBOB);

    // If we have write access to the source document, the ReprintableDocument can be saved but we
    // need to do it in admin mode because the ReprintableDocument entity is not writable by default
    try {
      OBContext.setAdminMode(true);
      ReprintableDocument reprintableDocument = OBProvider.getInstance()
          .get(ReprintableDocument.class);
      reprintableDocument.setClient((Client) sourceDocumentBOB.get("client"));
      reprintableDocument.setOrganization((Organization) sourceDocumentBOB.get("organization"));
      reprintableDocument.setName("reprintableDocument." + format.name().toLowerCase());
      reprintableDocument.setFormat(format.name());
      reprintableDocument.setAttachmentConfiguration(config);
      reprintableDocument.set(sourceDocument.getProperty(), sourceDocument.getBOB());
      OBDal.getInstance().save(reprintableDocument);
      return reprintableDocument;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private ReprintableDocument findReprintableDocument(SourceDocument sourceDocument) {
    BaseOBObject bob = sourceDocument.getBOB();

    SecurityChecker.getInstance().checkReadableAccess((OrganizationEnabled) bob);

    // If we have read access to the source document, its ReprintableDocument can be accessed but we
    // need to do it in admin mode because the ReprintableDocument entity is not readable by default
    try {
      OBContext.setAdminMode(true);
      return (ReprintableDocument) OBDal.getInstance()
          .createCriteria(ReprintableDocument.class)
          .add(Restrictions.eq(sourceDocument.getProperty(), bob))
          .setMaxResults(1)
          .uniqueResult();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private ReprintableDocumentAttachHandler getHandler(ReprintableDocument reprintableDocument) {
    String attachMethod = methodsOfAttachmentConfigs
        .get(reprintableDocument.getAttachmentConfiguration().getId());
    return WeldUtils
        .getInstances(ReprintableDocumentAttachHandler.class,
            new ComponentProvider.Selector(attachMethod))
        .stream()
        .findFirst()
        .orElseThrow(() -> new OBException(OBMessageUtils.messageBD("OBUIAPP_NoMethod")));
  }

  /**
   * Clears the cached information of the attachment configuration passed as parameter. For internal
   * use only.
   *
   * @param attachmentConfigurationId
   *          The attachment configuration ID
   */
  void invalidateAttachmentConfigurationCache(String attachmentConfigurationId) {
    methodsOfAttachmentConfigs.invalidate(attachmentConfigurationId);
  }

  /**
   * Clears the cached information of the document reprinting configuration for a given
   * organization.
   *
   * @param orgId
   *          The organization ID
   */
  void invalidateReprintDocumentConfigurationCache(String orgId) {
    reprintDocumentsConfiguration.invalidate(orgId);
  }
}
