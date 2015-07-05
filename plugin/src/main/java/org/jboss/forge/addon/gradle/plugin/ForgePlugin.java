package org.jboss.forge.addon.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;
import org.jboss.forge.addon.gradle.model.GradleModel;
import org.jboss.forge.addon.gradle.model.GradleModelBuilder;

import javax.inject.Inject;

/**
 * @author Adam Wyłuda
 */
public class ForgePlugin implements Plugin<Project>
{
   private final ToolingModelBuilderRegistry registry;

   @Inject
   public ForgePlugin(ToolingModelBuilderRegistry registry)
   {
      this.registry = registry;
   }

   @Override
   public void apply(Project project)
   {
      registry.register(new ForgeModelBuilder());
   }

   private static class ForgeModelBuilder implements ToolingModelBuilder
   {
      @Override
      public boolean canBuild(String modelName)
      {
         return modelName.equals(GradleModel.class.getName());
      }

      @Override
      public Object buildAll(String modelName, Project project)
      {
         GradleModelBuilder model = GradleModelBuilder.create();

         model.setName(project.getName());
         model.setGroup(project.getGroup().toString());

         return model;
      }
   }
}
