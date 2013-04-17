package org.openbravo.materialmgmt;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.QueryTimeoutException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.exception.GenericJDBCException;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCharacteristic;
import org.openbravo.model.common.plm.ProductCharacteristicValue;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

public class VariantChDescUpdateProcess implements Process {
  private static final Logger log4j = Logger.getLogger(VariantChDescUpdateProcess.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));

    OBContext.setAdminMode(false);
    try {
      // retrieve standard params
      final String strProductId = (String) bundle.getParams().get("mProductId");
      final String strChValueId = (String) bundle.getParams().get("mChValueId");

      StringBuffer where = new StringBuffer();
      where.append(" as p");
      where.append(" where p." + Product.PROPERTY_PRODUCTCHARACTERISTICLIST + " is not empty");
      if (StringUtils.isNotBlank(strProductId)) {
        where.append(" and p.id = :productId");
      }
      if (StringUtils.isNotBlank(strChValueId)) {
        where.append(" and exists (select 1 from p."
            + Product.PROPERTY_PRODUCTCHARACTERISTICVALUELIST + " as chv");
        where.append("    where chv." + ProductCharacteristicValue.PROPERTY_CHARACTERISTICVALUE
            + ".id = :chvid)");
      }
      OBQuery<Product> productQuery = OBDal.getInstance().createQuery(Product.class,
          where.toString());
      if (StringUtils.isNotBlank(strProductId)) {
        productQuery.setNamedParameter("productId", strProductId);
      }
      if (StringUtils.isNotBlank(strChValueId)) {
        productQuery.setNamedParameter("chvid", strChValueId);
      }
      productQuery.setFetchSize(1000);
      productQuery.setFilterOnReadableOrganization(false);
      productQuery.setFilterOnActive(false);

      ScrollableResults products = productQuery.scroll(ScrollMode.FORWARD_ONLY);
      int i = 0;
      while (products.next()) {
        Product product = (Product) products.get(0);
        String strChDesc = "";
        where = new StringBuffer();
        where.append(" as pch");
        where.append(" where pch." + ProductCharacteristic.PROPERTY_PRODUCT + " = :product");
        where.append(" order by pch." + ProductCharacteristic.PROPERTY_SEQUENCENUMBER);
        OBQuery<ProductCharacteristic> pchQuery = OBDal.getInstance().createQuery(
            ProductCharacteristic.class, where.toString());
        pchQuery.setFilterOnActive(false);
        pchQuery.setFilterOnReadableOrganization(false);
        pchQuery.setNamedParameter("product", product);
        for (ProductCharacteristic pch : pchQuery.list()) {
          if (StringUtils.isNotBlank(strChDesc)) {
            strChDesc += ", ";
          }
          strChDesc += pch.getCharacteristic().getName() + ":";
          where = new StringBuffer();
          where.append(" as pchv");
          where.append(" where pchv." + ProductCharacteristicValue.PROPERTY_CHARACTERISTIC
              + " = :ch");
          where
              .append("   and pchv." + ProductCharacteristicValue.PROPERTY_PRODUCT + " = :product");
          OBQuery<ProductCharacteristicValue> pchvQuery = OBDal.getInstance().createQuery(
              ProductCharacteristicValue.class, where.toString());
          pchvQuery.setFilterOnActive(false);
          pchvQuery.setFilterOnReadableOrganization(false);
          pchvQuery.setNamedParameter("ch", pch.getCharacteristic());
          pchvQuery.setNamedParameter("product", product);
          for (ProductCharacteristicValue pchv : pchvQuery.list()) {
            strChDesc += " " + pchv.getCharacteristicValue().getName();
          }
        }
        product.setCharacteristicDescription(strChDesc);
        OBDal.getInstance().save(product);

        if ((i % 100) == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();
        }
        i++;
      }

      bundle.setResult(msg);

      // Postgres wraps the exception into a GenericJDBCException
    } catch (GenericJDBCException ge) {
      log4j.error("Exception processing variant generation", ge);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(((GenericJDBCException) ge).getSQLException().getMessage());
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
      // Oracle wraps the exception into a QueryTimeoutException
    } catch (QueryTimeoutException qte) {
      log4j.error("Exception processing variant generation", qte);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(((QueryTimeoutException) qte).getSQLException().getMessage().split("\n")[0]);
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    } catch (final Exception e) {
      log4j.error("Exception processing variant generation", e);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    } finally {
      OBContext.restorePreviousMode();
    }

  }
}