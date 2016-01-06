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
package com.android.tools.idea.gradle.dsl.model.repositories;

import com.android.tools.idea.gradle.dsl.parser.elements.GradlePropertiesDslElement;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a repository defined with flatDir {dirs "..."} or flatDir dirs : ["..."].
 */
public class FlatDirRepositoryModel extends RepositoryModel {
  public static final String FLAT_DIR_ATTRIBUTE_NAME = "flatDir";

  private static final String DEFAULT_NAME = "flatDir";

  private static final String NAME_ATTRIBUTE = "name";
  private static final String DIRS_ATTRIBUTE = "dirs";

  @NotNull
  private final GradlePropertiesDslElement myDslElement;

  public FlatDirRepositoryModel(@NotNull GradlePropertiesDslElement dslElement) {
    myDslElement = dslElement;
  }

  @NotNull
  @Override
  public String name() {
    String name = myDslElement.getProperty(NAME_ATTRIBUTE, String.class);
    return name != null ? name : DEFAULT_NAME;
  }

  @NotNull
  public List<String> dirs() {
    List<String> dirs = myDslElement.getListProperty(DIRS_ATTRIBUTE, String.class);
    if (dirs != null) {
      return dirs;
    }

    String dir = myDslElement.getProperty(DIRS_ATTRIBUTE, String.class);
    if (dir != null) {
      return ImmutableList.of(dir);
    }

    return ImmutableList.of();
  }
}