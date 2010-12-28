package org.onebusaway.king_county_metro.service_alerts.snow.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.onebusaway.king_county_metro.service_alerts.snow.model.ActiveReroute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RerouteDownloaderTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(RerouteDownloaderTask.class);

  private RerouteDownloaderLibrary _library = new RerouteDownloaderLibrary();

  private URL _url;

  private SnowRerouteController _controller;

  public void setUrl(URL url) {
    _url = url;
  }

  public void setController(SnowRerouteController controller) {
    _controller = controller;
  }

  public void run() {

    try {

      BufferedReader reader = new BufferedReader(new InputStreamReader(
          _url.openStream()));

      List<ActiveReroute> activeReroutes = _library.parseRecords(reader);

      _controller.setActiveReroutes(activeReroutes);

    } catch (Exception ex) {
      _log.error("error downloading reroute information", ex);
    }
  }
}
