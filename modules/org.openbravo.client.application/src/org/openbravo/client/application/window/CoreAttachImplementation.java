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
import java.io.IOException;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.FileUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.utility.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@ComponentProvider.Qualifier(AttachmentUtils.DEFAULT_METHOD)
public class CoreAttachImplementation extends AttachImplementation {
  private static final Logger log = LoggerFactory.getLogger(CoreAttachImplementation.class);

  @Override
  public void uploadFile(Attachment attachment, String strDataType, Map<String, Object> parameters,
      File file, String strTab) throws OBException {
    log.debug("CoreAttachImplemententation - Uploading files");
    String tableId = attachment.getTable().getId();
    String strKey = attachment.getRecord();
    String strFileDir = getAttachmentDirectoryForNewAttachments(tableId, strKey);

    String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    File uploadedFile = null;
    uploadedFile = new File(attachmentFolder + File.separator + strFileDir);
    log.debug("Destination file before renaming: {}", uploadedFile);
    try {
      FileUtils.moveFileToDirectory(file, uploadedFile, true);
    } catch (IOException e) {
      log.error("Error moving the file to: " + uploadedFile, e);
      throw new OBException(OBMessageUtils.messageBD("UnreachableDestination") + " "
          + e.getMessage(), e);
    }

    attachment.setPath(getPath(strFileDir));
    attachment.setDataType(strDataType);
  }

  @Override
  public File downloadFile(Attachment attachment) {
    log.debug("CoreAttachImplemententation - download file");
    String fileDir = getAttachmentDirectory(attachment.getTable().getId(), attachment.getRecord(),
        attachment.getName());
    String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    final File file = new File(attachmentFolder + File.separator + fileDir, attachment.getName());
    return file;
  }

  @Override
  public void deleteFile(Attachment attachment) {
    log.debug("CoreAttachImplemententation - Removing files");
    String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    String fileDir = getAttachmentDirectory(attachment.getTable().getId(), attachment.getRecord(),
        attachment.getName());
    String fileDirPath = attachmentFolder + "/" + fileDir;
    final File file = new File(fileDirPath, attachment.getName());
    if (file.exists()) {
      file.delete();
    } else {
      log.warn("No file was removed as file could not be found");
    }
  }

  @Override
  public void updateFile(Attachment attachment, String strTab, Map<String, Object> parameters)
      throws OBException {
    log.debug("CoreAttachImplemententation - Updating files");
  }

  @Override
  public boolean isTempFile() {
    return false;
  }

  /**
   * Provides the directory in which the attachment has to be stored. For example for tableId "259",
   * recordId "0F3A10E019754BACA5844387FB37B0D5", the file directory returned is
   * "259/0F3/A10/E01/975/4BA/CA5/844/387/FB3/7B0/D5". In case 'SaveAttachmentsOldWay' preference is
   * enabled then the file directory returned is "259-0F3A10E019754BACA5844387FB37B0D5"
   * 
   * @param tableID
   *          UUID of the table
   * 
   * @param recordID
   *          UUID of the record
   * 
   * @return file directory to save the attachment
   */
  public static String getAttachmentDirectoryForNewAttachments(String tableID, String recordID) {
    String fileDir = tableID + "-" + recordID;
    String saveAttachmentsOldWay = null;
    try {
      saveAttachmentsOldWay = Preferences.getPreferenceValue("SaveAttachmentsOldWay", true,
          OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
              .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
              .getOBContext().getRole(), null);
    } catch (PropertyException e) {
      // if property not found, save attachments the new way
      saveAttachmentsOldWay = "N";
    }

    if ("Y".equals(saveAttachmentsOldWay)) {
      return fileDir;
    } else {
      fileDir = tableID + "/" + splitPath(recordID);
    }
    return fileDir;
  }

  /**
   * Provides the directory in which the attachment is stored. For example for tableId "259",
   * recordId "0F3A10E019754BACA5844387FB37B0D5", and fileName "test.txt" the file directory
   * returned is "259/0F3/A10/E01/975/4BA/CA5/844/387/FB3/7B0/D5". In case 'SaveAttachmentsOldWay'
   * preference is enabled then the file directory returned is
   * "259-0F3A10E019754BACA5844387FB37B0D5"
   * 
   * @param tableID
   *          UUID of the table
   * 
   * @param recordID
   *          UUID of the record
   * 
   * @param fileName
   *          Name of the file
   * 
   * @return file directory in which the attachment is stored
   */
  public static String getAttachmentDirectory(String tableID, String recordID, String fileName) {
    String fileDir = tableID + "-" + recordID;
    Table attachmentTable = null;
    try {
      OBContext.setAdminMode();
      attachmentTable = OBDal.getInstance().get(Table.class, tableID);
      OBCriteria<Attachment> attachmentCriteria = OBDal.getInstance().createCriteria(
          Attachment.class);
      attachmentCriteria.add(Restrictions.eq(Attachment.PROPERTY_RECORD, recordID));
      attachmentCriteria.add(Restrictions.eq(Attachment.PROPERTY_TABLE, attachmentTable));
      attachmentCriteria.add(Restrictions.eq(Attachment.PROPERTY_NAME, fileName));

      attachmentCriteria.setFilterOnReadableOrganization(false);
      if (attachmentCriteria.count() > 0) {
        Attachment attachment = attachmentCriteria.list().get(0);
        if (attachment.getPath() != null) {
          fileDir = attachment.getPath();
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return fileDir;
  }

  /**
   * Provides the value to be saved in path field in c_file. The path field is used to get the
   * location of the attachment. For example 259/0F3/A10/E01/975/4BA/CA5/844/387/FB3/7B0/D5. This
   * path is relative to the attachments folder
   * 
   * @param fileDirectory
   *          the directory that is retrieved from getFileDirectory()
   * 
   * @return value to be saved in path in c_file
   */
  public static String getPath(String fileDirectory) {
    if (fileDirectory != null && fileDirectory.contains("-")) {
      return null;
    } else {
      return fileDirectory;
    }
  }

  /**
   * Splits the path name component so that the resulting path name is 3 characters long sub
   * directories. For example 12345 is split to 123/45
   * 
   * @param origname
   *          Original name
   * @return split name.
   */
  public static String splitPath(final String origname) {
    String newname = "";
    for (int i = 0; i < origname.length(); i += 3) {
      if (i != 0) {
        newname += "/";
      }
      newname += origname.substring(i, Math.min(i + 3, origname.length()));
    }
    return newname;
  }

}