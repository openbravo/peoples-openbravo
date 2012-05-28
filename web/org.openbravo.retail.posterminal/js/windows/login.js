/*global define, $ */

define(['builder', 'i18n', 'components/clock',
        'components/commonbuttons', 'components/hwmanager', 'components/keyboard'
       ], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.Login = OB.COMP.CustomView.extend({
    createView: function () {
      return (
        {kind: B.KindJQuery('section'), content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row login-header-row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'login-header-company'}},
              {kind: B.KindJQuery('div'), attr: {'class': 'login-header-ob'}}
            ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row login-user-container'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'login-user-button'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'login-user-button-bottom'}, content: [
                    {kind: B.KindJQuery('span'), attr: {'class': 'login-user-button-bottom-icon', 'style': 'color: transparent;'}, content: ['.']},
                    {kind: B.KindJQuery('span'), attr: {'class': 'login-user-button-bottom-text'}, content: ['Openbravo']}
                  ]}
                ], init: function () {
                  this.$el.click(function (e) {
                    e.preventDefault();
                    var u = $('#username').val('Openbravo');
                    var p = $('#password').val('');
                     OB.POS.modelterminal.load(u, 'openbravo');
                  });
                }}
              ]}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid login-inputs-container'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span6 login-inputs-screenlocked'}, content: ['Screen locked']}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span6 login-inputs-userpassword'}, content: [
                    {kind: B.KindJQuery('input'), id: 'username', attr: {'id': 'username', 'type': 'text', 'placeholder': 'User'}}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span6 login-inputs-userpassword'}, content: [
                    {kind: B.KindJQuery('input'), id: 'username', attr: {'id': 'password', 'type': 'password', 'placeholder': 'Password'}}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span1', 'style': 'color: transparent;'}, content: ['.']},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span1', 'style': 'color: transparent;'}, content: ['.']},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
                    {kind: B.KindJQuery('a'), attr: {'id': 'loginaction', 'class': 'login-inputs-button', 'href': '#'}, content: ['Log In'],
                      init: function () {
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
                      }
                    }
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span1', 'style': 'color: transparent;'}, content: ['.']},
                  {kind: B.KindJQuery('div'), attr: {'class': 'span1'}, content: [
                    {kind: OB.COMP.Clock, attr: {'containerClassName': 'login-clock-container', 'clockClassName': 'login-clock-time', 'dateClassName': 'login-clock-date'}}
                  ]}
                ]}
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