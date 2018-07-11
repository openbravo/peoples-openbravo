package org.openbravo.retail.posterminal;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.mobile.core.servercontroller.MobileServerController;
import org.openbravo.mobile.core.servercontroller.MobileServerRequestExecutor;
import org.openbravo.mobile.core.servercontroller.MobileServerUtils;
import org.openbravo.model.common.plm.Product;

public class AssociateOrderLines extends ProcessHQLQuery {
  public static final Logger log = Logger.getLogger(AssociateOrderLines.class);
  public static final String AssociateOrderLinesPropertyExtension = "AssociateOrderLinesPropertyExtension";

  @Inject
  @Any
  @Qualifier(AssociateOrderLinesPropertyExtension)
  private Instance<ModelExtension> propextension;

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    Map<String, Object> params = new HashMap<String, Object>();
    String orgId = jsonsent.getString("organization");
    params.put("orgId", orgId);
    Map<String, Object> paramValues = getFilters(jsonsent);
    Iterator<?> it = paramValues.entrySet().iterator();
    while (it.hasNext()) {
      @SuppressWarnings("rawtypes")
      Map.Entry entry = (Map.Entry) it.next();
      String column = (String) entry.getKey();
      Object value = entry.getValue();
      if ("orderDate".equals(column)) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        params.put("orderDate", dateFormat.format(value));
      } else if ("documentNo".equals(column)) {
        params.put("documentNo", value);
      } else if ("businessPartner".equals(column)) {
        params.put("businessPartner", value);
      } else if ("orderId".equals(column)) {
        params.put("orderId", value);
      }
    }
    return params;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    HQLPropertyList queryHQLProperties = ModelExtensionUtils.getPropertyExtensions(propextension);

    Map<String, Object> paramValues = getFilters(jsonsent);

    String excluded = jsonsent.getString("excluded");
    JSONArray remoteFilters = jsonsent.getJSONArray("remoteFilters");
    JSONObject productFilter = remoteFilters.getJSONObject(0);

    String hqlPendingLines = "select " //
        + queryHQLProperties.getHqlSelect() + " from OrderLine as ol " //
        + "join ol.product as p " //
        + "join ol.salesOrder as salesOrder " //
        + "join salesOrder.businessPartner as bp "; //
    hqlPendingLines += " where p.productType = 'I'"
        + " and ol.organization.id = :orgId and ol.orderedQuantity > 0" //
        + " and salesOrder.obposApplications is not null " //
        + " and salesOrder.obposIsDeleted = false " //
        + " and ol.id not in (" + excluded + ")"; //

    Product product = OBDal.getInstance().get(Product.class, productFilter.getString("value"));
    if ("N".equals(product.getIncludedProductCategories())) {
      hqlPendingLines += " and exists";
      hqlPendingLines += "(from ServiceProductCategory as spc where spc.productCategory=p.productCategory and spc.product = '"
          + product.getId() + "')";
    } else if ("Y".equals(product.getIncludedProductCategories())) {
      hqlPendingLines += " and not exists";
      hqlPendingLines += " (from ServiceProductCategory as spc where spc.productCategory=p.productCategory and spc.product = '"
          + product.getId() + "')";
    }

    if ("N".equals(product.getIncludedProducts())) {
      hqlPendingLines += " and exists ";
      hqlPendingLines += "(from ServiceProduct as spc where spc.relatedProduct=p.id and spc.product = '"
          + product.getId() + "')";
    } else if ("Y".equals(product.getIncludedProducts())) {
      hqlPendingLines += " and not exists ";
      hqlPendingLines += "(from ServiceProduct as spc where spc.relatedProduct=p.id and spc.product = '"
          + product.getId() + "')";
    }

    Iterator<?> it = paramValues.entrySet().iterator();
    while (it.hasNext()) {
      @SuppressWarnings("rawtypes")
      Map.Entry entry = (Map.Entry) it.next();
      String column = (String) entry.getKey();
      if ("orderDate".equals(column)) {
        hqlPendingLines += " and salesOrder.orderDate = to_date(:orderDate, 'YYYY/MM/DD')";
      } else if ("documentNo".equals(column)) {
        hqlPendingLines += " and upper(salesOrder.documentNo) like :documentNo";
      } else if ("businessPartner".equals(column)) {
        hqlPendingLines += " and bp.id = :businessPartner";
      } else if ("orderId".equals(column)) {
        hqlPendingLines += " and salesOrder.id = :orderId";
      }
    }
    if (jsonsent.has("orderby") && !jsonsent.isNull("orderby")) {
      JSONObject orderby = jsonsent.getJSONObject("orderby");
      if (orderby != null) {
        String column = orderby.getString("name");
        String fullColumn = "";
        if ("orderDate".equals(column)) {
          fullColumn = "salesOrder.orderDate";
        } else if ("documentNo".equals(column)) {
          fullColumn = "salesOrder.documentNo";
        } else if ("businessPartner".equals(column)) {
          fullColumn = "bp.name";
        } else if ("lineNo".equals(column)) {
          fullColumn = "ol.lineNo";
        }
        if (!"".equals(fullColumn)) {
          hqlPendingLines += " order by " + fullColumn + " " + orderby.getString("direction");
        }
      }
    }
    return Arrays.asList(new String[] { hqlPendingLines });
  }

  private Map<String, Object> getFilters(JSONObject jsonsent) throws JSONException {
    JSONArray filters = jsonsent.getJSONArray("remoteFilters");
    Map<String, Object> paramValues = new HashMap<String, Object>();
    if (filters.length() > 0) {
      for (int i = 0; i < filters.length(); i++) {
        JSONObject flt = filters.getJSONObject(i);
        String operator = flt.getString("operator");
        String column = flt.getString("column");
        String value = flt.getString("value");
        if (!"".equals(value.trim())) {
          if ("orderDate".equals(column) || "deliveryDate".equals(column)) {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
              Date date = new Date(dateTimeFormat.parse(value).getTime());
              paramValues.put(column, date);
            } catch (ParseException e) {
              log.error("Error parsing Date", e);
            }
          } else {
            paramValues.put(column,
                operator.equalsIgnoreCase("contains") ? "%" + value.toUpperCase() + "%" : value);
          }
        }
      }
    }
    return paramValues;
  }

  @Override
  public void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException {
    try {
      // Get originServer parameter
      JSONObject params = jsonsent.getJSONObject("parameters");
      String originServer = params.optString("originServer");
      if (MobileServerController.getInstance().isThisAStoreServer()
          && "Central".equals(originServer)) {
        try {
          final JSONObject result = MobileServerRequestExecutor.getInstance().executeCentralRequest(
              MobileServerUtils.OBWSPATH + AssociateOrderLines.class.getName(), jsonsent);
          w.write(result.toString().substring(1, result.toString().length() - 1));
        } catch (Exception e) {
        }
      } else {
        super.exec(w, jsonsent);
      }
    } catch (JSONException e) {
      throw new OBException(e.getMessage());
    }
  }

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  protected boolean mustHaveRemoteFilters() {
    return true;
  }

}
