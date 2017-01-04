/*
 * #!
 * Ontopia Engine
 * #-
 * Copyright (C) 2001 - 2013 The Ontopia Project
 * #-
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
 * !#
 */

package net.ontopia.utils;

import junit.framework.TestCase;

public abstract class AbstractStringifierTest extends TestCase {

  protected int intended_size = 8;
  
  public AbstractStringifierTest(String name) {
    super(name);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  protected void assertStringifier(Object str, Object identical, Object different) {
    assertTrue("stringifier is not equal", str.equals(identical));
    assertTrue("stringifier is equal", !str.equals(different));
  }
  
}




