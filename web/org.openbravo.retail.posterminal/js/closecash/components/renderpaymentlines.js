/*global define, B , $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderPaymentLines = OB.COMP.CustomView.extend({
	_id:'renderpaymentlines',
    render: function() {
     var me = this;
     this.options.modeldaycash.paymentmethods.each(function(payment){
      me.$el.append(B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                   payment.get('name')+ ' expected'
             ]},
            {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                       payment.get('expected').toString()
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
                   'Expected amount'
            ]},
           {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                   me.options.modeldaycash.get('totalExpected').toString()
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
                   {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                      payment.get('name')+ ' counted'
                ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                          payment.get('counted').toString()
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
                      'Counted amount'
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                      me.options.modeldaycash.get('totalCounted').toString()
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
                   {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                      payment.get('name')+ ' difference'
                ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                          (payment.get('counted')-payment.get('expected')).toString()
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
                      'Difference amount'
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                      me.options.modeldaycash.get('totalDifference').toString()
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
