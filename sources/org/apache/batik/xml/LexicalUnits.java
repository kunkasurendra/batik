/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.xml;

/**
 * This interface defines the constants that represent XML lexical units.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public interface LexicalUnits {
    
    /**
     * Represents the EOF lexical unit.
     */
    int EOF = 0;

    /**
     * Represents the S (space) lexical unit.
     */
    int S = 1;

    /**
     * Represents an XML declaration start lexical unit, ie. '<?xml'.
     */
    int XML_DECL_START = 2;

    /**
     * Represents a doctype start lexical unit, ie. <!DOCTYPE.
     */
    int DOCTYPE_START = 3;

    /**
     * Represents a comment lexical unit.
     */
    int COMMENT = 4;

    /**
     * Represents a PI start lexical unit, ie. '<?Name'.
     */
    int PI_START = 5;

    /**
     * Represents a PI data lexical unit.
     */
    int PI_DATA = 6;

    /**
     * Represents a PI end lexical unit, ie. '?>'.
     */
    int PI_END = 7;

    /**
     * Represents a character data lexical unit, ie. the content of an
     * element.
     */
    int CHARACTER_DATA = 8;

    /**
     * Represents a start tag lexical unit, ie. '<Name'.
     */
    int START_TAG = 9;

    /**
     * Represents an end tag lexical unit, ie. '</Name'.
     */
    int END_TAG = 10;

    /**
     * Represents a CDATA section start lexical unit, ie. '<![CDATA['.
     */
    int CDATA_START = 11;

    /**
     * Represents a character reference lexical unit.
     */
    int CHARACTER_REFERENCE = 12;

    /**
     * Represents an entity reference lexical unit.
     */
    int ENTITY_REFERENCE = 13;

    /**
     * Represents a name lexical unit.
     */
    int NAME = 14;

    /**
     * Represents '=' lexical unit.
     */
    int EQ = 15;

    /**
     * Represents a first attribute fragment lexical unit.
     */
    int FIRST_ATTRIBUTE_FRAGMENT = 16;

    /**
     * Represents an attribute fragment lexical unit.
     */
    int ATTRIBUTE_FRAGMENT = 17;

    /**
     * Represents a last attribute fragment lexical unit.
     */
    int LAST_ATTRIBUTE_FRAGMENT = 18;

    /**
     * Represents an empty element end lexical unit, ie. '/>'.
     */
    int EMPTY_ELEMENT_END = 19;

    /**
     * Represents a end character lexical unit, ie. '>'.
     */
    int END_CHAR = 20;

    /**
     * Represents a section end lexical unit, ie. ']]>'.
     */
    int SECTION_END = 21;

    /**
     * Represents a 'version' lexical unit.
     */
    int VERSION_IDENTIFIER = 22;

    /**
     * Represents a 'encoding' lexical unit.
     */
    int ENCODING_IDENTIFIER = 23;

    /**
     * Represents a 'standalone' lexical unit.
     */
    int STANDALONE_IDENTIFIER = 24;

    /**
     * Represents a string lexical unit.
     */
    int STRING = 25;

    /**
     * Represents a 'SYSTEM' lexical unit.
     */
    int SYSTEM_IDENTIFIER = 26;

    /**
     * Represents a 'PUBLIC' lexical unit.
     */
    int PUBLIC_IDENTIFIER = 27;

    /**
     * Represents a '[' lexical unit.
     */
    int LSQUARE_BRACKET = 28;

    /**
     * Represents a ']' lexical unit.
     */
    int RSQUARE_BRACKET = 29;

    /**
     * Represents a element declaration start lexical unit, ie. '<!ELEMENT'.
     */
    int ELEMENT_DECLARATION_START = 30;

    /**
     * Represents an ATTLIST declaration start lexical unit, ie. '<!ATTLIST'.
     */
    int ATTLIST_START = 31;

    /**
     * Represents an entity start lexical unit, ie. '<!ENTITY'.
     */
    int ENTITY_START = 32;

    /**
     * Represents a notation start lexical unit, ie. '<!NOTATION'.
     */
    int NOTATION_START = 33;

    /**
     * Represents a parameter entity reference lexical unit, ie. '%Name;'.
     */
    int PARAMETER_ENTITY_REFERENCE = 34;

    /**
     * Represents a 'EMPTY' lexical unit.
     */
    int EMPTY_IDENTIFIER = 35;

    /**
     * Represents a 'ANY' lexical unit.
     */
    int ANY_IDENTIFIER = 36;

    /**
     * Represents a '?' lexical unit.
     */
    int QUESTION = 37;

    /**
     * Represents a '+' lexical unit.
     */
    int PLUS = 38;

    /**
     * Represents a '*' lexical unit.
     */
    int STAR = 39;

    /**
     * Represents a '(' lexical unit.
     */
    int LEFT_BRACE = 40;

    /**
     * Represents a ')' lexical unit.
     */
    int RIGHT_BRACE = 41;

    /**
     * Represents a '|' lexical unit.
     */
    int PIPE = 42;

    /**
     * Represents a ',' lexical unit.
     */
    int COMMA = 43;

    /**
     * Represents a '#PCDATA' lexical unit.
     */
    int PCDATA_IDENTIFIER = 44;

    /**
     * Represents a 'CDATA' lexical unit.
     */
    int CDATA_IDENTIFIER = 45;

    /**
     * Represents a 'ID' lexical unit.
     */
    int ID_IDENTIFIER = 46;

    /**
     * Represents a 'IDREF' lexical unit.
     */
    int IDREF_IDENTIFIER = 47;

    /**
     * Represents a 'IDREFS' lexical unit.
     */
    int IDREFS_IDENTIFIER = 48;

    /**
     * Represents a 'NMTOKEN' lexical unit.
     */
    int NMTOKEN_IDENTIFIER = 49;

    /**
     * Represents a 'NMTOKENS' lexical unit.
     */
    int NMTOKENS_IDENTIFIER = 50;

    /**
     * Represents a 'ENTITY' lexical unit.
     */
    int ENTITY_IDENTIFIER = 51;

    /**
     * Represents a 'ENTITIES' lexical unit.
     */
    int ENTITIES_IDENTIFIER = 52;

    /**
     * Represents a '#REQUIRED' lexical unit.
     */
    int REQUIRED_IDENTIFIER = 53;

    /**
     * Represents a '#IMPLIED' lexical unit.
     */
    int IMPLIED_IDENTIFIER = 54;

    /**
     * Represents a '#FIXED' lexical unit.
     */
    int FIXED_IDENTIFIER = 55;

    /**
     * Represents a Nmtoken lexical unit.
     */
    int NMTOKEN = 56;

    /**
     * Represents a 'NOTATION' lexical unit.
     */
    int NOTATION_IDENTIFIER = 57;

    /**
     * Represents a '%' lexical unit.
     */
    int PERCENT = 58;

    /**
     * Represents a 'NDATA' lexical unit.
     */
    int NDATA_IDENTIFIER = 59;

}