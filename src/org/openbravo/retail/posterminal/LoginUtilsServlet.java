package org.openbravo.retail.posterminal;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonUtils;

public class LoginUtilsServlet extends WebServiceAbstractServlet {

  private static final Logger log = Logger.getLogger(LoginUtilsServlet.class);

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    OBContext.setOBContext("0", "0", "0", "0");

    final String terminalName = request.getParameter("terminalName");
    final String command = request.getParameter("command");

    JSONObject result = new JSONObject();
    JSONObject resp = new JSONObject();
    JSONArray data = new JSONArray();
    JSONObject item = new JSONObject();
    try {
      if (command.equals("companyLogo")) {
        int queryCount = 1;

        // Get the organization of the current terminal
        final String hqlOrg = "select terminal.organization.id, terminal.organization.client.id "
            + "from OBPOS_Applications terminal "
            + "where terminal.searchKey = :theTerminalSearchKey";
        Query qryOrg = OBDal.getInstance().getSession().createQuery(hqlOrg);
        qryOrg.setParameter("theTerminalSearchKey", terminalName);
        qryOrg.setMaxResults(1);

        String strClient = "0";
        if (qryOrg.uniqueResult() != null) {
          final Object[] orgResult = (Object[]) qryOrg.uniqueResult();
          strClient = orgResult[1].toString();
        }

        String hqlCompanyImage = "select image.mimetype, image.bindaryData "
            + "from ADImage image, ClientInformation clientInfo "
            + "where clientInfo.obposCompanyLoginImage = image.id and clientInfo.client.id = :theClientId";
        Query qryCompanyImage = OBDal.getInstance().getSession().createQuery(hqlCompanyImage);
        qryCompanyImage.setParameter("theClientId", strClient);
        String companyImageData = "../../utility/ShowImageLogo?logo=yourcompanylogin";
        for (Object qryCompanyImageObject : qryCompanyImage.list()) {
          final Object[] qryCompanyImageObjectItem = (Object[]) qryCompanyImageObject;
          companyImageData = "data:"
              + qryCompanyImageObjectItem[0].toString()
              + ";base64,"
              + org.apache.commons.codec.binary.Base64
                  .encodeBase64String((byte[]) qryCompanyImageObjectItem[1]);
        }

        item.put("logoUrl", companyImageData);
        data.put(item);

        resp.put("startRow", 0);
        resp.put("endRow", (queryCount == 0 ? 0 : queryCount - 1));
        resp.put("totalRows", queryCount);
        resp.put("data", data);

        result.append("response", resp);
        writeResult(response, result.toString());
      } else if (command.equals("userImages")) {

        // Get the organization of the current terminal
        final String hqlOrg = "select terminal.organization.id, terminal.organization.client.id "
            + "from OBPOS_Applications terminal "
            + "where terminal.searchKey = :theTerminalSearchKey";
        Query qryOrg = OBDal.getInstance().getSession().createQuery(hqlOrg);
        qryOrg.setParameter("theTerminalSearchKey", terminalName);
        qryOrg.setMaxResults(1);

        String strOrg = "0";
        String strClient = "0";
        if (qryOrg.uniqueResult() != null) {
          final Object[] orgResult = (Object[]) qryOrg.uniqueResult();
          strOrg = orgResult[0].toString();
          strClient = orgResult[1].toString();
        }

        Set<String> orgNaturalTree = OBContext.getOBContext()
            .getOrganizationStructureProvider(strClient).getNaturalTree(strOrg);

        // Get the user name and uesrname list with the following criteria
        // * Belongs to a "Role" with anything inside "POS Access"
        // * Is in the same natural organization tree than the current "POS Terminal"
        final String hqlUser = "select distinct user.name, user.username, user.id "
            + "from ADUser user, ADUserRoles userRoles, ADRole role, "
            + "OBPOS_POS_Access posAccess "
            + "where user.username != '' and user.active = true and "
            + "userRoles.role.organization.id in :orgList and "
            + "user.id = userRoles.userContact.id and " + "userRoles.role.id = role.id and "
            + "userRoles.role.id = posAccess.role.id " + "order by user.name";
        Query qryUser = OBDal.getInstance().getSession().createQuery(hqlUser);
        qryUser.setParameterList("orgList", orgNaturalTree);
        int queryCount = 0;

        for (Object qryUserObject : qryUser.list()) {
          queryCount++;
          final Object[] qryUserObjectItem = (Object[]) qryUserObject;

          item.put("name", qryUserObjectItem[0]);
          item.put("userName", qryUserObjectItem[1]);

          // Get the image for the current user
          String hqlImage = "select image.mimetype, image.bindaryData "
              + "from ADImage image, ADUser user "
              + "where user.obposImage = image.id and user.id = :theUserId";
          Query qryImage = OBDal.getInstance().getSession().createQuery(hqlImage);
          qryImage.setParameter("theUserId", qryUserObjectItem[2].toString());
          String imageData = "none";
          for (Object qryImageObject : qryImage.list()) {
            final Object[] qryImageObjectItem = (Object[]) qryImageObject;
            imageData = "data:"
                + qryImageObjectItem[0].toString()
                + ";base64,"
                + org.apache.commons.codec.binary.Base64
                    .encodeBase64String((byte[]) qryImageObjectItem[1]);
          }
          item.put("image", imageData);

          // Get the session status for the current user
          String hqlSession = "select distinct session.username, session.sessionActive "
              + "from ADSession session "
              + "where session.username = :theUsername and session.sessionActive = 'Y' and "
              + "session.loginStatus = 'S'";
          Query qrySession = OBDal.getInstance().getSession().createQuery(hqlSession);
          qrySession.setParameter("theUsername", qryUserObjectItem[1].toString());
          qrySession.setMaxResults(1);
          String sessionData = "false";
          if (qrySession.uniqueResult() != null) {
            sessionData = "true";
          }
          item.put("connected", sessionData);

          data.put(item);
        }

        resp.put("startRow", 0);
        resp.put("endRow", (queryCount == 0 ? 0 : queryCount - 1));
        resp.put("totalRows", queryCount);
        resp.put("data", data);
        result.append("response", resp);
        writeResult(response, result.toString());
      }
    } catch (JSONException e) {
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}