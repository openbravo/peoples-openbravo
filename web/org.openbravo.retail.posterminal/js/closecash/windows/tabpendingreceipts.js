/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  var me = this;
  OB.COMP.PendingReceipts = OB.COMP.CustomView.extend({
  _id: 'pendingreceipts',
   createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'pendingreceipts', 'class': 'tab-pane'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;'}, content: [

                     OB.I18N.getLabel('OBPOS_LblStep1of3')

                  ]}
                ]}
              ]},
              {kind: B.KindJQuery('div')},
              {kind: OB.COMP.ListPendingReceipts}
            ]}
          ]}
        ]}
      );
    }
  });

}());