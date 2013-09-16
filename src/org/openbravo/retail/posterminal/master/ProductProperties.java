package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(Product.productPropertyExtension)
public class ProductProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("product.id", "id"));
        add(new HQLProperty("product.searchKey", "searchkey"));
        add(new HQLProperty("product.name", "_identifier"));
        add(new HQLProperty("product.taxCategory.id", "taxCategory"));
        add(new HQLProperty("product.productCategory.id", "productCategory"));
        add(new HQLProperty("product.obposScale", "obposScale"));
        add(new HQLProperty("product.uOM.id", "uOM"));
        add(new HQLProperty("product.uOM.symbol", "uOMsymbol"));
        add(new HQLProperty("product.uPCEAN", "uPCEAN"));
        add(new HQLProperty("img.bindaryData", "img"));
        add(new HQLProperty("product.description", "description"));
        add(new HQLProperty("product.obposGroupedproduct", "groupProduct"));
        add(new HQLProperty("product.stocked", "stocked"));
        add(new HQLProperty("product.obposShowstock", "showstock"));
        add(new HQLProperty("product.isGeneric", "isGeneric"));
        add(new HQLProperty("product.genericProduct.id", "generic_product_id"));
        add(new HQLProperty("product.brand.id", "brand"));
        add(new HQLProperty("product.characteristicDescription", "characteristicDescription"));
        add(new HQLProperty("product.obposShowChDesc", "showchdesc"));
        add(new HQLProperty("pli.bestseller", "bestseller"));
        add(new HQLProperty("'false'", "ispack"));
        add(new HQLProperty("ppp.listPrice", "listPrice"));
        add(new HQLProperty("ppp.standardPrice", "standardPrice"));
        add(new HQLProperty("ppp.priceLimit", "priceLimit"));
        add(new HQLProperty("ppp.cost", "cost"));
      }
    };
    return list;
  }

}
