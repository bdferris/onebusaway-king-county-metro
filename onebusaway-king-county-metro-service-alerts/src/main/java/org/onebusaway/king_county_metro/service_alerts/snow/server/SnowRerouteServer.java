package org.onebusaway.king_county_metro.service_alerts.snow.server;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.onebusaway.king_county_metro.service_alerts.snow.model.RerouteBean;
import org.onebusaway.siri.core.SiriServer;
import org.onebusaway.utility.ObjectSerializationLibrary;

public class SnowRerouteServer {

  private int _port = 8080;

  private File _bundlePath;

  private ScheduledExecutorService _executor;

  private String _id;

  public void setPort(int port) {
    _port = port;
  }

  public void setId(String id) {
    _id = id;
  }

  public void setBundlePath(File bundlePath) {
    _bundlePath = bundlePath;
  }

  public void run() throws Exception {

    SnowRerouteController controller = new SnowRerouteController();

    SiriServer siriServer = new SiriServer();
    String id = _id;
    if (id == null)
      id = UUID.randomUUID().toString();
    siriServer.setIdentity(id);

    Map<Number, RerouteBean> reroutesById = ObjectSerializationLibrary.readObject(_bundlePath);
    controller.setReroutes(reroutesById.values());

    SnowRerouteServlet servlet = new SnowRerouteServlet();
    servlet.setController(controller);
    servlet.setSiriServer(siriServer);

    Server server = new Server(_port);
    Context root = new Context(server, "/", Context.SESSIONS);
    root.addServlet(new ServletHolder(servlet), "/*");
    server.start();

    RerouteDownloaderTask downloader = new RerouteDownloaderTask();
    downloader.setUrl(new URL(
        "http://metro.kingcounty.gov/_inc/weathertable.inc.html"));
    downloader.setController(controller);

    _executor = Executors.newSingleThreadScheduledExecutor();
    _executor.scheduleAtFixedRate(downloader, 0, 1, TimeUnit.MINUTES);
  }

}
