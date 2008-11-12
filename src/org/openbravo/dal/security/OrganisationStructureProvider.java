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

package org.openbravo.dal.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Caches the accessible organisations for each organisation. Is used to
 * determine if an organisation of a refered object is in the natural tree of
 * the organisation of the referee.
 * 
 * @author mtaal
 */

public class OrganisationStructureProvider implements OBNotSingleton {

    private boolean isInitialized = false;
    private Map<String, Set<String>> naturalTreesByOrgID = new HashMap<String, Set<String>>();
    private String clientId;

    public void reInitialize() {
	isInitialized = false;
	initialize();
    }

    private void initialize() {
	if (isInitialized) {
	    return;
	}

	if (getClientId() == null) {
	    setClientId(OBContext.getOBContext().getCurrentClient().getId());
	}

	// read all trees of all clients, bypass DAL to prevent security checks
	final String qryStr = "select t from " + Tree.class.getName()
		+ " t where treetype='OO' and client.id='" + getClientId()
		+ "'";
	final Query qry = SessionHandler.getInstance().createQuery(qryStr);
	@SuppressWarnings("unchecked")
	final List<Tree> ts = qry.list();
	final List<TreeNode> treeNodes = new ArrayList<TreeNode>();
	for (Tree t : ts) {
	    final String nodeQryStr = "select tn from "
		    + TreeNode.class.getName() + " tn where tn.tree.id='"
		    + t.getId() + "'";
	    final Query nodeQry = SessionHandler.getInstance().createQuery(
		    nodeQryStr);
	    @SuppressWarnings("unchecked")
	    final List<TreeNode> tns = nodeQry.list();
	    treeNodes.addAll(tns);
	}

	final List<OrgNode> orgNodes = new ArrayList<OrgNode>(treeNodes.size());
	for (TreeNode tn : treeNodes) {
	    final OrgNode on = new OrgNode();
	    on.setTreeNode(tn);
	    orgNodes.add(on);
	}

	for (OrgNode on : orgNodes) {
	    on.resolve(orgNodes);
	}

	for (OrgNode on : orgNodes) {
	    naturalTreesByOrgID.put(on.getTreeNode().getNode(), on
		    .getNaturalTree());
	}
	isInitialized = true;
    }

    public Set<String> getNaturalTree(String orgId) {
	initialize();
	Set<String> result = naturalTreesByOrgID.get(orgId);
	if (result == null) {
	    result = new HashSet<String>();
	    result.add(orgId);
	}
	return result;
    }

    public boolean isInNaturalTree(Organization org1, Organization org2) {
	initialize();
	final String id1 = (String) DalUtil.getId(org1);
	final String id2 = (String) DalUtil.getId(org2);
	final Set<String> ids = getNaturalTree(id1);
	Check
		.isNotNull(
			ids,
			"Organisation with id "
				+ id1
				+ " does not have a computed natural tree, does this organisation exist?");
	return ids.contains(id2);
    }

    class OrgNode {

	private TreeNode treeNode;
	private OrgNode parent;
	private List<OrgNode> children = new ArrayList<OrgNode>();

	private Set<String> naturalTreeParent = null;
	private Set<String> naturalTreeChildren = null;
	private Set<String> naturalTree = null;

	void addChild(OrgNode child) {
	    children.add(child);
	}

	public void resolve(List<OrgNode> nodes) {
	    if (treeNode.getParent() == null) {
		return;
	    }
	    for (OrgNode on : nodes) {
		if (on.getTreeNode().getNode().equals(treeNode.getParent())) {
		    on.addChild(this);
		    setParent(on);
		    break;
		}
	    }
	}

	public Set<String> getNaturalTree() {
	    if (naturalTree == null) {
		naturalTree = new HashSet<String>();
		naturalTree.add(getTreeNode().getNode());
		if (getParent() != null) {
		    getParent().getParentPath(naturalTree);
		}
		for (OrgNode child : getChildren()) {
		    child.getChildPath(naturalTree);
		}
	    }
	    return naturalTree;
	}

	public void getParentPath(Set<String> theNaturalTree) {
	    if (naturalTreeParent == null) {
		naturalTreeParent = new HashSet<String>();
		naturalTreeParent.add(getTreeNode().getNode());
		if (getParent() != null) {
		    getParent().getParentPath(naturalTreeParent);
		}
	    }
	    theNaturalTree.addAll(naturalTreeParent);
	}

	public void getChildPath(Set<String> theNaturalTree) {
	    if (naturalTreeChildren == null) {
		naturalTreeChildren = new HashSet<String>();
		naturalTreeChildren.add(getTreeNode().getNode());
		for (OrgNode child : getChildren()) {
		    child.getChildPath(naturalTreeChildren);
		}
	    }
	    theNaturalTree.addAll(naturalTreeChildren);
	}

	public TreeNode getTreeNode() {
	    return treeNode;
	}

	public void setTreeNode(TreeNode treeNode) {
	    this.treeNode = treeNode;
	}

	public OrgNode getParent() {
	    return parent;
	}

	public void setParent(OrgNode parent) {
	    this.parent = parent;
	}

	public List<OrgNode> getChildren() {
	    return children;
	}

	public void setChildren(List<OrgNode> children) {
	    this.children = children;
	}
    }

    public String getClientId() {
	return clientId;
    }

    public void setClientId(String clientId) {
	this.clientId = clientId;
    }
}