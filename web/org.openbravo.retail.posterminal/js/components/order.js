/*global Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.OrderView = Backbone.View.extend({
    tag: 'div',
    contentView: [{
      id: 'tableview',
      view: OB.UI.TableView.extend({
        style: 'edit',
        renderEmpty: OB.COMP.RenderEmpty.extend({
          label: OB.I18N.getLabel('OBPOS_ReceiptNew')
        }),
        renderLine: OB.COMP.RenderOrderLine
      })
    }, {
      tag: 'ul',
      attributes: {
        'class': 'unstyled'
      },
      content: [

      {
        tag: 'li',
        content: [{
          tag: 'div',
          attributes: {
            style: 'position: relative; padding: 10px;'
          },
          content: [{
            tag: 'div',
            attributes: {
              style: 'float: left; width: 80%'
            },
            content: [
            OB.I18N.getLabel('OBPOS_ReceiptTotal')]
          }, {
            id: 'totalgross',
            tag: 'div',
            attributes: {
              style: 'float: left; width: 20%; text-align:right; font-weight:bold;'
            }
          }, {
            tag: 'div',
            attributes: {
              style: 'clear: both;'
            }
          }]
        }]
      }, {
        tag: 'li',
        content: [{
          tag: 'div',
          id: 'footer',
          attributes: {
            style: 'padding: 10px; border-top: 1px solid #cccccc; text-align: center; font-weight: bold; font-size: 30px; color: #f8941d'
          }
        }]
      }]
    }],
    
    initialize: function () {

      OB.UTIL.initContentView(this);

      // Set Model
      this.receipt = this.options.modelorder;
      var lines = this.receipt.get('lines');

      this.tableview.registerCollection(lines);
      this.receipt.on('change:gross', this.renderTotal, this);
      this.receipt.on('change:orderType', this.renderFooter, this);
      this.receipt.on('change:generateInvoice', this.renderFooter, this);

      // Initial total display...
      this.renderFooter();
      this.renderTotal();
    },
    
    renderFooter: function () {
      var s = [];
      if (this.receipt.get('orderType') === 1) {
        s.push(OB.I18N.getLabel('OBPOS_ToBeReturned'));
      }
      if (this.receipt.get('generateInvoice')) {
        s.push(OB.I18N.getLabel('OBPOS_ToInvoice'));
      }

      if (s.length > 0) {
        this.footer.text(s.join(' / '));
        this.footer.show();
      } else {
        this.footer.hide();
      }
    },

    renderTotal: function () {
      this.totalgross.text(this.receipt.printTotal());
    }
  });
}());