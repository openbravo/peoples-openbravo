/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, console, _ */

OB = window.OB || {};
OB.UTIL = window.OB.UTIL || {};

OB.UTIL.isDisableDiscount = function (receipt) {
  if (receipt.get('lines').length > 0) {
    return OB.POS.modelterminal.get('isDisableDiscount');
  } else {
    return true;
  }
};

OB.UTIL.getImageURL = function (id) {
  var imageUrl = 'productImages/';
  var i;
  for (i = 0; i < id.length; i += 3) {
    if (i !== 0) {
      imageUrl += "/";
    }
    imageUrl += id.substring(i, ((i + 3) < id.length) ? (i + 3) : id.length);
  }
  imageUrl += "/" + id;
  return imageUrl;
};