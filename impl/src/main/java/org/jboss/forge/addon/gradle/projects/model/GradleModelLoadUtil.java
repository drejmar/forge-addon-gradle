/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.gradle.projects.model;

import org.gradle.jarjar.com.google.common.collect.Lists;
import org.jboss.forge.addon.gradle.model.GradleDependency;
import org.jboss.forge.addon.gradle.model.GradleModel;
import org.jboss.forge.addon.gradle.model.GradleModelBuilder;
import org.jboss.forge.addon.gradle.model.GradlePlugin;
import org.jboss.forge.addon.gradle.model.GradleRepository;
import org.jboss.forge.addon.gradle.parser.GradleSourceUtil;

import java.util.List;
import java.util.Map;

/**
 * @author Adam Wyłuda
 */
public class GradleModelLoadUtil
{
   private GradleModelLoadUtil()
   {
   }

   /**
    * Loads only direct model from given script.
    */
   public static GradleModel load(GradleModel model, String script)
   {
      GradleModelBuilder modelBuilder = GradleModelBuilder.create(model);

      loadDirectModel(modelBuilder, script);

      return modelBuilder;
   }

   private static void loadDirectModel(GradleModelBuilder builder, String script)
   {
      builder.setDependencies(depsFromScript(script));
      builder.setManagedDependencies(managedDepsFromScript(script));
      builder.setPlugins(pluginsFromScript(script));
      builder.setRepositories(reposFromScript(script));
      builder.setProperties(propertiesFromScript(script));
   }

   private static List<GradleDependency> depsFromScript(String script)
   {
      List<GradleDependency> deps = Lists.newArrayList();
      deps.addAll(GradleSourceUtil.getDependencies(script));
      deps.addAll(GradleSourceUtil.getDirectDependencies(script));
      return deps;
   }

   private static List<GradleDependency> managedDepsFromScript(String script)
   {
      return GradleSourceUtil.getManagedDependencies(script);
   }

   private static List<GradlePlugin> pluginsFromScript(String script)
   {
      return GradleSourceUtil.getPlugins(script);
   }

   private static List<GradleRepository> reposFromScript(String script)
   {
      return GradleSourceUtil.getRepositories(script);
   }

   private static Map<String, String> propertiesFromScript(String script)
   {
      return GradleSourceUtil.getDirectProperties(script);
   }
}
