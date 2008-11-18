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
package org.openbravo.base.provider;

import org.openbravo.base.exception.OBException;

/**
 * Is thrown when instantiating a class fails in the factory.
 * 
 * @author mtaal
 */
public class OBProviderException extends OBException {

    private static final long serialVersionUID = 1L;

    public OBProviderException() {
        super();
    }

    public OBProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public OBProviderException(String message) {
        super(message);
    }

    public OBProviderException(Throwable cause) {
        super(cause);
    }
}
