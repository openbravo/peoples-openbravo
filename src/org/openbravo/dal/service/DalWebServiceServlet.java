/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.openbravo.dal.service;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.CheckException;

/**
 * Makes the REST full DAL webservice available to the outside world.
 * 
 * @author mtaal
 */

public class DalWebServiceServlet extends HttpSecureAppServlet {
  
  private static final long serialVersionUID = 1L;
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    final String pathInfo = request.getPathInfo();
    
    Document doc;
    if (pathInfo == null) {
      doc = DalToXMLConverter.getInstance().getTypesAsXML();
    } else if (pathInfo.substring(1).equals("schema")) {
      doc = DalToXMLConverter.getInstance().getSchema();
    } else {
      final String entityName = pathInfo.substring(1);
      
      final OBFilter f = new OBFilter();
      // f.setFilterOnUserClient(true);
      // f.setFilterOnUserOrganisation(true);
      
      try {
        ModelProvider.getInstance().getEntity(entityName);
      } catch (CheckException ce) {
        // sometimes also requests for css and js resources end up here
        return;
      }
      
      // check if we need to show one or a list
      final String id = request.getParameter("id");
      if (id == null) {
        // show all of type entityname
        final List<BaseOBObject> result = OBDal.getInstance().list(entityName, f);
        doc = DalToXMLConverter.getInstance().toXML(entityName, result);
      } else {
        final BaseOBObject result = OBDal.getInstance().get(entityName, id);
        doc = DalToXMLConverter.getInstance().toXML(result);
      }
    }
    if (request.getParameter("asxml") != null) {
      response.setContentType("text/xml");
      response.setCharacterEncoding("utf-8");
      final String xml = DalWebServiceUtil.getInstance().toString(doc);
      final Writer w = response.getWriter();
      w.write(xml);
      w.close();
    }
    if (request.getParameter("template") != null) {
      final Document newDoc = DalWebServiceUtil.getInstance().applyTemplate(doc, request.getParameter("template"));
      response.setContentType("text/html");
      response.setCharacterEncoding("utf-8");
      final String xml = DalWebServiceUtil.getInstance().toString(newDoc);
      final Writer w = response.getWriter();
      w.write(xml);
      w.close();
    }
    response.setContentType("text/xml");
    response.setCharacterEncoding("utf-8");
    final String xml = DalWebServiceUtil.getInstance().toString(doc);
    System.err.println(xml);
    final Writer w = response.getWriter();
    w.write(xml);
    w.close();
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
  }
  
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
  }
  
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
  }
}