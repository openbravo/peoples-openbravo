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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
  public FileUtility downloadFile(Attachment attachment) {
    String fileDir = null;
    log.debug("CoreAttachImplemententation - download file");

    FileUtility f = new FileUtility();
    fileDir = TabAttachments.getAttachmentDirectory(attachment.getTable().getId(),
        attachment.getRecord(), attachment.getName());
    // FIXME: Get the directory separator from Java runtime
    String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    final File file = new File(attachmentFolder + "/" + fileDir, attachment.getName());
    try {

      if (file.exists()) {
        f = new FileUtility(attachmentFolder + "/" + fileDir, attachment.getName(), false, true);
      } else {
        f = new FileUtility(attachmentFolder, attachment.getId(), false, true);
      }
      return f;
    } catch (IOException e) {
      throw new OBException("Error while downloading an attachment ", e);
    }
  }

  @Override
  public void downloadAll(Attachment attachmentFile, HashMap<String, Integer> writtenFiles,
      ZipOutputStream dest) {
    log.debug("CoreAttachImplemententation - downloadAll records");
    try {
      String attachmentDirectory = TabAttachments.getAttachmentDirectory(attachmentFile.getTable()
          .getId(), attachmentFile.getRecord(), attachmentFile.getName());
      String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("attach.path");
      final File file = new File(attachmentFolder + "/" + attachmentDirectory,
          attachmentFile.getName());
      String zipName = "";
      if (!writtenFiles.containsKey(file.getName())) {
        zipName = file.getName();
        writtenFiles.put(file.getName(), 0);
      } else {
        int num = writtenFiles.get(file.getName()) + 1;
        int indDot = file.getName().lastIndexOf(".");
        if (indDot == -1) {
          // file has no extension
          indDot = file.getName().length();
        }
        zipName = file.getName().substring(0, indDot) + " (" + num + ")"
            + file.getName().substring(indDot);
        writtenFiles.put(file.getName(), num);
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

    } catch (Exception e) {
      log.error("Error while downloading attachments", e);
      throw new OBException("Error while downloading attachments ", e);
    }
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