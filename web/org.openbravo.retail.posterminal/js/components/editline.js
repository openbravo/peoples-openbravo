/*global Backbone */

(function () {

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
                    'clickEvent': function () {
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
            content: [
                 {
                   id: 'editlineimage',

                   view: OB.UTIL.Thumbnail.extend({
                     className: 'image-wrap image-editline',
                     width: 128,
                     height: 128
                   })
                 }
              ]

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
    initialize: function () {
      var me = this;
      OB.UTIL.initContentView(this);

      this.products = this.options.DataProductPrice;
      this.receipt = this.options.modelorder;
      this.line = null;

      this.receipt.get('lines').on('selected', function (line) {
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
    render: function () {

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