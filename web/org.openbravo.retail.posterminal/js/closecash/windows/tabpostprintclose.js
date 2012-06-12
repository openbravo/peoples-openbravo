/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n'], function (B) {

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

                      OB.I18N.getLabel('OBPOS_LblStep3of3')

                  ]}
                ]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; text-align:center;'}, content: [

                       {kind: B.KindJQuery('img'), attr: {'style': 'padding: 20px 20px 20px 10px;', 'src':'http://www.openbravo.com/img-corp/logotypes/ob-logo.gif'}, content:[]},
                       {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px; text-align:center;'}, content:[
                             'User: Salvador'
                         ]},
                         {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 5px 15px 5px; text-align:center;'}, content:[
                            'Time: '+ new Date().toString().substring(3,24)
                        ]}
                     ]}
                  ]}
                ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         'Gross Sales'
                   ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                             '10.000,00'
                 ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},

              {kind: B.KindJQuery('div')}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         'Gross Sales'
                   ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                             '10.000,00'
                 ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},

              {kind: B.KindJQuery('div')}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         'Gross Sales'
                  ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                         '10.000,00'
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},

              {kind: B.KindJQuery('div')}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         'Gross Sales'
                  ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                         '10.000,00'
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},

              {kind: B.KindJQuery('div')}
            ]}
          ]}
        ]}
       ]}
      );
    }
  });

});