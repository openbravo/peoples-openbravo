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
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.FileUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.application.ParameterValue;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.erpCommon.businessUtility.TabAttachments;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.utility.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@ComponentProvider.Qualifier(CoreAttachImplementation.DEFAULT)
public class CoreAttachImplementation extends AttachImplementation {
  private Logger log = LoggerFactory.getLogger(CoreAttachImplementation.class);

  public static final String DEFAULT = "Default";

  // final public String DEFAULT = "Default";

  @Override
  public void uploadFile(Attachment attachment, String strDataType, Map<String, String> parameters,
      File file, String strTab, List<ParameterValue> parameterValues) throws OBException {
    log.debug("CoreAttachImplemententation - Uploading files");
    String tableId = attachment.getTable().getId();
    String strKey = attachment.getRecord();
    String strFileDir = TabAttachments.getAttachmentDirectoryForNewAttachments(tableId, strKey);

    // FIXME: Get the directory separator from Java runtime
    String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    String strName = "";
    File uploadedFile = null;
    strName = file.getName();
    uploadedFile = new File(attachmentFolder + "/" + strFileDir);
    log.debug("Destination file before renaming: " + uploadedFile);
    try {
      FileUtils.moveFileToDirectory(file, uploadedFile, true);
    } catch (IOException e) {
      log.error("Error moving the file to: " + uploadedFile, e);
      throw new OBException(OBMessageUtils.messageBD("UnreachableDestination") + " "
          + e.getMessage(), e);
    }

    attachment.setPath(TabAttachments.getPath(strFileDir));
    attachment.setDataType(strDataType);
  }

  @Override
  public File downloadFile(Attachment attachment) throws OBException {
    String fileDir = null;
    log.debug("CoreAttachImplemententation - download file");
    fileDir = TabAttachments.getAttachmentDirectory(attachment.getTable().getId(),
        attachment.getRecord(), attachment.getName());
    // FIXME: Get the directory separator from Java runtime
    String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    final File file = new File(attachmentFolder + "/" + fileDir, attachment.getName());
    return file;
  }

  @Override
  public void deleteFile(Attachment attachment) throws OBException {
    log.debug("CoreAttachImplemententation - Removing files");
    String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    String fileDir = TabAttachments.getAttachmentDirectory(attachment.getTable().getId(),
        attachment.getRecord(), attachment.getName());
    String fileDirPath = attachmentFolder + "/" + fileDir;
    final File file = new File(fileDirPath, attachment.getName());
    if (file.exists()) {
      file.delete();
    } else {
      log.warn("No file was removed as file could not be found");
    }
  }

  @Override
  public void updateFile(Attachment attachment, String strTab, Map<String, String> parameters,
      List<ParameterValue> parameterValues) throws OBException {
    log.debug("CoreAttachImplemententation - Updating files");
  }

  @Override
  public boolean isTempFile() {
    return false;
  }
}