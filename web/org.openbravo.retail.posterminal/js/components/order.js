/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

//  OB = window.OB || {};
//  OB.COMP = window.OB.COMP || {};

  enyo.kind({
    name: 'btninvoice',
    style: 'float: left; width: 50%;',
    components: [{
      kind: 'OB.UI.SmallButton',
      tag: 'button',
      style: 'width: 50px;',
      classes: 'btnlink-white btnlink-payment-clear btn-icon-small btn-icon-check',
      tap: function(){
        console.log('invoice button click');
      }
    },{
      tag: 'span',
      content: ' '
    },{
      tag: 'span',
      style: 'font-weight:bold; ',
      content: 'Invoice'
    }]
  });
    
    //Refactored as enyo view -> InvoiceButton
//    var InvoiceButton = OB.COMP.SmallButton.extend({
//    className: 'btnlink-white btnlink-payment-clear',
//    icon: 'btn-icon-small btn-icon-check',
//    label: 'Invoice',
//    attributes: {
//      style: 'width: 50px;'
//    },
//    clickEvent: function (e) {
//      this.options.parent.receipt.resetOrderInvoice();
//    }
//  });

    enyo.kind({
      name: 'OB.UI.OrderView',
      published: {
        order : null,
      },
      components: [{
        kind: 'OB.UI.Table',
        name: 'listOrderLines',
        renderLine: 'OB.UI.RenderOrderLine',
        renderEmpty: 'OB.UI.RenderOrderLineEmpty', //defined on redenderorderline.js
//        {kind: 'OB.UI.RenderEmpty',
//          label: OB.I18N.getLabel('OBPOS_ReceiptNew')
//        },
        listStyle: 'edit'
      },{
        tag: 'ul',
        classes: 'unstyled',
        components: [{
          tag: 'li',
          components: [{
            style: 'position: relative; padding: 10px;',
            components:[{
              style: 'float: left; width: 80%;',
              content: 'TOTAL'
            },{
              name: 'totalgross',
              style: 'float: left; width: 20%; text-align:right; font-weight:bold;',
            },{
              style: 'clear: both;'
            }]
          }]
        },{
          tag: 'li',
          components:[{
            style: 'padding: 10px; border-top: 1px solid #cccccc; height: 40px;',
            components: [{
              kind: 'btninvoice'
            },{
              name: 'return',
              style: 'float: right; width: 50%; text-align: right; font-weight:bold; font-size: 30px; color: #f8941d;',
              content: 'To be returned'
            },{
              style: 'clear: both;'
            }]
          }]
        }]
      }],
      renderTotal: function(){
        this.$.totalgross.setContent('12.25');
      },
      initComponents: function(){
        this.inherited(arguments);
//        debugger;
        //this.listOrderLines.setCollection(this.owner.owner.model.order.);
        //FIXME
        //OB.UTIL.initContentView(this);
        // Set Model
//        this.receipt = this.options.root.modelorder;
//        var lines = this.receipt.get('lines');
//
//        this.tableview.registerCollection(lines);
//        this.receipt.on('change:gross', this.renderTotal, this);
//        this.receipt.on('change:orderType', this.renderFooter, this);
//        this.receipt.on('change:generateInvoice', this.renderFooter, this);

        // Initial total display...
        //this.renderFooter();
        //this.$.divinvoice.setContent('divInvoice');
        //this.$.divreturn.setContent('divReturn');
        //this.renderTotal();
        this.$.totalgross.setContent('100,25');
      },
      orderChanged: function(order) {
        this.$.listOrderLines.setCollection(this.order.get('lines'));
//        this.order.on('total:change', function (model) {
//          this.$.total.setTotal(model.get('total'));
//        }, this);
      }
      });
  // Order list
//  OB.COMP.OrderView = Backbone.View.extend({
//    tag: 'div',
//    contentView: [{
//      id: 'tableview',
//      view: OB.UI.TableView.extend({
//        style: 'edit',
//        renderEmpty: OB.COMP.RenderEmpty.extend({
//          label: OB.I18N.getLabel('OBPOS_ReceiptNew')
//        }),
//        renderLine: OB.COMP.RenderOrderLine
//      })
//    }, {
//      tag: 'ul',
//      attributes: {
//        'class': 'unstyled'
//      },
//      content: [
//
//      {
//        tag: 'li',
//        content: [{
//          tag: 'div',
//          attributes: {
//            style: 'position: relative; padding: 10px;'
//          },
//          content: [{
//            tag: 'div',
//            attributes: {
//              style: 'float: left; width: 80%'
//            },
//            content: [
//            OB.I18N.getLabel('OBPOS_ReceiptTotal')]
//          }, {
//            id: 'totalgross',
//            tag: 'div',
//            attributes: {
//              style: 'float: left; width: 20%; text-align:right; font-weight:bold;'
//            }
//          }, {
//            tag: 'div',
//            attributes: {
//              style: 'clear: both;'
//            }
//          }]
//        },]
//      }, {
//        tag: 'li',
//        content: [{
//          tag: 'div',
//          attributes: {
//            style: 'padding: 10px; border-top: 1px solid #cccccc; height: 40px;'
//          },
//          content: [{
//            tag: 'div',
//            attributes: {
//              style: 'float: left; width: 50%;'
//            },
//            content: [{
//              id: 'btninvoice',
//              view: InvoiceButton
//            }, {
//              tag: 'span',
//              content: '&nbsp;'
//            }, {
//              id: 'divinvoice',
//              tag: 'span',
//              attributes: {
//                style: 'font-weight:bold; '
//              }
//            }]
//          }, {
//            id: 'divreturn',
//            tag: 'div',
//            attributes: {
//              style: 'float: right; width: 50%; text-align: right; font-weight:bold; font-size: 30px; color: #f8941d;'
//            }
//          }, {
//            tag: 'div',
//            attributes: {
//              style: 'clear: both;'
//            }
//          }]
//        }]
//      }]
//    }],
//
//    initialize: function () {
//
//      OB.UTIL.initContentView(this);
//
//      // Set Model
//      this.receipt = this.options.root.modelorder;
//      var lines = this.receipt.get('lines');
//
//      this.tableview.registerCollection(lines);
//      this.receipt.on('change:gross', this.renderTotal, this);
//      this.receipt.on('change:orderType', this.renderFooter, this);
//      this.receipt.on('change:generateInvoice', this.renderFooter, this);
//
//      // Initial total display...
//      this.renderFooter();
//      this.renderTotal();
//    },
//
//    renderFooter: function () {
//      if (this.receipt.get('generateInvoice')) {
//        this.btninvoice.$el.show();
//        this.divinvoice.text(OB.I18N.getLabel('OBPOS_ToInvoice'));
//      } else {
//        this.btninvoice.$el.hide();
//        this.divinvoice.text('');
//      }
//      this.divreturn.text(this.receipt.get('orderType') === 1 ? OB.I18N.getLabel('OBPOS_ToBeReturned') : '');
//    },
//
//    renderTotal: function () {
//      this.totalgross.text(this.receipt.printTotal());
//    }
//  });
}());