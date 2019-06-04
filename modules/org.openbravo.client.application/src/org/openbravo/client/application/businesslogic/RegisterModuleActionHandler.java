package org.openbravo.client.application.businesslogic;

import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.application.process.ResponseActionsBuilder;
import org.openbravo.client.application.process.ResponseActionsBuilder.MessageType;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.module.Module;
import org.openbravo.service.centralrepository.CentralRepository;
import org.openbravo.service.centralrepository.CentralRepository.Service;

public class RegisterModuleActionHandler extends BaseProcessActionHandler {

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject registationInfo = getRegistrationInfo(content);

    // Do not keep connection open while communicating with WS
    OBDal.getInstance().commitAndClose();

    JSONObject crResponse = CentralRepository.post(Service.REGISTER_MODULE, registationInfo);

    try {
      ResponseActionsBuilder rb = getResponseBuilder();
      String msg = OBMessageUtils
          .getI18NMessage(crResponse.getJSONObject("response").getString("msg"));
      if (crResponse.getBoolean("success")) {
        rb.showMsgInProcessView(MessageType.SUCCESS, OBMessageUtils.getI18NMessage("ProcessOK"),
            msg);
        Module module = OBDal.getInstance()
            .get(Module.class, registationInfo.getJSONObject("module").get("id"));
        module.setRegisterModule(true);
      } else {
        rb.showMsgInProcessView(MessageType.ERROR, OBMessageUtils.getI18NMessage("Error"), msg,
            true).retryExecution();
      }

      return rb.build();
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }

  private JSONObject getRegistrationInfo(String content) {
    JSONObject r = new JSONObject();
    try {
      JSONObject req = new JSONObject(content);
      JSONObject params = req.getJSONObject("_params");
      r.put("user", params.getString("user"));
      r.put("password", params.getString("password"));

      Module module = OBDal.getInstance().get(Module.class, req.getString("AD_Module_ID"));
      JSONObject jsonModule = new JSONObject();
      jsonModule.put("moduleID", module.getId());
      jsonModule.put("name", module.getName());
      jsonModule.put("packageName", module.getJavaPackage());
      jsonModule.put("author", module.getAuthor());
      jsonModule.put("type", module.getType());
      jsonModule.put("help", module.getHelpComment());
      if (!module.getModuleDBPrefixList().isEmpty()) {
        jsonModule.put("dbPrefix", module.getModuleDBPrefixList().get(0));
      }
      jsonModule.put("description", module.getDescription());
      r.put("module", jsonModule);
    } catch (JSONException e) {
      throw new OBException(e);
    }
    return r;
  }

}
