/*global define, $, Backbone, console */

define(['builder', 'i18n', 'components/clock',
        'components/commonbuttons', 'components/hwmanager', 'components/keyboard'
       ], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.LoginUserButton = Backbone.View.extend({
    tagName: 'div',
    className: 'login-user-button',
    initialize: function () {
      this.component = B(
        {kind: B.KindJQuery('div'), attr: {'class': 'login-user-button-bottom'}, content: [
          {kind: B.KindJQuery('span'), id: 'bottomIcon', attr: {'class': 'login-user-button-bottom-icon'}, content: ['.']},
          {kind: B.KindJQuery('span'), id: 'bottomText', attr: {'class': 'login-user-button-bottom-text'}}
        ]
      });
      this.$el.append(this.component.$el);

      this.$bottomIcon = this.component.context.bottomIcon.$el;
      this.$bottomText = this.component.context.bottomText.$el;
      this.$defaultPassword = 'openbravo';
      var me = this;
      this.$el.click(function (e) {
        e.preventDefault();
        var u = me.$user,
            p = me.$defaultPassword;
        $('#username').val(u);
        $('#password').val(p);
        OB.POS.modelterminal.login(u, p);
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
              {kind: B.KindJQuery('div'), id: 'loginUserContainer', attr: {'class': 'row login-user-container'}, content: ['.']}
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
  
                        OB.POS.modelterminal.on('loginfail', function (status) {
                          alert('Invalid user name or password.\nPlease try again.');
                          $('#password').val('');
                          $('#username').focus();
                        });
                        
                        this.$el.click(function (e) {
                          e.preventDefault();
                          var u = $('#username').val();
                          var p = $('#password').val();
                          if (!u || !p) {
                            alert('Please enter your username and password');
                          } else {
                            OB.POS.modelterminal.login(u, p);
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
            var me = this;
            function setUserImages(jsonImgData) {
              var name = [],
                  userName = [],
                  image = [],
                  connected = [];
              jsonImgData = jsonImgData.response[0].data;
              $.each(jsonImgData, function(k,v){
                name.push(v.name);
                userName.push(v.userName);
                image.push(v.image);
                connected.push(v.connected);
              });
              var content = {}, i,
                  target = me.context.loginUserContainer.$el,
                  isFirstTime = true;
              for (i=0; i<name.length; i++) {
                if (isFirstTime) {
                  target[0].innerHTML = '';
                  isFirstTime = false;
                }
                content = B({kind: OB.COMP.LoginUserButton, attr: {'user': userName[i], 'userImage': image[i], 'userConnected': connected[i]}, content: [name[i]]}).$el;
                target.append(content);
              }

            }
            $.ajax({
              url: '../../org.openbravo.service.retail.posterminal.loginutils',
              contentType: 'application/json;charset=utf-8',
              dataType: 'json',
              data: {
                terminalName: OB.POS.paramTerminal
              },
              success: function (data, textStatus, jqXHR) {
                setUserImages(data);
              }
            });

            this.context.username.$el.focus();
          }, this);
        }}

      );
    }
  });

  return OB.COMP.Login;
});