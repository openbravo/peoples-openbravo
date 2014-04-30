package org.openbravo.advpaymentmngt.datasource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.SQLQuery;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.common.datasource.ReturnFromCustomerPickEditLineRow;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.datasource.DefaultDataSourceService;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

public class GLItemDatasource extends DefaultDataSourceService {

  private static final String AD_TABLE_ID = "864A35C8FCD548B0AD1D69C89BBA6118";

  @Override
  public Entity getEntity() {
    return ModelProvider.getInstance().getEntityByTableId(AD_TABLE_ID);
  }

  @Override
  public String fetch(Map<String, String> parameters) {
    int startRow = 0;
    final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
    if (startRowStr != null) {
      startRow = Integer.parseInt(startRowStr);
    }

    final List<JSONObject> jsonObjects = new ArrayList<JSONObject>();

    final JSONObject jsonResult = new JSONObject();
    final JSONObject jsonResponse = new JSONObject();
    try {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_STARTROW, startRow);
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, jsonObjects.size() + startRow - 1);
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 0);
      jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(jsonObjects));
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
    } catch (JSONException e) {
    }

    return jsonResult.toString();
  }

  @Override
  public String add(Map<String, String> parameters, String content) {
    return content;

  }

  private int getCount(Map<String, String> parameters) {
    SQLQuery qry = null;
    if (parameters.get(JsonConstants.DISTINCT_PARAMETER) != null) {
      qry = OBDal.getInstance().getSession().createSQLQuery(getCountProductQuery(parameters));
    } else {
      qry = OBDal.getInstance().getSession().createSQLQuery(getSQLCountQuery(parameters));
    }
    OBDal.getInstance().getSession().createSQLQuery(getSQLCountQuery(parameters));
    BigInteger count = (BigInteger) qry.uniqueResult();
    return count.intValue();
  }

  private List<JSONObject> fetchJSONObject(Map<String, String> parameters) {
    final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
    final String endRowStr = parameters.get(JsonConstants.ENDROW_PARAMETER);
    int startRow = -1;
    int endRow = -1;
    if (startRowStr != null) {
      startRow = Integer.parseInt(startRowStr);
    }
    if (endRowStr != null) {
      endRow = Integer.parseInt(endRowStr);
    }
    final List<Map<String, Object>> data = getData(parameters, startRow, endRow);
    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);
    toJsonConverter.setAdditionalProperties(JsonUtils.getAdditionalProperties(parameters));
    return toJsonConverter.convertToJsonObjects(data);
  }

  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

    int offset = startRow;
    int nRows = endRow - startRow + 1;

    String fetchType = null;
    SQLQuery qry = null;
    if (parameters.get(JsonConstants.DISTINCT_PARAMETER) != null) {
      fetchType = (String) parameters.get(JsonConstants.DISTINCT_PARAMETER);
      qry = OBDal.getInstance().getSession()
          .createSQLQuery(getDistinctProductQuery(parameters, offset, nRows));
    } else {
      fetchType = "grid";
      qry = OBDal.getInstance().getSession().createSQLQuery(getSQLQuery(parameters, offset, nRows));
    }

    for (Object o : qry.list()) {
      Map<String, Object> row = createRow(o, fetchType);
      result.add(row);
    }

    return result;
  }

  private Map<String, Object> createRow(Object o, String fetchType) {
    Object[] values = (Object[]) o;
    Map<String, Object> map = null;
    if ("grid".equals(fetchType)) {
      ReturnFromCustomerPickEditLineRow line = new ReturnFromCustomerPickEditLineRow(values);
      map = line.toMap();

    } else if ("product".equals(fetchType)) {
      DistinctItem line = new DistinctItem("Product", values);
      map = line.toMap();
    }
    return map;
  }

  private String getSQLCountQuery(Map<String, String> parameters) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT count(*) ");
    queryBuilder.append(getSQLMainBody(parameters));
    return queryBuilder.toString();
  }

  private String getSQLQuery(Map<String, String> parameters, int offset, int nRows) {
    String sortClause = getSortClause(parameters);
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder
        .append("SELECT COALESCE(il.m_inoutline_id, rol.c_orderline_id) AS c_rm_order_pick_edit_lines_id,");
    queryBuilder
        .append("       COALESCE(rol.ad_client_id,il.ad_client_id) AS ad_client_id, COALESCE(rol.ad_org_id, il.ad_org_id) AS ad_org_id,");
    queryBuilder
        .append("       COALESCE(rol.ad_client_identifier,ic.name) AS ad_client_identifier, COALESCE(rol.ad_org_identifier, io.name) AS ad_org_identifier,");
    queryBuilder
        .append("       COALESCE(rol.isactive, il.isactive) AS isactive, COALESCE(rol.createdby, il.createdby) AS createdby,");
    queryBuilder
        .append("       COALESCE(rol.updatedby, il.updatedby) AS updatedby, COALESCE(rol.created, il.created) AS created,");
    queryBuilder.append("       COALESCE(rol.updated, il.updated) AS updated,");
    queryBuilder.append("       CASE WHEN rol.c_orderline_id IS NOT NULL THEN 'Y' ");
    queryBuilder.append("            ELSE 'N' END AS ob_selected,");
    queryBuilder
        .append("       il.m_inoutline_id, rol.c_order_id AS returnorderid, i.documentno AS inoutno,");
    queryBuilder
        .append("       i.movementdate, COALESCE(rol.m_product_id, il.m_product_id) AS m_product_id,");
    queryBuilder
        .append("       COALESCE(rol.m_product_identifier,ip.name) AS m_product_identifier,");
    queryBuilder
        .append("       COALESCE(rol.m_attributesetinstance_id, il.m_attributesetinstance_id) AS m_attributesetinstance_id,");
    queryBuilder
        .append("       COALESCE(rol.m_attributesetinstance_identifier, iat.description) AS m_attributesetinstance_identifier,");
    queryBuilder
        .append("       il.movementqty, COALESCE(rol.c_uom_id, il.c_uom_id) AS c_uom_id, COALESCE(rol.c_uom_identifier,iuom.name) AS c_uom_identifier,");
    queryBuilder.append("       (-1) * rol.qtyordered AS returned,");
    queryBuilder.append("       CASE WHEN rol.c_orderline_id IS NOT NULL THEN ");
    queryBuilder.append("                 CASE rol.istaxincluded WHEN");
    queryBuilder.append("                   'N' THEN rol.priceactual ");
    queryBuilder.append("                   ELSE rol.gross_unit_price END ");
    queryBuilder.append("            ELSE CASE pl.istaxincluded WHEN ");
    queryBuilder.append("                   'N' THEN ol.priceactual");
    queryBuilder.append("                   ELSE ol.gross_unit_price END ");
    queryBuilder.append("            END AS priceactual_,");
    queryBuilder
        .append("       rol.c_return_reason_id, rol.c_return_reason_identifier, o.documentno AS orderno,");
    queryBuilder
        .append("       (-1) * retol.returnedqty AS returnedqty, COALESCE(rol.c_bpartner_id, i.c_bpartner_id) AS c_bpartner_id,");
    queryBuilder
        .append("       COALESCE(rol.c_tax_id, ol.c_tax_id) AS c_tax_id, pl.istaxincluded, rol.c_orderline_id");
    queryBuilder.append(getSQLMainBody(parameters));
    if (sortClause != null && !sortClause.isEmpty()) {
      queryBuilder.append(sortClause);
    }
    queryBuilder
        .append(" OFFSET " + Integer.toString(offset) + " LIMIT " + Integer.toString(nRows));
    return queryBuilder.toString();
  }

  private String getCountProductQuery(Map<String, String> parameters) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT count(distinct COALESCE(rol.m_product_id,il.m_product_id))");
    queryBuilder.append(getSQLMainBody(parameters));
    return queryBuilder.toString();
  }

  private String getDistinctProductQuery(Map<String, String> parameters, int offset, int nRows) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder
        .append("SELECT distinct COALESCE(rol.m_product_id,il.m_product_id) AS m_product_id,");
    queryBuilder
        .append("       COALESCE(rol.m_product_identifier,ip.name) AS m_product_identifier");
    queryBuilder.append(getSQLMainBody(parameters));
    queryBuilder.append(" ORDER BY COALESCE(rol.m_product_identifier, ip.name) ");
    queryBuilder
        .append(" OFFSET " + Integer.toString(offset) + " LIMIT " + Integer.toString(nRows));
    return queryBuilder.toString();
  }

  public String getSQLMainBody(Map<String, String> parameters) {
    String businessPartnerId = parameters.get("@Order.businessPartner@");
    String orderId = parameters.get("@Order.id@");
    String sqlWhereClause = parameters.get("_sqlWhere");
    String clientId = OBContext.getOBContext().getCurrentClient().getId();

    String orgWhereClause = getOrgWhereClause(parameters);
    String filterClause = getFilterClause(parameters);
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append(" FROM m_inoutline il");
    queryBuilder.append(" JOIN ad_client ic ON il.ad_client_id = ic.ad_client_id");
    queryBuilder.append(" JOIN ad_org io ON il.ad_org_id= io.ad_org_id");
    queryBuilder
        .append(" LEFT JOIN m_attributesetinstance iat ON il.m_attributesetinstance_id = iat.m_attributesetinstance_id ");
    queryBuilder.append(" JOIN m_product ip ON il.m_product_id= ip.m_product_id");
    queryBuilder.append(" JOIN m_inout i ON il.m_inout_id = i.m_inout_id");
    queryBuilder.append("               AND i.processed = 'Y' AND i.docstatus <> 'VO' ");
    queryBuilder.append("               AND i.c_bpartner_id = '" + businessPartnerId + "'");
    queryBuilder.append(" JOIN c_doctype dt ON i.c_doctype_id = dt.c_doctype_id");
    queryBuilder.append("                  AND dt.isreturn = 'N'");
    queryBuilder.append(" LEFT JOIN c_orderline ol ON il.c_orderline_id = ol.c_orderline_id");
    queryBuilder.append(" LEFT JOIN c_order o ON ol.c_order_id = o.c_order_id");
    queryBuilder.append(" LEFT JOIN c_uom iuom ON il.c_uom_id = iuom.c_uom_id");
    queryBuilder.append(" LEFT JOIN m_pricelist pl ON o.m_pricelist_id = pl.m_pricelist_id");
    queryBuilder
        .append(" FULL JOIN ( SELECT rolc.name as ad_client_identifier, rolo.name as ad_org_identifier, rolp.name as m_product_identifier,");
    queryBuilder
        .append("                    ol2.c_orderline_id, ol2.ad_org_id, ol2.isactive, ol2.created,");
    queryBuilder
        .append("                    ol2.createdby, ol2.updated, ol2.updatedby, ol2.c_order_id, ol2.qtyordered,");
    queryBuilder
        .append("                    ol2.priceactual, ol2.c_return_reason_id, ol2.m_inoutline_id, o2.c_bpartner_id,");
    queryBuilder
        .append("                    o2.issotrx, o2.ad_client_id, ol2.m_product_id, ol2.m_attributesetinstance_id,");
    queryBuilder
        .append("                    ol2.c_uom_id, roluom.name as c_uom_identifier, ol2.c_tax_id, pl2.istaxincluded, ol2.gross_unit_price,");
    queryBuilder
        .append("                    rolat.description as m_attributesetinstance_identifier, rolreas.name as c_return_reason_identifier");
    queryBuilder.append("             FROM c_orderline ol2");
    queryBuilder.append("             JOIN c_order o2 ON ol2.c_order_id = o2.c_order_id");
    queryBuilder.append("                             AND o2.processed = 'N' ");
    queryBuilder.append("             JOIN ad_client rolc ON ol2.ad_client_id = rolc.ad_client_id");
    queryBuilder.append("             JOIN ad_org rolo ON ol2.ad_org_id= rolo.ad_org_id");
    queryBuilder.append("             LEFT JOIN c_uom roluom ON ol2.c_uom_id= roluom.c_uom_id");
    queryBuilder
        .append("             LEFT JOIN c_return_reason rolreas ON ol2.c_return_reason_id= rolreas.c_return_reason_id");
    queryBuilder
        .append("             LEFT JOIN m_attributesetinstance rolat ON ol2.m_attributesetinstance_id = rolat.m_attributesetinstance_id");
    queryBuilder.append("             JOIN m_product rolp ON ol2.m_product_id= rolp.m_product_id");
    queryBuilder
        .append("             JOIN m_pricelist pl2 ON pl2.m_pricelist_id = o2.m_pricelist_id");
    queryBuilder.append("                                  AND o2.c_bpartner_id = '"
        + businessPartnerId + "'");
    queryBuilder.append("                                  AND (o2.c_order_id = '" + orderId
        + "' OR o2.c_order_id IS NULL)");
    queryBuilder.append("                                  AND o2.issotrx = 'Y'");
    queryBuilder.append("             ) rol ON rol.m_inoutline_id = il.m_inoutline_id ");
    queryBuilder
        .append(" LEFT JOIN ( SELECT sum(ol3.qtyordered) AS returnedqty, ol3.m_inoutline_id ");
    queryBuilder.append("             FROM c_orderline ol3 ");
    queryBuilder.append("             JOIN c_order o3 ON ol3.c_order_id = o3.c_order_id");
    queryBuilder
        .append("                             AND o3.processed = 'Y' AND o3.docstatus <> 'VO'");
    queryBuilder.append(" WHERE ol3.m_inoutline_id IS NOT NULL ");
    queryBuilder
        .append(" GROUP BY ol3.m_inoutline_id) retol ON retol.m_inoutline_id = il.m_inoutline_id");
    queryBuilder
        .append(" WHERE (COALESCE(retol.returnedqty, 0) + COALESCE(il.movementqty, rol.qtyordered)) <> 0");
    queryBuilder.append(" AND ol.c_order_discount_id IS NULL ");
    queryBuilder.append(" AND COALESCE(rol.ad_client_id,il.ad_client_id) = '" + clientId + "'");
    queryBuilder.append(" AND i.issotrx = 'Y'");
    if (sqlWhereClause != null && !sqlWhereClause.isEmpty() && !"null".equals(sqlWhereClause)) {
      queryBuilder.append(" AND (" + sqlWhereClause.toString() + ")");
    }
    if (filterClause != null && !filterClause.isEmpty() && !"null".equals(filterClause)) {
      queryBuilder.append(filterClause.toString());
    }
    if (orgWhereClause != null && !orgWhereClause.isEmpty() && !"null".equals(orgWhereClause)) {
      queryBuilder.append(orgWhereClause.toString());
    }
    return queryBuilder.toString();
  }

  private String getOrgWhereClause(Map<String, String> parameters) {
    StringBuilder orgWhereCLause = new StringBuilder();
    final String orgId = parameters.get(JsonConstants.ORG_PARAMETER);
    final Set<String> orgs = OBContext.getOBContext().getOrganizationStructureProvider()
        .getNaturalTree(orgId);
    if (orgs.size() > 0) {
      orgWhereCLause.append(" AND COALESCE(rol.ad_org_id, il.ad_org_id) IN (");
      boolean addComma = false;
      for (String org : orgs) {
        if (addComma) {
          orgWhereCLause.append(",");
        }
        orgWhereCLause.append("'" + org + "'");
        addComma = true;
      }
      orgWhereCLause.append(") ");

    }
    return orgWhereCLause.toString();
  }

  private String getFilterClause(Map<String, String> parameters) {
    StringBuilder filterClause = new StringBuilder();
    try {
      JSONArray criteriaArray = (JSONArray) JsonUtils.buildCriteria(parameters).get("criteria");
      for (int i = 0; i < criteriaArray.length(); i++) {
        JSONObject criteria = criteriaArray.getJSONObject(i);
        // Basic advanced criteria handling
        if (criteria.has("_constructor")
            && "AdvancedCriteria".equals(criteria.getString("_constructor"))
            && criteria.has("criteria")) {
          JSONArray innerCriteriaArray = new JSONArray(criteria.getString("criteria"));
          criteria = innerCriteriaArray.getJSONObject(0);
        }
        String fieldName = criteria.getString("fieldName");
        String operatorName = criteria.getString("operator");
        String value = criteria.getString("value");
        if (!fieldName.equals("dummy")) {
          if (fieldName.equals("product$_identifier")) {
            filterClause.append(" AND COALESCE(rol.m_product_identifier,ip.name) ilike '%" + value
                + "%'");
          } else if (fieldName.equals("inOutDocumentNumber")) {
            filterClause.append(" AND i.documentno ilike '%" + value + "%'");
          } else if (fieldName.equals("movementDate")) {
            filterClause.append(buildComplexFilter("i.movementdate", operatorName, "to_date('"
                + value + "', 'yyyy/mm/dd')"));
          } else if (fieldName.equals("movementQuantity")) {
            filterClause.append(buildComplexFilter("il.movementqty", operatorName, value));
          } else if (fieldName.equals("uOM$_identifier")) {
            filterClause.append(" AND c_uom_id ilike '%" + value + "%'");
          } else if (fieldName.equals("attributeSetValue$_identifier")) {
            filterClause
                .append(" AND COALESCE(rol.m_attributesetinstance_identifier, iat.description) ilike '%"
                    + value + "%'");
          }
        }
      }
    } catch (JSONException e) {
    }
    if (filterClause.length() > 0) {
      return " AND (rol.c_orderline_id IS NOT NULL OR " + filterClause.toString().substring(4)
          + ")";
    } else {
      return "";
    }
  }

  private String buildComplexFilter(String fieldName, String operatorName, String value) {
    String operator = null;
    if ("greaterOrEqual".equals(operatorName)) {
      operator = ">=";
    } else if ("greaterThan".equals(operatorName)) {
      operator = ">";
    } else if ("lessOrEqual".equals(operatorName)) {
      operator = "<=";
    } else if ("lessThan".equals(operatorName)) {
      operator = "<";
    } else {
      operator = "=";
    }
    return " AND " + fieldName + operator + value;
  }

  private String getSortClause(Map<String, String> parameters) {
    String sortByField = parameters.get("_sortBy");
    StringBuilder sortClause = new StringBuilder();
    if (sortByField != null && !sortByField.isEmpty()) {
      sortClause.append(" ORDER BY ");
      boolean desc = false;
      if (sortByField.startsWith("-")) {
        desc = true;
        sortByField = sortByField.substring(1);
      }
      if (sortByField.equals("product$_identifier")) {
        sortClause.append(" m_product_identifier ");
      } else if (sortByField.equals("inOutDocumentNumber")) {
        sortClause.append(" i.documentno ");
      } else if (sortByField.equals("movementDate")) {
        sortClause.append(" i.movementdate ");
      } else if (sortByField.equals("movementQuantity")) {
        sortClause.append(" il.movementqty ");
      } else if (sortByField.equals("uOM$_identifier")) {
        sortClause.append(" c_uom_id ");
      } else if (sortByField.equals("attributeSetValue$_identifier")) {
        sortClause.append(" m_attributesetinstance_identifier ");
      } else if (sortByField.equals("orderNo")) {
        sortClause.append(" orderno ");
      } else if (sortByField.equals("unitPrice")) {
        sortClause.append(" priceactual_ ");
      } else if (sortByField.equals("returned")) {
        sortClause.append(" returned ");
      } else if (sortByField.equals("returnQtyOtherRM")) {
        sortClause.append(" returnedqty ");
      }
      if (desc) {
        sortClause.append(" DESC ");
      }
    } else {
      String sqlOrderBy = parameters.get("_sqlOrderBy");
      if (sqlOrderBy != null && !sqlOrderBy.isEmpty()) {
        sortClause.append(" ORDER BY ");
        sortClause.append(sqlOrderBy);
      }
    }
    return sortClause.toString();
  }

  public class DistinctItem {
    private String id;
    private String identifier;
    private String entityName;

    public DistinctItem(String entityName, Object[] values) {
      this.entityName = entityName;
      this.id = (String) values[0];
      this.identifier = (String) values[1];
    }

    public Map<String, Object> toMap() {
      Map<String, Object> row = new LinkedHashMap<String, Object>();
      row.put("$ref", this.entityName + "/" + this.id);
      row.put("_entityName", this.entityName);
      row.put("id", this.id);
      row.put("_identifier", this.identifier);
      return row;
    }
  }
}
