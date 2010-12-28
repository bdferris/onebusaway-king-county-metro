package org.onebusaway.king_county_metro_gtfs.service_alerts.snow.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.digester.Digester;
import org.onebusaway.king_county_metro_gtfs.service_alerts.snow.model.ActiveReroute;
import org.xml.sax.SAXException;

public class RerouteDownloaderLibrary {

  public List<ActiveReroute> parseRecords(BufferedReader reader)
      throws IOException, SAXException {

    StringBuilder b = getReaderAsRawXml(reader);

    String rawTableXml = b.toString();
    rawTableXml = rawTableXml.replaceAll("&nbsp;", " ");
    rawTableXml = rawTableXml.replaceAll("&", "&amp;");
    String xml = transformXml(rawTableXml, "raw_reroute_table.xsl");

    return parseReroutes(xml);
  }

  private StringBuilder getReaderAsRawXml(BufferedReader reader)
      throws IOException {

    StringBuilder b = new StringBuilder();
    b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    b.append("<table>\n");
    String line = null;

    while ((line = reader.readLine()) != null) {
      b.append(line);
      b.append("\n");
    }

    b.append("</table>\n");

    reader.close();
    return b;
  }

  @SuppressWarnings("unchecked")
  private List<ActiveReroute> parseReroutes(String xml) throws IOException,
      SAXException {

    Digester digester = new Digester();
    digester.addObjectCreate("active-reroutes", ArrayList.class);

    digester.addObjectCreate("active-reroutes/active-reroute",
        ActiveReroute.class);
    digester.addBeanPropertySetter("active-reroutes/active-reroute/routeId");
    digester.addBeanPropertySetter("active-reroutes/active-reroute/region");
    digester.addBeanPropertySetter("active-reroutes/active-reroute/neighborhood");
    digester.addBeanPropertySetter("active-reroutes/active-reroute/description");
    digester.addBeanPropertySetter("active-reroutes/active-reroute/url");
    digester.addBeanPropertySetter("active-reroutes/active-reroute/mapUrl");
    digester.addSetNext("active-reroutes/active-reroute", "add");

    return (List<ActiveReroute>) digester.parse(new StringReader(xml));
  }

  private String transformXml(String xmlSourceContent, String xslResourceName)
      throws IOException {

    try {
      StringReader reader = new StringReader(xmlSourceContent);
      StringWriter writer = new StringWriter();

      // JAXP reads data using the Source interface
      Source xmlSource = new StreamSource(reader);
      InputStream xsltInputStream = getClass().getResourceAsStream(
          xslResourceName);
      Source xsltSource = new StreamSource(xsltInputStream);

      // the factory pattern supports different XSLT processors
      TransformerFactory transFact = TransformerFactory.newInstance();
      Transformer trans = transFact.newTransformer(xsltSource);

      trans.transform(xmlSource, new StreamResult(writer));

      return writer.toString();
    } catch (TransformerException ex) {
      System.err.println(xmlSourceContent);
      throw new IOException("error transforming xml", ex);
    }
  }

}
