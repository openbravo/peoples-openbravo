/*global define, $ */

define(['builder', 'utilities', 'utilitiesui', 'components/commonbuttons', 'arithmetic', 'i18n'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderPayments = OB.COMP.CustomView.extend({
    render: function() {
      this.$el.append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
               {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 36%'}, content: [
                    this.model.get('_identifier')
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 17px 20px 17px 10px; border-bottom: 1px solid #cccccc; float: left; width: 20%'}, content: [
                    this.model.get('createdBy')
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': ' border-bottom: 1px solid #cccccc; float: left; width: 33%'}, content: [
                       {kind: OB.COMP.ButtonEdit.extend({_id :'editbutton_'+this.model.get('id'),rowid :this.model.get('id')})}, 
                       {kind: OB.COMP.ButtonOk.extend({_id :'okbutton_'+this.model.get('id'),rowid :this.model.get('id')})},
                       {kind: B.KindJQuery('div'), id :'counted_'+this.model.get('id') , rowid :this.model.get('id'), attr:{'hidden':'hidden','style':'padding: 17px 110px 17px 0px; float: right; width: 10%'},
                        content:[this.model.get('createdBy')]
                       }
                  ]}

               ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}
        ]}
      ).$el);
      return this;
    }
  });
});
