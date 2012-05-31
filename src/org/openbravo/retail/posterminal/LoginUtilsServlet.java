package org.openbravo.retail.posterminal;

import java.io.IOException;

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

    JSONObject result = new JSONObject();
    JSONObject resp = new JSONObject();
    JSONArray data = new JSONArray();
    JSONObject item;
    try {
      // Get the organization of the current terminal
      final String hqlOrg = "select terminal.organization.id "
          + "from OBPOS_Applications terminal "
          + "where terminal.searchKey = :theTerminalSearchKey";
      Query qryOrg = OBDal.getInstance().getSession().createQuery(hqlOrg);
      qryOrg.setParameter("theTerminalSearchKey", terminalName);
      qryOrg.setMaxResults(1);

      String strOrg = "0";
      if (qryOrg.uniqueResult() != null) {
        strOrg = qryOrg.list().get(0).toString();
      }

      // Set<String> orgChildTree =
      // OBContext.getOBContext().getOrganizationStructureProvider().getChildTree(strOrg, true);

      // Get the user name and uesrname list with the following criteria
      // * Belongs to a "Role" with anything inside "POS Access"
      // * Has a "Business Partner" which is "Employee" and "Sales Representative"
      final String hqlUser = "select distinct user.name, user.username, user.id "
          + "from ADUser user, ADUserRoles userRoles, ADRole role, ADSession session, "
          + "OBPOS_POS_Access posAccess " + "where user.username != '' and user.active = true and "
          + "user.id = userRoles.userContact.id and " + "userRoles.role.id = role.id and "
          + "userRoles.role.id = posAccess.role.id " + "order by user.name";
      Query qryUser = OBDal.getInstance().getSession().createQuery(hqlUser);

      int queryCount = 0;

      for (Object qryUserObject : qryUser.list()) {
        queryCount++;
        final Object[] qryUserObjectItem = (Object[]) qryUserObject;
        item = new JSONObject();
        item.put("name", qryUserObjectItem[0]);
        item.put("userName", qryUserObjectItem[1]);

        // Get the image for the current user
        String hqlImage = "select image.mimetype, image.bindaryData "
            + "from ADImage image, ADUser user "
            + "where user.obposImage = image.id and user.id = :theUserId";
        Query qryImage = OBDal.getInstance().getSession().createQuery(hqlImage);
        qryImage.setParameter("theUserId", qryUserObjectItem[2].toString());
        // qryImage.setParameter("orgList", orgChildTree);
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

    } catch (JSONException e) {
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    } finally {
      OBContext.restorePreviousMode();
    }

    writeResult(response, result.toString());
  }
}