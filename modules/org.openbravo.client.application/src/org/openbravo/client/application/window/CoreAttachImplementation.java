package org.openbravo.client.application.window;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.openbravo.base.ConfigParameters;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.TabAttachments;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.reporting.ReportingException;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.utils.FileUtility;

@ComponentProvider.Qualifier("Default")
public class CoreAttachImplementation extends AttachImplementation {
  private Logger log = Logger.getLogger(CoreAttachImplementation.class);

  @Override
  public void uploadFile(FileItem fileItem, Attachment attachment, String strDataType,
      String description, Map<String, Object> parameters, File file) {

    String tableId = attachment.getTable().getId();
    String key = attachment.getRecord();
    String fileDir = null;

    if (log.isDebugEnabled()) {
      log.debug("CoreAttachImplemententation - Uploading files");
    }
    fileDir = TabAttachments.getAttachmentDirectoryForNewAttachments(tableId, key);
    try {
      // FIXME: Get the directory separator from Java runtime
      ServletContext scontext = RequestContext.getServletContext();
      ConfigParameters globalParameters = ConfigParameters.retrieveFrom(scontext);
      final File uploadedDir = new File(globalParameters.strFTPDirectory + "/" + fileDir);
      if (!uploadedDir.exists()) {
        uploadedDir.mkdirs();
      }
      String strName = "";
      File uploadedFile = null;
      if (file == null) {
        strName = fileItem.getName();
        // FIXME: Get the directory separator from Java runtime
        int i = strName.lastIndexOf("\\");
        if (i != -1) {
          strName = strName.substring(i + 1);
          // FIXME: Get the directory separator from Java runtime
        } else if ((i = strName.lastIndexOf("/")) != -1) {
          strName = strName.substring(i + 1);
        }
        uploadedFile = new File(uploadedDir, strName);
        fileItem.write(uploadedFile);
      } else { // when fileItem == null
        strName = file.getName();
        uploadedFile = new File(uploadedDir, strName);
        log.debug("Destination file before renaming: " + uploadedFile);
        if (!file.renameTo(uploadedFile))
          throw new ReportingException(String.format(OBMessageUtils
              .messageBD("UnreachableDestination")) + uploadedDir);
      }

      attachment.setText(description);
      attachment.setPath(TabAttachments.getPath(fileDir));
      attachment.setDataType(strDataType);

    } catch (final Exception e) {
      throw new OBException(e.getMessage());
    }

  }

  @Override
  public void downloadFile(Attachment attachment) {
    HttpServletResponse response = RequestContext.get().getResponse();
    HttpServletRequest request = RequestContext.get().getRequest();
    String fileDir = null;

    if (log.isDebugEnabled()) {
      log.debug("CoreAttachImplemententation - download file");
    }

    FileUtility f = new FileUtility();
    fileDir = TabAttachments.getAttachmentDirectory(attachment.getTable().getId(),
        attachment.getRecord(), attachment.getName());
    // FIXME: Get the directory separator from Java runtime
    ServletContext scontext = RequestContext.getServletContext();
    ConfigParameters globalParameters = ConfigParameters.retrieveFrom(scontext);
    final File file = new File(globalParameters.strFTPDirectory + "/" + fileDir,
        attachment.getName());
    try {
      if (file.exists())
        f = new FileUtility(globalParameters.strFTPDirectory + "/" + fileDir, attachment.getName(),
            false, true);
      else
        f = new FileUtility(globalParameters.strFTPDirectory, attachment.getId(), false, true);

      if (attachment.getDataType().equals(""))
        response.setContentType("application/txt");
      else
        response.setContentType(attachment.getDataType());
      response.setCharacterEncoding("UTF-8");
      String userAgent = request.getHeader("user-agent");
      if (userAgent.contains("MSIE")) {
        response.setHeader(
            "Content-Disposition",
            "attachment; filename=\""
                + URLEncoder.encode(attachment.getName().replace("\"", "\\\""), "utf-8") + "\"");
      } else {
        response.setHeader(
            "Content-Disposition",
            "attachment; filename=\""
                + MimeUtility.encodeWord(attachment.getName().replace("\"", "\\\""), "utf-8", "Q")
                + "\"");
      }

      f.dumpFile(response.getOutputStream());
      response.getOutputStream().flush();
      response.getOutputStream().close();
    } catch (final Exception ex) {
      throw new OBException(ex.getMessage());// ServletException(ex);
    }
  }

  @Override
  public void downloadAll(Attachment attachmentFile, HashMap<String, Integer> writtenFiles,
      ZipOutputStream dest) {

    if (log.isDebugEnabled()) {
      log.debug("CoreAttachImplemententation - downloadAll records");
    }
    try {
      ServletContext scontext = RequestContext.getServletContext();
      ConfigParameters globalParameters = ConfigParameters.retrieveFrom(scontext);
      String attachmentDirectory = TabAttachments.getAttachmentDirectory(attachmentFile.getTable()
          .getId(), attachmentFile.getRecord(), attachmentFile.getName());
      final File file = new File(globalParameters.strFTPDirectory + "/" + attachmentDirectory,
          attachmentFile.getName());
      String zipName = "";
      if (!writtenFiles.containsKey(file.getName())) {
        zipName = file.getName();
        writtenFiles.put(file.getName(), new Integer(0));
      } else {
        int num = writtenFiles.get(file.getName()) + 1;
        int indDot = file.getName().lastIndexOf(".");
        if (indDot == -1) {
          // file has no extension
          indDot = file.getName().length();
        }
        zipName = file.getName().substring(0, indDot) + " (" + num + ")"
            + file.getName().substring(indDot);
        writtenFiles.put(file.getName(), new Integer(num));
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
      throw new OBException(e.getMessage());
    }
  }

  @Override
  public void deleteFile(Attachment attachment) {
    if (log.isDebugEnabled()) {
      log.debug("CoreAttachImplemententation - Removing files");
    }
    String attachmentFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    String fileDir = TabAttachments.getAttachmentDirectory(attachment.getTable().getId(),
        attachment.getRecord(), attachment.getName());
    String fileDirPath = attachmentFolder + "/" + fileDir;
    FileUtility f = new FileUtility();
    final File file = new File(fileDirPath, attachment.getName());
    if (file.exists()) {
      try {
        f = new FileUtility(fileDirPath, attachment.getName(), false);
        f.deleteFile();
      } catch (Exception e) {
        throw new OBException("//Error while removing file", e);
      }

    } else {
      log.warn("No file was removed as file could not be found");
    }
  }

  @Override
  public void updateFile(Attachment attachment, String strTab, String description,
      Map<String, Object> parameters) {
    OBContext.setAdminMode(true);

    if (log.isDebugEnabled()) {
      log.debug("CoreAttachImplemententation - Updating files");
    }
    try {
      attachment.setText(description);
      OBDal.getInstance().save(attachment);
      OBDal.getInstance().flush();
      // OBDal.getInstance().getConnection().commit();

    } catch (Exception e) {
      log.debug("coreAttachImplementation - Problem updating attachment: " + e.getMessage());
      // OBDal.getInstance().rollbackAndClose();
      throw new OBException("Error while updating file", e);

    } finally {
      OBContext.restorePreviousMode();
    }

  }
}
