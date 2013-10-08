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
    OB.Dal.find(OB.Model.Discount, {
      _whereClause: "where m_offer_type_id in ('D1D193305A6443B09B299259493B272A', '20E4EC27397344309A2185097392D964', '7B49D8CC4E084A75B7CB4D85A6A3A578', '8338556C0FBF45249512DB343FEFD280')"
    }, function (promos) {
      return (promos.length === 0);
    }, function () {
      return true;
    });
    return false;
  } else {
    return true;
  }
};

OB.UTIL.getImageURL = function (id) {
  var imageUrl = '/openbravo/web/org.openbravo.retail.posterminal/productImages/';
  var i;
  for (i = 0; i < id.length; i += 3) {
    if (i != 0) {
      imageUrl += "/";
    }
    imageUrl += id.substring(i, ((i + 3)< id.length)?(i+3):id.length);
  }
  imageUrl += "/"+id;
  return imageUrl;
};
