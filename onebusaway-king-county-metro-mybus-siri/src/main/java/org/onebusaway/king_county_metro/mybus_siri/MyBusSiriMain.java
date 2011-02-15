package org.onebusaway.king_county_metro.mybus_siri;

import its.SQL.ContentsData;
import its.backbone.sdd.SddReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.onebusaway.siri.core.SiriServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.siri.siri.BlockRefStructure;
import uk.org.siri.siri.CourseOfJourneyStructure;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.VehicleActivityStructure;
import uk.org.siri.siri.VehicleActivityStructure.MonitoredVehicleJourney;
import uk.org.siri.siri.VehicleMonitoringDeliveryStructure;
import uk.org.siri.siri.VehicleRefStructure;

public class MyBusSiriMain {

  private static Logger _log = LoggerFactory.getLogger(MyBusSiriMain.class);

  private static DatatypeFactory _dataTypeFactory;

  private static final String TIMEPOINT_PREDICTION_SERVER_NAME = "carpool.its.washington.edu";

  private static final int TIMEPOINT_PREDICTION_SERVER_PORT = 9002;

  private TimepointPredictionReceiver _receiver;

  private String _serverName = TIMEPOINT_PREDICTION_SERVER_NAME;

  private int _serverPort = TIMEPOINT_PREDICTION_SERVER_PORT;

  private SiriServer _siriServer;

  public static void main(String[] args) throws IOException,
      DatatypeConfigurationException {

    _dataTypeFactory = DatatypeFactory.newInstance();

    String serverUrl = null;
    if (args.length > 0)
      serverUrl = args[0];

    MyBusSiriMain m = new MyBusSiriMain();
    m.run(serverUrl);
  }

  public void run(String serverUrl) throws IOException {

    _siriServer = new SiriServer();

    if (serverUrl != null)
      _siriServer.setServerUrl(serverUrl);

    _siriServer.start();

    _receiver = new TimepointPredictionReceiver(_serverName, _serverPort);
    _receiver.start();
  }

  private void parsePredictions(Hashtable<?, ?> ht) {

    Map<String, List<TimepointPrediction>> predictionsByVehicleId = new HashMap<String, List<TimepointPrediction>>();

    if (ht.containsKey("PREDICTIONS")) {

      ContentsData data = (ContentsData) ht.get("PREDICTIONS");
      data.resetRowIndex();

      while (data.next()) {

        TimepointPrediction prediction = new TimepointPrediction();

        prediction.setAgencyId(data.getString(0));
        prediction.setBlockId(data.getString(1));
        prediction.setTripId(data.getString(2));
        prediction.setVehicleId(data.getString(6));
        prediction.setScheduleDeviation(data.getInt(14));
        prediction.setTimepointId(data.getString(3));
        prediction.setTimepointScheduledTime(data.getInt(4));
        prediction.setTimepointPredictedTime(data.getInt(13));
        prediction.setTimeOfPrediction(data.getInt(9));

        // Indicates that we don't have any real-time predictions for this
        // record
        if (prediction.getTimepointPredictedTime() == -1)
          continue;

        List<TimepointPrediction> predictions = predictionsByVehicleId.get(prediction.getVehicleId());
        if (predictions == null) {
          predictions = new ArrayList<TimepointPrediction>();
          predictionsByVehicleId.put(prediction.getVehicleId(), predictions);
        }

        predictions.add(prediction);
      }
    }

    ServiceDelivery delivery = new ServiceDelivery();
    List<VehicleMonitoringDeliveryStructure> vms = delivery.getVehicleMonitoringDelivery();

    VehicleMonitoringDeliveryStructure vm = new VehicleMonitoringDeliveryStructure();
    vms.add(vm);

    List<VehicleActivityStructure> activity = vm.getVehicleActivity();

    for (List<TimepointPrediction> predictions : predictionsByVehicleId.values()) {

      VehicleActivityStructure va = new VehicleActivityStructure();
      activity.add(va);

      TimepointPrediction prediction = getRepresentativePrediction(predictions);

      MonitoredVehicleJourney mvj = new MonitoredVehicleJourney();
      va.setMonitoredVehicleJourney(mvj);

      Duration delay = _dataTypeFactory.newDuration(prediction.getScheduleDeviation());
      mvj.setDelay(delay);

      BlockRefStructure blockRef = new BlockRefStructure();
      blockRef.setValue(prediction.getBlockId());
      mvj.setBlockRef(blockRef);

      /**
       * MonitoredVehicleJourney doesn't have a trip id, but we used the
       * CourseOfJourneyRef as the trip id in the MTA implementation
       */
      CourseOfJourneyStructure courseOfJourneyRef = new CourseOfJourneyStructure();
      courseOfJourneyRef.setValue(prediction.getTripId());
      mvj.setCourseOfJourneyRef(courseOfJourneyRef);

      VehicleRefStructure vehicleRef = new VehicleRefStructure();
      vehicleRef.setValue(prediction.getVehicleId());
      mvj.setVehicleRef(vehicleRef);

    }

    _siriServer.publish(delivery);
  }

  private TimepointPrediction getRepresentativePrediction(
      List<TimepointPrediction> predictions) {

    TimepointPrediction prev = null;
    int prevDelta = -1;

    for (TimepointPrediction record : predictions) {

      int delta = record.getTimepointPredictedTime()
          - record.getTimeOfPrediction();

      if (prev != null) {
        if (!prev.getTimepointId().equals(record.getTimepointId())
            && prevDelta >= 0 && delta > prevDelta)
          break;
      }

      prev = record;
      prevDelta = delta;
    }

    return prev;
  }

  private class TimepointPredictionReceiver extends SddReceiver {

    public TimepointPredictionReceiver(String serverName, int serverPort)
        throws IOException {
      super(serverName, serverPort);
    }

    @Override
    public void extractedDataReceived(
        @SuppressWarnings("rawtypes") Hashtable ht, String serialNum) {
      super.extractedDataReceived(ht, serialNum);

      try {
        parsePredictions(ht);
      } catch (Throwable ex) {
        _log.error("error parsing predictions from sdd data stream", ex);
      }
    }

  }

}
