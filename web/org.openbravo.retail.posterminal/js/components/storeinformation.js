/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

enyo.kind({
  name: 'OBPOS.UI.StoreInformation',
  kind: 'OB.UI.Modal',
  topPosition: '44px',
  body: {
    kind: 'OBPOS.UI.StoreInformationLine'
  },
  handlers: {
    onHideInfoPopup: 'hidePopup',
    onShowInfoPopup: 'showPopup'
  },
  storeId: null,
  executeOnShow: function () {
    this.$.header.setContent(this.args.storeName);
    this.storeId = this.args.storeId;
  },
  initComponents: function () {
    var me = this;
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OBPOS.UI.StoreInformationLine',
  components: [{
    classes: 'obpos-store-line span12',
    components: [{
      classes: 'obpos-icon-address',
      name: 'iconAddress',
    }, {
      classes: 'obpos-row-store-info',
      name: 'addressValue',
    }]
  }, {
    classes: 'obpos-clear-both'
  }, {
    classes: 'obpos-store-line span12',
    components: [{
      classes: 'span6',
      components: [{
        classes: 'obpos-icon-phone',
        name: 'iconPhone',
      }, {
        classes: 'obpos-row-store-info',
        name: 'phoneNumber',
      }]
    }, {
      classes: 'span6',
      components: [{
        classes: 'obpos-icon-fax',
        name: 'iconFax',
      }, {
        classes: 'obpos-row-store-info',
        name: 'faxNumber',
      }]
    }]
  }, {
    classes: 'obpos-store-line span12',
    components: [{
      classes: 'obpos-icon-phone',
      name: 'iconEmail',
    }, {
      classes: 'obpos-row-store-info',
      name: 'email',
    }]
  }, {
    classes: 'obpos-store-line span12',
    components: [{
      classes: 'obpos-icon-cif',
      name: 'iconCIF',
    }, {
      classes: 'obpos-row-store-info',
      name: 'cif',
    }]
  }, {
    classes: 'obpos-store-line span12',
    components: [{
      classes: 'span2',
      components: [{
        classes: 'obpos-icon-schedule',
        name: 'iconSchedule',
      }]
    }, {
      classes: 'span2',
      name: 'weekDays',
      components: [{
        classes: 'obpos-store-line-header',
      }, {
        classes: 'obpos-clear-both obpos-row-store-info',
        name: 'exampleDay',
      }]
    }, {
      classes: 'span3',
      name: 'openHour',
      components: [{
        classes: 'obpos-store-line-header',
        initComponents: function () {
          this.setContent(OB.I18N.getLabel('OBPOS_LblOpenHour'));
        }
      }, {
        classes: 'obpos-clear-both obpos-row-store-info',
        name: 'exampleHour',
      }]
    }, {
      classes: 'span3',
      name: 'callCenter',
      components: [{
        classes: 'obpos-store-line-header',
        initComponents: function () {
          this.setContent(OB.I18N.getLabel('OBPOS_LblCallCenter'));
        }
      }, {
        classes: 'obpos-clear-both obpos-row-store-info',
        name: 'exampleCenter',
      }]
    }]
  }, {
    classes: 'obpos-store-line span12 obpos-store-line-header2',
    components: [{
      classes: 'span5',
      name: 'openHolidays',
      components: [{
        classes: 'obpos-store-line-header obpos-center-text',
        initComponents: function () {
          this.setContent(OB.I18N.getLabel('OBPOS_LblOpenHolidays'));
        }
      }, {
        classes: 'obpos-clear-both obpos-center-text',
        name: 'exampleHolidays',
      }, {
        classes: 'obpos-clear-both obpos-store-line-header',
      }, {
        classes: 'obpos-store-line-header obpos-center-text',
        initComponents: function () {
          this.setContent(OB.I18N.getLabel('OBPOS_LblSpecialOpenHour'));
        }
      }, {
        classes: 'obpos-clear-both obpos-center-text',
        name: 'exampleSpecialOpenHour',
      }]
    }, {
      classes: 'span6',
      name: 'closeHolidays',
      components: [{
        classes: 'obpos-store-line-header obpos-center-text',
        initComponents: function () {
          this.setContent(OB.I18N.getLabel('OBPOS_LblCloseHolidays'));
        }
      }, {
        classes: 'obpos-clear-both obpos-center-text',
        name: 'exampleCloseHolidays',
      }]
    }]
  }, {
    classes: 'span12',
    components: [{
      classes: 'obpos-clear-both obpos-btnlink btnlink btnlink-small btnlink-gray',
      name: 'btnClose',
      kind: 'OB.UI.Button',
      i18nLabel: 'OBPOS_LblSlaveClose',
      tap: function () {
        if (this.disabled) {
          return true;
        }
      }
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.addressValue.setContent('C.C. Galaria - Calle J, 3 -CP: 3191 Cordovilla (Navarra)');
    this.$.phoneNumber.setContent('948 123456');
    this.$.faxNumber.setContent('948 123457');
    this.$.email.setContent('myshop@email.com');
    this.$.cif.setContent('A123456');

    this.$.exampleDay.setContent('lunes');
    this.$.exampleHour.setContent('10:00 - 22:00');
    this.$.exampleCenter.setContent('10:00 - 22:00');
    this.$.exampleHolidays.setContent('07/07/18 10:00 - 22:00');
    this.$.exampleCloseHolidays.setContent('18/11/18');
    this.$.exampleSpecialOpenHour.setContent('24/12/18 10:00 - 20:00');
  },
});