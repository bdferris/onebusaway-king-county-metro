package org.onebusaway.king_county_metro.service_alerts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.digester.Digester;
import org.onebusaway.service_alerts.model.AlertDescription;
import org.onebusaway.service_alerts.model.properties.AlertProperties;
import org.onebusaway.service_alerts.model.properties.EAlertPropertyType;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.xml.sax.SAXException;

public class AlertDownloaderLibrary {

  private static final String PROPERTY_ROUTE_ID = "routeId";

  private static final String PROPERTY_MAP_URL = "mapUrl";

  private static final String PROPERTY_URL = "url";

  private static final String PROPERTY_NEIGHBORHOOD = "neighborhood";

  private static final String PROPERTY_REGION = "region";

  private static final String PROPERTY_DESCRIPTION = "description";

  private static final String[] _agencyIds = {"1", "40"};

  private Map<String, String> _routeIdCache = new HashMap<String, String>();

  private TransitDataService _transitDataService;

  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public List<KingCountyMetroAlertDescription> parseRecords(
      BufferedReader reader) throws IOException, SAXException {

    StringBuilder b = getReaderAsRawXml(reader);

    String rawTableXml = b.toString();
    rawTableXml = rawTableXml.replaceAll("&nbsp;", " ");
    rawTableXml = rawTableXml.replaceAll("&", "&amp;");
    String xml = transformXml(rawTableXml, "raw_reroute_table.xsl");

    List<KingCountyMetroAlertDescription> records = parseReroutes(xml);
    for (KingCountyMetroAlertDescription record : records) {

      String url = record.getUrl();
      if (!(url == null || url.startsWith("http")))
        record.setUrl("http://metro.kingcounty.gov/up/rr/" + url);
    }

    return records;
  }

  public List<AlertDescription> getAlertsAsGenericDescriptions(
      List<KingCountyMetroAlertDescription> activeAlerts) {

    List<AlertDescription> descs = new ArrayList<AlertDescription>();

    for (KingCountyMetroAlertDescription alert : activeAlerts) {

      AlertDescription desc = new AlertDescription();
      desc.setId(alert.getId());

      AlertProperties props = desc.getProperties();

      String routeId = alert.getRouteId();
      routeId = resolveRouteId(routeId);

      if (routeId == null)
        routeId = "";

      props.putProperty(PROPERTY_ROUTE_ID, EAlertPropertyType.ROUTE_ID, routeId);
      props.putProperty(PROPERTY_REGION, EAlertPropertyType.STRING,
          alert.getRegion());
      props.putProperty(PROPERTY_DESCRIPTION, EAlertPropertyType.STRING,
          alert.getDescription());

      props.putProperty(PROPERTY_NEIGHBORHOOD, EAlertPropertyType.STRING,
          alert.getNeighborhood());
      props.putProperty(PROPERTY_URL, EAlertPropertyType.URL, alert.getUrl());
      props.putProperty(PROPERTY_MAP_URL, EAlertPropertyType.URL,
          alert.getMapUrl());

      AlertProperties group = props.subset(PROPERTY_ROUTE_ID, PROPERTY_REGION);
      desc.setGroup(group);

      AlertProperties key = props.subset(PROPERTY_ROUTE_ID, PROPERTY_REGION,
          PROPERTY_DESCRIPTION);
      desc.setKey(key);

      descs.add(desc);
    }

    return descs;
  }

  /****
   * 
   ****/

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
  private List<KingCountyMetroAlertDescription> parseReroutes(String xml)
      throws IOException, SAXException {

    Digester digester = new Digester();
    digester.addObjectCreate("active-reroutes", ArrayList.class);

    digester.addObjectCreate("active-reroutes/active-reroute",
        KingCountyMetroAlertDescription.class);
    digester.addBeanPropertySetter("active-reroutes/active-reroute/routeId");
    digester.addBeanPropertySetter("active-reroutes/active-reroute/region");
    digester.addBeanPropertySetter("active-reroutes/active-reroute/neighborhood");
    digester.addBeanPropertySetter("active-reroutes/active-reroute/description");
    digester.addBeanPropertySetter("active-reroutes/active-reroute/url");
    digester.addBeanPropertySetter("active-reroutes/active-reroute/mapUrl");
    digester.addSetNext("active-reroutes/active-reroute", "add");

    return (List<KingCountyMetroAlertDescription>) digester.parse(new StringReader(
        xml));
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

  private String resolveRouteId(String routeId) {

    if (_routeIdCache.containsKey(routeId))
      return _routeIdCache.get(routeId);

    for (String agencyId : _agencyIds) {
      String fullRouteId = agencyId + "_" + routeId;
      RouteBean route = _transitDataService.getRouteForId(fullRouteId);
      if (route != null) {
        _routeIdCache.put(routeId, fullRouteId);
        return fullRouteId;
      }
    }

    return null;
  }

}
