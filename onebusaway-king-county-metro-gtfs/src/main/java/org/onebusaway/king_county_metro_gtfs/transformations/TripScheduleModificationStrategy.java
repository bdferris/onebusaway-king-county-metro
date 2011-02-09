package org.onebusaway.king_county_metro_gtfs.transformations;

import java.util.Date;
import java.util.Set;

import org.onebusaway.king_county_metro_gtfs.model.MetroKCServiceId;

public interface TripScheduleModificationStrategy {

  public Set<Date> getCancellations(MetroKCServiceId key, Set<Date> dates);

  public Set<Date> getAdditions(MetroKCServiceId key, Set<Date> dates);
}
