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

package org.openbravo.service.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.provider.OBModulePrefixRequired;

/**
 * Defines the standard webservice interface which needs to be implemented by
 * all webservices.
 * 
 * @author mtaal
 */

public interface WebService extends OBModulePrefixRequired {

    public enum ChangeAction {
	CREATE, UPDATE, DELETE
    }

    public void doGet(String path, HttpServletRequest request,
	    HttpServletResponse response) throws Exception;

    public void doPost(String path, HttpServletRequest request,
	    HttpServletResponse response) throws Exception;

    public void doDelete(String path, HttpServletRequest request,
	    HttpServletResponse response) throws Exception;

    public void doPut(String path, HttpServletRequest request,
	    HttpServletResponse response) throws Exception;
}