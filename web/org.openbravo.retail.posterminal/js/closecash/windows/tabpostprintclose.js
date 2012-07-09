/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.PostPrintClose = OB.COMP.CustomView.extend({
  _id: 'postprintclose',
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'postprintclose', 'class': 'tab-pane'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;'}, content: [
                      OB.I18N.getLabel('OBPOS_LblStep4of4')
                  ]}
                ]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; text-align:center;'}, content: [
                       {kind: B.KindJQuery('img'), attr: {'style': 'padding: 20px 20px 20px 10px;', 'src':'../../utility/ShowImageLogo?logo=yourcompanymenu'}, content:[]},
                       {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px; text-align:center;'}, content:[
                             'User: '+OB.POS.modelterminal.get('context').user._identifier
                         ]},
                         {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 5px 15px 5px; text-align:center;'}, content:[
                            'Time: '+ new Date().toString().substring(3,24)
                        ]}
                     ]}
                  ]}
                ]},
               {kind: OB.COMP.SearchRetailTransactions},
               {kind: OB.COMP.RenderPaymentLines},
               {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
	               {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
	                 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
	                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
	              {kind: B.KindJQuery('div')}
	              ]},    {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
	                 ]}
	             ]}
          ]}
        ]}
       ]}
      );
    }
  });
}());