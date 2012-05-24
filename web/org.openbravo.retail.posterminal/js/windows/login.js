/*global define, $ */

define(['builder', 'i18n', 
        'components/commonbuttons', 'components/hwmanager', 'components/keyboard'
       ], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.Login = OB.COMP.CustomView.extend({
    createView: function () {
      return (
        {kind: B.KindJQuery('section'), content: [  
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [   
                  {kind: B.KindJQuery('strong'), attr: {'style': 'color: white;'}, content: [   
                     "User Name"                                                                
                  ]}                                                             
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [   
                  {kind: B.KindJQuery('input'), id: 'username', attr: {'id': 'username', 'type': 'text', 'value': 'Openbravo'}}                                                                              
                ]}
              ]},                                                                          
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [   
                  {kind: B.KindJQuery('strong'), attr: {'style': 'color: white;'}, content: [   
                     "Password"                                                                
                  ]}                                                                  
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [   
                  {kind: B.KindJQuery('input'), id: 'password', attr: {'id': 'password', 'type': 'password', 'value': 'openbravo'}, init: function () {
                    this.$el.keyup(function (e) {
                        if(event.keyCode === 13){
                            $("#loginaction").click();
                        }
                    });                  
                  }}                                                                              
                ]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('a'), attr: {'id': 'loginaction', 'class': 'btnlink', 'href': '#'}, content: [
                  {kind: B.KindJQuery('i'), attr: {'class': 'icon-ok  icon-white'}}, ' Log in'
                ], init: function () {
                    this.$el.click(function (e) {
                      e.preventDefault();
                      var u = $('#username').val();
                      var p = $('#password').val();
                      if (!u || !p) {
                        alert('Please enter your username and password');
                      } else {
                        OB.POS.modelterminal.load(u, p);
                      }
                    });
                }}
              ]}
            ]}
          ]}
        ], init: function () {
          this.context.on('domready', function () {
            this.context.username.$el.focus();
          }, this);
        }}

      );           
    }   
  });
  
  return OB.COMP.Login;
});