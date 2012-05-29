/*global define, $, Backbone, console */

define(['builder', 'i18n', 'components/clock',
        'components/commonbuttons', 'components/hwmanager', 'components/keyboard'
       ], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.LoginUserButton = Backbone.View.extend({
    tagName: 'div',
    className: 'login-user-button',
    click: function() {
      console.log('a');
    },
    initialize: function () {
      this.component = B(
        {kind: B.KindJQuery('div'), id: 'paco', attr: {'class': 'login-user-button-bottom'}, content: [
          {kind: B.KindJQuery('span'), id: 'bottomIcon', attr: {'class': 'login-user-button-bottom-icon'}, content: ['.']},
          {kind: B.KindJQuery('span'), id: 'bottomText', attr: {'class': 'login-user-button-bottom-text'}}
        ]
      });
      this.$el.append(this.component.$el);

      this.$bottomIcon = this.component.context.bottomIcon.$el;
      this.$bottomText = this.component.context.bottomText.$el;
      // this.$user;
      this.$defaultPassword = 'openbravo';
      var me = this;
      this.$el.click(function (e) {
        e.preventDefault();
        var u = me.$user,
            p = me.$defaultPassword;
        $('#username').val(u);
        $('#password').val(p);
        OB.POS.modelterminal.load(u, p);
      });
    },
    append: function (child) {
      if (child.$el) {
        this.$bottomText.append(child.$el);
      }
    },
    attr: function (attributes) {
      if (attributes.userImage && attributes.userImage !== 'none') {
        this.$el.attr('style', 'background-image: url("' + attributes.userImage + '")');
      }
      if (attributes.userConnected === 'true') {
        this.$bottomIcon.attr('style', 'background-image: url("img/login-connected.png");');
      }
      if (attributes.user) {
        this.$user = attributes.user;
      }
    }
  });
  
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
              {kind: B.KindJQuery('div'), id: 'loginUserContainer', attr: {'class': 'row login-user-container'}}
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
                    {kind: OB.COMP.Clock, attr: {'className': 'login-clock'}}
                  ]}
                ]}
              ]}
            ]}
          ]}
        ], init: function () {
          this.context.on('domready', function () {
            var returnedJson = { //TODO: Obtain this JSON from a webservice with real data
              1 : {
                user: 'Openbravo',
                userName: 'Openbravo',
                image: 'none',
                connected: 'true'
             }, 2 : {
                user: 'test',
                userName: 'Test User',
                image: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAAGXcA1uAAAAB3RJTUUH2wgCAAoreAfUzAAAAAlwSFlzAABOIAAATiABFn2Z3gAAAARnQU1BAACxjwv8YQUAAAKfSURBVHjapVQ9b9pAGLYRzAUlY5WyVBkYKFs6oJiOzVDWKEP5CckviPsL2v6C0CFpIqUqVApqUIUdMZBKSDEDQzMQkg4MSNgSg5EYrs/r3tHzB9RVX+nx+e699/OeO0VZJip9Ph4fm483NrbD6tFoxGhM0Baz1bryaV3XNYH0Uu/9ft8zT4ooQpHL5dQv9Tpbakm7GY+RBsI7KTWRns9CDiyC10VwCtxut1mxWFQTmL+SrE17MvE1pYaBirN29/b2aY0x5tBcVVVNeIROS3AnC2+2bQ8B5jhODQh1NiH960Dta6NRzmQyKoxMQBkMBhrX6bJBQTJMIwXte6djTSaTN4DxdHNzoUzy8Qb5qRH9tZqXlxTBWFtfLylx5ez0TIu9OSgLrom2oZ2COzaQOT05oWKfAQ705ZAHEMERo/iXjz4ZjDCbzR6hnTUasXQP3TseIVrAKF0wCwgVK2pYKBDFi4YD81LAedAZUV3Gktb/FlC0QjQFtKgaQqFB3Wq32z0izzyTQqitAbFwrSmV10AJnI/aExZQgxHEPLkighcF7b3H+CSWdxLwKB17c1BAmyygA+W4NuI6iDbLlCeuWfP5vIL/bCqV0nG9h5iT7jMf32N9H7bEHDnz8BnLz5As0+mUceh8npbWIm1kX0lpvRDgh1cBDuEA/2+Bw/F4fIi57OuAOwxWUIgKoAc2kaHyrdmkl1D9+fBAOnJUVf6cOp0FXa6K4r9gDtf9vUVRcvvjdggwDjNui1bxVRbLnbmU7RCgJ2K71+uZ+Xxei/KhxnAYKded6yyGO2npauv5lhbctwgQQdNV4hHAaBlUyY0cpPSi5AviC7DyfVoijYuLCoYjaenDy52dipisommsSuCs+un8nBhGVKZ3xfzXJP9LfgFzgJUOj4I2DQAAAABJRU5ErkJggg==',
                connected: 'false'
                }
            };
            var user = [],
                userName = [],
                image = [],
                connected = [];
            $.each(returnedJson, function(k,v){
              user.push(v.user);
              userName.push(v.userName);
              image.push(v.image);
              connected.push(v.connected);
            });
            var content = {}, i,
                target = this.context.loginUserContainer.$el;
            for (i=0; i<user.length; i++) {
              content = B({kind: OB.COMP.LoginUserButton, attr: {'user': user[i], 'userImage': image[i], 'userConnected': connected[i]}, content: [userName[i]]}).$el;
              target.append(content);
            }

            this.context.username.$el.focus();
          }, this);
        }}

      );
    }
  });

  return OB.COMP.Login;
});