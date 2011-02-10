package org.onebusaway.king_county_metro_gtfs.transformations;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkLightTrailHeadsignStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(LinkLightTrailHeadsignStrategy.class);

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Route route = getRoute(dao);

    if (route == null) {
      _log.warn("could not find Link Light Rail route");
      return;
    }

    List<Trip> trips = dao.getTripsForRoute(route);

    for (Trip trip : trips) {

      List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);

      if (stopTimes.isEmpty())
        continue;

      StopTime stopTime = stopTimes.get(stopTimes.size() - 1);

      Stop stop = stopTime.getStop();
      AgencyAndId stopId = stop.getId();
      String id = stopId.getId();

      if ("1121".equals(id))
        trip.setTripHeadsign("Westlake");
      else if ("99904".equals(id))
        trip.setTripHeadsign("Sea-Tac Airport");
    }

  }

  private Route getRoute(GtfsMutableRelationalDao dao) {
    for (Route route : dao.getAllRoutes()) {
      if ("LINK".equals(route.getShortName()))
        return route;
    }
    return null;
  }

}
