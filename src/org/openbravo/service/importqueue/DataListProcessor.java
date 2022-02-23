package org.openbravo.service.importqueue;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.SessionInfo;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

public class DataListProcessor {

  @FunctionalInterface
  public interface ImportEntryRecord {
    public JSONObject save(JSONObject jsonorder) throws Exception;
  }

  public static JSONObject processMessage(JSONObject message, ImportEntryRecord saveRecord)
      throws QueueException, JSONException {

    // try {
    // return saveRecord(record);
    // } catch (Exception e) {
    // throw new QueueException(e.getMessage(), e);
    // }

    OBContext.setAdminMode(false);

    // {
    // "messageId":"D06E6D14FE94540620C1155521EB3E9B",
    // "entrykey":"Order"
    // "csrfToken":"F84D68997AD7450F83B52ED7EB366028",
    // "appName":"WebPOS",
    // "client":"39363B0921BB4293B48383844325E84C",
    // "organization":"D270A5AC50874F8BA67A88EE977F8E3B",
    // "pos":"9104513C2D0741D4850AE8493998A7C8",
    // "terminalName":"VBS-1",
    // "timeout":10000,
    // "parameters":{
    // "terminalTime":"2022-02-21T23:07:10.489Z",
    // "terminalTimeOffset":{
    // "value":-60
    // }
    // },
    // "data":[

    try {

      JSONArray datalist = message.getJSONArray("data");
      for (int i = 0; i < datalist.length(); i++) {
        JSONObject record = datalist.getJSONObject(i);
        String orgId = getOrganizationId(record);
        Organization org = OBDal.getInstance().get(Organization.class, orgId);
        Client client = org.getClient();
        String clientId = client.getId();
        String userId = getUserId(record);
        User user = OBDal.getInstance().get(User.class, userId);
        Role role = user.getDefaultRole();
        String roleId = role.getId();
        initOBContext(userId, roleId, clientId, orgId);

        try {
          saveRecord.save(record);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      OBDal.getInstance().commitAndClose();

      return new JSONObject();
    } finally {
      OBContext.setOBContext((OBContext) null);
      OBContext.restorePreviousMode();
      ensureConnectionRelease();
    }
  }

  private static void ensureConnectionRelease() {
    // bit rough but ensures that the connection is released/closed
    try {
      OBDal.getInstance().rollbackAndClose();
    } catch (Exception ignored) {
    }

    try {
      if (TriggerHandler.getInstance().isDisabled()) {
        TriggerHandler.getInstance().enable();
      }
      OBDal.getInstance().commitAndClose();
    } catch (Exception ignored) {
    }
  }

  private static void initOBContext(String userId, String roleId, String clientId, String orgId) {

    OBContext.setOBContext(userId, roleId, clientId, orgId);
    OBContext context = OBContext.getOBContext();
    context.getEntityAccessChecker(); // forcing access checker initialization
    context.getOrganizationStructureProvider().reInitialize();

    setVariablesSecureApp(context);

    OBDal.getInstance().getSession().clear();

    SessionInfo.setUserId(userId);
    SessionInfo.setProcessType(SessionInfo.IMPORT_ENTRY_PROCESS);
    SessionInfo.setProcessId(SessionInfo.IMPORT_ENTRY_PROCESS);
  }

  private static void setVariablesSecureApp(OBContext obContext) {
    OBContext.setAdminMode(true);
    try {
      final VariablesSecureApp variablesSecureApp = new VariablesSecureApp(
          obContext.getUser().getId(), obContext.getCurrentClient().getId(),
          obContext.getCurrentOrganization().getId(), obContext.getRole().getId(),
          obContext.getLanguage().getLanguage());
      RequestContext.get().setVariableSecureApp(variablesSecureApp);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static String getOrganizationId(final JSONObject jsonRecord)
      throws QueueException, JSONException {
    if (jsonRecord.has("trxOrganization")) {
      return jsonRecord.getString("trxOrganization");
    }
    if (jsonRecord.has("organization")) {
      return jsonRecord.getString("organization");
    }
    throw new QueueException("Cannot find Organization");
  }

  private static String getUserId(final JSONObject jsonRecord)
      throws QueueException, JSONException {
    if (jsonRecord.has("updatedBy") && !"null".equals(jsonRecord.getString("updatedBy"))) {
      return jsonRecord.getString("updatedBy");
    }
    if (jsonRecord.has("createdBy") && !"null".equals(jsonRecord.getString("createdBy"))) {
      return jsonRecord.getString("createdBy");
    }
    if (jsonRecord.has("userId") && !"null".equals(jsonRecord.getString("userId"))) {
      return jsonRecord.getString("userId");
    }
    throw new QueueException("Cannot find User");
  }
}
