package org.openbravo.retail.posterminal;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.BaseKernelServlet;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Region;
import org.openbravo.retail.posterminal.org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.web.WebServiceUtil;

/**
 * @author iperdomo
 * 
 */
public class DataSourceServlet extends BaseKernelServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(DataSourceServlet.class);
  private static final String servletPath = "org.openbravo.retail.posterminal.datasource";
  private static final Map<String, String> whereOrderByClause = new HashMap<String, String>();

  static {
    whereOrderByClause.put("Product", "order by name");
    whereOrderByClause.put("FinancialMgmtTaxRate", "where active = true and parentTaxRate is null "
        + "and salesPurchaseType in ('S', 'B') "
        + "and (country = :fromCountry or country is null) "
        + "and (region = :fromRegion or region is null) "
        + "and (destinationCountry = :toCountry or destinationCountry is null) "
        + "and (destinationRegion = :toRegion or destinationRegion is null) "
        + "order by validFromDate desc");
    whereOrderByClause.put("OBPOS_App_Payment", "obposApplications.searchKey = :pos");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    if (!request.getRequestURI().contains("/" + servletPath)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Invalid url " + request.getRequestURI());
      return;
    }

    final int nameIndex = request.getRequestURI().indexOf(servletPath);
    final String servicePart = request.getRequestURI().substring(nameIndex);
    final String[] pathParts = WebServiceUtil.getInstance().getSegments(servicePart);

    if (pathParts.length < 2) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing entity name in request url "
          + request.getRequestURI());
      return;
    }

    final String entityName = pathParts[1];

    if (whereOrderByClause.containsKey(entityName)) {
      getData(request, response, entityName);
    } else {
      emptyResult(request, response);
    }
  }

  private void getData(HttpServletRequest request, HttpServletResponse response, String entity)
      throws IOException {

    response.setContentType(JsonConstants.JSON_CONTENT_TYPE);

    int idx = 0;
    final long t1 = System.currentTimeMillis();
    final PrintWriter out = response.getWriter();
    final DataToJsonConverter converter = new DataToJsonConverter();

    // {"response": {"startRow": 0, "endRow": 10, "data": [], "status": 0}}

    out.println("{\"response\": {\"data\": [");
    out.flush();

    final String whereClause = whereOrderByClause.containsKey(entity) ? whereOrderByClause
        .get(entity) : "";

    final OBQuery<BaseOBObject> obq = OBDal.getInstance().createQuery(entity, whereClause);

    if ("FinancialMgmtTaxRate".equals(entity)) {
      // POS info
      final OBQuery<OBPOSApplications> posAppQuery = OBDal.getInstance().createQuery(
          OBPOSApplications.class, "where searchKey = 'POS-1'"); // FIXME: value is not unique
      final OBPOSApplications posDetail = posAppQuery.list().get(0); // FIXME: could throw
                                                                     // IndexOutOfBoundsException

      // FROM
      final OrganizationInformation orgInfo = posDetail.getOrganization()
          .getOrganizationInformationList().get(0); // FIXME: expected org info?
                                                    // IndexOutOfBoundsException?

      final Country fromCountry = orgInfo.getLocationAddress().getCountry();
      final Region fromRegion = orgInfo.getLocationAddress().getRegion();

      // TO
      final Country toCountry = posDetail.getPartnerAddress().getLocationAddress().getCountry();
      final Region toRegion = posDetail.getPartnerAddress().getLocationAddress().getRegion();

      obq.setNamedParameter("fromCountry", fromCountry);
      obq.setNamedParameter("fromRegion", fromRegion);
      obq.setNamedParameter("toCountry", toCountry);
      obq.setNamedParameter("toRegion", toRegion);
    } else if ("OBPOS_App_Payment".equals(entity)) {
      obq.setNamedParameter("pos", "POS-1");
    }

    final ScrollableResults results = obq.scroll(ScrollMode.FORWARD_ONLY);

    try {
      while (results.next()) {
        if (idx > 0) {
          out.print(",");
        }

        final BaseOBObject obj = (BaseOBObject) results.get(0);
        final String jsonObj = converter.toJsonObject(obj, DataResolvingMode.FULL).toString();
        // log.debug(jsonObj);
        out.println(jsonObj);
        idx++;
        if (idx % 100 == 0) {
          out.flush();
          OBDal.getInstance().getSession().clear();
        }
      }
    } finally {
      results.close();
      OBDal.getInstance().getSession().clear(); // always clear the session (eg. result < 100 rows)
    }
    out.print("]");
    out.print(", \"startRow\": 0");
    out.print(", \"endRow\": " + (idx - 1));
    out.print(", \"status\": 0");
    out.print("}}");
    out.close();
    log.debug("total fetch time: " + (System.currentTimeMillis() - t1) + "ms");
  }

  private void emptyResult(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    final PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    try {
      JSONObject result = new JSONObject();
      result.put("data", new JSONArray());
      JSONObject error = new JSONObject();
      error.put("type", "error");
      error.put("message", "Unkonwn entity"); // FIXME: Change to DB message
      result.put("message", error);
      out.print(result.toString());
    } catch (JSONException e) {
      log.error("Error trying to build response: " + e.getMessage(), e); // nothing more to do
    }
    out.close();
  }
}
