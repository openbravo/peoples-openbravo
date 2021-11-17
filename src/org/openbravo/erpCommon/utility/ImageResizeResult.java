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
 * All portions are Copyright (C) 2021 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

/**
 * A POJO used to return the results of an image resize calculation
 * 
 * @see Utility#applyImageResizeAction
 */
public class ImageResizeResult {

  private byte[] imageData;
  private String mimeType;
  private Long[] oldSize;
  private Long[] newSize;
  private boolean sizeActionApplied;

  /**
   * @return the binary data of the resized image
   */
  public byte[] getImageData() {
    return imageData;
  }

  void setImageData(byte[] imageData) {
    this.imageData = imageData;
  }

  /**
   * @return the mime type of the resized image
   */
  public String getMimeType() {
    return mimeType;
  }

  void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * @return the old size of the image, the value at index 0 is the old width, and the value at
   *         index 1 is the old height
   */
  public Long[] getOldSize() {
    return oldSize;
  }

  void setOldSize(Long[] oldSize) {
    this.oldSize = oldSize;
  }

  /**
   * @return the new size of the image, the value at index 0 is the new width, and the value at
   *         index 1 is the new height
   */
  public Long[] getNewSize() {
    return newSize;
  }

  void setNewSize(Long[] newSize) {
    this.newSize = newSize;
  }

  /**
   * @return true if the resize action could be applied. If the resize could not be applied, because
   *         the original image is a vector image, false is returned
   */
  public boolean isSizeActionApplied() {
    return sizeActionApplied;
  }

  void setSizeActionApplied(boolean sizeActionApplied) {
    this.sizeActionApplied = sizeActionApplied;
  }

}
