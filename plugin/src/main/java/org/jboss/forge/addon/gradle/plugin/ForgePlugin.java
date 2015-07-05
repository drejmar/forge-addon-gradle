package org.jboss.forge.addon.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;
import org.jboss.forge.addon.gradle.model.GradleModel;
import org.jboss.forge.addon.gradle.model.GradleModelBuilder;
import org.jboss.forge.addon.gradle.model.GradlePlugin;
import org.jboss.forge.addon.gradle.model.GradlePluginBuilder;
import org.jboss.forge.addon.gradle.model.GradlePluginType;
import org.jboss.forge.addon.gradle.model.GradleSourceDirectory;
import org.jboss.forge.addon.gradle.model.GradleSourceDirectoryBuilder;
import org.jboss.forge.addon.gradle.model.GradleSourceSet;
import org.jboss.forge.addon.gradle.model.GradleSourceSetBuilder;

import javax.inject.Inject;
import java.util.ArrayList;

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

         ArrayList<GradlePlugin> plugins = new ArrayList<GradlePlugin>();
         plugins.add(GradlePluginBuilder.create(GradlePluginType.JAVA));
         model.setEffectivePlugins(plugins);

         ArrayList<GradleSourceSet> sourceSets = new ArrayList<GradleSourceSet>();
         ArrayList<GradleSourceDirectory> dirs = new ArrayList<GradleSourceDirectory>();
         dirs.add(GradleSourceDirectoryBuilder.create().setPath("java"));
         GradleSourceSetBuilder mainSourceSet = GradleSourceSetBuilder.create()
                  .setName("main").setJavaDirectories(dirs);
         sourceSets.add(mainSourceSet);
         model.setEffectiveSourceSets(sourceSets);

         return model;
      }
   }
}
