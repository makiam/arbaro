import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
//import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class XMLTreeFileHandler extends DefaultHandler {

    public void startElement(String namespaceURI,String localName,
            String qName,Attributes atts) {
        System.out.print("<" + qName);
        for(int i=0; i<atts.getLength(); i++) {
            System.out.print(" " + atts.getQName(i) +
                    "=\"" + atts.getValue(i) + "\"");
        }
	System.out.println(">");
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

    public void parse(String sourceURI) throws SAXException, IOException {
        // parse an XML tree file
	//InputSource is = new InputSource(sourceURI);
	parser.parse(sourceURI,new XMLTreeFileHandler());
    }
}
