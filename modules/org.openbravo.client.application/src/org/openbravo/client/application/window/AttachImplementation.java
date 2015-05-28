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
import java.util.List;
import java.util.Map;

import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.ParameterValue;
import org.openbravo.model.ad.utility.Attachment;

/**
 * Public class to allow extend the functionality
 */

// @RequestScoped
public abstract class AttachImplementation {

  /**
   * Abstract method to upload files
   * 
   * @param attachment
   *          The attachment created in c_file with empty metadata
   * @param strDataType
   *          DataType of the attachment
   * @param parameters
   *          A map with the metadata and its values to be updated in the corresponding file
   *          management system and in the attachment
   * @param file
   *          The file to be uploaded
   * @param strTab
   *          The tabID where the file is attached
   */
  public abstract void uploadFile(Attachment attachment, String strDataType,
      Map<String, String> parameters, File file, String strTab, List<ParameterValue> parameterValues)
      throws OBException;

  /**
   * Abstract method to download a single file
   * 
   * @param attachment
   *          The attachment that will be downloaded
   */
  public abstract File downloadFile(Attachment attachment) throws OBException;

  /**
   * Abstract method to delete a file
   * 
   * @param attachment
   *          The attachment that want to be removed
   */
  public abstract void deleteFile(Attachment attachment) throws OBException;

  /**
   * Abstract method to update file's metadata
   * 
   * @param attachment
   *          The attachment to be modified
   * @param strTab
   *          The tabID where the file was attached
   * @param parameters
   *          The metadata to be modified
   */
  public abstract void updateFile(Attachment attachment, String strTab,
      Map<String, String> parameters, List<ParameterValue> parameterValues) throws OBException;

  /**
   * This method is used to know whether the attach method is creating a temporary file in the temp
   * directory of Openbravo server when downloading a file. If it is true, the process will remove
   * the temporary file. If it s false, the process will not remove the file
   * 
   * @return true if the attachment method creates a temporary file in Openbravo server. False if it
   *         does not create a temporary file
   */
  public abstract boolean isTempFile();

}