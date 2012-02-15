/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

(function (NS) {

  NS.DOM = function (dom) {
    if (typeof (dom) === "string") { // Is an String
      return document.createTextNode(dom);
    } else if (dom.nodeType) { // Is a DOM Node
      return dom;
    } else if (dom.tag) { //
      var e = document.createElement(dom.tag);

      // attributes
      if (dom.attr) {
        for (var attr in dom.attr) {
          e.setAttribute(attr, dom.attr[attr]);
        }
      }

      // children. Always an array
      if (dom.children) {
        for (var i = 0, max = dom.children.length; i < max; i++) {
          var child = dom.children[i];
          if (child.jquery) {
            for (var j = 0, maxj = child.length; j < maxj; j++) {
              e.appendChild(NS.DOM(child[j]));
            }
          } else {
            e.appendChild(NS.DOM(child));
          }
        }
      }
      return e;
    }
  }

  NS.NODE = function (tag, attr, children) {
    return {
      tag: tag,
      attr: attr,
      children: children
    };
  }
}(window));