/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.chemistry.client.app.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.nuxeo.chemistry.client.common.xml.StaxReader;

/**
 * TODO: Default NS is not matched when explicitly specifying the NS. 
 * See {@link #readAttribute(XMLStreamReader, String)}. How to read attributes easily? 
 * 
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public abstract class AbstractFeedReader<T, E> implements FeedReader<T> {
    
    protected EntryReader<E> entryReader;
    
    protected abstract T newFeed(Object context, StaxReader reader);
    protected abstract void addEntry(T feed, E entry);
    
    
    protected AbstractFeedReader(EntryReader<E> entryReader) {
        this.entryReader = entryReader;
    }
    
    public T read(Object context, File file) throws XMLStreamException, IOException {
        InputStream in = new FileInputStream(file);
        try {
            return read(context, in);
        } finally {
            in.close();
        }
    }
    
    public T read(Object context, URL url) throws XMLStreamException, IOException {
        InputStream in = url.openStream();
        try {
            return read(context, in);
        } finally {
            in.close();
        }
    }
    
    public T read(Object context, InputStream in) throws XMLStreamException {        
        StaxReader xr = StaxReader.newReader(in);
        try {
            return read(context, xr);
        } finally {
            xr.close();
        }
    }

    public T read(Object context, Reader reader) throws XMLStreamException {
        StaxReader xr = StaxReader.newReader(reader);
        try {
            return read(context, xr);
        } finally {
            xr.close();
        }
    }

    public T read(Object context, StaxReader reader) throws XMLStreamException {
        if (!reader.fwdTag()) {
            throw new XMLStreamException("Parse error: empty XML");
        }
        if (!FEED.equals(reader.getName())) {
            throw new XMLStreamException("Parse error: Not an atom feed");
        }
        return readFeed(context, reader);
    }
    
    public T readFeed(Object context, StaxReader reader) throws XMLStreamException {
        //create a new feed object to be filled 
        T feed = newFeed(context, reader);
        while(reader.fwdTag() && !isDone(reader)) {
            String nsUri = reader.getNamespaceURI();            
            if (ATOM.ATOM_NS.equals(nsUri)) {
                if ("entry".equals(reader.getLocalName())) {
                    readEntry(context, reader, feed);
                } else {
                    readAtomTag(context, reader, nsUri, feed);
                }
            } else {
                readExtensionTag(context, reader, nsUri, feed);
            }
        }
        return feed;
    }
    
    protected boolean isDone(StaxReader reader) throws XMLStreamException {
        return false;
    }

    protected void readEntry(Object context, StaxReader reader, T feed) throws XMLStreamException {
        reader.push();
        addEntry(feed, entryReader.readEntry(context, reader));
        reader.pop();
    }

    protected void readAtomTag(Object context, StaxReader reader, String nsUri, T feed) throws XMLStreamException {
    }

    protected void readExtensionTag(Object context, StaxReader reader, String nsUri, T feed) throws XMLStreamException {
    }
    
}