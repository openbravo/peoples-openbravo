/*global OB, Backbone, enyo */
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ReceiptView',
  classes: 'span6',
  published: {
    order: null,
  },
  components: [{
    style: 'overflow:auto; margin: 5px',
    components: [{
      style: 'position: relative;background-color: #ffffff; color: black;',
      components: [{
        kind: 'OB.UI.ReceiptsCounter'
      }, {
        style: 'padding: 5px;',
        components: [{
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components: [{
              style: 'padding: 5px 0px 10px 0px; border-bottom: 1px solid #cccccc;',
              components: [{
                kind: 'OB.UI.OrderDetails',
              }, {
                kind: 'OB.UI.BusinessPartner'
              }, {
                style: 'clear:both;'
              }]
            }]
          }]
        }, {
          classes: 'row-fluid',
          style: 'max-height: 536px; overflow: auto;',
          components: [{
            classes: 'span12',
            components: [{
              kind: 'OB.UI.OrderView',
              name: 'orderview'
            }]
          }]
        }]
      }]
    }]
  }],
  orderChanged: function(oldValue) {
    this.$.orderview.setOrder(this.order);
  }
});