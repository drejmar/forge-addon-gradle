/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.gradle.projects;

import org.jboss.forge.addon.gradle.model.GradleModel;

/**
 * Manages Gradle build system.
 * 
 * @author Adam Wyłuda
 */
public interface GradleManager
{
   /**
    * @return True if build was successful, false otherwise.
    */
   boolean runGradleBuild(String directory, String task, String... arguments);

   /**
    * Builds {@link GradleModel} for given directory. Requires path to <i>forge-plugin.gradle</i> script
    * which applies Forge plugin to project.
    */
   GradleModel buildModel(String directory, String forgeScriptLocation, String forgePluginVersion);
}
