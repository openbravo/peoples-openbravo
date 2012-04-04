define(['utilities', 'i18n', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Scan = function (context) {
    var me = this;
    
    var msgwelcome = OB.UTIL.EL(
        {tag: 'div', attr: {'style': 'padding: 10px; display: none;'}, content: [
          {tag: 'div', attr: {'style': 'float:right;'}, content: [
            OB.I18N.getLabel('OBPOS_WelcomeMessage')
          ]}
        ]}
    );    
    
    var undoclick;
    
    var txtaction = OB.UTIL.EL(
      {tag: 'div', attr: {'style': 'float:left;'}}       
    );
    
    var msgaction = OB.UTIL.EL(
        {tag: 'div', attr: {'style': 'padding: 10px; display: none;'}, content: [
          txtaction,
          {tag: 'a', attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-orange', 'style': 'float:right;'}, content: [
            'Undo'                       
          ], init: function () {           
              this.click(function(e) {
                e.preventDefault();
                if (undoclick) {
                  undoclick();
                }
              });
          }}                  
        ]}          
    );
    
    this.$ = OB.UTIL.EL(
      {tag: 'div', content: [
        {tag: 'div', attr: {'style': 'background-color: #7da7d9; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
          {tag: 'div', content: [
            msgwelcome,
            msgaction
          ]}                    
        ]}        
      ]}
    );
    
    this.receipt = context.get('modelorder');
    
    this.receipt.on('clear change:undo', function() {
      var undoaction = this.receipt.get('undo');
      if (undoaction) {
        msgwelcome.hide();
        msgaction.show();
        if (undoaction.action === 'deleteline') {
          txtaction.text(OB.I18N.getLabel('OBPOS_DeleteLine', [undoaction.line.get('qty'), undoaction.line.get('productidentifier')]));
        } else if (undoaction.action === 'addline') {
          txtaction.text(OB.I18N.getLabel('OBPOS_AddLine', [undoaction.line.get('qty'), undoaction.line.get('productidentifier')]));
        } else if (undoaction.action === 'add') {
          txtaction.text(OB.I18N.getLabel('OBPOS_AddUnits', [undoaction.line.get('qty') - undoaction.oldqty, undoaction.line.get('productidentifier')]));
        } else if (undoaction.action === 'rem') {
          txtaction.text(OB.I18N.getLabel('OBPOS_RemoveUnits', [undoaction.oldqty - undoaction.line.get('qty'), undoaction.line.get('productidentifier')]));
        } else if (undoaction.action === 'set') {
          txtaction.text(OB.I18N.getLabel('OBPOS_SetUnits', [undoaction.line.get('qty'), undoaction.line.get('productidentifier')]));
        } else if (undoaction.action === 'setbp') {
          txtaction.text(OB.I18N.getLabel('OBPOS_SetBP', [undoaction.bp.get('_identifier')]));
        } else if (undoaction.action === 'resetbp') {
          txtaction.text(OB.I18N.getLabel('OBPOS_ResetBP'));
        }
        undoclick = undoaction.undo;        
      } else {
        msgaction.hide();
        msgwelcome.show();
      }
    }, this);
    
  };
  
});     