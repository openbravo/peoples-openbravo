/*global require,$, _, Backbone, confirm, OB */

require.config({
  paths: {
    jQuery: 'libs/jquery/jquery',
    Underscore: 'libs/underscore/underscore',
    Backbone: 'libs/backbone/backbone',
    text: 'libs/text-1.0.8.min'
  }
});

require(['builder', 'loginwindow', 'utilitiesui', 'arithmetic', 'datasource', 'model/terminal', 'components/terminal'], function(B, login) {
  
  var modelterminal = new OB.MODEL.Terminal();
  
  var terminal = new OB.COMP.Terminal($("#terminal"), $('#yourcompany'), $('#yourcompanyproperties'), $('#loggeduser'), $('#loggeduserproperties'));
  terminal.setModel(modelterminal); 
  
  // alert all errors
  window.onerror = function (e) {
    alert(e);
  };
  
  // global components.
  OB.POS = {
      modelterminal: modelterminal,
  
      logout: function (callback) {
        if (confirm('Are you sure that you want to logout from the application?')) {
          $.ajax({
            url: '../../org.openbravo.service.retail.posterminal.jsonrest/logout?auth=false',
            contentType: 'application/json;charset=utf-8',
            dataType: 'json',
            type: 'GET',
            success: function (data, textStatus, jqXHR) {
              modelterminal.load();  
            },
            error: function (jqXHR, textStatus, errorThrown) {
              modelterminal.load();  
            }
          });
        }
      }
  };
  
  OB.Constants = {
      FIELDSEPARATOR: '$'
  };
  
  modelterminal.on('ready', function () {
    // We are Logged !!!
    
    $('#logoutaction').css('visibility', 'visible');
    
    // Set Hardware..
    OB.POS.hwserver = new OB.DS.HWServer(modelterminal.get('terminal').hardwareurl, modelterminal.get('terminal').scaleurl);
    
    // Set Arithmetic properties:
    OB.DEC.setContext(OB.POS.modelterminal.get('currency').pricePrecision, BigDecimal.prototype.ROUND_HALF_EVEN);  
    
    var webwindowname = "../../" + (OB.UTIL.getParameterByName("window") || "org.openbravo.retail.posterminal/windows/pointofsale");
    
    require([webwindowname], function (webwindow) { // load window...
      var c = _.extend({}, Backbone.Events);
      $("#containerwindow").empty().append(B(webwindow(), c).$el);   
      c.trigger('domready'); 
    });
  });    
  
  modelterminal.on('fail', function (exception) {
    // We are not logged...     
    $('#logoutaction').css('visibility', 'hidden');
    
    if (exception.status === 401 && exception.username) {
      // The user tried to log in
      alert('Invalid user name or password.\nPlease try again.');
    }
    
    var c = _.extend({}, Backbone.Events);
    $("#containerwindow").empty().append(B(login(), c).$el);   
    c.trigger('domready'); 
  });
  
  $(document).ready(function () {
    // Entry Point
    modelterminal.load();  
  });
  
});