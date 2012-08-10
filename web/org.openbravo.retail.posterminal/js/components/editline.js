/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

enyo.kind({
  name: 'OB.UI.EditLine',
  components: [{
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
                tap: function() {
                  var line = this.owner.line,
                      receipt = this.owner.receipt;
                  if (line && receipt) {
                    receipt.deleteLine(line)
                    receipt.trigger('scan');
                  }
                }
              }]
            }]
          }]
        }, {
          style: 'padding: 0px 0px 0px 25px; width:100%; line-height: 140%;',
          components: [{
            classes: 'row-fluid',
            components: [{
              classes: 'span4',
              content: OB.I18N.getLabel('OBPOS_LineDescription')
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlinename'
              }]
            }]
          }, {
            classes: 'row-fluid',
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

  init: function() {
    this.inherited(arguments);

    this.receipt = this.owner.owner.owner.model.get('order');
    this.line = null;


    this.receipt.get('lines').on('selected', function(line) {
      if (this.line) {
        this.line.off('change', this.render);
      }
      this.line = line;
      if (this.line) {
        this.line.on('change', this.render, this);
      }
      this.render();
    }, this);
  },

  render: function() {
    this.inherited(arguments);

    if (this.line) {
      this.$.msgaction.hide();
      this.$.msgedit.show();
      this.$.editlineimage.img = this.line.get('product').get('img');
      this.$.editlineimage.render();
      this.$.editlinename.setContent(this.line.get('product').get('_identifier'));
      this.$.editlineqty.setContent(this.line.printQty());
      this.$.editlinediscount.setContent(this.line.printDiscount());
      this.$.editlineprice.setContent(this.line.printPrice());
      this.$.editlinegross.setContent(this.line.printGross());
    } else {
      this.$.txtaction.setContent(OB.I18N.getLabel('OBPOS_NoLineSelected'));
      this.$.msgedit.hide();
      this.$.msgaction.show();
      this.$.editlineimage.img = null;
      this.$.editlineimage.render();
      this.$.editlinename.setContent('');
      this.$.editlineqty.setContent('');
      this.$.editlinediscount.setContent('');
      this.$.editlineprice.setContent('');
      this.$.editlinegross.setContent('');
    }
  }
});

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.EditLine = Backbone.View.extend({
    tag: 'div',
    contentView: [{
      tag: 'div',
      attributes: {
        'style': 'background-color: #ffffff; color: black; height: 200px; margin: 5px; padding: 5px'
      },
      content: [{
        tag: 'div',
        id: 'msgedit',
        attributes: {
          'class': 'row-fluid',
          'style': 'display: none;'
        },
        content: [{
          tag: 'div',
          attributes: {
            'class': 'span7'
          },
          content: [{
            tag: 'div',
            attributes: {
              style: 'padding: 5px; width:100%'
            },
            content: [{
              tag: 'div',
              attributes: {
                'class': 'row-fluid'
              },
              content: [{
                tag: 'div',
                attributes: {
                  'class': 'span12'
                },
                content: [{
                  view: OB.COMP.SmallButton.extend({
                    'label': OB.I18N.getLabel('OBPOS_ButtonDelete'),
                    'className': 'btnlink-orange',
                    'clickEvent': function() {
                      var parent = this.options.parent;
                      if (parent.line) {
                        parent.receipt.deleteLine(parent.line);
                        parent.receipt.trigger('scan');
                      }
                    }
                  })
                }]
              }]
            }]
          }, {
            tag: 'div',
            attributes: {
              style: 'padding: 0px 0px 0px 25px; width:100%; line-height: 140%;'
            },
            content: [{
              tag: 'div',
              attributes: {
                'class': 'row-fluid'
              },
              content: [{
                tag: 'div',
                attributes: {
                  'class': 'span4'
                },
                content: [
                OB.I18N.getLabel('OBPOS_LineDescription')]
              }, {
                tag: 'div',
                attributes: {
                  'class': 'span8'
                },
                content: [{
                  tag: 'span',
                  id: 'editlinename'
                }]
              }]
            }, {
              tag: 'div',
              attributes: {
                'class': 'row-fluid'
              },
              content: [{
                tag: 'div',
                attributes: {
                  'class': 'span4'
                },
                content: [
                OB.I18N.getLabel('OBPOS_LineQuantity')]
              }, {
                tag: 'div',
                attributes: {
                  'class': 'span8'
                },
                content: [{
                  tag: 'span',
                  id: 'editlineqty'
                }]
              }]
            }, {
              tag: 'div',
              attributes: {
                'class': 'row-fluid'
              },
              content: [{
                tag: 'div',
                attributes: {
                  'class': 'span4'
                },
                content: [
                OB.I18N.getLabel('OBPOS_LinePrice')]
              }, {
                tag: 'div',
                attributes: {
                  'class': 'span8'
                },
                content: [{
                  tag: 'span',
                  id: 'editlineprice'
                }]
              }]
            }, {
              tag: 'div',
              attributes: {
                'class': 'row-fluid'
              },
              content: [{
                tag: 'div',
                attributes: {
                  'class': 'span4'
                },
                content: [
                OB.I18N.getLabel('OBPOS_LineDiscount')]
              }, {
                tag: 'div',
                attributes: {
                  'class': 'span8'
                },
                content: [{
                  tag: 'span',
                  id: 'editlinediscount'
                }]
              }]
            }, {
              tag: 'div',
              attributes: {
                'class': 'row-fluid'
              },
              content: [{
                tag: 'div',
                attributes: {
                  'class': 'span4'
                },
                content: [
                OB.I18N.getLabel('OBPOS_LineTotal')]
              }, {
                tag: 'div',
                attributes: {
                  'class': 'span8'
                },
                content: [{
                  tag: 'span',
                  id: 'editlinegross'
                }]
              }]
            }]
          }]
        }, {
          tag: 'div',
          attributes: {
            'class': 'span5',
            'style': 'text-align: right'
          },
          content: [{
            tag: 'div',
            attributes: {
              'style': 'padding: 60px 10px 20px 10px;'
            },
            content: [{
              id: 'editlineimage',

              view: OB.UTIL.Thumbnail.extend({
                className: 'image-wrap image-editline',
                width: 128,
                height: 128
              })
            }]

          }]
        }]
      }, {
        tag: 'div',
        id: 'msgaction',
        attributes: {
          'style': 'padding: 10px; display: none;'
        },
        content: [{
          tag: 'div',
          id: 'txtaction',
          attributes: {
            'style': 'float:left;'
          }
        }]
      }]
    }],
    initialize: function() {
      var me = this;
      OB.UTIL.initContentView(this);

      this.products = this.options.root.DataProductPrice;
      this.receipt = this.options.root.modelorder;
      this.line = null;

      this.receipt.get('lines').on('selected', function(line) {
        if (this.line) {
          this.line.off('change', this.render);
        }
        this.line = line;
        if (this.line) {
          this.line.on('change', this.render, this);
        }
        this.render();
      }, this);
    },
    render: function() {

      if (this.line) {
        this.msgaction.hide();
        this.msgedit.show();
        this.editlineimage.img = this.line.get('product').get('img');
        this.editlineimage.render();
        this.editlinename.text(this.line.get('product').get('_identifier'));
        this.editlineqty.text(this.line.printQty());
        this.editlinediscount.text(this.line.printDiscount());
        this.editlineprice.text(this.line.printPrice());
        this.editlinegross.text(this.line.printGross());
      } else {
        this.txtaction.text(OB.I18N.getLabel('OBPOS_NoLineSelected'));
        this.msgedit.hide();
        this.msgaction.show();
        this.editlineimage.img = null;
        this.editlineimage.render();
        this.editlinename.empty();
        this.editlineqty.empty();
        this.editlinediscount.empty();
        this.editlineprice.empty();
        this.editlinegross.empty();
      }
      return this;
    }
  });
}());