/*global define, B , $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderRetailTransactions = OB.COMP.CustomView.extend({
  _id:'renderretailtransactions',
    render: function() {
      this.me.options.modeldaycash.transactions = this.me.transactions;
      var me = this, dropsAmount = OB.DEC.Zero, depositsAmount = OB.DEC.Zero;
      me.$el.append(B(
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         OB.I18N.getLabel('OBPOS_LblNetSales')
                   ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                             OB.I18N.formatCurrency(this.model.get('netSales'))
                 ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},
              {kind: B.KindJQuery('div')}
            ]}
      ).$el);
      this.model.get('salesTaxes').forEach(function(tax){
        me.$el.append(B(
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         tax.taxName+' * '+ OB.I18N.formatCurrency(me.model.get('netSales'))
                   ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                        OB.I18N.formatCurrency(tax.taxAmount)
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
                 OB.I18N.getLabel('OBPOS_LblGrossSales')
          ]},
         {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                 OB.I18N.formatCurrency(this.model.get('grossSales'))
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
      me.$el.append(B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                   OB.I18N.getLabel('OBPOS_LblNetReturns')
             ]},
            {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                       OB.I18N.formatCurrency(this.model.get('netReturns'))
           ]},
           {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
        ]},
        {kind: B.KindJQuery('div')}
      ]}
       ).$el);
      this.model.get('returnsTaxes').forEach(function(tax){
        me.$el.append(B(
        {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                     tax.taxName+' * '+ OB.I18N.formatCurrency(me.model.get('netReturns'))
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                         OB.I18N.formatCurrency(tax.taxAmount)
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
                   OB.I18N.getLabel('OBPOS_LblGrossReturns')
            ]},
           {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                   OB.I18N.formatCurrency(this.model.get('grossReturns'))
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
      me.$el.append(B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
        {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
            {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
               OB.I18N.getLabel('OBPOS_LblTotalRetailTrans')
             ]},
             {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
               OB.I18N.formatCurrency(this.model.get('totalRetailTransactions'))
              ]},
             {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
           ]}
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
      this.model.get('drops').forEach(function(drop){
      dropsAmount = OB.DEC.add(dropsAmount,drop.amount);
      me.$el.append(B(
      {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                   drop.description
             ]},
            {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                   OB.I18N.formatCurrency(drop.amount)
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
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;' }, content: [
                    OB.I18N.getLabel('OBPOS_LblTotalWithdrawals')
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                     OB.I18N.formatCurrency(dropsAmount)
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
      this.model.get('deposits').forEach(function(deposit){
        depositsAmount = OB.DEC.add(depositsAmount,deposit.amount);
        me.$el.append(B(
        {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                     deposit.description
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                     OB.I18N.formatCurrency(deposit.amount)
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
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;' }, content: [
                      OB.I18N.getLabel('OBPOS_LblTotalDeposits')
                 ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                       OB.I18N.formatCurrency(depositsAmount)
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
      return this;
    }
  });
}());
