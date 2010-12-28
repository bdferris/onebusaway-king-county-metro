package org.onebusaway.king_county_metro_gtfs.service_alerts.snow.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.king_county_metro.service_alerts.snow.model.ActiveReroute;
import org.onebusaway.king_county_metro.service_alerts.snow.model.ActiveRerouteRef;
import org.onebusaway.king_county_metro.service_alerts.snow.server.RerouteDownloaderLibrary;

public class RerouteDownloaderLibraryTest {

  @Test
  public void test() throws Exception {

    File dir = new File("/Users/bdferris/Desktop/Weather");
    File[] files = dir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith("html");
      }
    });

    RerouteDownloaderLibrary library = new RerouteDownloaderLibrary();

    Map<ActiveRerouteRef, Map<String, ActiveReroute>> reroutesByRefAndDescription = new FactoryMap<ActiveRerouteRef, Map<String, ActiveReroute>>(
        new HashMap<String, ActiveReroute>());

    for (File file : files) {

      System.out.println(file.getName());

      Map<ActiveRerouteRef, Map<String, ActiveReroute>> newReroutesByRefAndDescription = new FactoryMap<ActiveRerouteRef, Map<String, ActiveReroute>>(
          new HashMap<String, ActiveReroute>());

      BufferedReader reader = new BufferedReader(new FileReader(file));
      List<ActiveReroute> records = library.parseRecords(reader);
      for (ActiveReroute record : records) {
        ActiveRerouteRef ref = record.getRef();
        Map<String, ActiveReroute> reroutesByDescription = newReroutesByRefAndDescription.get(ref);
        ActiveReroute existing = reroutesByDescription.put(
            record.getDescription(), record);
        if (existing != null && ! existing.allPropertiesAreEqual(record)) {
          System.err.println("overlapping reroutes: from=" + existing);
          System.err.println("overlapping reroutes:   to=" + record);
        }

        record.getDescription();
      }

      Set<ActiveRerouteRef> refs = new HashSet<ActiveRerouteRef>();
      refs.addAll(reroutesByRefAndDescription.keySet());
      refs.addAll(newReroutesByRefAndDescription.keySet());
      List<ActiveRerouteRef> orderedRefs = new ArrayList<ActiveRerouteRef>(refs);
      Collections.sort(orderedRefs);
      for (ActiveRerouteRef ref : orderedRefs) {
        System.out.println("ref=" + ref);
        Map<String, ActiveReroute> from = reroutesByRefAndDescription.get(ref);
        Map<String, ActiveReroute> to = newReroutesByRefAndDescription.get(ref);
        Set<String> descs = new HashSet<String>();
        descs.addAll(from.keySet());
        descs.addAll(to.keySet());
        List<String> orderedDescs = new ArrayList<String>(descs);
        Collections.sort(orderedDescs);
        for (String desc : orderedDescs) {
          ActiveReroute fromReroute = from.get(desc);
          ActiveReroute toReroute = to.get(desc);
          if (fromReroute == null && toReroute != null) {
            System.out.println("   ADD: " + desc);
          } else if (fromReroute != null && toReroute == null) {
            System.out.println("   DEL: " + desc);
          } else if (fromReroute != null && toReroute != null) {
            //System.out.println("  SAME: " + desc);
          } else {
            System.out.println("  unexpected: " + desc);
          }
        }
      }

      reroutesByRefAndDescription = newReroutesByRefAndDescription;
    }
  }
}
