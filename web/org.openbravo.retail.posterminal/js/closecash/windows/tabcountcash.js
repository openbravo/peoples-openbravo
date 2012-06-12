/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n' ], function (B) {

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

                     OB.I18N.getLabel('OBPOS_LblStep2of3')

                  ]}
                ]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: left; width: 36%'}, content: [
                       OB.I18N.getLabel('OBPOS_LblPaymentMethod')
                    ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 20px 10px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
                         OB.I18N.getLabel('OBPOS_LblExpected')
                    ]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 0px 10px 0px; border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
                         OB.I18N.getLabel('OBPOS_LblCounted')
                  ]}

                  ]}
                ]},
				{kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; height: 500px; margin: 5px;'}, content: [
                 {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px'}, content: [
                   {kind: OB.COMP.ListPaymentMethods}
                 ]}
               ]}





//              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
//        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
//          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 36%'}, content: [
//    'Cash'
//          ]},
//        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
//           '6.195'
//      ]},
//      {kind: B.KindJQuery('div'), attr: {'style': ' border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
//                       {kind: OB.COMP.ButtonEdit}, {kind: OB.COMP.ButtonOk},
//
//                       {kind: OB.COMP.CustomView.extend({
//                       _id :'countedcash',
//                       createView: function () {
//                          return ({kind: B.KindJQuery('div'),attr:{'style':'padding: 17px 110px 17px 0px; float: right; width: 10%'},
//                        content:[this.options.modeldaycash.attributes.cash]});
//                     }
//                       })}
//    ]}
//
//        ]}
//      ]},
//              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
//        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
//          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 36%'}, content: [
//    'Card'
//          ]},
//        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
//           '1.255,50'
//      ]},
//      {kind: B.KindJQuery('div'), attr: {'style': ' border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
//                     {kind: OB.COMP.ButtonEdit}, {kind: OB.COMP.ButtonOk}
//    ]}
//
//        ]}
//      ]},
//      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
//          {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
//            {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 36%'}, content: [
//      'Voucher'
//            ]},
//          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
//             '1.000'
//        ]},
//        {kind: B.KindJQuery('div'), attr: {'style': ' border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
//                     {kind: OB.COMP.ButtonEdit}, {kind: OB.COMP.ButtonOk}
//      ]}
//
//          ]}
//        ]},
//      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
//        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
//        ]}
//      ]},
//      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
//        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
//          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 36%'}, content: [
//    'TOTAL'
//          ]},
//        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
//           '8.450,50'
//      ]},
//      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 5px 17px 0px; border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
//                   '0,00'
//    ]}
//
//        ]}
//      ]}

            ]}

          ]}
        ]}
      );
    }
  });

});