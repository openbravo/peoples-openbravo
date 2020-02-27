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
  initComponents: function() {
    return this;
  }
});

enyo.kind({
  name: 'OB.OBPOSCountSafeBox.UI.SafeBoxHeader',
  classes: 'cashupDisplayFlex',
  style: 'align-items: center; height: 48px;',
  components: [
    {
      name: 'name',
      style:
        'display: table-cell; vertical-align: middle; padding: 2px 0px 2px 5px;'
    },
    {
      name: 'searchkey',
      style:
        'display: table-cell; vertical-align: middle; padding: 2px 0px 2px 5px;'
    },
    {
      name: 'user',
      style:
        'display: table-cell; vertical-align: middle; padding: 2px 5px 2px 5px;'
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSCountSafeBox.UI.InfoSafeBox',
  classes: 'infoSafeBox',
  components: [
    {
      name: 'safeBoxHeader',
      kind: 'OB.OBPOSCountSafeBox.UI.SafeBoxHeader'
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSCountSafeBox.UI.RenderSafeBoxLine',
  events: {
    onCountSafeBox: ''
  },
  components: [
    {
      style:
        'display: table; height: 42px; width: 100%; border-bottom: 1px solid #cccccc;',
      components: [
        {
          classes: 'buttonVoid',
          components: [
            {
              name: 'countSafeBox',
              kind: 'OB.OBPOSCountSafeBox.UI.ButtonCountSafeBox',
              ontap: 'countSafeBox'
            }
          ]
        },
        {
          name: 'infoSafeBox',
          kind: 'OB.OBPOSCountSafeBox.UI.InfoSafeBox'
        },
        {
          style: 'clear: both;'
        }
      ]
    }
  ],
  create: function() {
    this.inherited(arguments);

    this.$.infoSafeBox.$.safeBoxHeader.$.name.setContent(
      this.model.get('safeBoxName')
    );
    this.$.infoSafeBox.$.safeBoxHeader.$.searchkey.setContent(
      this.model.get('safeBoxSearchKey')
    );
    // Safe Box User is not a mandatory field
    if (this.model.get('safeBoxUserName')) {
      this.$.infoSafeBox.$.safeBoxHeader.$.user.show();
      this.$.infoSafeBox.$.safeBoxHeader.$.user.setContent(
        this.model.get('safeBoxUserName')
      );
    } else {
      this.$.infoSafeBox.$.safeBoxHeader.$.user.hide();
      this.$.infoSafeBox.$.safeBoxHeader.$.user.setContent('');
    }

    this.safeBox = this.model.attributes;
  },
  countSafeBox: function(inSender, inEvent) {
    this.doCountSafeBox();
  }
});

enyo.kind({
  name: 'OB.OBPOSCountSafeBox.UI.ListSafeBoxes',
  published: {
    collection: null
  },
  handlers: {
    onCountSafeBox: 'countSafeBox'
  },
  components: [
    {
      classes: 'tab-pane',
      components: [
        {
          style: 'overflow:auto; height: 500px; margin: 5px',
          components: [
            {
              style: 'background-color: #ffffff; color: black; padding: 5px;',
              components: [
                {
                  classes: 'row-fluid',
                  components: [
                    {
                      classes: 'span12',
                      components: [
                        {
                          name: 'stepsheader',
                          style:
                            'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
                          renderHeader: function(step, count) {
                            this.setContent(
                              OB.I18N.getLabel('OBPOS_LblStepNumber', [
                                step,
                                count
                              ]) +
                                ' ' +
                                OB.I18N.getLabel('OBPOS_LblStepSafeBoxList') +
                                OB.OBPOSCloseCash.UI.CloseCash.getTitleExtensions()
                            );
                          }
                        }
                      ]
                    },
                    {
                      style: 'clear: both;'
                    }
                  ]
                },
                {
                  classes: 'row-fluid',
                  components: [
                    {
                      style: 'span12',
                      components: [
                        {
                          classes: 'row-fluid',
                          kind: 'Group',
                          components: [
                            {
                              name: 'safeBoxesList',
                              kind: 'OB.UI.Table',
                              renderLine:
                                'OB.OBPOSCountSafeBox.UI.RenderSafeBoxLine',
                              renderEmpty: 'OB.UI.RenderEmpty',
                              listStyle: 'list'
                            }
                          ]
                        }
                      ]
                    }
                  ]
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
