/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.tools.idea.actions;

import com.android.tools.idea.wizard.NewProjectWizard;
import com.intellij.ide.impl.NewProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;

public class AndroidNewProjectAction extends AnAction implements DumbAware {
  public static final String NEW_NEW_PROJECT_WIZARD = "android.newProjectWizard";

  public AndroidNewProjectAction() {
    super("New Project...");
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    String prop = System.getProperty(NEW_NEW_PROJECT_WIZARD);
    if (prop != null && Boolean.parseBoolean(prop)) {
      NewProjectWizard dialog = new NewProjectWizard();
      dialog.show();
      if (!dialog.isOK()) {
        return;
      }
      dialog.createProject();
    } else {
      NewProjectUtil.createNewProject(PlatformDataKeys.PROJECT.getData(e.getDataContext()), null);
    }
  }
}
