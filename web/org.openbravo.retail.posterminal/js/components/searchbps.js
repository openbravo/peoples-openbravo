/*global B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.SearchBP = function (context) {
    var me = this;

    this.searchAction = function (criteria){
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

      OB.Dal.find(OB.Model.BusinessPartner, criteria , successCallbackBPs, errorCallback);
    };

    this._id = 'SearchBPs';

    this.receipt = context.modelorder;
    this.bps = new OB.Collection.BusinessPartnerList();

    this.bps.on('click', function (model) {
      this.receipt.setBPandBPLoc(model);
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
                    {kind: B.KindJQuery('input'), id: 'bpname', attr: {'type': 'text', 'x-webkit-speech': 'x-webkit-speech', 'class': 'input', 'style': 'width: 100%;'}}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'display: table-cell;'}, content: [
                    //{kind: OB.COMP.ClearButton}
                    {kind: OB.COMP.SmallButton, attr: {'className': 'btnlink-gray', 'icon': 'btn-icon btn-icon-clear', 'style': 'width: 100px; margin: 0px 5px 8px 19px;',
                      'clickEvent': function() {
                        this.$el.parent().prev().children().val('');
                      }
                    }}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'display: table-cell;'}, content: [
                    {kind: OB.COMP.SmallButton, attr: {'className': 'btnlink-yellow', 'icon': 'btn-icon btn-icon-search', 'style': 'width: 100px; margin: 0px 0px 8px 5px;',
                      'clickEvent': function() {
                        var criteria = {};
                        if (me.bpname.val() && me.bpname.val() !== '') {
                          criteria._identifier  = {
                            operator: OB.Dal.CONTAINS,
                            value: me.bpname.val()
                          };
                        }
                        me.searchAction(criteria);
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
                {kind: OB.COMP.TableView, id: 'tableview', attr: {
                  collection: this.bps,
                  renderEmpty: function () {
                    return (
                      {kind: B.KindJQuery('div'), attr: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc'}, content: [
                        OB.I18N.getLabel('OBPOS_SearchNoResults')
                      ]}
                    );
                  },
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