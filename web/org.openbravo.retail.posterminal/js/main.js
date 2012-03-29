

require.config({
  paths: {
    jQuery: 'libs/jquery/jquery',
    Underscore: 'libs/underscore/underscore',
    Backbone: 'libs/backbone/backbone'
  }
});


require(['builder', 'pointofsalewindow', 'datasource', 'model/terminal', 'components/terminal'], function(B, pos) {
  
  var hwserver = new OB.DS.HWServer();  // 'http://192.168.0.8:8090/printer'
  var modelterminal = new OB.MODEL.Terminal();
  
  var terminal = new OB.COMP.Terminal($("#terminal"), $('#yourcompany'), $('#yourcompanyproperties'));
  terminal.setModel(modelterminal); 
  
  var context = new B.Context();  
  context.set('hwserver', hwserver);
  context.set('modelterminal', modelterminal);  
      
  modelterminal.on('ready', function() {
    $("#container").append(B(pos(), context).$);   
    context.trigger('ready');   
  });    
  
  $(document).ready(function () {
    modelterminal.load();  
    hwserver.print('res/welcome.xml');
  });
  
});