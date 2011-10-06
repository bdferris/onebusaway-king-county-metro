package org.onebusaway.king_county_metro_gtfs.transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.impl.TransformLibrary;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class RemoveNonFrequencyTrips implements GtfsTransformStrategy {

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    removeNonFrequencyTripsForRoute(dao,
        dao.getRouteForId(new AgencyAndId("ST", "100479")));
    removeNonFrequencyTripsForRoute(dao,
        dao.getRouteForId(new AgencyAndId("KCM", "100512")));
    removeNonFrequencyTripsForRoute(dao,
        dao.getRouteForId(new AgencyAndId("KCM", "100528")));
  }

  private void removeNonFrequencyTripsForRoute(GtfsMutableRelationalDao dao,
      Route route) {

    if (route == null)
      return;

    List<Trip> toRemove = new ArrayList<Trip>();

    Map<ServiceIdAndDirection, List<Frequency>> frequenciesByServiceId = getFrequenciesByServiceId(
        dao, route);

    for (Trip trip : dao.getTripsForRoute(route)) {
      List<Frequency> frequencies = dao.getFrequenciesForTrip(trip);

      if (frequencies.isEmpty()) {
        ServiceIdAndDirection id = new ServiceIdAndDirection(
            trip.getServiceId(), trip.getDirectionId());
        List<Frequency> frequenciesForServiceId = frequenciesByServiceId.get(id);
        List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
        if (hasOverlap(stopTimes, frequenciesForServiceId))
          toRemove.add(trip);
      } else {
        trip.setBlockId(null);
      }
    }

    TransformLibrary library = new TransformLibrary();
    for (Trip trip : toRemove)
      library.removeEntity(dao, trip);
  }

  private Map<ServiceIdAndDirection, List<Frequency>> getFrequenciesByServiceId(
      GtfsMutableRelationalDao dao, Route route) {
    Map<ServiceIdAndDirection, List<Frequency>> frequenciesByServiceId = new FactoryMap<ServiceIdAndDirection, List<Frequency>>(
        new ArrayList<Frequency>());
    for (Trip trip : dao.getTripsForRoute(route)) {
      List<Frequency> frequencies = dao.getFrequenciesForTrip(trip);
      if (frequencies.isEmpty())
        continue;
      AgencyAndId serviceId = trip.getServiceId();
      String directionId = trip.getDirectionId();
      ServiceIdAndDirection id = new ServiceIdAndDirection(serviceId,
          directionId);
      frequenciesByServiceId.get(id).addAll(frequencies);
    }
    return frequenciesByServiceId;
  }

  private boolean hasOverlap(List<StopTime> stopTimes,
      List<Frequency> frequenciesForServiceId) {
    if (stopTimes.isEmpty())
      return true;
    StopTime stopTime = stopTimes.get(0);
    for (Frequency frequency : frequenciesForServiceId) {
      if (frequency.getStartTime() <= stopTime.getDepartureTime()
          && stopTime.getDepartureTime() <= frequency.getEndTime())
        return true;
    }
    return false;
  }

  private static class ServiceIdAndDirection {
    private final AgencyAndId serviceId;
    private final String directionId;

    public ServiceIdAndDirection(AgencyAndId serviceId, String directionId) {
      this.serviceId = serviceId;
      this.directionId = directionId;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + directionId.hashCode();
      result = prime * result + serviceId.hashCode();
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ServiceIdAndDirection other = (ServiceIdAndDirection) obj;
      if (!directionId.equals(other.directionId))
        return false;
      if (!serviceId.equals(other.serviceId))
        return false;
      return true;
    }
  }
}
