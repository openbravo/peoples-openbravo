/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ToolbarScan = OB.COMP.KeyboardComponent.extend({
    tagName: 'div',
    attributes: {'style': 'display:none'},
    contentView: [
      {tag: 'div', attributes: {'style': 'display:table; width:100%'}, content: [
         {view: OB.COMP.ButtonKey.extend({
          command: 'code',
          classButtonActive: 'btnactive-blue',
          contentViewButton: [ OB.I18N.getLabel('OBPOS_KbCode') ]
        })}
      ]},
      {tag: 'div', attributes: {'style': 'display:table; width:100%'}, content: [
         {view: OB.COMP.ButtonKey}
      ]},
      {tag: 'div', attributes: {'style': 'display:table; width:100%'}, content: [
         {view: OB.COMP.ButtonKey}
      ]},
      {tag: 'div', attributes: {'style': 'display:table; width:100%'}, content: [
         {view: OB.COMP.ButtonKey}
      ]},
      {tag: 'div', attributes: {'style': 'display:table; width:100%'}, content: [
         {view: OB.COMP.ButtonKey}
      ]},
      {tag: 'div', attributes: {'style': 'display:table; width:100%'}, content: [
         {view: OB.COMP.ButtonKey}
      ]}     
    ],
    shown: function() {
      this.options.parent.showKeypad('index');
      this.options.parent.defaultcommand = 'code';
    }
  });
  
  
}());
