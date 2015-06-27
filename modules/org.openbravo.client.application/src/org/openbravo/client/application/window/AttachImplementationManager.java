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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.application.window;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.application.ParameterValue;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.SecurityChecker;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.List;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.domain.Selector;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.model.ad.utility.AttachmentMethod;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.utils.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttachImplementationManager {

  private static final Logger log = LoggerFactory.getLogger(AttachImplementationManager.class);

  public static final String REFERENCE_LIST = "17";
  public static final String REFERENCE_SELECTOR_REFERENCE = "95E2A8B50A254B2AAE6774B8C2F28120";

  @Inject
  @Any
  private Instance<AttachImplementation> attachImplementationHandlers;

  /**
   * Method to upload files. This method calls needed handler class
   * 
   * @param requestParams
   * 
   * @param strTab
   *          the tab Id where the attachment is done
   * @param strKey
   *          the recordId where the attachment is done
   * @param strDataType
   *          the datatype of the document that is attached
   * @param strDocumentOrganization
   *          the organization ID of the record where the attachment is done
   * @param file
   *          The file to be uploaded
   * @throws OBException
   *           any exception thrown during the attachment uploading
   */
  public void upload(Map<String, String> requestParams, String strTab, String strKey,
      String strDataType, String strDocumentOrganization, File file) throws OBException {
    Organization org = OBDal.getInstance().get(Organization.class, strDocumentOrganization);

    Tab tab = OBDal.getInstance().get(Tab.class, strTab);
    if (file == null) {
      throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoFileToAttach"));
    }

    AttachmentConfig attachConf = AttachmentUtils.getAttachmentConfig(org.getClient());
    AttachmentMethod attachMethod;
    if (attachConf == null) {
      attachMethod = AttachmentUtils.getDefaultAttachmentMethod();
    } else {
      attachMethod = attachConf.getAttachmentMethod();
    }

    String strName = file.getName();

    Attachment attachment = null;
    try {
      OBContext.setAdminMode(true);
      attachment = getAttachment(tab.getTable(), strKey, strName);
      if (attachment == null) {
        attachment = OBProvider.getInstance().get(Attachment.class);
        attachment.setSequenceNumber(getSequenceNumber(tab.getTable(), strKey));
        attachment.setName(strName);
        attachment.setTable(tab.getTable());
        attachment.setRecord(strKey);
      }
      attachment.setAttachmentConf(attachConf);
      attachment.setOrganization(org);
      attachment.setActive(true);

      OBDal.getInstance().save(attachment);

      AttachImplementation handler = getHandler(attachMethod == null ? "Default" : attachMethod
          .getValue());

      if (handler == null) {
        throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoMethod"));
      }
      Map<String, Object> typifiedParameters = saveMetadata(requestParams, attachment, strTab,
          strKey, attachMethod);
      handler.uploadFile(attachment, strDataType, typifiedParameters, file, strTab);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  /**
   * Method to update file's metadata. This method calls needed handler class
   * 
   * @param requestParams
   * 
   * @param attachID
   *          the attachmentID that will be updated
   * @param tabId
   *          the TabId where the attachment is being modified
   * @throws OBException
   *           any exception thrown when updating the document
   */
  public void update(Map<String, String> requestParams, String attachID, String tabId)
      throws OBException {
    try {
      OBContext.setAdminMode(true);

      Attachment attachment = OBDal.getInstance().get(Attachment.class, attachID);
      if (attachment == null) {
        throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoAttachmentFound"));
      }

      checkReadableAccess(attachment);

      AttachImplementation handler = getHandler(attachment.getAttachmentConf() == null ? "Default"
          : attachment.getAttachmentConf().getAttachmentMethod().getValue());

      if (handler == null) {
        throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoMethod"));
      }
      Map<String, Object> typifiedParameters = saveMetadata(requestParams, attachment, tabId,
          attachment.getRecord(), attachment.getAttachmentConf().getAttachmentMethod());
      handler.updateFile(attachment, tabId, typifiedParameters);
      OBDal.getInstance().save(attachment);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Method to download a file. This method calls needed handler class
   * 
   * @param attachmentId
   *          the attachment Id that will be downloaded
   * @param os
   *          The output stream to dump the file
   * @throws OBException
   *           any exception thrown during the download
   */
  public void download(String attachmentId, OutputStream os) throws OBException {

    try {
      OBContext.setAdminMode(true);
      Attachment attachment = OBDal.getInstance().get(Attachment.class, attachmentId);

      if (attachment == null) {
        throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoAttachmentFound"));
      }

      checkReadableAccess(attachment);

      AttachImplementation handler = getHandler(attachment.getAttachmentConf() == null ? "Default"
          : attachment.getAttachmentConf().getAttachmentMethod().getValue());
      if (handler == null) {
        throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoMethod"));
      }
      File file = handler.downloadFile(attachment);
      FileUtility fileUt = null;
      if (file.exists()) {
        fileUt = new FileUtility(file.getParent(), attachment.getName(), false, true);
      } else {
        throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoAttachmentFound"));
      }

      fileUt.dumpFile(os);
      boolean isTempFile = handler.isTempFile();
      if (isTempFile) {
        fileUt.deleteFile();
      }

    } catch (IOException e) {

      throw new OBException(OBMessageUtils.messageBD("Error downloading file"));
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
   * @param recordIds
   *          All RecordIds from where are downloading the documents
   * @param os
   * @throws OBException
   *           any exception thrown during the download of all documents
   */

  public void dowloadAll(String tabId, String recordIds, OutputStream os) throws OBException {

    try {
      OBContext.setAdminMode(true);
      Tab tab = OBDal.getInstance().get(Tab.class, tabId);
      String tableId = (String) DalUtil.getId(tab.getTable());
      final ZipOutputStream dest = new ZipOutputStream(os);
      HashMap<String, Integer> writtenFiles = new HashMap<String, Integer>();
      OBCriteria<Attachment> attachmentFiles = OBDao.getFilteredCriteria(Attachment.class,
          Restrictions.eq("table.id", tableId), Restrictions.in("record", recordIds.split(",")));
      attachmentFiles.setFilterOnReadableOrganization(false);
      for (Attachment attachmentFile : attachmentFiles.list()) {
        checkReadableAccess(attachmentFile);
        AttachImplementation handler = getHandler(attachmentFile.getAttachmentConf()
            .getAttachmentMethod() == null ? "Default" : attachmentFile.getAttachmentConf()
            .getAttachmentMethod().getValue());
        if (handler == null) {
          throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoMethod"));
        }
        File file = handler.downloadFile(attachmentFile);
        if (!file.exists()) {
          throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoAttachmentFound") + " :"
              + file.getName());
        }
        String zipName = "";
        if (!writtenFiles.containsKey(file.getName())) {
          zipName = file.getName();
          writtenFiles.put(file.getName(), 0);
        } else {
          int num = writtenFiles.get(file.getName()) + 1;
          int indDot = file.getName().lastIndexOf(".");
          if (indDot == -1) {
            // file has no extension
            indDot = attachmentFile.getName().length();
          }
          zipName = attachmentFile.getName().substring(0, indDot) + " (" + num + ")"
              + attachmentFile.getName().substring(indDot);
          writtenFiles.put(attachmentFile.getName(), num);
        }
        byte[] buf = new byte[1024];
        dest.putNextEntry(new ZipEntry(zipName));

        FileInputStream in = new FileInputStream(file.toString());
        int len;
        while ((len = in.read(buf)) > 0) {
          dest.write(buf, 0, len);
        }
        dest.closeEntry();
        in.close();
        boolean isTempFile = handler.isTempFile();
        if (isTempFile) {
          file.delete();
        }
      }
      dest.close();

    } catch (IOException e) {
      throw new OBException(OBMessageUtils.messageBD("OBUIAPP_ErrorWiththeFile"));
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  /**
   * Method to delete files. This method calls needed handler class
   * 
   * @param attachment
   *          the attachment that will be removed
   * @throws OBException
   *           any exception thrown when deleting an attachment
   */
  public void delete(Attachment attachment) throws OBException {
    try {
      OBContext.setAdminMode(true);
      checkReadableAccess(attachment);
      AttachImplementation handler = getHandler(attachment.getAttachmentConf()
          .getAttachmentMethod() == null ? "Default" : attachment.getAttachmentConf()
          .getAttachmentMethod().getValue());
      if (handler == null) {
        throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoMethod"));
      }
      handler.deleteFile(attachment);
      OBDal.getInstance().remove(attachment);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public AttachmentConfig getAttachmenConfig(Client client) {
    OBCriteria<AttachmentConfig> obc = OBDal.getInstance().createCriteria(AttachmentConfig.class);
    obc.add(Restrictions.eq(AttachmentConfig.PROPERTY_CLIENT, client));
    obc.setMaxResults(1);
    if (obc.uniqueResult() != null) {
      return ((AttachmentConfig) obc.uniqueResult());
    }
    OBCriteria<AttachmentMethod> am = OBDal.getInstance().createCriteria(AttachmentMethod.class);
    am.add(Restrictions.eq(AttachmentMethod.PROPERTY_VALUE, "Default"));
    am.setMaxResults(1);
    if (am.uniqueResult() != null) {
      return (AttachmentConfig) am.uniqueResult();
    } else {
      throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoMethod"));
    }
  }

  /**
   * It gets the sequence number for the attachment
   * 
   * @param table
   *          the table of the attachment
   * @param recordId
   *          the recordId of the attachment
   * @return returns the sequence number.
   */
  private Long getSequenceNumber(Table table, String recordId) {
    OBCriteria<Attachment> obc = OBDal.getInstance().createCriteria(Attachment.class);
    obc.add(Restrictions.eq(Attachment.PROPERTY_RECORD, recordId));
    obc.add(Restrictions.eq(Attachment.PROPERTY_TABLE, table));
    obc.addOrderBy(Attachment.PROPERTY_SEQUENCENUMBER, false);
    obc.setFilterOnReadableOrganization(false);
    obc.setMaxResults(1);
    if (obc.uniqueResult() != null) {
      Attachment attach = (Attachment) obc.uniqueResult();
      return attach.getSequenceNumber() + 10L;
    } else {
      return 10L;
    }
  }

  /**
   * Gets the attachment for given parameters.
   * 
   * @param table
   *          the table where the attachment is done
   * @param recordId
   *          The record ID where the attachment is done
   * @param fileName
   *          The name of the attachment
   * @return If exists, the attachment is returned. Else, null is returned
   */
  private Attachment getAttachment(Table table, String recordId, String fileName) {
    OBCriteria<Attachment> obc = OBDal.getInstance().createCriteria(Attachment.class);
    obc.add(Restrictions.eq(Attachment.PROPERTY_RECORD, recordId));
    obc.add(Restrictions.eq(Attachment.PROPERTY_NAME, fileName));
    obc.add(Restrictions.eq(Attachment.PROPERTY_TABLE, table));
    obc.setFilterOnReadableOrganization(false);
    obc.setMaxResults(1);
    return (Attachment) obc.uniqueResult();
  }

  /**
   * It gets the class that must be used, depending on the given attachMethod.
   * 
   * @param strAttachMethod
   *          attachmentMethod, that is the qualifier of the class.
   * @return Class needed which extends from AttachImplementation
   */
  private AttachImplementation getHandler(String strAttachMethod) {
    AttachImplementation handler = null;
    for (AttachImplementation nextHandler : attachImplementationHandlers
        .select(new ComponentProvider.Selector(strAttachMethod))) {
      if (handler == null) {
        handler = nextHandler;
      } else {
        throw new OBException(OBMessageUtils.messageBD("MoreThanOneImplementation"));
      }
    }
    return handler;
  }

  /**
   * Checks if the user has readable access to the record where the file is attached
   * 
   * @param attachment
   *          attachment to check access.
   */
  private void checkReadableAccess(Attachment attachment) {
    Entity entity = ModelProvider.getInstance().getEntityByTableId(attachment.getTable().getId());
    if (entity != null) {
      Object object = OBDal.getInstance().get(entity.getMappingClass(), attachment.getRecord());
      if (object instanceof OrganizationEnabled) {
        SecurityChecker.getInstance().checkReadableAccess((OrganizationEnabled) object);
      }
    }
  }

  /**
   * Save metadata in OBUIAPP_Parameter_Value records. It also updates the description of the
   * attachment based on the new metadata values.
   * 
   * @param requestParams
   *          Map with all the request parameters including the new values of the metadata as
   *          Strings.
   * @param attachment
   *          attachment for which is saving metadata.
   * @param tabId
   *          The tab id where the attachment is being done.
   * @param strKey
   *          The record id owner of the attachment.
   * @param attachMethod
   * @return Map of parameters with typified values
   * @throws OBException
   *           any exception thrown while saving metadata
   */
  private Map<String, Object> saveMetadata(Map<String, String> requestParams,
      Attachment attachment, String tabId, String strKey, AttachmentMethod attachMethod)
      throws OBException {
    try {
      String attachmentText = "";
      boolean isfirst = true;
      final String delimiter = OBMessageUtils.messageBD("OBUIAPP_Attach_Description_Delimiter");
      final String paramDesc = OBMessageUtils.messageBD("OBUIAPP_Attach_Description");

      Map<String, Object> metadataValues = new HashMap<String, Object>();
      for (Parameter parameter : AttachmentUtils.getMethodMetadataParameters(attachMethod, OBDal
          .getInstance().get(Tab.class, tabId))) {
        final String strMetadataId = parameter.getId();

        ParameterValue metadataStoredValue = null;
        final OBCriteria<ParameterValue> critStoredMetadata = OBDal.getInstance().createCriteria(
            ParameterValue.class);
        critStoredMetadata.add(Restrictions.eq(ParameterValue.PROPERTY_FILE, attachment));
        critStoredMetadata.add(Restrictions.eq(ParameterValue.PROPERTY_PARAMETER, parameter));
        critStoredMetadata.setMaxResults(1);
        metadataStoredValue = (ParameterValue) critStoredMetadata.uniqueResult();
        if (metadataStoredValue == null) {
          metadataStoredValue = OBProvider.getInstance().get(ParameterValue.class);
          metadataStoredValue.setFile(attachment);
          metadataStoredValue.setParameter(parameter);
        }

        Object value = "";
        // Load the value. If the parameter is fixed calculate it, if not retrieve from the request
        // parameters.
        if (parameter.isFixed()) {
          if (parameter.getPropertyPath() != null) {
            // not relevant value
            value = AttachmentUtils.getPropertyPathValue(parameter, tabId, strKey);
          } else if (parameter.isEvaluateFixedValue()) {
            value = ParameterUtils.getParameterFixedValue(requestParams, parameter);
          } else {
            value = parameter.getFixedValue();
          }
        } else {
          value = requestParams.get(strMetadataId);
        }

        String strValue = "";
        if (value == null) {
          // There is no value for this parameter. Reset all values and continue with next metadata.
          metadataValues.put(strMetadataId, null);
          metadataStoredValue.setValueDate(null);
          metadataStoredValue.setValueKey(null);
          metadataStoredValue.setValueNumber(null);
          metadataStoredValue.setValueString(null);
        } else {
          String strReferenceId = (String) DalUtil.getId(parameter.getReference());
          if (REFERENCE_LIST.equals(strReferenceId)) {
            strValue = (String) value;
            Reference reference = parameter.getReferenceSearchKey();
            for (List currentList : reference.getADListList()) {
              // TODO: Check if the compare must be done against the search key
              if (currentList.getName().equals(strValue)) {
                metadataStoredValue.setValueKey(currentList.getId());
                metadataStoredValue.setValueString(currentList.getName());
                JSONObject jsonValue = new JSONObject();
                jsonValue.put("id", currentList.getId());
                jsonValue.put("name", currentList.getName());
                metadataValues.put(strMetadataId, jsonValue);
                break;
              }
            }
          } else if (REFERENCE_SELECTOR_REFERENCE.equals(strReferenceId)) {
            strValue = (String) value;
            Reference reference = parameter.getReferenceSearchKey();
            Selector selector = reference.getADSelectorList().get(0);
            BaseOBObject object = OBDal.getInstance().get(selector.getTable().getEntityName(),
                strValue);
            metadataStoredValue.setValueKey(object.getId().toString());
            metadataStoredValue.setValueString(object.getIdentifier());
            JSONObject jsonValue = new JSONObject();
            jsonValue.put("id", object.getId().toString());
            jsonValue.put("name", object.getIdentifier());
            metadataValues.put(strMetadataId, jsonValue);
          } else {
            JSONObject jsonValue = new JSONObject();
            if (value instanceof Date) {
              strValue = JsonUtils.createDateFormat().format((Date) value);
            } else if (value instanceof BigDecimal) {
              strValue = ((BigDecimal) value).toPlainString();
              // TODO: Check what happens with boolean parameters.
            } else {
              strValue = value.toString();
            }
            jsonValue.put("value", strValue);
            ParameterUtils.setParameterValue(metadataStoredValue, jsonValue);
            metadataValues
                .put(strMetadataId, ParameterUtils.getParameterValue(metadataStoredValue));
          }
        }
        if (parameter.isShowInDescription()) {
          if (isfirst) {
            isfirst = false;
          } else {
            attachmentText += delimiter;
          }
          Map<String, String> paramValues = new HashMap<String, String>();
          paramValues.put("paramName", (String) parameter.get(Parameter.PROPERTY_NAME, OBContext
              .getOBContext().getLanguage()));
          paramValues.put("paramValue", strValue);
          attachmentText += OBMessageUtils.parseTranslation(paramDesc, paramValues);
        }
        OBDal.getInstance().save(metadataStoredValue);
      }
      if (attachmentText.length() > 2000) {
        attachmentText = attachmentText.substring(0, 1997) + "...";
      }
      attachment.setText(attachmentText.trim());

      return metadataValues;
    } catch (Exception e) {
      log.error("Error saving the metadata value." + e.getMessage(), e);
      throw new OBException(OBMessageUtils.messageBD("OBUIAPP_ErrorInsertMetadata"), e);
    }
  }
}
