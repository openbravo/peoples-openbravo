/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ToolbarScan = function (context) {
    this.toolbar = [{
        command: 'code', 
        classButtonActive: 'btnactive-blue',
        label: OB.I18N.getLabel('OBPOS_KbCode')
      }];
  };
}());
