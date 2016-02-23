/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.gradle.structure.model.android;

import com.android.builder.model.*;
import com.android.ide.common.repository.GradleVersion;
import com.android.tools.idea.gradle.dsl.model.dependencies.ArtifactDependencyModel;
import com.android.tools.idea.gradle.dsl.model.dependencies.ModuleDependencyModel;
import com.android.tools.idea.gradle.structure.model.PsdArtifactDependencySpec;
import com.android.tools.idea.gradle.structure.model.PsdModuleModel;
import com.android.tools.idea.gradle.structure.model.PsdParsedDependencyModels;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.android.tools.idea.gradle.structure.model.pom.MavenPoms.findDependenciesInPomFile;

class PsdAndroidDependencyModels {
  @NotNull private final PsdAndroidModuleModel myParent;

  // Key: module's Gradle path
  @NotNull private final Map<String, PsdModuleDependencyModel> myModuleDependencies = Maps.newHashMap();

  @NotNull private final Map<PsdArtifactDependencySpec, PsdLibraryDependencyModel> myLibraryDependencies = Maps.newHashMap();

  PsdAndroidDependencyModels(@NotNull PsdAndroidModuleModel parent) {
    myParent = parent;
    for (PsdVariantModel variantModel : parent.getVariantModels()) {
      addDependencies(variantModel);
    }
  }

  private void addDependencies(@NotNull PsdVariantModel variantModel) {
    for (PsdAndroidArtifactModel artifactModel : variantModel.getArtifacts()) {
      collectDependencies(artifactModel);
    }
  }

  private void collectDependencies(@NotNull PsdAndroidArtifactModel artifactModel) {
    BaseArtifact artifact = artifactModel.getGradleModel();
    Dependencies dependencies = artifact.getDependencies();

    for (AndroidLibrary androidLibrary : dependencies.getLibraries()) {
      String gradlePath = androidLibrary.getProject();
      if (gradlePath != null) {
        addModule(gradlePath, artifactModel);
      }
      else {
        // This is an AAR
        addLibrary(androidLibrary, artifactModel);
      }
    }

    for (JavaLibrary javaLibrary : dependencies.getJavaLibraries()) {
      addLibrary(javaLibrary, artifactModel);
    }
  }

  private void addModule(@NotNull String gradlePath, @NotNull PsdAndroidArtifactModel artifactModel) {
    PsdParsedDependencyModels parsedDependencies = myParent.getParsedDependencyModels();

    ModuleDependencyModel matchingParsedDependency = parsedDependencies.findMatchingModuleDependency(gradlePath);

    Module module = null;
    PsdModuleModel moduleModel = myParent.getParent().findModelByGradlePath(gradlePath);
    if (moduleModel != null) {
      module = moduleModel.getModule();
    }
    PsdModuleDependencyModel dependencyModel = findModuleDependency(gradlePath);
    if (dependencyModel == null) {
      dependencyModel = new PsdModuleDependencyModel(myParent, gradlePath, module, matchingParsedDependency);
      myModuleDependencies.put(gradlePath, dependencyModel);
    }
    dependencyModel.addContainer(artifactModel);
  }

  @Nullable
  private PsdAndroidDependencyModel addLibrary(@NotNull Library library, @NotNull PsdAndroidArtifactModel artifactModel) {
    PsdParsedDependencyModels parsedDependencies = myParent.getParsedDependencyModels();

    MavenCoordinates coordinates = library.getResolvedCoordinates();
    if (coordinates != null) {
      ArtifactDependencyModel matchingParsedDependency = parsedDependencies.findMatchingArtifactDependency(coordinates);
      if (matchingParsedDependency != null) {
        String parsedVersionValue = matchingParsedDependency.version().value();
        if (parsedVersionValue != null) {
          // The dependency has a version in the build.gradle file.
          // "tryParse" just in case the build.file has an invalid version.
          GradleVersion parsedVersion = GradleVersion.tryParse(parsedVersionValue);

          GradleVersion versionFromGradle = GradleVersion.parse(coordinates.getVersion());
          if (parsedVersion != null && compare(parsedVersion, versionFromGradle) == 0) {
            // Match.
            PsdArtifactDependencySpec spec = PsdArtifactDependencySpec.create(matchingParsedDependency);
            return addLibrary(library, spec, artifactModel, matchingParsedDependency);
          }
          else {
            // Version mismatch. This can happen when the project specifies an artifact version but Gradle uses a different version
            // from a transitive dependency.
            // Example:
            // 1. Module 'app' depends on module 'lib'
            // 2. Module 'app' depends on Guava 18.0
            // 3. Module 'lib' depends on Guava 19.0
            // Gradle will force module 'app' to use Guava 19.0

            // Create the dependency model that will be displayed in the "Dependencies" table.
            PsdArtifactDependencySpec spec = PsdArtifactDependencySpec.create(coordinates);
            addLibrary(library, spec, artifactModel, matchingParsedDependency);

            // Create a dependency model for the transitive dependency, so it can be displayed in the "Variants" tool window.
            return addLibrary(library, spec, artifactModel, null);
          }
        }
      }
      else {
        // This dependency was not declared, it could be a transitive one.
        PsdArtifactDependencySpec spec = PsdArtifactDependencySpec.create(coordinates);
        return addLibrary(library, spec, artifactModel, null);
      }
    }
    return null;
  }

  @Nullable
  private PsdAndroidDependencyModel addLibrary(@NotNull Library library,
                                               @NotNull PsdArtifactDependencySpec resolvedSpec,
                                               @NotNull PsdAndroidArtifactModel artifactModel,
                                               @Nullable ArtifactDependencyModel parsedDependencyModel) {
    if (library instanceof AndroidLibrary) {
      AndroidLibrary androidLibrary = (AndroidLibrary)library;
      return addAndroidLibrary(androidLibrary, resolvedSpec, artifactModel, parsedDependencyModel);
    }
    else if (library instanceof JavaLibrary) {
      JavaLibrary javaLibrary = (JavaLibrary)library;
      return addJavaLibrary(javaLibrary, resolvedSpec, artifactModel, parsedDependencyModel);
    }
    return null;
  }

  @NotNull
  private PsdAndroidDependencyModel addAndroidLibrary(@NotNull AndroidLibrary androidLibrary,
                                                      @NotNull PsdArtifactDependencySpec resolvedSpec,
                                                      @NotNull PsdAndroidArtifactModel artifactModel,
                                                      @Nullable ArtifactDependencyModel parsedDependencyModel) {
    PsdAndroidDependencyModel dependencyModel = getOrCreateDependency(resolvedSpec, androidLibrary, parsedDependencyModel);

    for (AndroidLibrary library : androidLibrary.getLibraryDependencies()) {
      PsdAndroidDependencyModel transitive = addLibrary(library, artifactModel);
      if (transitive != null && dependencyModel instanceof PsdLibraryDependencyModel) {
        PsdLibraryDependencyModel libraryDependencyModel = (PsdLibraryDependencyModel)dependencyModel;
        libraryDependencyModel.addTransitiveDependency(transitive.getValueAsText());
      }
    }

    dependencyModel.addContainer(artifactModel);
    return dependencyModel;
  }

  @NotNull
  private PsdAndroidDependencyModel addJavaLibrary(@NotNull JavaLibrary javaLibrary,
                                                   @NotNull PsdArtifactDependencySpec resolvedSpec,
                                                   @NotNull PsdAndroidArtifactModel artifactModel,
                                                   @Nullable ArtifactDependencyModel parsedDependencyModel) {
    PsdAndroidDependencyModel dependencyModel = getOrCreateDependency(resolvedSpec, javaLibrary, parsedDependencyModel);

    for (JavaLibrary library : javaLibrary.getDependencies()) {
      PsdAndroidDependencyModel transitive = addLibrary(library, artifactModel);
      if (transitive != null && dependencyModel instanceof PsdLibraryDependencyModel) {
        PsdLibraryDependencyModel libraryDependencyModel = (PsdLibraryDependencyModel)dependencyModel;
        libraryDependencyModel.addTransitiveDependency(transitive.getValueAsText());
      }
    }

    dependencyModel.addContainer(artifactModel);
    return dependencyModel;
  }

  @VisibleForTesting
  static int compare(@NotNull GradleVersion parsedVersion, @NotNull GradleVersion versionFromGradle) {
    int result = versionFromGradle.compareTo(parsedVersion);
    if (result == 0) {
      return result;
    }
    else if (result < 0) {
      // The "parsed" version might have a '+' sign.
      if (parsedVersion.getMajorSegment().acceptsGreaterValue()) {
        return 0;
      }
      else if (parsedVersion.getMinorSegment() != null && parsedVersion.getMinorSegment().acceptsGreaterValue()) {
        return parsedVersion.getMajor() - versionFromGradle.getMajor();
      }
      else if (parsedVersion.getMicroSegment() != null && parsedVersion.getMicroSegment().acceptsGreaterValue()) {
        result = parsedVersion.getMajor() - versionFromGradle.getMajor();
        if (result != 0) {
          return result;
        }
        return parsedVersion.getMinor() - versionFromGradle.getMinor();
      }
    }
    return result;
  }

  @NotNull
  private PsdAndroidDependencyModel getOrCreateDependency(@NotNull PsdArtifactDependencySpec resolvedSpec,
                                                          @NotNull Library gradleModel,
                                                          @Nullable ArtifactDependencyModel parsedModel) {
    PsdLibraryDependencyModel dependencyModel = myLibraryDependencies.get(resolvedSpec);
    if (dependencyModel == null) {
      dependencyModel = new PsdLibraryDependencyModel(myParent, resolvedSpec, gradleModel, parsedModel);
      myLibraryDependencies.put(resolvedSpec, dependencyModel);

      File libraryPath = null;
      if (gradleModel instanceof AndroidLibrary) {
        libraryPath = ((AndroidLibrary)gradleModel).getBundle();
      }
      else if (gradleModel instanceof JavaLibrary) {
        libraryPath = ((JavaLibrary)gradleModel).getJarFile();
      }
      List<PsdArtifactDependencySpec> pomDependencies = Collections.emptyList();
      if (libraryPath != null) {
        pomDependencies = findDependenciesInPomFile(libraryPath);
      }
      dependencyModel.setPomDependencies(pomDependencies);
    }
    return dependencyModel;
  }

  @NotNull
  List<PsdAndroidDependencyModel> getDeclaredDependencies() {
    List<PsdAndroidDependencyModel> models = Lists.newArrayList();
    for (PsdLibraryDependencyModel model : myLibraryDependencies.values()) {
      if (model.isEditable()) {
        models.add(model);
      }
    }
    for (PsdAndroidDependencyModel model : myModuleDependencies.values()) {
      if (model.isEditable()) {
        models.add(model);
      }
    }
    return models;
  }

  @NotNull
  public List<PsdAndroidDependencyModel> getDependencies() {
    List<PsdAndroidDependencyModel> dependencies = Lists.newArrayList();
    dependencies.addAll(myLibraryDependencies.values());
    dependencies.addAll(myModuleDependencies.values());
    return dependencies;
  }

  @Nullable
  public PsdModuleDependencyModel findModuleDependency(@NotNull String dependency) {
    return myModuleDependencies.get(dependency);
  }

  @Nullable
  public PsdLibraryDependencyModel findLibraryDependency(@NotNull PsdArtifactDependencySpec dependency) {
    return myLibraryDependencies.get(dependency);
  }
}