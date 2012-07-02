/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Terminal = function (yourterminal, yourcompany, yourcompanyproperties, loggeduser, loggeduserproperties) {
    this.yourterminal = yourterminal;
    this.yourcompany = yourcompany;
    this.yourcompanyproperties = yourcompanyproperties;
    this.loggeduser = loggeduser;
    this.loggeduserproperties = loggeduserproperties;
  };

  OB.COMP.Terminal.prototype.setModel = function (terminal) {
    this.terminal = terminal;

    this.terminal.on('change:context', function() {
      var ctx = this.terminal.get('context');
      if (ctx) {
        this.loggeduser.text(ctx.user._identifier);
        this.loggeduserproperties.empty();
        this.loggeduserproperties.append(B(
          {kind: B.KindJQuery('div'), attr: {style: 'height: 60px; background-color: #FFF899;'}, content: [
            {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 55px; margin: 6px 0px 0px 6px;'}, content: [
              {kind: OB.UTIL.Thumbnail, attr: {img: ctx.img, 'default': 'img/anonymous-icon.png'}}
            ]},
            {kind: B.KindJQuery('div'), attr: {style: 'float: left; margin: 6px 0px 0px 0px; line-height: 150%;'}, content: [
              {kind: B.KindJQuery('div'), content: [
                {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: 600; margin: 0px 0px 0px 5px;'}, content: [
                  ctx.user._identifier
                ]}
              ]},
              {kind: B.KindJQuery('div'), content: [
                {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: 600; margin: 0px 0px 0px 5px;'}, content: [
                  ctx.role._identifier
                ]}
              ]}
            ]}
          ]}
        ).$el);
        this.loggeduserproperties.append(B(
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), attr: {style: 'height: 5px;'}},
            {kind: OB.COMP.MenuAction.extend({clickEvent: function() { $('#profileDialog').modal('show'); }, label: OB.I18N.getLabel('OBPOS_LblProfile')})},
            {kind: B.KindJQuery('div'), attr: {style: 'height: 5px;'}},
            /*{kind: OB.COMP.MenuAction.extend({clickEvent: function() { OB.POS.lock(); }, label: OB.I18N.getLabel('OBPOS_LogoutDialogLock')})},
            {kind: B.KindJQuery('div'), attr: {style: 'height: 5px;'}},*/ //Disabled until feature be ready
            {kind: OB.COMP.MenuAction.extend({clickEvent: function() { $('#logoutDialog').modal('show'); }, label: OB.I18N.getLabel('OBPOS_LogoutDialogLogout')})},
            {kind: B.KindJQuery('div'), attr: {style: 'height: 5px;'}}
          ]}
        ).$el);
      } else {
        this.loggeduser.text('');
        this.loggeduserproperties.empty();
      }

    },this);

    this.terminal.on('change:terminal change:bplocation change:location change:pricelist change:pricelistversion', function () {
      var name = '';
      var clientname = '';
      var orgname = '';
      var pricelistname = '';
      var currencyname = '';
      var locationname = '';

      if (this.terminal.get('terminal')) {
        name = this.terminal.get('terminal')._identifier;
        clientname = this.terminal.get('terminal')['client' + OB.Constants.FIELDSEPARATOR + '_identifier'];
        orgname = this.terminal.get('terminal')['organization' + OB.Constants.FIELDSEPARATOR + '_identifier'];
      }
      if (this.terminal.get('pricelist')) {
        pricelistname = this.terminal.get('pricelist')._identifier;
        currencyname = this.terminal.get('pricelist')['currency' + OB.Constants.FIELDSEPARATOR + '_identifier'];
      }
      if (this.terminal.get('location')) {
        locationname = this.terminal.get('location')._identifier;
      }

      this.yourterminal.text(name);
      this.yourcompany.text(orgname);
      this.yourcompanyproperties.empty().append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('span'), content: [
              OB.I18N.getLabel('OBPOS_CompanyClient')
            ]},
            {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'}, content: [
              clientname
            ]}
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('span'), content: [
              OB.I18N.getLabel('OBPOS_CompanyOrg')
            ]},
            {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'}, content: [
              orgname
            ]}
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('span'), content: [
              OB.I18N.getLabel('OBPOS_CompanyPriceList')
            ]},
            {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'}, content: [
              pricelistname
            ]}
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('span'), content: [
              OB.I18N.getLabel('OBPOS_CompanyCurrency')
            ]},
            {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'}, content: [
              currencyname
            ]}
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('span'), content: [
              OB.I18N.getLabel('OBPOS_CompanyLocation')
            ]},
            {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: bold; margin: 0px 0px 0px 5px;'}, content: [
              locationname
            ]}
          ]}
        ]}
      ).$el);
    }, this);

  };

}());