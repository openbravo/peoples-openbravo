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

package org.openbravo.base.model;

/**
 * Is used to keep track of the correct accesslevel of the entity
 * 
 * @author Martin Taal
 */
public enum AccessLevel {
    SYSTEM, CLIENT, ORGANIZATION, CLIENT_ORGANIZATION, SYSTEM_CLIENT, ALL
}
