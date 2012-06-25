/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.DepositsDrops = OB.COMP.CustomView.extend({
	  _id: 'depositsdrops',
	    createView: function () {
	      return (
	        {kind: B.KindJQuery('div'), attr: {'id': 'countcash', 'class': 'tab-pane'}, content: [
	          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
	            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
	              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
	                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
	                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc;text-align:center;'}, content: [
	                     OB.I18N.getLabel('OBPOS_LblDepositsDrops')
	                  ]}
	                ]}
	              ]},
	              {kind: B.KindJQuery('div')},
                   {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                     {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: left; width: 69%'}, content: [
                     OB.I18N.getLabel('OBPOS_LblTotalCash')
                  ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
                   '0,0'
              ]}
                ]}
             ]},
             {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: left; width: 69%'}, content: [
                     OB.I18N.getLabel('OBPOS_LblTotalAvailableCash')
                  ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
                    '250,00'
                 ]}
                ]}
              ]}
	            ]}
	          ]}
	        ]}
	      );
	    }
	  });
}());