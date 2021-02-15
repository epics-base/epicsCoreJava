/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration for {@link CompositeDataSource}. This object is mutable, and
 * therefore not thread-safe.
 *
 * @author carcassi
 */
public final class CompositeDataSourceConfiguration {

    private String delimiter = "://";
    private String defaultDataSource;

    /**
     * Creates a new configuration.
     */
    public CompositeDataSourceConfiguration() {
    }

    /**
     * Loads the configuration from the given input stream which must correspond
     * to an XML file.
     *
     * @param input the xml configuration
     */
    public CompositeDataSourceConfiguration(InputStream input) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(input);

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xPath = xpathFactory.newXPath();

            String ver = xPath.evaluate("/dataSources/@version", document);
            if (!ver.equals("1")) {
                throw new IllegalArgumentException("Unsupported version " + ver);
            }

            String delimiter = xPath.evaluate("/dataSources/compositeDataSource/@delimiter", document);
            if (delimiter != null && delimiter.length() != 0) {
                this.delimiter = delimiter;
            }

            String defaultDataSource = xPath.evaluate("/dataSources/compositeDataSource/@defaultDataSource", document);
            if (defaultDataSource != null && defaultDataSource.length() != 0) {
                this.defaultDataSource = defaultDataSource;
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CompositeDataSourceConfiguration.class.getName()).log(Level.FINEST, "Couldn't load dataSources configuration", ex);
            throw new IllegalArgumentException("Couldn't load dataSources configuration", ex);
        } catch (SAXException ex) {
            Logger.getLogger(CompositeDataSourceConfiguration.class.getName()).log(Level.FINEST, "Couldn't load dataSources configuration", ex);
            throw new IllegalArgumentException("Couldn't load dataSources configuration", ex);
        } catch (IOException ex) {
            Logger.getLogger(CompositeDataSourceConfiguration.class.getName()).log(Level.FINEST, "Couldn't load dataSources configuration", ex);
            throw new IllegalArgumentException("Couldn't load dataSources configuration", ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(CompositeDataSourceConfiguration.class.getName()).log(Level.FINEST, "Couldn't load dataSources configuration", ex);
            throw new IllegalArgumentException("Couldn't load dataSources configuration", ex);
        }
    }

    /**
     * Returns the delimeter that divides the data source name from the
     * channel name. Default is "://" so that "ca://pv1" corresponds
     * to the "pv1" channel from the "ca" datasource.
     *
     * @return data source delimeter; can't be null
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Changes the data source delimiter.
     *
     * @param delimiter new data source delimiter; can't be null
     * @return this
     */
    public CompositeDataSourceConfiguration delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * Returns which data source is used if no data source is specified in the
     * channel name.
     *
     * @return the default data source, or null if it was never set
     */
    public String getDefaultDataSource() {
        return defaultDataSource;
    }

    /**
     * Sets the data source to be used if the channel does not specify
     * one explicitely. The data source must have already been added.
     *
     * @param defaultDataSource the default data source
     * @return this data source configuration
     */
    public CompositeDataSourceConfiguration defaultDataSource(String defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
        return this;
    }

}
