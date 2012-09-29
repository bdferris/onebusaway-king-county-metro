package org.onebusaway.king_county_metro_gtfs.transformations;

import java.util.List;

import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.UpdateLibrary;

public class NoTripsWithBlockIdAndFrequenciesStrategy implements
    GtfsTransformStrategy {

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    for (Trip trip : dao.getAllTrips()) {
      List<Frequency> frequencies = dao.getFrequenciesForTrip(trip);
      if (!frequencies.isEmpty()) {
        trip.setBlockId(null);
      }
    }

    UpdateLibrary.clearDaoCache(dao);
  }
}
