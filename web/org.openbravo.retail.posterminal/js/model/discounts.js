OB.Model.Discounts = {
  applyDiscounts: function(receipt, line) {
    var discounts = [];
    if (line) {
      // check which are the discounts to be applied
      // 2x1 example
      var
      criteria = {
        '_whereClause': 'where exists (select 1 from m_offer_product p where m_offer.m_offer_id = p.m_offer_id and m_product_id = ?)',
        params: [line.get('product').id]
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
  }
};