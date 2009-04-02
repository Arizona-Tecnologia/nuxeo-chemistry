/*
 * Copyright 2009 Nuxeo SA <http://nuxeo.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors:
 *     Florent Guillaume
 */
package org.nuxeo.ecm.core.chemistry.impl;

import java.io.Serializable;

import org.apache.chemistry.property.Property;
import org.apache.chemistry.property.PropertyDefinition;
import org.apache.chemistry.property.Updatability;
import org.apache.chemistry.type.Type;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.model.PropertyException;

/**
 * A live property of an object.
 *
 * @author Florent Guillaume
 */
public class NuxeoProperty implements Property {

    /**
     * Factory for a new Property.
     */
    public static Property getProperty(DocumentModel doc, Type type,
            String name, CoreSession session) throws ClientException {
        PropertyDefinition pd = type.getPropertyDefinition(name);
        if (pd == null) {
            throw new IllegalArgumentException(name);
        }
        Serializable value;
        org.nuxeo.ecm.core.api.model.Property prop = null;
        if (Property.ID.equals(name)) {
            value = doc.getId();
        } else if (Property.URI.equals(name)) {
            value = null;
        } else if (Property.TYPE_ID.equals(name)) {
            value = doc.getType();
        } else if (Property.CREATED_BY.equals(name)) {
            value = doc.getPropertyValue(NuxeoType.NX_DC_CREATOR);
        } else if (Property.CREATION_DATE.equals(name)) {
            value = doc.getPropertyValue(NuxeoType.NX_DC_CREATED);
        } else if (Property.LAST_MODIFIED_BY.equals(name)) {
            value = doc.getPropertyValue("dc:contributors");
            if (value == null || ((String[]) value).length == 0) {
                value = null;
            } else {
                value = ((String[]) value)[0];
            }
        } else if (Property.LAST_MODIFICATION_DATE.equals(name)) {
            value = doc.getPropertyValue(NuxeoType.NX_DC_MODIFIED);
        } else if (Property.CHANGE_TOKEN.equals(name)) {
            value = null;
        } else if (Property.NAME.equals(name)) {
            value = doc.getName();
        } else if (Property.IS_IMMUTABLE.equals(name)) {
            value = Boolean.FALSE; // TODO check write permission
        } else if (Property.IS_LATEST_VERSION.equals(name)) {
            value = Boolean.TRUE;
        } else if (Property.IS_MAJOR_VERSION.equals(name)) {
            value = Boolean.FALSE;
        } else if (Property.IS_LATEST_MAJOR_VERSION.equals(name)) {
            value = Boolean.FALSE;
        } else if (Property.VERSION_LABEL.equals(name)) {
            // value = doc.getVersionLabel();
            value = null;
        } else if (Property.VERSION_SERIES_ID.equals(name)) {
            value = doc.getId();
        } else if (Property.IS_VERSION_SERIES_CHECKED_OUT.equals(name)) {
            value = Boolean.FALSE;
        } else if (Property.VERSION_SERIES_CHECKED_OUT_BY.equals(name)) {
            value = null;
        } else if (Property.VERSION_SERIES_CHECKED_OUT_ID.equals(name)) {
            value = null;
        } else if (Property.CHECKIN_COMMENT.equals(name)) {
            value = null;
        } else if (Property.CONTENT_STREAM_ALLOWED.equals(name)) {
            value = type.getContentStreamAllowed().toString();
        } else if (Property.CONTENT_STREAM_LENGTH.equals(name)) {
            value = doc.hasSchema("file") ? doc.getPropertyValue("file:content/length")
                    : null;
            if (value != null) {
                value = Integer.valueOf(((Long) value).intValue());
            }
        } else if (Property.CONTENT_STREAM_MIME_TYPE.equals(name)) {
            value = doc.hasSchema("file") ? doc.getPropertyValue("file:content/mime-type")
                    : null;
        } else if (Property.CONTENT_STREAM_FILENAME.equals(name)) {
            value = null;
            prop = doc.hasSchema("file") ? doc.getProperty("file:filename")
                    : null;
        } else if (Property.CONTENT_STREAM_URI.equals(name)) {
            value = "http://TODO/XXX"; // XXX
        } else if (Property.PARENT_ID.equals(name)) {
            // TODO cache this
            DocumentRef parentRef = doc.getParentRef();
            if (parentRef == null) {
                value = null;
            } else {
                value = session.getDocument(parentRef).getId();
            }
        } else if (Property.ALLOWED_CHILD_OBJECT_TYPE_IDS.equals(name)) {
            value = null;
        } else if (Property.SOURCE_ID.equals(name)) {
            value = null;
        } else if (Property.TARGET_ID.equals(name)) {
            value = null;
        } else if (Property.POLICY_NAME.equals(name)) {
            value = null;
        } else if (Property.POLICY_TEXT.equals(name)) {
            value = null;
        } else if (pd.getUpdatability() == Updatability.READ_WRITE) {
            // read/write property
            value = null;
            prop = doc.getProperty(name);
        } else {
            // read-only property
            value = doc.getPropertyValue(name);
        }

        if (prop == null) {
            // read-only
            return new ReadOnly(value, pd);
        } else {
            return new NuxeoProperty(prop, pd);
        }
    }

    private final org.nuxeo.ecm.core.api.model.Property prop;

    private final PropertyDefinition propertyDefinition;

    public NuxeoProperty(org.nuxeo.ecm.core.api.model.Property prop,
            PropertyDefinition propertyDefinition) {
        this.prop = prop;
        this.propertyDefinition = propertyDefinition;
    }

    public PropertyDefinition getDefinition() {
        return propertyDefinition;
    }

    public Serializable getValue() {
        try {
            return prop.getValue();
        } catch (PropertyException e) {
            throw new RuntimeException(e.toString(), e); // TODO
        }
    }

    public void setValue(Serializable value) {
        try {
            prop.setValue(value);
        } catch (PropertyException e) {
            throw new RuntimeException(e.toString(), e); // TODO
        }
    }

    /**
     * A read-only property.
     */
    public static class ReadOnly implements Property {

        private final Serializable value;

        private final PropertyDefinition propertyDefinition;

        public ReadOnly(Serializable value,
                PropertyDefinition propertyDefinition) {
            this.value = value;
            this.propertyDefinition = propertyDefinition;
        }

        public PropertyDefinition getDefinition() {
            return propertyDefinition;
        }

        public Serializable getValue() {
            return value;
        }

        public void setValue(Serializable value) {
            throw new UnsupportedOperationException("Read-only property: "
                    + propertyDefinition.getName());
        }

    }
}
