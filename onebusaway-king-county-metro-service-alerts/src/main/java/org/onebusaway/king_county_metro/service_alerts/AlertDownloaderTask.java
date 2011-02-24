package org.onebusaway.king_county_metro.service_alerts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.service_alerts.model.AlertDescription;
import org.onebusaway.service_alerts.services.AlertDescriptionService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AlertDownloaderTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(AlertDownloaderTask.class);

  private AlertDownloaderLibrary _library;

  private ScheduledExecutorService _executor;

  private AlertDescriptionService _alertDescriptionService;

  private URL _url;

  private int _refreshPeriod = 60;

  private TransitDataService _transitDataService;

  public void setUrl(URL url) {
    _url = url;
  }

  @Autowired
  public void setAlertDescriptionService(
      AlertDescriptionService alertDescriptionService) {
    _alertDescriptionService = alertDescriptionService;
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public void setRefreshPeriod(int refreshPeriod) {
    _refreshPeriod = refreshPeriod;
  }

  @PostConstruct
  public void start() {
    
    _library = new AlertDownloaderLibrary();
    _library.setTransitDataService(_transitDataService);
    
    _executor = Executors.newSingleThreadScheduledExecutor();
    _executor.scheduleAtFixedRate(this, 0, _refreshPeriod, TimeUnit.SECONDS);
  }

  @PreDestroy
  public void stop() {
    _executor.shutdown();
  }

  public void run() {

    try {

      BufferedReader reader = new BufferedReader(new InputStreamReader(
          _url.openStream()));

      List<KingCountyMetroAlertDescription> activeAlerts = _library.parseRecords(reader);
      List<AlertDescription> descriptions = _library.getAlertsAsGenericDescriptions(activeAlerts);

      _alertDescriptionService.setActiveAlertDescriptions(_url.toExternalForm(), descriptions);

    } catch (Exception ex) {
      _log.error("error downloading reroute information", ex);
    }
  }
}
