/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.tools.idea.model;

import com.android.builder.model.InstantRun;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;

/**
 * Creates a deep copy of {@link InstantRun}.
 *
 * @see IdeAndroidProject
 */
public class IdeInstantRun implements InstantRun, Serializable {
  @NotNull private final File myInfoFile;
  private final boolean mySupportedByArtifact;
  private final int mySupportStatus;

  public IdeInstantRun(@NotNull InstantRun run) {
    myInfoFile = run.getInfoFile();
    mySupportedByArtifact = run.isSupportedByArtifact();
    mySupportStatus = run.getSupportStatus();
  }

  @Override
  @NotNull
  public File getInfoFile() {
    return myInfoFile;
  }

  @Override
  public boolean isSupportedByArtifact() {
    return mySupportedByArtifact;
  }

  @Override
  public int getSupportStatus() {
    return mySupportStatus;
  }
}
