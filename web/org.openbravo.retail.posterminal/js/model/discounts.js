OB.Model.Discounts = {
  applyDiscounts: function(receipt, line) {
    var discounts = [], bpId = receipt.get('bp').id, productId;
    console.log('applyDiscounts',receipt);
    if (line) {
      productId = line.get('product').id;
      // check which are the discounts to be applied
      // 2x1 example
      var
      criteria = {
        '_whereClause': this.standardFilter,
        params: [bpId, bpId, bpId, bpId, productId, productId, productId, productId]
      };
      OB.Dal.find(OB.Model.Discount, criteria, function(d) { //OB.Dal.find success
        console.log('ds', d);
        d.forEach(function(disc) {
          discounts.push({
            name: disc.get('name'),
            gross: line.get('priceList')
          });
        });
        receipt.addDiscount(line, discounts);
      }, function() {});


      //--
    } else {
      // TODO: apply discounts for the whole ticket
    }
  },
  
  standardFilter: "WHERE date('now') BETWEEN DATEFROM AND COALESCE(date(DATETO), date('9999-12-31'))"
      +" AND((BPARTNER_SELECTION = 'Y'  "
 	 +" AND NOT EXISTS"
 	 +" (SELECT 1"
 	 +" FROM M_OFFER_BPARTNER"
 	 +" WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID"
 	 +"   AND C_BPARTNER_ID = ?"
 	 +" ))"
 	 +" OR(BPARTNER_SELECTION = 'N'"
 	 +" AND EXISTS"
 	 +" (SELECT 1"
 	 +" FROM M_OFFER_BPARTNER"
 	 +" WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID"
 	 +"   AND C_BPARTNER_ID = ?"
 	 +" )))"
 	 +" AND((BP_GROUP_SELECTION = 'Y'"
 	 +" AND NOT EXISTS"
 	 +" (SELECT 1"
 	 +" FROM C_BPARTNER B,"
 	 +"   M_OFFER_BP_GROUP OB"
 	 +" WHERE OB.M_OFFER_ID = M_OFFER.M_OFFER_ID"
 	 +"   AND B.C_BPARTNER_ID = ?"
 	 +"   AND OB.C_BP_GROUP_ID = B.C_BP_GROUP_ID"
 	 +" ))"
 	 +" OR(BP_GROUP_SELECTION = 'N'"
 	 +" AND EXISTS"
 	 +" (SELECT 1"
 	 +" FROM C_BPARTNER B,"
 	 +"   M_OFFER_BP_GROUP OB"
 	 +" WHERE OB.M_OFFER_ID = M_OFFER.M_OFFER_ID"
 	 +"   AND B.C_BPARTNER_ID = ?"
 	 +"   AND OB.C_BP_GROUP_ID = B.C_BP_GROUP_ID"
 	 +" )))"
 	 +" AND((PRODUCT_SELECTION = 'Y'"
 	 +" AND NOT EXISTS"
 	 +" (SELECT 1"
 	 +" FROM M_OFFER_PRODUCT"
 	 +" WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID"
 	 +"   AND M_PRODUCT_ID = ?"
 	 +" ))"
 	 +" OR(PRODUCT_SELECTION = 'N'"
 	 +" AND EXISTS"
 	 +" (SELECT 1"
 	 +" FROM M_OFFER_PRODUCT"
 	 +" WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID"
 	 +"   AND M_PRODUCT_ID = ?"
 	 +" )))"
 	 +" AND((PROD_CAT_SELECTION = 'Y'"
 	 +" AND NOT EXISTS"
 	 +" (SELECT 1"
 	 +" FROM M_PRODUCT P,"
 	 +"   M_OFFER_PROD_CAT OP"
 	 +" WHERE OP.M_OFFER_ID = M_OFFER.M_OFFER_ID"
 	 +"   AND P.M_PRODUCT_ID = ?"
 	 +"   AND OP.M_PRODUCT_CATEGORY_ID = P.M_PRODUCT_CATEGORY_ID"
 	 +" ))"
 	 +" OR(PROD_CAT_SELECTION = 'N'"
 	 +" AND EXISTS"
 	 +" (SELECT 1"
 	 +" FROM M_PRODUCT P,"
 	 +"   M_OFFER_PROD_CAT OP"
 	 +" WHERE OP.M_OFFER_ID = M_OFFER.M_OFFER_ID"
 	 +"   AND P.M_PRODUCT_ID = ?"
 	 +"   AND OP.M_PRODUCT_CATEGORY_ID = P.M_PRODUCT_CATEGORY_ID"
 	 +" )))"
};



