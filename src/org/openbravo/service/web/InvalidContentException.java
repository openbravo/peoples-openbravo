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

import org.openbravo.base.exception.OBException;

/**
 * Thrown when the posted content is not valid.
 * 
 * @author mtaal
 */
public class InvalidContentException extends OBException {

    private static final long serialVersionUID = 1L;

    public InvalidContentException() {
	super();
    }

    public InvalidContentException(String message, Throwable cause) {
	super(message, cause);
    }

    public InvalidContentException(String message) {
	super(message);
    }

    public InvalidContentException(Throwable cause) {
	super(cause);
    }
}
