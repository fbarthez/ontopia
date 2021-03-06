/*
 * #!
 * Ontopia OSL Schema
 * #-
 * Copyright (C) 2001 - 2014 The Ontopia Project
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

package net.ontopia.topicmaps.schema.core;

/**
 * INTERNAL: Represents constraints that may only match a specified
 * number of objects.
 * 
 * @deprecated The schema tools are no longer maintained in favor of a future TMCL implementation
 */
@Deprecated
public interface CardinalityConstraintIF extends ConstraintIF {
  int INFINITY = -1;

  /**
   * INTERNAL: Returns the minimum number of objects that the constraint
   * must match.
   */
  int getMinimum();

  /**
   * INTERNAL: Returns the maximum number of objects that the constraint
   * may match.
   */
  int getMaximum();

  /**
   * INTERNAL: Sets the minimum number of objects that the constraint
   * must match.
   */
  void setMinimum(int minimum);

  /**
   * INTERNAL: Sets the maximum number of objects that the constraint
   * may match.
   */
  void setMaximum(int maximum);
  
}





