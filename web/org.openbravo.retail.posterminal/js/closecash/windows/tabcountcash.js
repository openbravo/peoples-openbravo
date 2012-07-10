/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

//  var sdf = new SimpleDateFormat("HH:mm:ss");
//  document.write(sdf.format(new Date()));

  OB.COMP.CountCash = OB.COMP.CustomView.extend({
  _id: 'countcash',
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'countcash', 'class': 'tab-pane'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;text-align:center;'}, content: [

                     OB.I18N.getLabel('OBPOS_LblStep2of4')

                  ]}
                ]}
              ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black;'}, content: [
                   {kind: OB.COMP.ListPaymentMethods}
                 ]}
            ]}
          ]}
        ]}
      );
    }
  });

}());