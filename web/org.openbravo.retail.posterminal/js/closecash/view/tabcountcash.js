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
  name: 'OB.OBPOSCashUp.UI.RenderPaymentsLine',
  events: {
    onLineEditCount: ''
  },
  components: [{
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        style: 'border-bottom: 1px solid #cccccc;',
        components: [{
          style: 'float:left; display: table; width: 50%',
          components: [{          
            name: 'name',
            style: 'padding: 10px 20px 10px 10px; display: table-cell; width: 40%;'
          }, {
            name: 'foreignExpected',
            style: 'padding: 10px 10px 10px 0px; text-align:right; display: table-cell; width: 35%;'
          }, {
            name: 'expected',
            style: 'padding: 10px 10px 10px 0px; text-align:right; display: table-cell; width: 35%;'
          }]            
        }, {
          style: 'float:left; display: table; width: 50%;',
          components: [{
            style: 'display: table-cell; width: 15%;',
            components: [{
              name: 'buttonEdit',
              kind: 'OB.UI.SmallButton',
              classes: 'btnlink-orange btnlink-cashup-edit btn-icon-small btn-icon-edit',
              ontap: 'lineEdit'
            }]
          }, {
            style: 'display: table-cell; width: 15%;',
            components: [{
              name: 'buttonOk',
              kind: 'OB.UI.SmallButton',
              classes: 'btnlink-green btnlink-cashup-ok btn-icon-small btn-icon-check',
              ontap: 'lineOK'
            }]
          }, {
            name: 'foreignCounted',
            style: 'display: table-cell; padding: 10px 10px 10px 0px; text-align:right; width: 35%;',
            content: ''
          }, {
            name: 'counted',
            style: 'display: table-cell; padding: 10px 10px 10px 0px; text-align:right; width: 35%;',
            showing: false
          }]
        }]
      }]
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.name.setContent(this.model.get('name'));
    if (this.model.get('rate') && this.model.get('rate') !== '1') {
      this.$.foreignExpected.setContent('(' + OB.I18N.formatCurrency(this.model.get('foreignExpected')) + ' ' + this.model.get('isocode') + ')');
      this.$.foreignExpected.show();
    }
    this.$.expected.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('expected'))));
  },
  render: function () {
    var udfn, counted, foreignCounted;
    this.inherited(arguments);
    counted = this.model.get('counted');
    if (counted !== null && counted !== udfn) {
      this.$.counted.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, counted)));
      this.$.counted.show();
      if (this.model.get('rate') && this.model.get('rate') !== '1') {
        this.$.foreignCounted.setContent('(' + OB.I18N.formatCurrency(this.model.get('foreignCounted')) + ' ' + this.model.get('isocode') + ')');
      }
      this.$.buttonOk.hide();
    }
    this.$.buttonEdit.setDisabled(this.model.get('paymentMethod').iscash && this.model.get('paymentMethod').countcash);
  },
  lineEdit: function () {
    this.doLineEditCount();
  },
  lineOK: function (inSender, inEvent) {
    this.model.set('counted', this.model.get('expected'));
    this.model.set('foreignCounted', this.model.get('foreignExpected'));
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ListPaymentMethods',
  handlers: {
    onAnyCounted: 'anyCounted'
  },
  events: {
    onCountAllOK: ''
  },
  components: [{
    classes: 'tab-pane',
    components: [{
      style: 'overflow:auto; height: 500px; margin: 5px',
      components: [{
        style: 'background-color: #ffffff; color: black; padding: 5px;',
        components: [{
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components: [{
              style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
              initComponents: function () {
                this.setContent(OB.I18N.getLabel('OBPOS_LblStep2of4') + OB.OBPOSCashUp.UI.CashUp.getTitleExtensions());
              }
            }]
          }]
        }, {
          style: 'background-color: #ffffff; color: black;',
          components: [{
            components: [{
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                style: 'border-bottom: 1px solid #cccccc;',
                components: [{
                  style: 'float: left; display:table; width: 50%; ',
                  components: [{
                    style: 'padding: 10px 10px 10px 10px; display: table-cell; width: 60%;',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_LblPaymentMethod'));
                    }
                  }, {
                    style: 'padding: 10px 10px 10px 0px; display: table-cell; width: 40%; text-align:right;',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_LblExpected'));
                    }
                  }]
                }, {
                  style: 'float: left; display:table; width: 50%; ',
                  components: [{                  
                    style: 'padding: 10px 10px 10px 0px; display: table-cell; width: 100%; text-align: right;',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_LblCounted'));
                    }
                  }]
                }]
              }]
            }, {
              name: 'paymentsList',
              kind: 'OB.UI.Table',
              renderLine: 'OB.OBPOSCashUp.UI.RenderPaymentsLine',
              renderEmpty: 'OB.UI.RenderEmpty',
              listStyle: 'list'
            }, {
              components: [{
                classes: 'row-fluid',
                components: [{
                  classes: 'span12',
                  style: 'border-bottom: 1px solid #cccccc;',
                  components: [{
                    style: 'float: left; display:table; width: 50%; ',
                    components: [{
                      style: 'display:table-cell; width: 100%;', 
                      content: '&nbsp;', 
                      allowHtml: true
                    }]
                  }, {
                    style: 'float: left; display:table; width: 50%; ',
                    components: [{
                      style: 'display:table-cell; width: 50px; padding: 10px 10px 10px 0px;'
                    }, {               
                      style: 'display:table-cell; width: 15%;',
                      components: [{
                        name: 'buttonAllOk',
                        kind: 'OB.UI.SmallButton',
                        classes: 'btnlink-green btnlink-cashup-ok btn-icon-small btn-icon-check',
                        ontap: 'doCountAllOK'
                      }]
                    }, {        
                      // This element has been intentionally created empty.
                    }]
                  }]
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                style: 'border-bottom: 1px solid #cccccc;',
                components: [{
                  style: 'float: left; display:table; width: 50%; ',   
                  components: [{                                                 
                    name: 'totalLbl',
                    style: 'padding: 10px 10px 10px 10px; display: table-cell; width: 60%;',
                    initComponents: function () {
                      this.setContent(OB.I18N.getLabel('OBPOS_ReceiptTotal'));
                    }
                  }, {
                    style: 'padding: 10px 10px 10px 0px; display: table-cell; width: 40%; text-align:right;',
                    components: [{
                      name: 'total',
                      kind: 'OB.OBPOSCashUp.UI.RenderTotal'
  
                    }]
                  }]
                }, {
                  components: [{
                    style: 'float: left; display:table; width: 50%; ',                
                    components: [{
                      style: 'padding: 10px 10px 10px 0px; display: table-cell; width: 100%; text-align: right;',
                      name: 'difference',
                      kind: 'OB.OBPOSCashUp.UI.RenderTotal'
                    }]
                  }]
                }]
              }]
            }]
          }]
        }]
      }]
    }]
  }],
  setCollection: function (col) {
    this.$.paymentsList.setCollection(col);
  },
  anyCounted: function () {
    this.$.buttonAllOk.applyStyle('visibility', 'hidden'); //hiding in this way to keep the space
  },
  displayStep: function (model) {
    // this function is invoked when displayed.  
    var opendrawer = model.get('paymentList').any(function (payment) {
      var paymentmethod = payment.get('paymentMethod');
      return paymentmethod.iscash && !paymentmethod.countcash && paymentmethod.allowopendrawer;
    });

    if (opendrawer) {
      OB.POS.hwserver.openDrawer();
    }
  },
  verifyStep: function (model, callback) {
    // this function is invoked when going next, invokes callback to continue
    // do not invoke callback to cancel going next.
    var firstdiff = model.get('paymentList').find(function (payment) {
      return payment.get('difference') !== 0;
    });

    if (firstdiff) {
      // there is at leat 1 payment with differences
      OB.UTIL.Approval.requestApproval(
      model, 'OBPOS_approval.cashupdifferences', function (approved, supervisor, approvalType) {
        if (approved) {
          callback();
        }
      });
    } else {
      callback();
    }
  }
});