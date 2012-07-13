/*global B, setInterval */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.CashMgmtInfo = function (context) {
    var me = this;

    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), attr: {'style': 'position: relative; background: #363636; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
          {kind: OB.COMP.Clock, attr: {'className': 'pos-clock'}},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), id: 'msginfo', attr: {'style': 'padding: 10px; float: left; width: 320px; line-height: 23px;'} , content: [OB.I18N.getLabel('OBPOS_LblDepositsDropsMsg')]},
            {kind: B.KindJQuery('div'), id: 'msgaction', attr: {'style': 'padding: 5px; float: right;'}, content: [
              {kind: OB.COMP.SmallButton.extend({attributes: {'href': '#modalCancel', 'data-toggle': 'modal'}, className: 'btnlink-white btnlink-fontgrey'}), attr: {'label': OB.I18N.getLabel('OBPOS_LblCancel')}}
            ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {'align':'center', 'style': 'width: 100%; float: left;'}, content: [{kind: OB.COMP.ButtonNextCashMgmt}]}
        ]}
      ]}
    , context);
//    context.depositsdropsTicket.$el.hide();
    this.$el = this.component.$el;
  };
}());