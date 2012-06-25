/*global B, setInterval */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.CashMgmtInfo = function (context) {
    var me = this;

    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), attr: {'style': 'position: relative; background: darkgray; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
          {kind: OB.COMP.Clock, attr: {'className': 'pos-clock'}},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), id: 'msgaction', attr: {'style': 'padding: 10px;'}, content: [
              {kind: B.KindJQuery('a'), attr: {'data-toggle':'modal', 'href': '#modalCancel', 'class': 'btnlink btnlink-small btnlink-orange', 'style': 'float:right;'}, content: [
                OB.I18N.getLabel('OBPOS_LblCancel')
              ]}
            ]}
          ]},
          {kind: B.KindJQuery('div'), content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 0px 150px 20px 10px; float: left; font-weight:bold; font-size: 140%;'}  , content: [OB.I18N.getLabel('OBPOS_LblDepositsDropsMsg')]}
            ]}
          ,
          {kind: B.KindJQuery('div'), content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 50px;', 'align':'center' }, content: [{kind: OB.COMP.Done}]}
            ]}
        ]}
      ]}
    , context);
    this.$el = this.component.$el;
  };
}());