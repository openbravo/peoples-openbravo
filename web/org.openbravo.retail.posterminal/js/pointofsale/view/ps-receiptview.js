/*global OB, Backbone, enyo */
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ReceiptView',
  components: [{
    classes: 'span6',
    components: [{
      attributes: {
        style: 'overflow:auto; margin: 5px'
      },
      components: [{
        attributes: {
          style: 'position: relative;background-color: #ffffff; color: black;'
        },
        components: [{
          kind: 'OB.UI.ReceiptsCounter'
        }, {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components: [{
              attributes: {
                style: 'padding: 5px 0px 10px 0px; border-bottom: 1px solid #cccccc;'
              },
              components: [{
                kind: 'OB.UI.OrderDetails',
              }, {
                kind: 'OB.UI.BusinessPartner'
              }, {
                attributes: {
                  style: 'clear:both;'
                }
              }]
            }]
          }, {
            clases: 'row-fluid',
            attributes: {
              style: 'max-height: 536px; overflow: auto;'
            },
            components: [{
              clases: 'span12',
              components: [{
                kind: 'OB.UI.OrderView'
              }]
            }]
          }]
        }]
      }]
    }]
  }]
});