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

import javax.enterprise.context.RequestScoped;

import org.apache.commons.fileupload.FileItem;
import org.openbravo.model.ad.utility.Attachment;

/**
 * Public class to allow extend the functionality
 */

@RequestScoped
public abstract class AttachImplementation {

  /**
   * Abstract method to upload files
   * 
   * @param fileItem
   *          FileItem received from 'Add' Attachment functionality. This is parameter is null when
   *          using print & attach functionality
   * @param attachment
   *          The attachment created in c_file. The extra parameters/metadata.... should be updated.
   * @param strDataType
   *          DataType of the attachemnt
   * @param description
   *          The description parameter/metadata
   * @param parameters
   *          A map with the metadata and its values
   * @param file
   *          This is the report in /attachments/tmp created when using 'print and attach'
   *          functionality. This parameter is null when using 'Add' attachment functionality.
   */
  public abstract void uploadFile(FileItem fileItem, Attachment attachment, String strDataType,
      String description, Map<String, Object> parameters, File file);

  /**
   * Abstract method to download a single file
   * 
   * @param attachment
   *          The attachment that will be downloaded
   */
  public abstract void downloadFile(Attachment attachment);

  /**
   * Abstract method to delete a file
   * 
   * @param attachment
   *          The attachment that want to be removed
   */
  public abstract void deleteFile(Attachment attachment);

  /**
   * Abstract method to update file's metadata
   * 
   * @param attachment
   *          The attachment to be modified
   * @param strTab
   *          The tabID where the file was attached
   * @param description
   *          The description (metadata) to be modified
   * @param parameters
   *          More meetadata to be modified
   */
  public abstract void updateFile(Attachment attachment, String strTab, String description,
      Map<String, Object> parameters);

  /**
   * Abstract method to download all the files in a .zip file
   * 
   * @param tabId
   *          The tab Id where the download process is being executed
   * @param recordId
   *          All the attachment related to this recordID will be downloaded in a single .zip file
   */
  public abstract void downloadAll(Attachment attachmentFile,
      HashMap<String, Integer> writtenFiles, ZipOutputStream dest);

}
