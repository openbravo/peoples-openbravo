/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ToolbarScan = function (context) {
    this.toolbar = [
      {command:'code', label: OB.I18N.getLabel('OBPOS_KbCode')}
    ];
  };
}());
