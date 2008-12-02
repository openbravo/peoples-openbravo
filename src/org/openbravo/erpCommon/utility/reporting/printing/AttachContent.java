package org.openbravo.erpCommon.utility.reporting.printing;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;

public class AttachContent implements FieldProvider {

    static Logger log4j = Logger.getLogger(AttachContent.class);
    public String fileName;
    public FileItem fileItem;

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
        else {
            log4j.debug("Field does not exist: " + fieldName);
            return null;
        }
    }

}
