/*global define,_ */

define(['utilities', 'arithmetic', 'i18n'], function () {
  
  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.OrderTaxes = function(context) {
    this._id = 'logicOrderTaxes';
      
    this.receipt =  context.modelorder;
    this.taxrate = context.DataTaxRate;
    
    this.receipt.on('taxes', function (modelorderlist) {
     var r = this.receipt,
         bpTaxCategory = r.get('bp').get('taxCategory'),
         lines = r.get('lines'),
         len = lines.length,
         db = OB.DATA.OfflineDB,
         sql = 'select * from c_tax where c_taxcategory_id = ? and c_bp_taxcategory_id ' + (bpTaxCategory === null ? ' is null ' : ' = ? ') + ' order by idx',
         taxes = {};

     db.readTransaction(function (tx) {
       _.each(lines.models, function (element, index, list) {
         var product = element.get('product'),
             params = [product.get('product').taxCategory];

         if(bpTaxCategory !== null) {
           params.push(bpTaxCategory);
         }

         tx.executeSql(sql, params, function (tr, result) {
           var taxRate, rate, taxAmt, net, pricenet, amount, taxId;

           if(result.rows.length < 1) {
             window.console.error('No applicable tax found for product: ' + product.get('product').id);
             return;
           }

           taxRate = result.rows.item(0);
           rate = OB.DEC.div(taxRate.rate, 100);
           pricenet = OB.DEC.div(element.get('price'), OB.DEC.add(1, rate));
           net = OB.DEC.div(element.get('gross'), OB.DEC.add(1, rate));
           amount = OB.DEC.sub(element.get('gross'), net);
           taxId = taxRate.c_tax_id;

           element.set('taxId', taxId);
           element.set('net', net);
           element.set('pricenet', pricenet);

           if(taxes[taxId]) {
             taxes[taxId].net = OB.DEC.add(taxes[taxId].net, net);
             taxes[taxId].amount = OB.DEC.add(taxes[taxId].amount, amount);
           } else {
             taxes[taxId] = {};
             taxes[taxId].name = taxRate.name;
             taxes[taxId].rate = rate;
             taxes[taxId].net = net;
             taxes[taxId].amount = amount;
           }
         }, function(tr, err) {
           window.console.error(arguments);
         });
       });
     }, function () {}, function () {
       r.set('taxes', taxes);
       r.trigger('closed');
       modelorderlist.deleteCurrent();
     });
    }, this);    
  };
});
