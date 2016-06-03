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
 *
 * THIS FILE WAS GENERATED BY codergen. EDIT WITH CARE.
 */
package com.android.tools.idea.editors.gfxtrace.service.path;

import org.jetbrains.annotations.NotNull;

import com.android.tools.rpclib.binary.*;
import com.android.tools.rpclib.schema.*;

import java.io.IOException;

public final class TypedMemoryPath extends Path {
  @Override
  public String getSegmentString() {
    return "Type<" + myType + '>';
  }

  @Override
  public AtomPath getParent() {
    return myRange.getParent();
  }

  //<<<Start:Java.ClassBody:1>>>
  private MemoryRangePath myRange;
  private MemoryType myType;

  // Constructs a default-initialized {@link TypedMemoryPath}.
  public TypedMemoryPath() {}


  public MemoryRangePath getRange() {
    return myRange;
  }

  public TypedMemoryPath setRange(MemoryRangePath v) {
    myRange = v;
    return this;
  }

  public MemoryType getType() {
    return myType;
  }

  public TypedMemoryPath setType(MemoryType v) {
    myType = v;
    return this;
  }

  @Override @NotNull
  public BinaryClass klass() { return Klass.INSTANCE; }


  private static final Entity ENTITY = new Entity("path", "TypedMemory", "", "");

  static {
    ENTITY.setFields(new Field[]{
      new Field("Range", new Pointer(new Struct(MemoryRangePath.Klass.INSTANCE.entity()))),
      new Field("Type", new Struct(MemoryType.Klass.INSTANCE.entity())),
    });
    Namespace.register(Klass.INSTANCE);
  }
  public static void register() {}
  //<<<End:Java.ClassBody:1>>>
  public enum Klass implements BinaryClass {
    //<<<Start:Java.KlassBody:2>>>
    INSTANCE;

    @Override @NotNull
    public Entity entity() { return ENTITY; }

    @Override @NotNull
    public BinaryObject create() { return new TypedMemoryPath(); }

    @Override
    public void encode(@NotNull Encoder e, BinaryObject obj) throws IOException {
      TypedMemoryPath o = (TypedMemoryPath)obj;
      e.object(o.myRange);
      e.value(o.myType);
    }

    @Override
    public void decode(@NotNull Decoder d, BinaryObject obj) throws IOException {
      TypedMemoryPath o = (TypedMemoryPath)obj;
      o.myRange = (MemoryRangePath)d.object();
      o.myType = new MemoryType();
      d.value(o.myType);
    }
    //<<<End:Java.KlassBody:2>>>
  }
}
