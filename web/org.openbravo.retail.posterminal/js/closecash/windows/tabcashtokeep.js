/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

//  var sdf = new SimpleDateFormat("HH:mm:ss");
//  document.write(sdf.format(new Date()));

  OB.COMP.CashToKeep = OB.COMP.CustomView.extend({
  _id: 'cashtokeep',
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'countcash', 'class': 'tab-pane'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;text-align:center;'}, content: [

                     OB.I18N.getLabel('OBPOS_LblStep3of4')

                  ]}
                ]}
              ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black;'}, content: [
                     {kind: B.KindJQuery('div'), attr: {'class': 'btn-group','data-toggle':'buttons-radio' }, content: [
                        {kind: OB.COMP.RadioButton.extend({attributes: {'class': 'btn', 'id': 'keepfixedamount' }})},
                        {kind: B.KindJQuery('div'), attr: { 'id': 'keepfixedamountlbl' }, content: []},
                        {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}},

                        {kind: OB.COMP.RadioButton.extend({attributes: {'class': 'btn', 'id': 'allowmoveeverything' }})},
                        {kind: B.KindJQuery('div'), attr: { 'id': 'allowmoveeverythinglbl' }, content: []},
                        {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}},

                        {kind: OB.COMP.RadioButton.extend({attributes: {'class': 'btn', 'id': 'allowdontmove' }})},
                        {kind: B.KindJQuery('div'), attr: {'id': 'allowdontmovelbl' }, content: []},
                        {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}},

                        {kind: OB.COMP.RadioButton.extend({attributes: {'class': 'btn', 'id': 'allowvariableamount' }})},
                        {kind: B.KindJQuery('div'), attr: { 'id': 'allowvariableamountlbl' }, content: []},
                        {kind: B.KindJQuery('input'), attr: {'class': 'span1', 'id': 'variableamount', 'type':'text' }, content: []}
                     ]}
                  ]}
            ]}
          ]}
        ]}
      );
    }
  });

}());