/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.UTIL = window.OB.UTIL || {};

  function isScrolledIntoView(container, elem) {

    var docViewTop = container.scrollTop();
    var docViewBottom = docViewTop + container.height();

    var elemTop = elem.offset().top;
    var elemBottom = elemTop + elem.height();

    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom) && (elemBottom <= docViewBottom) && (elemTop >= docViewTop));
  }

  OB.UTIL.makeElemVisible = function (container, elem) {

    var docViewTop = container.offset().top;
    var docViewBottom = docViewTop + container.height();

    var elemTop = elem.offset().top;
    var elemBottom = elemTop + elem.height();

    var currentScroll = container.scrollTop();

    if (elemTop < docViewTop) {
      container.scrollTop(currentScroll - docViewTop + elemTop);
    } else if (elemBottom > docViewBottom) {
      container.scrollTop(currentScroll + elemBottom - docViewBottom);
    }
  };

  // public
  OB.UTIL.Thumbnail = function (base64, width, height, contentType) {
    this.component = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'image-wrap'}, content: [
        {kind: B.KindJQuery('div'), id: 'image'}
      ]}
    );
    this.$el = this.component.$el;
    this.image = this.component.context.image.$el;
  };
  OB.UTIL.Thumbnail.prototype.attr = function (attr) {
    var url = (attr.img)
      ? 'data:' + (attr.contentType ? attr.contentType : 'image/png') + ';base64,' + attr.img
      : (attr['default'] ? attr['default'] :'img/box.png');
    this.$el.css('height', attr.height ? attr.height : '48');
    this.$el.css('width', attr.width ? attr.width : '48');
    this.image
        .css('margin', 'auto')
        .css('height', '100%')
        .css('width', '100%')
        .css('background', '#ffffff url(' + url + ') center center no-repeat')
        .css('background-size', 'contain');
  };

  OB.UTIL.showAlert = function (s, title, type) {
    var c = B(
      {kind: B.KindJQuery('div'), attr: {'class': 'alert fade in ' + type, style: 'position:absolute; right:35px; top: 5px'}, content: [
        {kind: B.KindJQuery('button'), attr: {'class': 'close', 'data-dismiss': 'alert'}, content: [
          {kind: B.KindHTML('<span>&times;</span>')}
        ]},
        {kind: B.KindJQuery('strong'), content: [ title ]}, ' ',
        {kind: B.KindJQuery('span'), content: [ s ]}
      ]}
    );

    $("#container").append(c.$el);
    setTimeout(function () {
      $('.alert').alert('close');
    }, 5000);
  };

  OB.UTIL.showSuccess = function (s) {
    OB.UTIL.showAlert(s, OB.I18N.getLabel('OBPOS_LblSuccess'), 'alert-success');
  };

  OB.UTIL.showWarning = function (s) {
    OB.UTIL.showAlert(s, OB.I18N.getLabel('OBPOS_LblWarning'), '');
  };

  OB.UTIL.showError = function (s) {
    OB.UTIL.showAlert(s, OB.I18N.getLabel('OBPOS_LblError'), 'alert-error');
  };
}());
