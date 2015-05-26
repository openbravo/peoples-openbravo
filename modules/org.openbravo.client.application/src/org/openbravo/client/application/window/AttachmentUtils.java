package org.openbravo.client.application.window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.Parameter;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.model.ad.utility.AttachmentMethod;

public class AttachmentUtils {
  private static Map<String, String> clientConfigs = new HashMap<String, String>();
  public final static String DEFAULT_METHOD = "Default";

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
    OBCriteria<AttachmentMethod> critAttMethod = OBDal.getInstance().createCriteria(
        AttachmentMethod.class);
    critAttMethod.add(Restrictions.eq(AttachmentMethod.PROPERTY_VALUE, DEFAULT_METHOD));
    critAttMethod.setMaxResults(1);
    if (critAttMethod.uniqueResult() != null) {
      return (AttachmentMethod) critAttMethod.uniqueResult();
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
}
