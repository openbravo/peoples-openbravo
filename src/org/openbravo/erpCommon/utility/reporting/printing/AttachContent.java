/*************************************************************************
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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************/
package org.openbravo.erpCommon.utility.reporting.printing;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;

public class AttachContent implements FieldProvider {

    static Logger log4j = Logger.getLogger(AttachContent.class);
    public String fileName;
    public FileItem fileItem;
    public String id;
    public String visible;

    public String getVisible() {
        return visible;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FileItem getFileItem() {
        return fileItem;
    }

    public void setFileItem(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getField(String fieldName) {
        if (fieldName.equalsIgnoreCase("FILENAME"))
            return fileName;
        else if (fieldName.equalsIgnoreCase("ID")) {
            return id;
        } else if (fieldName.equalsIgnoreCase("VISIBLE")) {
            return visible;
        } else {
            log4j.debug("Field does not exist: " + fieldName);
            return null;
        }
    }

}
