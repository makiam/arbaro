//  #**************************************************************************
//  #
//  #    $Id$  - class for reading a tree species definition from a XML file
//  #
//  #    Copyright (C) 2003  Wolfram Diestel
//  #
//  #    This program is free software; you can redistribute it and/or modify
//  #    it under the terms of the GNU General Public License as published by
//  #    the Free Software Foundation; either version 2 of the License, or
//  #    (at your option) any later version.
//  #
//  #    This program is distributed in the hope that it will be useful,
//  #    but WITHOUT ANY WARRANTY; without even the implied warranty of
//  #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  #    GNU General Public License for more details.
//  #
//  #    You should have received a copy of the GNU General Public License
//  #    along with this program; if not, write to the Free Software
//  #    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//  #
//  #    Send comments and bug fixes to diestel@steloj.de
//  #
//  #**************************************************************************/

package params;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class XMLTreeFileHandler extends DefaultHandler {

    Params params;

    public XMLTreeFileHandler(Params par) {
	params = par;
    }

    public void startElement(String namespaceURI,String localName,
            String qName,Attributes atts) throws SAXException {
	
	if (qName.equals("species")) {
	    params.species = atts.getValue("name");
	} else if (qName.equals("param")) {

	    try {
		params.setParam(atts.getValue("name"),atts.getValue("value"));
	    } catch (ErrorParam e) {
		throw new SAXException(e.getMessage());
	    }
	}
    }

    /*
    public void endElement(String namespaceURI,String localName,
            String qName) {
        System.out.println("</" + qName + ">");
    }
    */
}


public class XMLTreeParser {
    SAXParser parser;

    public XMLTreeParser() 
	throws ParserConfigurationException, SAXException
    {
        // get a parser factory 
        SAXParserFactory spf = SAXParserFactory.newInstance();
        // get a XMLReader 
	parser = spf.newSAXParser();
    }

    public void parse(InputSource is, Params params) throws SAXException, IOException {
        // parse an XML tree file
	//InputSource is = new InputSource(sourceURI);
	parser.parse(is,new XMLTreeFileHandler(params));
    }
}









