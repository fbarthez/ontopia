/*
 * #!
 * Ontopia Rest
 * #-
 * Copyright (C) 2001 - 2017 The Ontopia Project
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

package net.ontopia.topicmaps.rest.v1.variant;

import net.ontopia.topicmaps.rest.exceptions.OntopiaRestErrors;
import net.ontopia.topicmaps.rest.model.VariantName;
import net.ontopia.topicmaps.rest.v1.AbstractV1ResourceTest;
import org.junit.Test;

public class VariantNameResourceDELETETest extends AbstractV1ResourceTest {
	
	public VariantNameResourceDELETETest() {
		super(VARIANTS_LTM, "variants");
	}

	@Test
	public void deleteVariantName() {
		delete("4", VariantName.class);
		assertGetFails("4", OntopiaRestErrors.MANDATORY_ATTRIBUTE_IS_NULL);
	}

	@Test
	public void deleteInvalidVariantName() {
		assertDeleteFails("1", OntopiaRestErrors.MANDATORY_ATTRIBUTE_IS_WRONG_TYPE);
	}

	@Test
	public void deleteUnexistingVariantName() {
		assertDeleteFails("unexisting_name_id", OntopiaRestErrors.MANDATORY_ATTRIBUTE_IS_NULL);
	}
}
