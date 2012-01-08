package org.onebusaway.king_county_metro.sandbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.DelimiterTokenizerStrategy;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.BlockTripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.realtime.history.BlockLocationArchiveRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.client.HessianProxyFactory;

public class RealtimeAnalysisMain {

  private static final Logger _log = LoggerFactory.getLogger(RealtimeAnalysisMain.class);

  public static void main(String[] args) throws MalformedURLException {
    RealtimeAnalysisMain m = new RealtimeAnalysisMain();
    if (args.length < 2) {
      System.err.println("usage: url paths");
      System.exit(-1);
    }
    HessianProxyFactory factory = new HessianProxyFactory();
    TransitDataService service = (TransitDataService) factory.create(
        TransitDataService.class, args[0]);
    m.setTransitDataService(service);

    String[] paths = new String[args.length - 1];
    System.arraycopy(args, 1, paths, 0, args.length - 1);
    m.run(paths);
  }

  private TransitDataService _service;

  public void setTransitDataService(TransitDataService service) {
    _service = service;
  }

  private Map<AgencyAndId, List<BlockLocationArchiveRecord>> _recordsByVehicleId = new FactoryMap<AgencyAndId, List<BlockLocationArchiveRecord>>(
      new ArrayList<BlockLocationArchiveRecord>());

  private int _count;

  private void run(String[] paths) {
    List<File> files = new ArrayList<File>();
    for (String path : paths) {
      files.add(new File(path));
    }

    CsvEntityReader reader = new CsvEntityReader();
    DelimiterTokenizerStrategy tokenizerStrategy = new DelimiterTokenizerStrategy(
        "\t");
    tokenizerStrategy.setReplaceLiteralNullValues(true);
    reader.setTokenizerStrategy(tokenizerStrategy);

    reader.addEntityHandler(new Go());

    try {
      for (File file : files) {
        _log.info("path=" + file.getAbsolutePath());
        _recordsByVehicleId.clear();
        InputStream in = openFileForInput(file);
        reader.readEntities(BlockLocationArchiveRecord.class, in);
        in.close();
        for (List<BlockLocationArchiveRecord> records : _recordsByVehicleId.values()) {
          processRecords(records);
        }
      }
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private InputStream openFileForInput(File path) throws IOException {
    InputStream in = new FileInputStream(path);
    if (path.getName().endsWith(".gz"))
      in = new GZIPInputStream(in);
    return in;
  }

  private void processRecord(BlockLocationArchiveRecord record) {
    AgencyAndId vehicleId = record.getVehicleId();
    if (vehicleId == null)
      return;
    if (!vehicleId.getAgencyId().equals("1"))
      return;
    if (record.getLocationLat() != null || record.getLocationLon() != null)
      return;
    if (record.getScheduleDeviation() == null || record.getTripId() == null)
      return;
    List<BlockLocationArchiveRecord> records = _recordsByVehicleId.get(vehicleId);
    if (!records.isEmpty()) {
      BlockLocationArchiveRecord prevRecord = records.get(records.size() - 1);
      double prevScheduleDeviation = prevRecord.getScheduleDeviation();
      double scheduleDeviation = record.getScheduleDeviation();
      if (prevScheduleDeviation == scheduleDeviation)
        return;
    }

    records.add(record);
    _count++;
  }

  private void processRecords(List<BlockLocationArchiveRecord> records) {
    boolean early = false;
    for (BlockLocationArchiveRecord record : records) {
      double deviation = record.getScheduleDeviation();
      if (deviation < -5 * 60)
        early = true;
    }
    if (!early) {
      return;
    }

    AgencyAndId currentBlockId = null;
    BlockInstanceBean blockInstance = null;

    for (int i = 0; i < records.size(); ++i) {
      BlockLocationArchiveRecord record = records.get(i);
      AgencyAndId blockId = record.getBlockId();
      if (!blockId.equals(currentBlockId)) {
        blockInstance = _service.getBlockInstance(blockId.toString(),
            record.getServiceDate());
      }
      double deviation = record.getScheduleDeviation();
      if (deviation < -5 * 60) {
        BlockTripBean blockTrip = getBlockTrip(blockInstance, record);
        double distanceAlongTrip = record.getDistanceAlongTrip();
        double totalTripDistance = blockTrip.getTrip().getTotalTripDistance();
        if (distanceAlongTrip < 500
            || (totalTripDistance - distanceAlongTrip) < 500) {
          System.err.println(record.getVehicleId() + " " + record.getBlockId()
              + " " + record.getTime() + " " + record.getScheduleDeviation()
              + " ");
        }
      }
    }
  }

  private BlockTripBean getBlockTrip(BlockInstanceBean blockInstance,
      BlockLocationArchiveRecord record) {
    String tripId = record.getTripId().toString();
    for (BlockTripBean trip : blockInstance.getBlockConfiguration().getTrips()) {
      if (trip.getTrip().getId().equals(tripId)) {
        return trip;
      }
    }
    return null;
  }

  private class Go implements EntityHandler {

    @Override
    public void handleEntity(Object bean) {
      BlockLocationArchiveRecord record = (BlockLocationArchiveRecord) bean;
      processRecord(record);
    }
  }
}
