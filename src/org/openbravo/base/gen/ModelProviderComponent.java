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

package org.openbravo.base.gen;

import org.openarchitectureware.workflow.WorkflowComponent;
import org.openarchitectureware.workflow.WorkflowContext;
import org.openarchitectureware.workflow.ast.parser.Location;
import org.openarchitectureware.workflow.container.CompositeComponent;
import org.openarchitectureware.workflow.issues.Issues;
import org.openarchitectureware.workflow.monitor.ProgressMonitor;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.session.OBPropertiesProvider;

/**
 * Sets the model in the work flow context, so that the generator can pick it
 * up.
 * 
 * @author mtaal
 */

public class ModelProviderComponent implements WorkflowComponent {

    private String propFile;

    public void checkConfiguration(Issues arg0) {
    }

    public CompositeComponent getContainer() {
	return null;
    }

    public Location getLocation() {
	return null;
    }

    public void invoke(WorkflowContext wc, ProgressMonitor pm, Issues issues) {
	wc.set("model", ModelProvider.getInstance());
    }

    public void setContainer(CompositeComponent arg0) {
    }

    public void setLocation(Location arg0) {
    }

    public void setPropFile(String propFile) {
	OBPropertiesProvider.getInstance().setProperties(propFile);
	this.propFile = propFile;
    }

    public String getPropFile() {
	return propFile;
    }
}
