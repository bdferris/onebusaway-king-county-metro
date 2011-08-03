package org.onebusaway.king_county_metro_gtfs.transformations;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
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
  }

  private void removeNonFrequencyTripsForRoute(GtfsMutableRelationalDao dao,
      Route route) {

    if (route == null)
      return;

    List<Trip> toRemove = new ArrayList<Trip>();

    for (Trip trip : dao.getTripsForRoute(route)) {
      List<Frequency> frequencies = dao.getFrequenciesForTrip(trip);
      if (frequencies.isEmpty())
        toRemove.add(trip);
      else
        trip.setBlockId(null);
    }

    TransformLibrary library = new TransformLibrary();
    for (Trip trip : toRemove)
      library.removeEntity(dao, trip);
  }
}
