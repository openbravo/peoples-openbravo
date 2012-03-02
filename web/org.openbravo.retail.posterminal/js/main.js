

require.config({
  paths: {
    jQuery: 'libs/jquery/jquery',
    Underscore: 'libs/underscore/underscore',
    Backbone: 'libs/backbone/backbone'
  }
});


require(["pointofsalewindow", 'datasource', 'model/terminal', 'components/terminal'], function(pos) {
  
  var hwserver = new OB.DS.HWServer('http://192.168.0.8:8090/printer');  
  var modelterminal = new OB.MODEL.Terminal();
  
  var terminal = new OB.COMP.Terminal($("#terminal"), $("#status"));
  terminal.setModel(modelterminal); 
  
  // Call the function window...
  pos(modelterminal, hwserver);
  
  $(document).ready(function () {
    modelterminal.load();  
    hwserver.print('res/welcome.xml');
  });
  
});