/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchBP = function (context) {
    var me = this;

    this.searchAction = function (){
      function errorCallback(tx, error) {
        OB.UTIL.showError("OBDAL error: " + error);
      }

      function successCallbackBPs(dataBps) {
        if (dataBps && dataBps.length > 0){
          me.bps.reset(dataBps.models);
        }else{
          me.bps.reset();
        }
      }

      var criteria = {};
      if (me.bpname.val() && me.bpname.val() !== '') {
        criteria._identifier = {
          operator: OB.Dal.CONTAINS,
          value: me.bpname.val()
        };
      }

      OB.Dal.find(OB.Model.BusinessPartner, criteria , successCallbackBPs, errorCallback);
    };

    this._id = 'SearchBPs';

    this.receipt = context.modelorder;
    this.bps = new OB.Collection.BusinessPartnerList();

    this.bps.on('click', function (model) {
      var saveInDB = true;
      this.receipt.setBPandBPLoc(model, false, saveInDB);
    }, this);

    this.receipt.on('clear', function() {
      me.bpname.val('');
      me.searchAction(null);
    }, this);

   this.component = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid', 'style':  'border-bottom: 1px solid #cccccc;'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px'},  content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'display: table;'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'display: table-cell; width: 100%;'}, content: [
                    {kind: OB.COMP.SearchInput, id: 'bpname', attr: {'type': 'text', 'xWebkitSpeech': 'x-webkit-speech', 'className': 'input', 'style': 'width: 100%;',
                      'clickEvent': function(e) {
                        if(e && e.keyCode === 13) {
                          me.searchAction();
                          return false;
                        } else {
                          return true;
                        }
                      }
                    }}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'display: table-cell;'}, content: [
                    {kind: OB.COMP.SmallButton, attr: {'className': 'btnlink-gray', 'icon': 'btn-icon-small btn-icon-clear', 'style': 'width: 100px; margin: 0px 5px 8px 19px;',
                      'clickEvent': function() {
                        this.$el.parent().prev().children().val('');
                        me.searchAction();
                      }
                    }}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'display: table-cell;'}, content: [
                    {kind: OB.COMP.SmallButton, attr: {'className': 'btnlink-yellow', 'icon': 'btn-icon-small btn-icon-search', 'style': 'width: 100px; margin: 0px 0px 8px 5px;',
                      'clickEvent': function() {
                        me.searchAction();
                      }
                    }}
                  ]}
                ]}
              ]}
            ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
              {kind: B.KindJQuery('div'), content: [
                {kind: OB.UI.TableView, id: 'tableview', attr: {
                  collection: this.bps,
                  renderEmpty: OB.COMP.RenderEmpty,
                  renderLine: OB.COMP.RenderBusinessPartner
                }}
              ]}
            ]}
          ]}
        ]}
      ]}
    );

    this.$el = this.component.$el;
    this.bpname = this.component.context.bpname.$el;
    this.tableview = this.component.context.tableview;
  };
}());