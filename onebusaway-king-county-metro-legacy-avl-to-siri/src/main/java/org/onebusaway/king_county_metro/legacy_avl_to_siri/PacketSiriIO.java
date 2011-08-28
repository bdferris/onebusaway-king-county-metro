package org.onebusaway.king_county_metro.legacy_avl_to_siri;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.onebusaway.siri.core.SiriTypeFactory;

import uk.org.siri.siri.FramedVehicleJourneyRefStructure;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.Siri;
import uk.org.siri.siri.VehicleActivityStructure;
import uk.org.siri.siri.VehicleActivityStructure.MonitoredVehicleJourney;
import uk.org.siri.siri.VehicleMonitoringDeliveryStructure;

public class PacketSiriIO {

  public static Siri getPacketsAsSiri(List<Packet> packets) {

    Siri siri = new Siri();

    ServiceDelivery serviceDelivery = new ServiceDelivery();
    siri.setServiceDelivery(serviceDelivery);

    VehicleMonitoringDeliveryStructure vmDelivery = new VehicleMonitoringDeliveryStructure();
    serviceDelivery.getVehicleMonitoringDelivery().add(vmDelivery);

    Calendar c = Calendar.getInstance();
    Date now = c.getTime();

    c.add(Calendar.MINUTE, 5);
    Date fiveMinutes = c.getTime();

    vmDelivery.setResponseTimestamp(now);
    vmDelivery.setStatus(Boolean.TRUE);

    vmDelivery.setValidUntil(fiveMinutes);

    List<VehicleActivityStructure> activities = vmDelivery.getVehicleActivity();

    for (Packet packet : packets) {

      String routeName = Short.toString(packet.getServiceRoute());

      VehicleActivityStructure activity = new VehicleActivityStructure();
      activities.add(activity);

      activity.setRecordedAtTime(now);
      activity.setValidUntilTime(fiveMinutes);

      MonitoredVehicleJourney mvj = new MonitoredVehicleJourney();
      activity.setMonitoredVehicleJourney(mvj);

      mvj.setLineRef(SiriTypeFactory.lineRef(routeName));
      mvj.setPublishedLineName(SiriTypeFactory.nls(routeName));
      mvj.setJourneyPatternRef(SiriTypeFactory.journeyPatternRef(packet.getPattern()));
      mvj.setBlockRef(SiriTypeFactory.blockRef(packet.getRoute() + "/"
          + packet.getRun()));
      mvj.setOperatorRef(SiriTypeFactory.operatorRef(Integer.toString(packet.getOperatorId())));
      mvj.setDelay(SiriTypeFactory.duration(packet.getScheduleDeviation() * 1000L));
      mvj.setVehicleRef(SiriTypeFactory.vehicleRef(Short.toString(packet.getVehicleId())));

      mvj.setMonitored(Boolean.TRUE);
      mvj.setInCongestion(Boolean.FALSE);
      mvj.setInPanic(Boolean.FALSE);

      FramedVehicleJourneyRefStructure fvjRef = new FramedVehicleJourneyRefStructure();
      fvjRef.setDataFrameRef(SiriTypeFactory.dataFrameRef(now));
      fvjRef.setDatedVehicleJourneyRef(Integer.toString(packet.getTrip()));
      mvj.setFramedVehicleJourneyRef(fvjRef);
    }

    return siri;
  }
}
