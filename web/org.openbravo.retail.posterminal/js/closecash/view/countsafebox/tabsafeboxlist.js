/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.OBPOSCountSafeBox.UI.ButtonCountSafeBox',
  kind: 'OB.UI.RadioButton',
  classes: 'obObposCountSafeBoxUiButtonCountSafeBox',
  initComponents: function() {
    return this;
  }
});

enyo.kind({
  name: 'OB.OBPOSCountSafeBox.UI.SafeBoxHeader',
  classes: 'obObposCountSafeBoxUiSafeBoxHeader',
  components: [
    {
      name: 'name',
      classes: 'obObposCountSafeBoxUiSafeBoxHeader-name'
    },
    {
      name: 'user',
      classes: 'obObposCountSafeBoxUiSafeBoxHeader-user'
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSCountSafeBox.UI.RenderSafeBoxLine',
  classes: 'obObposCountSafeBoxUiRenderSafeBoxLine',
  events: {
    onCountSafeBox: ''
  },
  components: [
    {
      name: 'countSafeBox',
      kind: 'OB.OBPOSCountSafeBox.UI.ButtonCountSafeBox',
      classes: 'obObposCountSafeBoxUiRenderSafeBoxLine-countSafeBox',
      ontap: 'countSafeBox'
    },
    {
      name: 'safeBoxHeader',
      kind: 'OB.OBPOSCountSafeBox.UI.SafeBoxHeader',
      classes: 'obObposCountSafeBoxUiRenderSafeBoxLine-safeBoxHeader'
    }
  ],
  create: function() {
    this.inherited(arguments);

    this.$.safeBoxHeader.$.name.setContent(
      `${this.model.get('safeBoxName')} (${this.model.get(
        'safeBoxSearchKey'
      )}) `
    );
    // Safe Box User is not a mandatory field
    if (this.model.get('safeBoxUserName')) {
      this.$.safeBoxHeader.$.user.show();
      this.$.safeBoxHeader.$.user.setContent(this.model.get('safeBoxUserName'));
    } else {
      this.$.safeBoxHeader.$.user.hide();
      this.$.safeBoxHeader.$.user.setContent('');
    }

    this.safeBox = this.model.attributes;
  },
  countSafeBox: function(inSender, inEvent) {
    this.doCountSafeBox();
  }
});

enyo.kind({
  name: 'OB.OBPOSCountSafeBox.UI.ListSafeBoxes',
  classes: 'obObposCountSafeBoxUiListSafeBoxes',
  published: {
    collection: null
  },
  handlers: {
    onCountSafeBox: 'countSafeBox'
  },
  components: [
    {
      classes: 'obObposCountSafeBoxUiListSafeBoxes-wrapper',
      components: [
        {
          classes: 'obObposCountSafeBoxUiListSafeBoxes-wrapper-components',
          components: [
            {
              name: 'stepsheader',
              classes:
                'obObposCountSafeBoxUiListSafeBoxes-wrapper-components-title',
              renderHeader: function(step, count) {
                this.setContent(
                  OB.I18N.getLabel('OBPOS_LblStepNumber', [step, count]) +
                    ' ' +
                    OB.I18N.getLabel('OBPOS_LblStepSafeBoxList') +
                    OB.OBPOSCloseCash.UI.CloseCash.getTitleExtensions()
                );
              }
            },
            {
              classes:
                'obObposCountSafeBoxUiListSafeBoxes-wrapper-components-body',
              kind: 'Group',
              components: [
                {
                  name: 'safeBoxesList',
                  kind: 'OB.UI.Table',
                  classes:
                    'obObposCountSafeBoxUiListSafeBoxes-wrapper-components-body-formkeep',
                  renderLine: 'OB.OBPOSCountSafeBox.UI.RenderSafeBoxLine',
                  renderEmpty: 'OB.UI.RenderEmpty',
                  listStyle: 'list'
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  init: function(model) {
    this.model = model;
  },
  collectionChanged: function() {
    this.$.safeBoxesList.setCollection(this.collection);
  },
  countSafeBox: function(inSender, inEvent) {
    const safeBoxSelected = inEvent.originator.safeBox;

    this.model.prepareCashUpReport(safeBoxSelected, () => {
      let safeBoxes = JSON.parse(OB.UTIL.localStorage.getItem('safeBoxes'));
      const currentSafeBox = safeBoxes.find(
        safeBox => safeBox.searchKey === safeBoxSelected.safeBoxSearchKey
      );
      OB.UTIL.localStorage.setItem(
        'currentSafeBox',
        JSON.stringify(currentSafeBox)
      );
      this.model.trigger('selectedSafeBox');
    });
  },
  displayStep: function(model) {
    OB.UTIL.localStorage.removeItem('currentSafeBox');
    // this function is invoked when displayed.
    this.$.stepsheader.renderHeader(
      model.stepNumber('OB.CountSafeBox.StepSafeBoxList'),
      model.stepCount()
    );
  }
});
