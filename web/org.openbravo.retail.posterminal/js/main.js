/*global require,$, _, Backbone, confirm, OB */

require.config({
  paths: {
    jQuery: 'libs/jquery/jquery',
    Underscore: 'libs/underscore/underscore',
    Backbone: 'libs/backbone/backbone'
  }
});


require(['builder', 'utilitiesui', 'loginwindow', 'arithmetic', 'datasource', 'model/terminal', 'components/terminal'], function(B, login) {
  
  var modelterminal = new OB.MODEL.Terminal();
  
  var terminal = new OB.COMP.Terminal($("#terminal"), $('#yourcompany'), $('#yourcompanyproperties'));
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
  
  modelterminal.on('ready', function () {
    // We are Logged !!!
    
    $('#logoutaction').css('visibility', 'visible');
    
    // Set Hardware..
    OB.POS.hwserver = new OB.DS.HWServer(modelterminal.get('terminal').hardwareurl);
    OB.POS.hwserver.print('res/welcome.xml', {}, function (e) {
      if (e.exception) {
        OB.UTIL.showError(e.exception.message);
      }   
    });
    
    // Set Arithmetic properties:
    OB.DEC.setContext(OB.POS.modelterminal.get('currency').pricePrecision, BigDecimal.prototype.ROUND_HALF_EVEN);  
    
    var webwindowname = "../../" + (OB.UTIL.getParameterByName("window") || "org.openbravo.retail.posterminal/windows/pointofsale");
    
    require([webwindowname], function (webwindow) { // load window...
      var c = _.extend({}, Backbone.Events);
      $("#containerwindow").empty().append(B(webwindow(), c).$);   
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
    $("#containerwindow").empty().append(B(login(), c).$);   
    c.trigger('domready'); 
  });
  
  $(document).ready(function () {
    // Entry Point
    modelterminal.load();  
  });
  
});