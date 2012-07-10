/*global define, B , $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderPaymentLines = OB.COMP.CustomView.extend({
	_id:'renderpaymentlines',
    render: function() {
     var me = this;
     if(!this.options.modeldaycash.paymentmethods){
     this.options.modeldaycash.paymentmethods= new OB.MODEL.PaymentMethodCol();
     }
     this.options.modeldaycash.paymentmethods.each(function(payment){
      me.$el.append(B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                   payment.get('name')+' '+OB.I18N.getLabel('OBPOS_LblExpected')
             ]},
            {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                       OB.I18N.formatCurrency(payment.get('expected'))
           ]},
           {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
        ]},
        {kind: B.KindJQuery('div')}
      ]}
      ).$el);
     });
     me.$el.append(B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
            {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
                   OB.I18N.getLabel('OBPOS_LblTotalExpected')
            ]},
           {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                   OB.I18N.formatCurrency(me.options.modeldaycash.get('totalExpected'))
          ]},
          {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
        ]},
       {kind: B.KindJQuery('div')}
        ]}
      ).$el);
     me.$el.append(B(
        {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
                   {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
              ]}
          ]},
          {kind: B.KindJQuery('div')}
          ]}
      ).$el);
     this.options.modeldaycash.paymentmethods.each(function(payment){
         me.$el.append(B(
         {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                   {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                   {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                      payment.get('name')+' '+OB.I18N.getLabel('OBPOS_LblCounted')
                ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                          OB.I18N.formatCurrency(payment.get('counted'))
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
           ]},
           {kind: B.KindJQuery('div')}
         ]}
         ).$el);
        });
        me.$el.append(B(
         {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
               {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
                      OB.I18N.getLabel('OBPOS_LblTotalCounted')
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                      OB.I18N.formatCurrency(me.options.modeldaycash.get('totalCounted'))
             ]},
             {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
           ]},
          {kind: B.KindJQuery('div')}
           ]},
           {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
               {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
                      {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                 ]}
             ]},
             {kind: B.KindJQuery('div')}
             ]}
         ).$el);
        me.$el.append(B(
        {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
                   {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
              ]}
          ]},
          {kind: B.KindJQuery('div')}
          ]}
      ).$el);
     this.options.modeldaycash.paymentmethods.each(function(payment){
         me.$el.append(B(
         {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                   {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                   {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                      payment.get('name')+' '+ OB.I18N.getLabel('OBPOS_LblDifference')
                ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                          OB.I18N.formatCurrency(OB.DEC.sub(payment.get('counted'),payment.get('expected')))
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
           ]},
           {kind: B.KindJQuery('div')}
         ]}
         ).$el);
        });
        me.$el.append(B(
         {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
               {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
                      OB.I18N.getLabel('OBPOS_LblTotalDifference')
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                      OB.I18N.formatCurrency(me.options.modeldaycash.get('totalDifference'))
             ]},
             {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
           ]},
          {kind: B.KindJQuery('div')}
           ]},
           {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
               {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
                      {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                 ]}
             ]},
             {kind: B.KindJQuery('div')}
             ]}
         ).$el);
      return this;
    }
  });
}());
