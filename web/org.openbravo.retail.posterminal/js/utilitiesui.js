/*global B, Backbone $ */

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
  OB.UTIL.Thumbnail = Backbone.View.extend({
    tagName: 'div',
    className: 'image-wrap',
    img: null,
    contentType: 'img/png',
    width: 48,
    height: 48,
    'default': 'img/box.png',
    initialize: function () {
      this.$image = $('<div/>')          
          .css('margin', 'auto')
          .css('height', '100%')
          .css('width', '100%')
          .css('background-size', 'contain');
      this.$el.append(this.$image);
    },
    attr: function (attr) {
      this.img = attr.img || this.img;
      this.contentType = attr.contentType || this.contentType;
      this.width = attr.width || this.width;
      this.height = attr.height || this.height;
      this['default'] = attr['default'] || this['default'];     
    },
    render: function () {
      var url = (this.img)
        ? 'data:' + this.contentType + ';base64,' + this.img
        : this['default'];
      this.$el.css('height', this.height);
      this.$el.css('width', this.width);
      this.$image.css('background', '#ffffff url(' + url + ') center center no-repeat');      
      return this;
    }
  });

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

  OB.UTIL.showLoading = function (value) {
    if (value) {
      $('#containerLoading').css('display', '');
      $('#containerWindow').css('display', 'none');
    } else {
      $('#containerLoading').css('display', 'none');
      $('#containerWindow').css('display', '');
    }
  };

  OB.UTIL.showSuccess = function (s) {
    OB.UTIL.showAlert(s, OB.I18N.getLabel('OBPOS_LblSuccess'), 'alert-success');
  };

  OB.UTIL.showWarning = function (s) {
    OB.UTIL.showAlert(s, OB.I18N.getLabel('OBPOS_LblWarning'), '');
  };

  OB.UTIL.showError = function (s) {
    OB.UTIL.showLoading(false);
    OB.UTIL.showAlert(s, OB.I18N.getLabel('OBPOS_LblError'), 'alert-error');
  };
}());
