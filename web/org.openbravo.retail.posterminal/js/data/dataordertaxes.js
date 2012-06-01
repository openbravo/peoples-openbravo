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
         sql = 'select * from c_tax where c_taxcategory_id = ? and c_bp_taxcategory_id ' + (bpTaxCategory === null ? ' is null ' : ' = ? ') + ' order by idx';

     db.readTransaction(function (tx) {
       _.each(lines.models, function (element, index, list) {
         var product = element.get('product'),
             params = [product.get('product').taxCategory];

         if(bpTaxCategory !== null) {
           params.push(bpTaxCategory);
         }

         tx.executeSql(sql, params, function (tr, result) {
           var taxRate, rate, taxAmt, netPrice;

           if(result.rows.length < 1) {
             window.console.error('No applicable tax found for product: ' + product.get('product').id);
             return;
           }
           taxRate = result.rows.item(0);
           rate = taxRate.rate;
           taxAmt = OB.DEC.mul(OB.DEC.div(rate, 100), element.get('price'));
           netPrice = OB.DEC.sub(element.get('price'), taxAmt);
           element.set('taxId', taxRate.c_tax_id);
           element.set('net', OB.DEC.mul(netPrice, element.get('qty')));
           element.set('netPrice', netPrice);
         }, function(tr, err) {
           window.console.error(arguments);
         });
       });
     }, function () {}, function () {
       r.trigger('closed');
       modelorderlist.deleteCurrent();
     });
    }, this);    
  };
});