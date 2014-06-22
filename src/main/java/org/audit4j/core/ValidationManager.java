/*
 * Copyright 2014 Janith Bandara, This source is a part of Audit4j - 
 * An open-source audit platform for Enterprise java platform.
 * http://mechanizedspace.com/audit4j
 * http://audit4j.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.audit4j.core;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.audit4j.core.dto.AuditEvent;
import org.audit4j.core.exception.ValidationException;
import org.audit4j.core.handler.Handler;

/**
 * The Class ValidationManager.
 * 
 * @author <a href="mailto:janith3000@gmail.com">Janith Bandara</a>
 */
public final class ValidationManager {

	/**
	 * Instantiates a new validation manager.
	 */
	private ValidationManager() {
	}

	/**
	 * Validate event.
	 * 
	 * @param event
	 *            the event
	 * @throws ValidationException
	 *             the validation exception
	 */
	static void validateEvent(AuditEvent event) throws ValidationException {
		if (event == null || event.getActor() == null || event.getOrigin() == null) {
			throw new ValidationException("Invalid Input", ValidationException.VALIDATION_LEVEL_WARN);
		}
	}

	/**
	 * Validate configurations.
	 * 
	 * @param conf
	 *            the conf
	 * @throws ValidationException
	 *             the validation exception
	 */
	static void validateConfigurations(Configuration conf) throws ValidationException {
		if (null == conf.getHandlers()) {
			throw new ValidationException(
					"Handler should not be null, One or more handler implementation shuld be configured in the configuration",
					ValidationException.VALIDATION_LEVEL_INVALID);
		}
		if (null == conf.getLayout()) {
			throw new ValidationException(
					"Layout should not be null,  One or more Layout implementation shuld be configured in the configuration",
					ValidationException.VALIDATION_LEVEL_INVALID);
		}
		if (null == conf.getMetaData()) {
			throw new ValidationException(
					"Handler should not be nulll,  One or more Metadata implementaion shuld be configured in the configuration",
					ValidationException.VALIDATION_LEVEL_INVALID);
		}

		for (Handler handler : conf.getHandlers()) {
			if (!isSerializable(handler)) {
				throw new ValidationException("Handler implementation(" + handler.getClass().getName()
						+ ") shuold be a serializable type.", ValidationException.VALIDATION_LEVEL_INVALID);
			}
		}

		if (!isSerializable(conf.getLayout())) {
			throw new ValidationException("Layout implementation(" + conf.getLayout().getClass().getName()
					+ ") shuold be a serializable type.", ValidationException.VALIDATION_LEVEL_INVALID);
		}
		if (!isSerializable(conf.getMetaData())) {
			throw new ValidationException("Meta Data implementation(" + conf.getMetaData().getClass().getName()
					+ ") shuold be a serializable type.", ValidationException.VALIDATION_LEVEL_INVALID);
		}
	}

	/**
	 * Checks if is serializable.
	 * 
	 * @param object
	 *            the object
	 * @return true, if is serializable
	 */
	static boolean isSerializable(Object object) {
		final boolean retVal;
		if (implementsInterface(object)) {
			retVal = attemptToSerialize(object);
		} else {
			retVal = false;
		}
		return (retVal);
	}

	/**
	 * Implements interface.
	 *
	 * @param o the o
	 * @return true, if successful
	 */
	private static boolean implementsInterface(final Object o) {
		final boolean retVal;
		retVal = ((o instanceof Serializable) || (o instanceof Externalizable));
		return (retVal);
	}

	/**
	 * Attempt to serialize.
	 *
	 * @param o the o
	 * @return true, if successful
	 */
	private static boolean attemptToSerialize(final Object o) {
		final OutputStream sink;
		ObjectOutputStream stream;

		stream = null;

		try {
			sink = new ByteArrayOutputStream();
			stream = new ObjectOutputStream(sink);
			stream.writeObject(o);
			// could also re-serilalize at this point too
		} catch (final IOException ex) {
			return (false);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (final IOException ex) {
					// should not be able to happen
				}
			}
		}

		return (true);
	}
}
