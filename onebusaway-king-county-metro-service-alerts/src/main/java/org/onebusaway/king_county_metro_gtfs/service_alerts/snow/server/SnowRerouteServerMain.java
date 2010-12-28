package org.onebusaway.king_county_metro_gtfs.service_alerts.snow.server;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class SnowRerouteServerMain {

  public static void main(String[] args) throws Exception {

    Options options = new Options();
    options.addOption("p", "port", true, "");

    PosixParser parser = new PosixParser();
    CommandLine cli = parser.parse(options, args);

    args = cli.getArgs();
    if (args.length != 1) {
      printUsage();
      System.exit(-1);
    }

    SnowRerouteServer server = new SnowRerouteServer();

    server.setBundlePath(new File(args[0]));
    
    if (cli.hasOption('p')) {
      server.setPort(Integer.parseInt(cli.getOptionValue('p')));
    }

    server.run();
  }

  private static void printUsage() {
    System.err.println("usage: [-p,--port=] bundle_path");
  }
}
