/*global B,_ */

(function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.OrderTaxes = function (context) {
    this._id = 'logicOrderTaxes';

    this.receipt = context.modelorder;

    this.receipt.calculateTaxes = function (callback) {
      var me = this,
          bpTaxCategory = this.get('bp').get('taxCategory'),
          lines = this.get('lines'),
          len = lines.length,
          taxes = {},
          totalnet = OB.DEC.Zero,
          queue = {},
          triggerNext = false,
          gross = OB.DEC.Zero;

      _.each(lines.models, function (element, index, list) {
        var product = element.get('product');

        // OB.Dal.find(model, criteria, success, error);
        OB.Dal.find(OB.Model.TaxRate, {
          taxCategory: product.get('taxCategory'),
          businessPartnerTaxCategory: bpTaxCategory
        }, function (coll) { // success
          var taxRate, rate, taxAmt, net, pricenet, amount, taxId;

          if (coll.length === 0) {
            throw 'No tax rate found';
          }

          taxRate = coll.at(0);
          taxId = taxRate.get('id');

          rate = new BigDecimal(String(taxRate.get('rate')));
          rate = rate.divide(new BigDecimal('100'), 20, BigDecimal.prototype.ROUND_UNNECESSARY);

          pricenet = OB.DEC.div(element.get('price'), rate.add(new BigDecimal('1')));

          net =  OB.DEC.mul(pricenet, element.get('qty'));
          amount = OB.DEC.sub(element.get('gross'), net);
          gross = element.get('gross');

          element.set('tax', taxId);
          element.set('taxAmount', amount);
          element.set('net', net);
          element.set('pricenet', pricenet);

          totalnet = OB.DEC.add(totalnet, net);

          if (taxes[taxId]) {
            taxes[taxId].net = OB.DEC.add(taxes[taxId].net, net);
            taxes[taxId].amount = OB.DEC.add(taxes[taxId].amount, amount);
            taxes[taxId].gross = OB.DEC.add(taxes[taxId].gross, gross);
          } else {
            taxes[taxId] = {};
            taxes[taxId].name = taxRate.get('name');
            taxes[taxId].rate = taxRate.get('rate');
            taxes[taxId].net = net;
            taxes[taxId].amount = amount;
            taxes[taxId].gross = gross;
          }

          // processed = yes
          queue[element.cid] = true;

          // checking queue status
          triggerNext = OB.UTIL.queueStatus(queue);

          // triggering next steps
          if (triggerNext) {
            me.set('taxes', taxes);
            me.set('net', totalnet);
            if (callback) {
              callback();
            }
          }
        }, function () { // error
          window.console.error(arguments);
        });

        // add line to queue of pending to be processed
        queue[element.cid] = false;
      });
    };
  };
}());