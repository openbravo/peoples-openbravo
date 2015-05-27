package org.openbravo.client.application.window;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.Parameter;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.model.ad.utility.AttachmentMethod;

public class AttachmentUtils {
  private static Map<String, String> clientConfigs = new HashMap<String, String>();
  public static final String DEFAULT_METHOD = "Default";
  public static final String DEFAULT_METHOD_ID = "D7B1319FC2B340799283BBF8E838DF9F";

  public static AttachmentConfig getAttachmentConfig(Client client) {
    String strAttachmentConfigId = clientConfigs.get(DalUtil.getId(client));
    if (strAttachmentConfigId == null) {
      // Only one active AttachmentConfig is allowed per client.
      OBCriteria<AttachmentConfig> critAttConf = OBDal.getInstance().createCriteria(
          AttachmentConfig.class);
      critAttConf.add(Restrictions.eq(AttachmentConfig.PROPERTY_CLIENT, client));
      if (!OBDal.getInstance().isActiveFilterEnabled()) {
        critAttConf.setFilterOnActive(true);
      }
      critAttConf.setMaxResults(1);
      AttachmentConfig attConf = (AttachmentConfig) critAttConf.uniqueResult();
      if (attConf != null) {
        clientConfigs.put((String) DalUtil.getId(client), attConf.getId());
      }
      return attConf;
    }
    return OBDal.getInstance().get(AttachmentConfig.class, strAttachmentConfigId);
  }

  public static AttachmentConfig getAttachmentConfig() {
    Client client = OBContext.getOBContext().getCurrentClient();
    return getAttachmentConfig(client);
  }

  public static AttachmentMethod getDefaultAttachmentMethod() {
    AttachmentMethod attMethod = OBDal.getInstance().get(AttachmentMethod.class, DEFAULT_METHOD_ID);
    if (attMethod != null) {
      return attMethod;
    } else {
      throw new OBException(OBMessageUtils.messageBD("OBUIAPP_NoMethod"));
    }
  }

  public static List<Parameter> getMethodMetadataParameters(AttachmentMethod attachMethod, Tab tab) {
    StringBuilder where = new StringBuilder();
    where.append(Parameter.PROPERTY_ATTACHMENTMETHOD + "= :attMethod");
    where.append(" and (" + Parameter.PROPERTY_TAB + " is null or " + Parameter.PROPERTY_TAB
        + " = :tab)");
    final OBQuery<Parameter> qryParams = OBDal.getInstance().createQuery(Parameter.class,
        where.toString());
    qryParams.setNamedParameter("attMethod", attachMethod);
    qryParams.setNamedParameter("tab", tab);
    return qryParams.list();
  }

  public static List<JSONObject> getTabAttachmentsForRows(Tab tab, String[] recordIds) {
    String tableId = (String) DalUtil.getId(tab.getTable());
    OBCriteria<Attachment> attachmentFiles = OBDao.getFilteredCriteria(Attachment.class,
        Restrictions.eq("table.id", tableId), Restrictions.in("record", recordIds));
    attachmentFiles.addOrderBy("creationDate", false);
    List<JSONObject> attachments = new ArrayList<JSONObject>();
    // do not filter by the attachment's organization
    // if the user has access to the record where the file its attached, it has access to all its
    // attachments
    attachmentFiles.setFilterOnReadableOrganization(false);
    for (Attachment attachment : attachmentFiles.list()) {
      JSONObject attachmentobj = new JSONObject();
      try {
        attachmentobj.put("id", attachment.getId());
        attachmentobj.put("name", attachment.getName());
        attachmentobj.put("age", (new Date().getTime() - attachment.getUpdated().getTime()));
        attachmentobj.put("updatedby", attachment.getUpdatedBy().getName());
        attachmentobj.put("description", attachment.getText());
        String attachmentMethod = DEFAULT_METHOD_ID;
        if (attachment.getAttachmentConf() != null) {
          attachmentMethod = (String) DalUtil.getId(attachment.getAttachmentConf()
              .getAttachmentMethod());
        }
        attachmentobj.put("attmethod", attachmentMethod);
      } catch (Exception e) {
        throw new OBException("Error while reading attachments:", e);
      }
      attachments.add(attachmentobj);
    }
    return attachments;
  }
}
