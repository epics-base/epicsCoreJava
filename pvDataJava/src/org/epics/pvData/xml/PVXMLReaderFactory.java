/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.xml;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.epics.pvData.pv.MessageType;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * IOCXMLReaderFactory implements IOCXMLReader.
 * Only one reader is created and getReader returns the instance.
 * @author mrk
 *
 */
public class PVXMLReaderFactory {
    
    static private PVReader pvReader = new PVReader();
    static private PVXMLListener listener = null;
    static private String rootElementName = null;
    static private Map<String,String> substituteMap = new TreeMap<String,String>();
    static private List<String> pathList = new ArrayList<String>();
    static private Pattern separatorPattern = Pattern.compile("[, ]");
    static private Pattern equalPattern = Pattern.compile("[=]");
    
    /**
     * Get the IOCXMLReader.
     * @return The reader.
     */
    static public PVXMLReader getReader() {
        return pvReader;
    }
    
    private static class PVReader implements PVXMLReader {
        private AtomicBoolean isInUse = new AtomicBoolean(false);
        private Handler currentHandler = null;
        
        /* (non-Javadoc)
         * @see org.epics.pvData.xml.PVXMLReader#parse(java.lang.String, java.lang.String, org.epics.pvData.xml.PVXMLListener)
         */
        public void parse(String rootElementName,String fileName, PVXMLListener listener) 
        {
            boolean gotIt = isInUse.compareAndSet(false,true);
            if(!gotIt) {
                listener.message("IOCReader is already active",MessageType.fatalError);
                return;
            }
            if(listener==null) {
                System.out.println("IOCXMLReader was called with a null listener");
                return;
            }
            try {
                PVXMLReaderFactory.rootElementName = rootElementName;
                PVXMLReaderFactory.listener = listener;
                PVXMLReaderFactory.substituteMap.clear();
                PVXMLReaderFactory.pathList.clear();
                create(null,fileName);
            } finally {
                isInUse.set(false);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.xml.PVXMLReader#message(java.lang.String, org.epics.pvData.pv.MessageType)
         */
        public void message(String message, MessageType messageType) {
            currentHandler.message(message,messageType);
            
        }
        
        private void setCurrentReader(Handler handler) {
            currentHandler = handler;
        }
        
        private Handler create(Handler parent,String fileName) throws IllegalStateException
        {
            int start = fileName.indexOf("${");
            int end = fileName.indexOf("}");
            if(start>=0 && end>start) {
                String name = fileName.substring(start+2, end);
                name = System.getenv(name);
                fileName = fileName.substring(0, start) + name + fileName.substring(end+1);
                
            }
            String uri = null;
            try {
                uri = new File(fileName).toURI().toURL().toString();
            } catch (MalformedURLException e) {
                throw new IllegalStateException(
                        String.format("%n")
                        + "IOCXMLReader.convert terminating with MalformedURLException"
                        + String.format("%n")
                        + e.getMessage());
            }
            XMLReader reader;
            Handler handler = new Handler(parent);
            try {
                reader = XMLReaderFactory.createXMLReader();
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);
                reader.parse(uri);
            } catch (SAXException e) {
                // nothing to do. ErrorHandler reports errors.
            } catch (IOException e) {
                String message = String.format(
                    "IOCXMLReader.convert terminating with IOException: %s%n%s%n",
                    e.getMessage(),handler.showLocation());
                listener.message(message, MessageType.fatalError);
            } catch (IllegalArgumentException e) {
                String message = String.format(
                    "Illegal Argument Exception: %s%n%s%n",
                    e.getMessage(), handler.showLocation());
                listener.message(message,MessageType.fatalError);
            }
            return handler;
        }
    }
    private static class Handler implements ContentHandler, ErrorHandler {
        private Handler parent = null;
        private Locator locator;
        private int nInfo = 0;
        private int nWarning = 0;
        private int nError = 0;
        private int nFatal = 0;
        private boolean gotFirstElement = false;
        private StringBuilder charBuilder = new StringBuilder();
        
        private Handler(Handler parent) {
            this.parent = parent;
            pvReader.setCurrentReader(this);
        }
        
        private String showLocation() {
            String result = "";
            if(locator!=null) {
                result = String.format("line %d column %d in %s%n",
                        locator.getLineNumber(),
                        locator.getColumnNumber(),
                        locator.getSystemId());
            }
            if(parent!=null) result += parent.showLocation();
            return result;
        }
        
        private void message(String message,MessageType messageType)
        {
            listener.message(String.format("%s %s%n%s",
                messageType.name(),message,showLocation()),
                messageType);
            switch(messageType) {
            case info:  nInfo ++; break;
            case warning: nWarning ++; break;
            case error: nError++; break;
            case fatalError: nFatal++; break;
            }
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        public void error(SAXParseException e) throws SAXException {
            message(e.toString(),MessageType.error);
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
         */
        public void fatalError(SAXParseException e) throws SAXException {
            message(e.toString(),MessageType.fatalError);
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
         */
        public void warning(SAXParseException e) throws SAXException {
            message(e.toString(),MessageType.warning);
        }

        private enum CharState {
            idle,
            got$,
            gotPrefix
        }
        private CharState charState = CharState.idle;
        
        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#characters(char[], int, int)
         */
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(charState) {
            case idle:
                for(int i=0; i< length; i++) {
                    if(ch[start+i]=='$') {
                        if(i+1<length) {
                            if(ch[start+i+1]=='{') {
                                if(i>0) listener.characters(ch,start,i);
                                charState = CharState.got$;
                                characters(ch,start+i+1,length-(i+1));
                                return;
                            } else {
                                continue;
                            }
                        } else {
                            if(i>0) listener.characters(ch,start,i);
                            charState = CharState.got$;
                            return;
                        }
                    }
                }
                listener.characters(ch,start,length);
                return;
            case got$:
                if(ch[start]=='{') {
                    charState = CharState.gotPrefix;
                    charBuilder.setLength(0);
                    start++;
                    if(length>1) characters(ch,start,length-1);
                    return;
                }
                char[] str$ = new char[] {'$'};
                listener.characters(str$,0,1);
                charState = CharState.idle;
                if(length>1) listener.characters(ch,start+1,length-1);
                return;
            case gotPrefix:
                for(int i=0; i<length; i++) {
                    if(ch[start+i]=='}') {
                        if(i>0) charBuilder.append(ch,start,i);
                        String from = charBuilder.toString();
                        String to = substituteMap.get(from);
                        if(to!=null) {
                            char[] charArray = to.toCharArray();
                            listener.characters(charArray,0,charArray.length);
                        }
                        charState = CharState.idle;
                        if(i+1<length) characters(ch,start+i+1,length-(i+1));
                        return;
                    }
                }
                charBuilder.append(ch,start,length);
                return;
            }
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#endDocument()
         */
        public void endDocument() throws SAXException {
            if(parent==null) listener.endDocument();
            if(nWarning>0 || nError>0 || nFatal>0) {
                message(String.format("%s endDocument: warning %d severe %d fatal %d",
                    locator.getSystemId(),nWarning,nError,nFatal),MessageType.info);
            }
            pvReader.setCurrentReader(parent);
            parent = null;
            locator = null;
        }
         
        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if(!gotFirstElement) {
                gotFirstElement = true;
                if(!qName.equals(rootElementName)) {
                    message(
                        "rootElementName is " + qName +
                        " but expected " + rootElementName,
                        MessageType.error);
                }
                return;
            }
            if(qName.equals("include")) {
                includeElement(atts);
                return;
            }
            if(qName.equals("substitute")) {
                substituteElement(atts);
                return;
            }
            charBuilder.setLength(0);
            Map<String,String> attributes = new TreeMap<String,String>();
            for(int i=0; i<atts.getLength(); i++) {
                String name = atts.getQName(i);
                String value = atts.getValue(i);
                int prefix = value.indexOf("${");
                int end = 0;
                if(prefix>=0) {
                    end = value.indexOf("}",prefix);
                    if(end<0 || (end-prefix)<3) {
                        message("attribute " + name + " has bad value",
                                MessageType.error);
                    } else {
                        StringBuilder builder = new StringBuilder();
                        if(prefix>0) builder.append(value.substring(0,prefix));
                        String temp = value.substring(prefix+2,end);
                        temp = substituteMap.get(temp);
                        if(temp==null) {
                            message("attribute " + name + " no substitution found",
                                    MessageType.error);
                        } else {
                            builder.append(temp);
                        }
                        if(end+1<value.length()) {
                            builder.append(value.substring(end+1));
                        }
                        value = builder.toString();
                     }
                }
                attributes.put(name,value);
            }
            listener.startElement(qName,attributes);
        }
        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(qName.equals(rootElementName)) return;
            if(qName.equals("include")) return;
            if(qName.equals("substitute")) return;
            listener.endElement(qName);
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
         */
        public void endPrefixMapping(String prefix) throws SAXException {}

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
         */
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
         */
        public void processingInstruction(String target, String data) throws SAXException {}

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
         */
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
         */
        public void skippedEntity(String name) throws SAXException {}

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#startDocument()
         */
        public void startDocument() throws SAXException {}

        /* (non-Javadoc)
         * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
         */
        public void startPrefixMapping(String prefix, String uri) throws SAXException {}
        
        private String convertSeparator(String original) {
            if(File.separator.equals("/")) return original;
            String convertedString = "";
            String subString = original;
            while(subString.length()>0) {
                int index = subString.indexOf('/');
                if(index<0) {
                    convertedString += subString;
                    break;
                }
                convertedString += subString.substring(0, index) + File.separator;
                subString = subString.substring(index+1);
            }
            return convertedString;
        }
        
        private String convertFieldName(String string) {
            string = convertSeparator(string);
            while(true) {
                int startIndex = string.indexOf("${");
                if(startIndex<0) return string;
                int endIndex = string.indexOf('}', startIndex);
                if(endIndex<startIndex+3) {
                    message("illegal env definition in " + string,MessageType.error);
                    break;
                }
                String from = string.substring(startIndex+2, endIndex);
                String to = System.getProperty(from, System.getenv(from));
                if(to==null) {
                    message("envVariable " + from + " not found",MessageType.error);
                    break;
                }
                string = string.substring(0, startIndex) + to + string.substring(endIndex+1);
            }
            return null;
        }
        
        private void includeElement(Attributes atts) {
            String removePath = atts.getValue("removePath");
            if(removePath!=null) {
                removePath = convertFieldName(removePath);
                if(!pathList.remove(removePath)) {
                    message("path " + removePath + " not in pathList",
                            MessageType.warning);
                }
            }
            String addPath = atts.getValue("addPath");
            if(addPath!=null) {
                addPath = convertFieldName(addPath);
                pathList.add(0, addPath);
            }
            String href = atts.getValue("href");
            if(href==null) {
                if(removePath==null && addPath==null) {
                    message("no attribute was recognized",MessageType.warning);
                }
                return;
            }
            String fileName = href;
      outer:
            while(true) {
                File file = new File(fileName);
                if(file.exists()) break;
                for(int index = 0; index<pathList.size(); index++) {
                    fileName = pathList.get(index) + File.separator + href;
                    file = new File(fileName);
                    if(file.exists()) break outer;
                }
                message("file " + href + " not found",MessageType.error);
                return;
            }
            pvReader.create(this,fileName);
            return;
        }
        
        private void substituteElement(Attributes atts) {
            
            String remove = atts.getValue("remove");
            if(remove!=null) {
                if(substituteMap.remove(remove)==null) {
                    message(remove + " not found",
                            MessageType.warning);
                }
            }
            String from = atts.getValue("from");
            if(from!=null) {
                String to = atts.getValue("to");
                if(to==null) {
                    message("from without corresonding to",
                            MessageType.warning);
                } else {
                    substituteMap.put(from,to);
                }
            }
            String fromTo = atts.getValue("fromTo");
            if(fromTo==null) {
                if(remove==null && from==null) {
                    message("no attribute was recognized",
                            MessageType.warning);
                }
                return;
            }
            String[] items = separatorPattern.split(fromTo);
            for(String item : items) {
                String[] parts = equalPattern.split(item);
                if(parts.length!=2) {
                    message(item + " is not a valid substitution",
                            MessageType.warning);
                } else {
                    substituteMap.put(parts[0],parts[1]);
                }
            }
        }
    }
}
