package org.onebusaway.king_county_metro.mybus_siri;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.onebusaway.siri.core.SiriServer;
import org.onebusaway.siri.core.handlers.SiriSubscriptionManagerListener;
import org.onebusaway.siri.core.subscriptions.server.SiriServerSubscriptionManager;
import org.onebusaway.siri.core.versioning.SiriVersioning;
import org.onebusaway.siri.jetty.SiriJettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.siri.Siri;
import uk.org.siri.siri.ServiceDelivery;

public class SiriFromLogFilesMain {

  private static Logger _log = LoggerFactory.getLogger(SiriFromLogFilesMain.class);

  private static final String ARG_CONSUMER_ADDRESS_DEFAULT = "consumerAddressDefault";

  private SiriServer _siriServer;

  private String _consumerAddressDefault;

  private Deque<File> _dataFiles = new ArrayDeque<File>();

  private ExecutorService _executor;

  private Future<?> _task = null;

  public static void main(String[] args) throws IOException,
      DatatypeConfigurationException, ParseException {

    SiriFromLogFilesMain m = new SiriFromLogFilesMain();

    Options options = new Options();
    options.addOption(ARG_CONSUMER_ADDRESS_DEFAULT, true, "");

    Parser parser = new GnuParser();
    CommandLine cli = parser.parse(options, args);

    args = cli.getArgs();

    if (args.length != 2) {
      System.err.println("usage: url dataDir");
      System.exit(-1);
    }

    String serverUrl = args[0];
    File dataDir = new File(args[1]);

    if (cli.hasOption(ARG_CONSUMER_ADDRESS_DEFAULT)) {
      String consumerAddressDefault = cli.getOptionValue(ARG_CONSUMER_ADDRESS_DEFAULT);
      m.setConsumerAddressDefault(consumerAddressDefault);
    }

    m.run(serverUrl, dataDir);
  }

  private void setConsumerAddressDefault(String consumerAddressDefault) {
    _consumerAddressDefault = consumerAddressDefault;
  }

  public void run(String serverUrl, File dataDir) throws IOException {

    _siriServer = new SiriJettyServer();

    if (serverUrl != null)
      _siriServer.setUrl(serverUrl);

    SiriServerSubscriptionManager manager = _siriServer.getSubscriptionManager();
    manager.addListener(new SiriSubscriptionManagerListenerImpl());

    if (_consumerAddressDefault != null) {
      manager.setConsumerAddressDefault(_consumerAddressDefault);
    }

    for (File file : dataDir.listFiles()) {
      if (!file.getName().endsWith(".xml"))
        continue;
      _dataFiles.add(file);
    }

    _executor = Executors.newSingleThreadExecutor();

    _siriServer.start();
  }

  private synchronized void checkExecutor(SiriServerSubscriptionManager manager) {

    List<String> channels = manager.getActiveSubscriptionChannels();
    int numberOfActiveChannels = channels.size();

    if (_task == null && numberOfActiveChannels > 0) {
      _log.info("starting task");
      _task = _executor.submit(new DataSourceTask());
    } else if (_task != null && numberOfActiveChannels == 0) {
      _log.info("stopping task");
      _task.cancel(true);
      _task = null;
    }
  }

  private class SiriSubscriptionManagerListenerImpl implements
      SiriSubscriptionManagerListener {

    @Override
    public void subscriptionAdded(SiriServerSubscriptionManager manager) {
      checkExecutor(manager);
    }

    @Override
    public void subscriptionRemoved(SiriServerSubscriptionManager manager) {
      checkExecutor(manager);
    }
  }

  private class DataSourceTask implements Runnable {

    @Override
    public void run() {

      SiriVersioning versioning = SiriVersioning.getInstance();

      while (!_dataFiles.isEmpty() && !Thread.interrupted()) {

        _log.info("remaining files: " + _dataFiles.size());

        File file = _dataFiles.poll();

        try {

          Reader reader = new FileReader(file);
          Object object = _siriServer.unmarshall(reader);
          reader.close();

          ServiceDelivery delivery = null;

          if (object instanceof uk.org.siri.Siri) {
            uk.org.siri.Siri siri = (Siri) object;
            uk.org.siri.ServiceDelivery serviceDelivery = siri.getServiceDelivery();
            if (serviceDelivery != null) {
              delivery = (ServiceDelivery) versioning.getPayloadAsVersion(
                  serviceDelivery, versioning.getDefaultVersion());
            }
          }

          if (delivery != null) {
            _siriServer.publish(delivery);
          }

        } catch (Exception ex) {
          _log.warn("error processing file: " + file, ex);
        }

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          return;
        }
      }
    }

  }
}
