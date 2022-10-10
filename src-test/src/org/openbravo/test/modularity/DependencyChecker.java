package org.openbravo.test.modularity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.DataSetTable;
import org.openbravo.test.base.OBBaseTest;

public class DependencyChecker extends OBBaseTest {
  private static final Entity MODULE_ENTITY = ModelProvider.getInstance().getEntity(Module.class);

  @Test
  public void checkDependencies() {
    setSystemAdministratorContext();

    Map<String, Set<String>> allDeps = getAllModuleDependencies();

    OBCriteria<DataSet> q = OBDal.getInstance()
        .createCriteria(DataSet.class)
        .add(Restrictions.eq(DataSet.PROPERTY_SEARCHKEY, "AD"));
    List<DataSetTable> dsTables = ((DataSet) q.uniqueResult()).getDataSetTableList();
    for (DataSetTable dsTable : dsTables) {

      Entity m = ModelProvider.getInstance().getEntityByTableId(dsTable.getTable().getId());

      ReferencedEntitiesWithModules r = new ReferencedEntitiesWithModules(m);
      if (!r.hasModule() || !r.hasReferencesToModuleEntities()) {
        continue;
      }

      System.out.println(r);

      List<BaseOBObject> bobs = OBDal.getInstance()
          .createCriteria(r.entity.getName())
          .add(Restrictions.isNotNull(r.moduleProperty.getName()))
          .list();

      for (BaseOBObject bob : bobs) {
        boolean hasErrors = false;
        Module bobModule = (Module) bob.get(r.moduleProperty.getName());

        for (Property p : r.fksToEntitiesWithModule) {
          BaseOBObject referencedBob = (BaseOBObject) bob.get(p.getName());
          if (referencedBob == null) {
            continue;
          }

          Module referencedBobModule = (Module) referencedBob.get(
              ReferencedEntitiesWithModules.getModuleProperty(referencedBob.getEntity()).getName());

          if (!isDependency(bobModule.getId(), referencedBobModule.getId(), allDeps)) {
            if (!hasErrors) {
              System.out.println("  " + bob + " - " + bobModule);
            }
            hasErrors = true;
            System.out.println(
                "      " + p.getName() + " ->" + referencedBob + "  " + referencedBobModule);
          }
        }
      }

    }

  }

  private boolean isDependency(String referencedModuleId, String baseModuleId,
      Map<String, Set<String>> allDeps) {
    return allDeps.get(baseModuleId).contains(referencedModuleId);
  }

  private static Map<String, Set<String>> getAllModuleDependencies() {
    Map<String, Set<String>> deps = new HashMap<>();
    for (Module mod : OBDal.getInstance().createCriteria(Module.class).list()) {
      deps.put(mod.getId(), KernelUtils.getInstance().getAncestorsDependencyTree(mod));
    }
    return deps;

  }

  private static class ReferencedEntitiesWithModules {
    private Entity entity;
    private Property moduleProperty;
    private Set<Property> fksToEntitiesWithModule;

    ReferencedEntitiesWithModules(Entity e) {
      entity = e;
      moduleProperty = e.getProperties()
          .stream()
          .filter(p -> MODULE_ENTITY.equals(p.getTargetEntity()))
          .findFirst()
          .orElse(null);

      if (moduleProperty == null) {
        return;
      }

      fksToEntitiesWithModule = e.getProperties()
          .stream()
          .filter(p -> p.getTargetEntity() != null && !p.isOneToMany()
              && getModuleProperty(p.getTargetEntity()) != null)
          .collect(Collectors.toSet());

    }

    boolean hasModule() {
      return moduleProperty != null;
    }

    boolean hasReferencesToModuleEntities() {
      return !fksToEntitiesWithModule.isEmpty();
    }

    private static Property getModuleProperty(Entity m) {
      return m.getProperties()
          .stream()
          .filter(p -> MODULE_ENTITY.equals(p.getTargetEntity()))
          .findFirst()
          .orElse(null);
    }

    @Override
    public String toString() {

      return entity + "  " + moduleProperty + " " + fksToEntitiesWithModule;
    }
  }

}
