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
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.client.application.window;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.SecurityChecker;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.model.ad.utility.AttachmentMethod;
import org.openbravo.model.common.enterprise.Organization;

public class AttachImplementationManager {

  @Inject
  @Any
  private Instance<AttachImplementation> attachImplementationHandlers;

  /**
   * Method to upload files. This method calls needed handler class
   * 
   * @param fileItem
   *          fileItem obtained when uploading files from [Add] in Attachment section
   * @param tableId
   *          the tableId where the attachment is going to be done
   * @param key
   *          the recordId where the attachment is going to be done
   * @param strDataType
   *          the datatype of the document that is going to be attached
   * @param documentOrganization
   *          the organization of the record where the attacfhment is goingto be done
   * @param strText
   *          the description of the attachment
   * @param parameters
   *          more metadata that will be save in the attachment
   * @param file
   *          report converted into PDF file. The parameter will be used when using 'print and
   *          attach' functionality
   */
  public void upload(FileItem fileItem, String tableId, String key, String strDataType,
      String documentOrganization, String strText, Map<String, Object> parameters, File file) {
    Organization org = OBDal.getInstance().get(Organization.class, documentOrganization);
    Table table = OBDal.getInstance().get(Table.class, tableId);

    if (fileItem == null && file == null) {
      throw new OBException(String.format(OBMessageUtils.messageBD("OBUIAPP_NoFileToAttach")));
    }

    AttachmentMethod attachMethod = getAttachmenMethod();
    if (attachMethod == null) {
      throw new OBException(String.format(OBMessageUtils.messageBD("OBUIAPP_NoMethod")));
    }

    String strName = "";
    if (file == null) {
      strName = fileItem.getName();
      // FIXME: Get the directory separator from Java runtime
      int i = strName.lastIndexOf("\\");
      if (i != -1) {
        strName = strName.substring(i + 1);
        // FIXME: Get the directory separator from Java runtime
      } else if ((i = strName.lastIndexOf("/")) != -1) {
        strName = strName.substring(i + 1);
      }
    } else {
      strName = file.getName();
    }
    Attachment attachment = null;
    try {
      OBContext.setAdminMode();
      attachment = getAttachment(table, key, strName);
      if (attachment == null) {
        attachment = OBProvider.getInstance().get(Attachment.class);
        attachment.setClient(org.getClient());
        attachment.setSequenceNumber(getSequenceNumber(table, key));
        attachment.setName(strName);
        attachment.setTable(table);
        attachment.setRecord(key);
      }
      attachment.setAttachmentMethod(attachMethod);
      attachment.setOrganization(org);
      attachment.setActive(true);

      OBDal.getInstance().save(attachment);

      AttachImplementation handler = getHandler(attachMethod == null ? "Default" : attachMethod
          .getValue());

      if (handler == null) {
        throw new OBException(String.format(OBMessageUtils.messageBD("OBUIAPP_NoMethod")));
      }
      handler.uploadFile(fileItem, attachment, strDataType, strText, parameters, file);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private AttachmentMethod getAttachmenMethod() {
    OBCriteria<AttachmentConfig> obc = OBDal.getInstance().createCriteria(AttachmentConfig.class);
    obc.setMaxResults(1);
    if (obc.uniqueResult() != null) {
      return ((AttachmentConfig) obc.uniqueResult()).getAttachmentMethod();
    }
    OBCriteria<AttachmentMethod> am = OBDal.getInstance().createCriteria(AttachmentMethod.class);
    obc.add(Restrictions.eq(AttachmentMethod.PROPERTY_VALUE, "Default"));
    obc.setMaxResults(1);
    if (am.uniqueResult() != null) {
      return (AttachmentMethod) am.uniqueResult();
    }
    return null;
  }

  /**
   * It gets the sequence number for the attachment
   * 
   * @param table
   *          the table of the attachment
   * @param recordId
   *          the recordId of the attachment
   * @return
   */
  private Long getSequenceNumber(Table table, String recordId) {
    OBCriteria<Attachment> obc = OBDal.getInstance().createCriteria(Attachment.class);
    obc.add(Restrictions.eq(Attachment.PROPERTY_RECORD, recordId));
    obc.add(Restrictions.eq(Attachment.PROPERTY_TABLE, table));
    obc.addOrderBy(Attachment.PROPERTY_SEQUENCENUMBER, false);
    obc.setMaxResults(1);
    if (obc.uniqueResult() != null) {
      Attachment attach = (Attachment) obc.uniqueResult();
      return attach.getSequenceNumber() + 10L;
    } else {
      return 10L;
    }
  }

  /**
   * checks if the attachment already exists for given parameters.
   * 
   * @param table
   * @param recordId
   * @param fileName
   * @return If exists, the attachment is returned. Else, null is returned
   */
  private Attachment getAttachment(Table table, String recordId, String fileName) {
    OBCriteria<Attachment> obc = OBDal.getInstance().createCriteria(Attachment.class);
    obc.add(Restrictions.eq(Attachment.PROPERTY_RECORD, recordId));
    obc.add(Restrictions.eq(Attachment.PROPERTY_NAME, fileName));
    obc.add(Restrictions.eq(Attachment.PROPERTY_TABLE, table));
    obc.setMaxResults(1);
    if (obc.uniqueResult() != null) {
      return (Attachment) obc.uniqueResult();
    }

    return null;
  }

  /**
   * Method to delete files. This method calls needed handler class
   * 
   * @param attachment
   *          the attachment that will be removed
   */
  public void delete(Attachment attachment) {
    AttachImplementation handler = getHandler(attachment.getAttachmentMethod() == null ? "Default"
        : attachment.getAttachmentMethod().getValue());
    if (handler == null) {
      throw new OBException(String.format(OBMessageUtils.messageBD("OBUIAPP_NoMethod")));
    }
    handler.deleteFile(attachment);
    OBDal.getInstance().remove(attachment);
    OBDal.getInstance().flush();
  }

  /**
   * It gets the class that must be used, depending on the given attachMethod.
   * 
   * @param strAttachMethod
   *          attachmentMethod, that is the qualifier of the class.
   * @return
   */

  private AttachImplementation getHandler(String strAttachMethod) {
    // TODO: in the component provider.selector add the strAttachMethod
    AttachImplementation handler = null;
    for (AttachImplementation nextHandler : attachImplementationHandlers
        .select(new ComponentProvider.Selector(strAttachMethod))) {
      if (handler == null) {
        handler = nextHandler;
      } else {
        throw new OBException(OBMessageUtils.parseTranslation("@MoreThanOneImplementation@"));
      }
    }
    return handler;
  }

  /**
   * Method to update file's metadata. This method calls needed handler class
   * 
   * @param attachID
   *          the attachmentID that will be updated
   * @param tabId
   *          the TabId where the attachment is being modified
   * @param description
   *          the new description to be updated
   * @param parameters
   *          more metadata to be updated
   */
  public void update(String attachID, String tabId, String description,
      Map<String, Object> parameters) {

    Tab tab = OBDal.getInstance().get(Tab.class, tabId);
    String tableId = (String) DalUtil.getId(tab.getTable());
    Attachment attachment = OBDal.getInstance().get(Attachment.class, attachID);
    if (attachment == null) {
      throw new OBException(String.format(OBMessageUtils.messageBD("OBUIAPP_NoAttachmentFound")));
    }

    // Checks if the user has readable access to the record where the file is attached
    Entity entity = ModelProvider.getInstance().getEntityByTableId(tableId);
    if (entity != null) {
      Object object = OBDal.getInstance().get(entity.getMappingClass(), attachment.getRecord());
      if (object instanceof OrganizationEnabled) {
        SecurityChecker.getInstance().checkReadableAccess((OrganizationEnabled) object);
      }
    }

    AttachImplementation handler = getHandler(attachment.getAttachmentMethod() == null ? "Default"
        : attachment.getAttachmentMethod().getValue());
    if (handler == null) {
      throw new OBException(String.format(OBMessageUtils.messageBD("OBUIAPP_NoMethod")));
    }
    handler.updateFile(attachment, tabId, description, parameters);

  }

  /**
   * Method to download a file. This method calls needed handler class
   * 
   * @param attachmentId
   *          the attachmentId that will be downloaded
   */
  public void download(String attachmentId) {
    OBContext.setAdminMode();
    try {
      Attachment attachment = OBDal.getInstance().get(Attachment.class, attachmentId);

      if (attachment == null) {
        throw new OBException(String.format(OBMessageUtils.messageBD("OBUIAPP_NoAttachmentFound")));
      }

      // Checks if the user has readable access to the record where the file is attached
      Entity entity = ModelProvider.getInstance().getEntityByTableId(attachment.getTable().getId());
      if (entity != null) {
        Object object = OBDal.getInstance().get(entity.getMappingClass(), attachment.getRecord());
        if (object instanceof OrganizationEnabled) {
          SecurityChecker.getInstance().checkReadableAccess((OrganizationEnabled) object);
        }
      }

      AttachImplementation handler = getHandler(attachment.getAttachmentMethod() == null ? "Default"
          : attachment.getAttachmentMethod().getValue());
      if (handler == null) {
        throw new OBException(String.format(OBMessageUtils.messageBD("OBUIAPP_NoMethod")));
      }
      handler.downloadFile(attachment);
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  /**
   * Method to download all the files related to the record, in a single .zip dile. This method
   * calls needed handler class
   * 
   * @param tabId
   *          The tab Id where the download process is being executed
   * @param recordId
   *          All the attachment related to this recordID will be downloaded in a single .zip file
   */

  public void dowloadAll(String tabId, String recordIds) {

    Tab tab = OBDal.getInstance().get(Tab.class, tabId);
    String tableId = (String) DalUtil.getId(tab.getTable());

    // Checks if the user has readable access to the record where the file is attached
    Entity entity = ModelProvider.getInstance().getEntityByTableId(tableId);
    if (entity != null) {
      Object object = OBDal.getInstance().get(entity.getMappingClass(), recordIds);
      if (object instanceof OrganizationEnabled) {
        SecurityChecker.getInstance().checkReadableAccess((OrganizationEnabled) object);
      }
    }

    try {
      OBContext.setAdminMode(true);
      HttpServletResponse response = RequestContext.get().getResponse();

      response.setContentType("application/zip");
      response.setHeader("Content-Disposition", "attachment; filename=attachments.zip");
      final ZipOutputStream dest = new ZipOutputStream(response.getOutputStream());
      // attachmentFiles.list().toArray();
      HashMap<String, Integer> writtenFiles = new HashMap<String, Integer>();
      OBCriteria<Attachment> attachmentFiles = OBDao.getFilteredCriteria(Attachment.class,
          Restrictions.eq("table.id", tableId), Restrictions.in("record", recordIds.split(",")));
      for (Attachment attachmentFile : attachmentFiles.list()) {
        AttachImplementation handler = getHandler(attachmentFile.getAttachmentMethod() == null ? "Default"
            : attachmentFile.getAttachmentMethod().getValue());
        if (handler == null) {
          throw new OBException(String.format(OBMessageUtils.messageBD("OBUIAPP_NoMethod")));
        }
        handler.downloadAll(attachmentFile, writtenFiles, dest);
      }
      dest.close();

    } catch (Exception e) {
      // log.error("Error while downloading attachments", e);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }

  }

}
