/*global B, $, Backbone, console */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  OB.UTIL = window.OB.UTIL || {};
  OB.UTIL.loginButtonAction = function () {
    var u = $('#username').val();
    var p = $('#password').val();
    if (!u || !p) {
      alert('Please enter your username and password');
    } else {
      OB.POS.modelterminal.login(u, p);
    }
  };

  OB.COMP.LoginUserButton = Backbone.View.extend({
    tagName: 'div',
    className: 'login-user-button',
    initialize: function () {
      this.component = B(
        {kind: B.KindJQuery('div'), attr: {'class': 'login-user-button-bottom'}, content: [
          {kind: B.KindJQuery('div'), id: 'bottomIcon', attr: {'class': 'login-user-button-bottom-icon'}, content: ['.']},
          {kind: B.KindJQuery('div'), id: 'bottomText', attr: {'class': 'login-user-button-bottom-text'}}
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
        //$('#password').val(p); // Disable 'openbravo' autologin feature
        $('#password').val(''); // Disable 'openbravo' autologin feature
        $('#password').focus(); // Disable 'openbravo' autologin feature
        //OB.POS.modelterminal.login(u, p, 'userImgPress'); // Disable 'openbravo' autologin feature
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
        this.$bottomIcon.attr('style', 'background-image: url("img/iconOnlineUser.png");');
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
              {kind: B.KindJQuery('div'), id: 'loginHeaderCompany', attr: {'class': 'login-header-company'}},
              {kind: B.KindJQuery('div'), attr: {'class': 'login-header-ob'}}
            ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), id: 'loginUserContainer', attr: {'class': 'row login-user-container'}, content: ['.']}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid login-inputs-container'}, content: [
                {kind: B.KindJQuery('div'), attr: {'id': 'login-inputs'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span6 login-inputs-screenlocked'}, content: [OB.I18N.getLabel('OBPOS_LoginScreenLocked')]}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span6 login-inputs-userpassword'}, content: [
                      {kind: B.KindJQuery('input'), id: 'username', attr: {'id': 'username', 'class': 'login-inputs-username', 'type': 'text', 'placeholder': OB.I18N.getLabel('OBPOS_LoginUserInput'), 'onkeydown': 'if(event && event.keyCode == 13) { OB.UTIL.loginButtonAction(); }; return true;'}}
                    ]}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span6 login-inputs-userpassword'}, content: [
                      {kind: B.KindJQuery('input'), id: 'password', attr: {'id': 'password', 'class': 'login-inputs-password', 'type': 'password', 'placeholder': OB.I18N.getLabel('OBPOS_LoginPasswordInput'), 'onkeydown': 'if(event && event.keyCode == 13) { OB.UTIL.loginButtonAction(); }; return true;'}}
                    ]}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span1', 'style': 'color: transparent;'}, content: ['.']},
                    {kind: B.KindJQuery('div'), attr: {'class': 'span1', 'style': 'color: transparent;'}, content: ['.']},
                    {kind: B.KindJQuery('div'), attr: {'class': 'span2', 'style': 'margin: 20px 0px 0px 0px; text-align: center;'}, content: [
                      {kind: OB.COMP.ModalDialogButton, 'id': 'loginaction', attr: {'id': 'loginaction', 'label': OB.I18N.getLabel('OBPOS_LoginButton'),
                        'clickEvent': function() {
                          OB.UTIL.loginButtonAction();
                        }
                      }}
                    ]},
                    {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
                      {kind: OB.COMP.Clock, attr: {'className': 'login-clock'}}
                    ]}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'id': 'login-browsernotsupported', 'style': 'display: none;'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span6 login-browsernotsupported-title'}, content: [
                      OB.I18N.getLabel('OBPOS_LoginBrowserNotSupported')
                    ]}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span4'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'class': 'login-browsernotsupported-content'}, content: [
                        OB.I18N.getLabel('OBPOS_LoginBrowserNotSupported_P1')
                      ]},
                      {kind: B.KindJQuery('div'), attr: {'class': 'login-browsernotsupported-content'}, content: [
                        OB.I18N.getLabel('OBPOS_LoginBrowserNotSupported_P2', ['Chrome, Safari, Safari (iOS)','Android'])
                      ]},
                      {kind: B.KindJQuery('div'), attr: {'class': 'login-browsernotsupported-content'}, content: [
                        OB.I18N.getLabel('OBPOS_LoginBrowserNotSupported_P3')
                      ]}
                    ]},
                    {kind: B.KindJQuery('div'), attr: {'class': 'span2'}, content: [
                      {kind: OB.COMP.Clock, attr: {'className': 'login-browsernotsupported-clock'}}
                    ]}
                  ]}
                ]}
              ]}
            ]}
          ]}
        ], init: function () {
          OB.POS.modelterminal.on('loginfail', function (status, data) {
            var msg;
            if (data && data.messageTitle) {
              msg = data.messageTitle;
            }

            if (data && data.messageText) {
              msg += (msg?'\n':'')+data.messageText;
            }
            msg = msg || 'Invalid user name or password.\nPlease try again.';
            alert(msg);
            $('#password').val('');
            $('#username').focus();
          });
          OB.POS.modelterminal.on('loginUserImgPressfail', function (status) {
            //If the user image press (try to login with default password) fails, then no alert is shown and the focus goes directly to the password input
            $('#password').val('');
            $('#password').focus();
          });
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
                  target.html('');
                  isFirstTime = false;
                }
                content = B({kind: OB.COMP.LoginUserButton, attr: {'user': userName[i], 'userImage': image[i], 'userConnected': connected[i]}, content: [name[i]]}).$el;
                target.append(content);
              }
              OB.UTIL.showLoading(false);
              return true;
            }
            function setCompanyLogo(jsonCompanyLogo) {
              var logoUrl = [];
              jsonCompanyLogo = jsonCompanyLogo.response[0].data;
              $.each(jsonCompanyLogo, function(k,v){
                logoUrl.push(v.logoUrl);
              });
              me.context.loginHeaderCompany.$el.css('background-image', 'url("' + logoUrl[0] + '")');
              return true;
            }
            $.ajax({
              url: '../../org.openbravo.retail.posterminal.service.loginutils',
              contentType: 'application/json;charset=utf-8',
              dataType: 'json',
              data: {
                command: 'companyLogo',
                terminalName: OB.POS.paramTerminal
              },
              success: function (data, textStatus, jqXHR) {
                setCompanyLogo(data);
              }
            });
            if (!$.browser.webkit || !window.openDatabase) { //If the browser is not supported, show message and finish.
              $('#login-inputs').css('display', 'none');
              $('#login-browsernotsupported').css('display', 'block');
              OB.UTIL.showLoading(false);
              return true;
            }
            $.ajax({
              url: '../../org.openbravo.retail.posterminal.service.loginutils',
              contentType: 'application/json;charset=utf-8',
              dataType: 'json',
              data: {
                command: 'userImages',
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

}());