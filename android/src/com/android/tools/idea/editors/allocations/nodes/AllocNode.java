/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.tools.idea.editors.allocations.nodes;

import com.android.ddmlib.AllocationInfo;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents a set of same-typed allocations that share similar allocation circumstances.
 * For example, multiple individual {@link Byte} allocations at the same stack location can be grouped together.
 */
public class AllocNode extends AbstractTreeNode {
  @NotNull final private AllocationInfo myAllocation;

  AllocNode(@NotNull AllocationInfo allocation) {
    myAllocation = allocation;
    myCount = 1; // Start the count at one for this object itself.
  }

  @Override
  public boolean getAllowsChildren() {
    return false;
  }

  @NotNull
  public AllocationInfo getAllocation() {
    return myAllocation;
  }

  @Override
  public int getValue() {
    return myCount * myAllocation.getSize();
  }

  /**
   * A single {@link AllocNode} can represent multiple similar allocations.
   * This method simply increments the counter that keeps track of the number of similar allocations that have occurred.
   */
  protected void incrementCount() {
    myCount++;
  }
}
