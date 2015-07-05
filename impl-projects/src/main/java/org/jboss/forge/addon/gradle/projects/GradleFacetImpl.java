/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.gradle.projects;

import org.jboss.forge.addon.configuration.Configuration;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.gradle.parser.GradleSourceUtil;
import org.jboss.forge.addon.gradle.projects.model.GradleModel;
import org.jboss.forge.addon.gradle.projects.model.GradleModelLoadUtil;
import org.jboss.forge.addon.gradle.projects.model.GradleModelMergeUtil;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.resource.WriteableResource;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.roaster.model.util.Strings;

import javax.inject.Inject;
import java.io.File;

/**
 * @author Adam Wyłuda
 */
public class GradleFacetImpl extends AbstractFacet<Project> implements GradleFacet
{
   private static final String INITIAL_BUILD_FILE_CONTENTS = "" +
            "apply plugin: 'java'\n" +
            "repositories {\n" +
            "    mavenCentral()\n" +
            "}\n";
   private static final String FORGE_OUTPUT_LIBRARY_LOCATION_CONF_KEY = "forgeOutputLibraryLocation";

   @Inject
   private GradleManager manager;
   @Inject
   private ResourceFactory resourceFactory;
   @Inject
   private Configuration configuration;

   // Cached model
   private GradleModel model;

   @Override
   public boolean install()
   {
      if (!this.isInstalled())
      {
         if (!getBuildScriptResource().exists())
         {
            getBuildScriptResource().createNewFile();
            getBuildScriptResource().setContents(INITIAL_BUILD_FILE_CONTENTS);
         }
      }
      return isInstalled();
   }

   @Override
   public boolean isInstalled()
   {
      return getBuildScriptResource().exists();
   }

   @Override
   public boolean executeTask(String task, String... arguments)
   {
      return manager.runGradleBuild(getFaceted().getRoot().getFullyQualifiedName(), task, arguments);
   }

   @Override
   public GradleModel getModel()
   {
      if (this.model != null)
      {
         return this.model;
      }
      loadModel();
      return this.model;
   }

   @Override
   public void setModel(GradleModel newModel)
   {
      String oldSource = getBuildScriptResource().getContents();
      String newSource = GradleModelMergeUtil.merge(oldSource, model, newModel);
      getBuildScriptResource().setContents(newSource);

      // If we need to change model name then it must be done in settings.gradle
      if (!this.model.getName().equals(newModel.getName()))
      {
         String settingsScript = getSettingsScriptResource().exists() ? getSettingsScriptResource().getContents() : "";
         // Because setting project name in model also changes the project path
         // we must take project path from old model
         settingsScript = GradleSourceUtil.setProjectName(settingsScript, this.model.getProjectPath(),
                  newModel.getName());
         getSettingsScriptResource().setContents(settingsScript);
      }

      this.model = null;
   }

   @Override
   public FileResource<?> getBuildScriptResource()
   {
      return (FileResource<?>) getFaceted().getRoot().getChild("build.gradle");
   }

   @SuppressWarnings("unchecked")
   @Override
   public FileResource<?> getSettingsScriptResource()
   {
      return resourceFactory.create(FileResource.class, new File(
               getModel().getRootProjectPath(), "settings.gradle"));
   }

   @Override
   public void installForgeLibrary()
   {
      if (!isForgeLibraryInstalled())
      {
         String script = getBuildScriptResource().getContents();
         String newScript = GradleSourceUtil.checkForIncludeForgeLibraryAndInstall(script);

         // If Forge library is not included
         if (!script.equals(newScript))
         {
            getBuildScriptResource().setContents(newScript);
         }

         installFileFromResources(getFaceted().getRoot(), GradleSourceUtil.FORGE_LIBRARY, GradleSourceUtil.FORGE_LIBRARY_RESOURCE);
      }
   }

   @Override
   public boolean isForgeLibraryInstalled()
   {
      return (getFaceted().getRoot().getChild(GradleSourceUtil.FORGE_LIBRARY)).exists() &&
               GradleSourceUtil.checkForIncludeForgeLibrary(getBuildScriptResource().getContents());
   }

   private void loadModel()
   {
      if (!isForgeOutputLibraryInstalled())
      {
         installForgeOutputLibrary();
      }
      runGradleWithForgeOutputLibrary();

      String forgeOutput = readForgeOutputAndClean();
      String script = getBuildScriptResource().getContents();
      GradleModel loadedModel = GradleModelLoadUtil.load(script, forgeOutput);

      this.model = loadedModel;
   }

   private String readForgeOutputAndClean()
   {
      Resource<?> forgeOutputFile = getFaceted().getRoot().getChild(GradleSourceUtil.FORGE_OUTPUT_XML);
      String forgeOutput = forgeOutputFile.getContents();
      forgeOutputFile.delete();

      return forgeOutput;
   }

   private boolean isForgeOutputLibraryInstalled()
   {
      String libLocation = configuration.getString(FORGE_OUTPUT_LIBRARY_LOCATION_CONF_KEY);

      return !Strings.isNullOrEmpty(libLocation)
               && resourceFactory.create(new File(libLocation)).exists();
   }

   private Resource<?> installFileFromResources(Resource<?> targetDirectory, String targetFileName,
            String resourceFileName)
   {
      WriteableResource<?, ?> forgeLib = (WriteableResource<?, ?>) targetDirectory.getChild(targetFileName);
      forgeLib.setContents(getClass().getResourceAsStream(resourceFileName));

      return forgeLib;
   }

   private void installForgeOutputLibrary()
   {
      File temporaryDir = OperatingSystemUtils.createTempDir();
      Resource<?> forgeLib = installFileFromResources(resourceFactory.create(temporaryDir),
               GradleSourceUtil.FORGE_OUTPUT_LIBRARY, GradleSourceUtil.FORGE_OUTPUT_LIBRARY_RESOURCE);
      configuration.setProperty(FORGE_OUTPUT_LIBRARY_LOCATION_CONF_KEY, forgeLib.getFullyQualifiedName());
   }

   private void runGradleWithForgeOutputLibrary()
   {
      manager.runGradleBuild(getFaceted().getRoot().getFullyQualifiedName(),
               GradleSourceUtil.FORGE_OUTPUT_TASK, "-I", configuration.getString(FORGE_OUTPUT_LIBRARY_LOCATION_CONF_KEY));
   }
}
