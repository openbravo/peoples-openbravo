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
                             this.model.get('netSales').toString()
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
                         tax.taxName+' * '+ me.model.get('netSales').toString()
                   ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                        tax.taxAmount.toString()
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
                 this.model.get('grossSales').toString()
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
                       this.model.get('netReturns').toString()
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
                     tax.taxName+' * '+ me.model.get('netReturns').toString()
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                         tax.taxAmount.toString()
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
                   this.model.get('grossReturns').toString()
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
               this.model.get('totalRetailTransactions').toString()
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
                   drop.amount.toString()
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
                    OB.I18N.getLabel('OBPOS_LblTotalDrops')
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                     dropsAmount.toString()
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
                     deposit.amount.toString()
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
                       depositsAmount.toString()
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
