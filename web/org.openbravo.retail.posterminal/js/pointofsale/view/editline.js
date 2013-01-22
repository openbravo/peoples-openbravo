/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.EditLine',
  published: {
    receipt: null
  },
  events: {
    onDeleteLine: '',
    onEditLine: ''
  },
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxBehavior'
  },
  checkBoxBehavior: function (inSender, inEvent) {
    if (inEvent.status) {
      this.line = null;
      //WARN! When off is done the components which are listening to this event
      //are removed. Because of it, the callback for the selected event are saved
      //and then recovered.
      this.selectedCallbacks = this.receipt.get('lines')._callbacks.selected;
      this.receipt.get('lines').off('selected');
      this.render();
    } else {
      //WARN! recover the callbacks for the selected events
      this.receipt.get('lines')._callbacks.selected = this.selectedCallbacks;

      if (this.receipt.get('lines').length > 0) {
        var line = this.receipt.get('lines').at(0);
        line.trigger('selected', line);
      }
    }
  },
  executeOnShow: function (args) {
    if (args && args.discounts) {
      this.$.defaultEdit.hide();
      this.$.discountsEdit.show();
      return;
    }
    this.$.defaultEdit.show();
    this.$.discountsEdit.hide();
  },
  components: [{
    kind: 'OB.OBPOSPointOfSale.UI.Discounts',
    showing: false,
    name: 'discountsEdit'
  }, {
    name: 'defaultEdit',
    style: 'background-color: #ffffff; color: black; height: 200px; margin: 5px; padding: 5px',
    components: [{
      name: 'msgedit',
      classes: 'row-fluid',
      showing: false,
      components: [{
        classes: 'span7',
        components: [{
          style: 'padding: 5px; width:100%',
          components: [{
            classes: 'row-fluid',
            components: [{
              classes: 'span12',
              components: [{
                kind: 'OB.UI.SmallButton',
                content: OB.I18N.getLabel('OBPOS_ButtonDelete'),
                classes: 'btnlink-orange',
                tap: function () {
                  this.owner.doDeleteLine({
                    line: this.owner.line
                  });
                },
                init: function (model) {
                  this.model = model;
                  this.model.get('order').on('change:isPaid', function (newValue) {
                    if (newValue) {
                      if (newValue.get('isPaid') === true) {
                        this.setShowing(false);
                        return;
                      }
                    }
                    this.setShowing(true);
                  }, this);
                }
              }, {
                kind: 'OB.UI.SmallButton',
                content: OB.I18N.getLabel('OBPOS_LblDescription'),
                classes: 'btnlink-orange',
                tap: function () {
                  this.owner.doEditLine({
                    line: this.owner.line
                  });
                },
                init: function (model) {
                  this.model = model;
                  this.model.get('order').on('change:isPaid', function (newValue) {
                    if (newValue) {
                      if (newValue.get('isPaid') === true) {
                        this.setShowing(false);
                        return;
                      }
                    }
                    this.setShowing(true);
                  }, this);
                }
              }]
            }]
          }]
        }, {
          kind: 'Scroller',
          maxHeight: '120px',
          thumb: true,
          horizontal: 'hidden',
          style: 'padding: 0px 0px 0px 25px; width:100%; line-height: 140%;',
          components: [{
            classes: 'row-fluid',
            style: 'clear: both;',
            components: [{
              classes: 'span4',
              content: OB.I18N.getLabel('OBPOS_LineName')
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlinename'
              }]
            }]
          }, {
            classes: 'row-fluid',
            style: 'clear: both;',
            components: [{
              classes: 'span4',
              content: OB.I18N.getLabel('OBPOS_LineQuantity')
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlineqty'
              }]
            }]
          }, {
            classes: 'row-fluid',
            style: 'clear: both;',
            components: [{
              classes: 'span4',
              content: OB.I18N.getLabel('OBPOS_LinePrice')
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlineprice'
              }]
            }]
          }, {
            classes: 'row-fluid',
            style: 'clear: both;',
            components: [{
              classes: 'span4',
              content: OB.I18N.getLabel('OBPOS_LineDiscount')
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlinediscount'
              }]
            }]
          }, {
            classes: 'row-fluid',
            style: 'clear: both;',
            components: [{
              classes: 'span4',
              content: OB.I18N.getLabel('OBPOS_LineTotal')
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlinegross'
              }]
            }]
          }]
        }]
      }, {
        classes: 'span5',
        sytle: 'text-align: right',
        components: [{
          style: 'padding: 60px 10px 20px 10px;',
          components: [{
            name: 'editlineimage',
            kind: 'OB.UI.Thumbnail',
            classes: 'image-wrap image-editline',
            width: '128px',
            height: '128px'
          }]
        }]
      }]
    }, {
      name: 'msgaction',
      style: 'padding: 10px;',
      components: [{
        name: 'txtaction',
        style: 'float:left;'
      }]
    }]
  }],
  selectedListener: function (line) {
    if (this.line) {
      this.line.off('change', this.render);
    }
    this.line = line;
    if (this.line) {
      this.line.on('change', this.render, this);
    }
    this.render();
  },
  receiptChanged: function () {
    this.inherited(arguments);

    this.line = null;

    this.receipt.get('lines').on('selected', this.selectedListener, this);
  },

  render: function () {
    this.inherited(arguments);

    if (this.line) {
      this.$.msgaction.hide();
      this.$.msgedit.show();
      this.$.editlineimage.setImg(this.line.get('product').get('img'));
      this.$.editlinename.setContent(this.line.get('product').get('_identifier'));
      this.$.editlineqty.setContent(this.line.printQty());
      this.$.editlinediscount.setContent(this.line.printDiscount());
      this.$.editlineprice.setContent(this.line.printPrice());
      this.$.editlinegross.setContent(this.line.printGross());
    } else {
      this.$.txtaction.setContent(OB.I18N.getLabel('OBPOS_NoLineSelected'));
      this.$.msgedit.hide();
      this.$.msgaction.show();
      this.$.editlineimage.setImg(null);
      this.$.editlinename.setContent('');
      this.$.editlineqty.setContent('');
      this.$.editlinediscount.setContent('');
      this.$.editlineprice.setContent('');
      this.$.editlinegross.setContent('');
    }
  }
});