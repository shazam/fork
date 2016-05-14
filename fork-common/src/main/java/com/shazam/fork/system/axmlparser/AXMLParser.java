/*
 * Copyright 2008 Android4ME
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
 */
package com.shazam.fork.system.axmlparser;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parser for Android's binary xml files (axml).
 * @author Dmitry Skiba
 */
@SuppressWarnings("ALL")
public final class AXMLParser {

    /**
     * Types of returned tags.
     * Values are compatible to those in XmlPullParser.
     */
    public static final int
            START_DOCUMENT 		=0,
            END_DOCUMENT		=1,
            START_TAG			=2,
            END_TAG				=3,
            TEXT				=4;

    /**
     * Creates object and reads file info.
     * Call next() to read first tag.
     * @param stream the input stream
     * @throws IOException when any I/O errors occur
     */
    public AXMLParser(InputStream stream) throws IOException {
        m_stream=stream;
        doStart();
    }

    /**
     * Closes parser:
     * 	* closes (and nulls) underlying stream
     * 	* nulls dynamic data
     * 	* moves object to 'closed' state, where methods
     * 	  return invalid values and next() throws IOException.
     */
    public final void close() {
        if (m_stream==null) {
            return;
        }
        try {
            m_stream.close();
        }
        catch (IOException e) {
        }
        if (m_nextException==null) {
            m_nextException=new IOException("Closed.");
        }
        m_stream=null;
        resetState();
    }

    public final int next() throws IOException {
        if (m_nextException!=null) {
            throw m_nextException;
        }
        try {
            return doNext();
        }
        catch (IOException e) {
            m_nextException=e;
            resetState();
            throw e;
        }
    }

    /**
     * Returns current tag type.
     * @return the tag type
     */
    public final int getType() {
        return m_tagType;
    }

    public final String getName() {
        if (m_tagName==-1) {
            return null;
        }
        return getString(m_tagName);
    }

    public final int getLineNumber() {
        return m_tagSourceLine;
    }

    public final int getAttributeCount() {
        if (m_tagAttributes==null) {
            return -1;
        }
        return m_tagAttributes.length;
    }

    /**
     * Returns attribute namespace.
     * @param index the index
     * @return the attribute namespace at index
     */
    public final String getAttributeNamespace(int index) {
        return getString(getAttribute(index).namespace);
    }

    /**
     * Returns attribute name.
     * @param index the index
     * @return the attribute name at index
     */
    public final String getAttributeName(int index) {
        return getString(getAttribute(index).name);
    }

    /**
     * Returns attribute resource ID.
     * @param index the index
     * @return the attribute resource ID at index
     */
    public final int getAttributeResourceID(int index) {
        int resourceIndex=getAttribute(index).name;
        if (m_resourceIDs==null ||
                resourceIndex<0 || resourceIndex>=m_resourceIDs.length)
        {
            return 0;
        }
        return m_resourceIDs[resourceIndex];
    }

    /**
     * Returns type of attribute value.
     * See TypedValue.TYPE_ values.
     * @param index the index
     * @return the attribute type at index
     */
    public final int getAttributeValueType(int index) {
        return getAttribute(index).valueType;
    }

    /**
     * For attributes of type TypedValue.TYPE_STRING returns
     *  string value. For other types returns empty string.
     * @param index the index
     * @return the attribute string at index
     */
    public final String getAttributeValueString(int index) {
        return getString(getAttribute(index).valueString);
    }

    /**
     * Returns integer attribute value.
     * This integer interpreted according to attribute type.
     * @param index the index
     * @return the attribute at index
     */
    public final int getAttributeValue(int index) {
        return getAttribute(index).value;
    }

    ///////////////////////////////////////////// implementation

    private static final class TagAttribute {
        public int namespace;
        public int name;
        public int valueString;
        public int valueType;
        public int value;
    }

    private final void resetState() {
        m_tagType=-1;
        m_tagSourceLine=-1;
        m_tagName=-1;
        m_tagAttributes=null;
    }

    private final void doStart() throws IOException {
        ReadUtil.readCheckType(m_stream,AXML_CHUNK_TYPE);
        /*chunk size*/ReadUtil.readInt(m_stream);

        m_strings=StringBlock.read(new IntReader(m_stream,false));

        ReadUtil.readCheckType(m_stream,RESOURCEIDS_CHUNK_TYPE);
        int chunkSize=ReadUtil.readInt(m_stream);
        if (chunkSize<8 || (chunkSize%4)!=0) {
            throw new IOException("Invalid resource ids size ("+chunkSize+").");
        }
        m_resourceIDs=ReadUtil.readIntArray(m_stream,chunkSize/4-2);

        resetState();
    }

    private final int doNext() throws IOException {
        if (m_tagType==END_DOCUMENT) {
            return END_DOCUMENT;
        }

        m_tagType=(ReadUtil.readInt(m_stream) & 0xFF);/*other 3 bytes?*/
        /*some source length*/ReadUtil.readInt(m_stream);
        m_tagSourceLine=ReadUtil.readInt(m_stream);
        /*0xFFFFFFFF*/ReadUtil.readInt(m_stream);

        m_tagName=-1;
        m_tagAttributes=null;

        switch (m_tagType) {
            case START_DOCUMENT:
            {
                /*namespace?*/ReadUtil.readInt(m_stream);
                /*name?*/ReadUtil.readInt(m_stream);
                break;
            }
            case START_TAG:
            {
                /*0xFFFFFFFF*/ReadUtil.readInt(m_stream);
                m_tagName=ReadUtil.readInt(m_stream);
                /*flags?*/ReadUtil.readInt(m_stream);
                int attributeCount=ReadUtil.readInt(m_stream);
                /*?*/ReadUtil.readInt(m_stream);
                m_tagAttributes=new TagAttribute[attributeCount];
                for (int i=0;i!=attributeCount;++i) {
                    TagAttribute attribute=new TagAttribute();
                    attribute.namespace=ReadUtil.readInt(m_stream);
                    attribute.name=ReadUtil.readInt(m_stream);
                    attribute.valueString=ReadUtil.readInt(m_stream);
                    attribute.valueType=(ReadUtil.readInt(m_stream)>>>24);/*other 3 bytes?*/
                    attribute.value=ReadUtil.readInt(m_stream);
                    m_tagAttributes[i]=attribute;
                }
                break;
            }
            case END_TAG:
            {
                /*0xFFFFFFFF*/ReadUtil.readInt(m_stream);
                m_tagName=ReadUtil.readInt(m_stream);
                break;
            }
            case TEXT:
            {
                m_tagName=ReadUtil.readInt(m_stream);
                /*?*/ReadUtil.readInt(m_stream);
                /*?*/ReadUtil.readInt(m_stream);
                break;
            }
            case END_DOCUMENT:
            {
                /*namespace?*/ReadUtil.readInt(m_stream);
                /*name?*/ReadUtil.readInt(m_stream);
                break;
            }
            default:
            {
                throw new IOException("Invalid tag type ("+m_tagType+").");
            }
        }
        return m_tagType;
    }

    private final TagAttribute getAttribute(int index) {
        if (m_tagAttributes==null) {
            throw new IndexOutOfBoundsException("Attributes are not available.");
        }
        if (index>=m_tagAttributes.length) {
            throw new IndexOutOfBoundsException("Invalid attribute index ("+index+").");
        }
        return m_tagAttributes[index];
    }

    private final String getString(int index) {
        if (index==-1) {
            return "";
        }
        return m_strings.getRaw(index);
    }

    /////////////////////////////////// data

    private InputStream m_stream;

    private StringBlock m_strings;
    private int[] m_resourceIDs;

    private IOException m_nextException;

    private int m_tagType;
    private int m_tagSourceLine;
    private int m_tagName;
    private TagAttribute[] m_tagAttributes;

    private static final int
            AXML_CHUNK_TYPE			=0x00080003,
            RESOURCEIDS_CHUNK_TYPE	=0x00080180;
}
