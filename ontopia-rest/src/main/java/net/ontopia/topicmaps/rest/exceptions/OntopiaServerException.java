/*
 * #!
 * Ontopia Rest
 * #-
 * Copyright (C) 2001 - 2016 The Ontopia Project
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

package net.ontopia.topicmaps.rest.exceptions;

import org.restlet.data.Status;

public class OntopiaServerException extends OntopiaRestException {

	private static final long serialVersionUID = 1L;

	public OntopiaServerException(OntopiaRestErrors error, Object... parameters) {
		super(error, parameters);
	}

	public OntopiaServerException(OntopiaRestErrors error, Throwable cause, Object... parameters) {
		super(error, cause, parameters);
	}

	public OntopiaServerException(String description) {
		this(description, null);
	}

	public OntopiaServerException(Throwable cause) {
		this(null, cause);
	}

	public OntopiaServerException(String description, Throwable cause) {
		super(Status.SERVER_ERROR_INTERNAL, description, cause);
	}
}
