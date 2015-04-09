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
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.common.actionhandler.OrderCreatePOLines;
import org.openbravo.erpCommon.businessUtility.TabAttachments;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.reporting.ReportingException;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.utils.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@ComponentProvider.Qualifier(CoreAttachImplementation.DEFAULT)
public class CoreAttachImplementation extends AttachImplementation {
  private Logger log = LoggerFactory.getLogger(OrderCreatePOLines.class);

  public static final String DEFAULT = "Default";

  // final public String DEFAULT = "Default";

  @Override
  public void uploadFile(Attachment attachment, String strDataType, String description,
      Map<String, Object> parameters, File file, String strTab) {
    log.debug("CoreAttachImplemententation - Uploading files");
    String tableId = attachment.getTable().getId();
    String strKey = attachment.getRecord();
    String strFileDir = TabAttachments.getAttachmentDirectoryForNewAttachments(tableId, strKey);

    try {
      // FIXME: Get the directory separator from Java runtime
      String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("attach.path");
      final File uploadedDir = new File(attachmentFolder + "/" + strFileDir);
      if (!uploadedDir.exists()) {
        uploadedDir.mkdirs();
      }
      String strName = "";
      File uploadedFile = null;
      strName = file.getName();
      uploadedFile = new File(uploadedDir, strName);
      log.debug("Destination file before renaming: " + uploadedFile);
      if (!file.renameTo(uploadedFile)) {
        throw new ReportingException(OBMessageUtils.messageBD("UnreachableDestination")
            + uploadedDir);
      }
      // }

      attachment.setText(description);
      attachment.setPath(TabAttachments.getPath(strFileDir));
      attachment.setDataType(strDataType);

    } catch (final Exception e) {
      throw new OBException("Error while uploading a file", e);
    }

  }

  @Override
  public File downloadFile(Attachment attachment) {
    String fileDir = null;
    log.debug("CoreAttachImplemententation - download file");

    FileUtility f = new FileUtility();
    fileDir = TabAttachments.getAttachmentDirectory(attachment.getTable().getId(),
        attachment.getRecord(), attachment.getName());
    // FIXME: Get the directory separator from Java runtime
    String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    final File file = new File(attachmentFolder + "/" + fileDir, attachment.getName());
    return file;
  }

  @Override
  public void deleteFile(Attachment attachment) {
    log.debug("CoreAttachImplemententation - Removing files");
    String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    String fileDir = TabAttachments.getAttachmentDirectory(attachment.getTable().getId(),
        attachment.getRecord(), attachment.getName());
    String fileDirPath = attachmentFolder + "/" + fileDir;
    final File file = new File(fileDirPath, attachment.getName());
    if (file.exists()) {
      try {
        file.delete();
      } catch (Exception e) {
        log.error("coreAttachImplementation - Problem deleting attachment: " + e);
        throw new OBException("CoreAttachImplemententation - Error while removing file", e);
      }
    } else {
      log.warn("No file was removed as file could not be found");
    }
  }

  @Override
  public void updateFile(Attachment attachment, String strTab, String description,
      Map<String, Object> parameters) {
    log.debug("CoreAttachImplemententation - Updating files");
    try {
      attachment.setText(description);
    } catch (Exception e) {
      log.error("coreAttachImplementation - Problem updating attachment: " + e);
      throw new OBException("Error while updating a file", e);

    }

  }

  @Override
  public boolean isTempFile() {
    return false;
  }
}