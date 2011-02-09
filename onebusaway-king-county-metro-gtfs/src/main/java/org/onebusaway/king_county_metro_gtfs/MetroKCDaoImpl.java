package org.onebusaway.king_county_metro_gtfs;

import java.util.Collection;

import org.onebusaway.gtfs.impl.GenericDaoImpl;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCBlockTrip;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCChangeDate;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCPatternPair;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCStop;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCStopTime;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCTrip;
import org.onebusaway.king_county_metro_gtfs.model.VersionedId;

public class MetroKCDaoImpl extends GenericDaoImpl implements MetroKCDao {

  @Override
  public Collection<MetroKCChangeDate> getAllChangeDates() {
    return getAllEntitiesForType(MetroKCChangeDate.class);
  }

  @Override
  public Collection<MetroKCTrip> getAllTrips() {
    return getAllEntitiesForType(MetroKCTrip.class);
  }

  @Override
  public MetroKCChangeDate getChangeDateForId(String changeDateId) {
    return getEntityForId(MetroKCChangeDate.class, changeDateId);
  }

  @Override
  public MetroKCStop getStopForId(int id) {
    return getEntityForId(MetroKCStop.class, id);
  }

  @Override
  public MetroKCTrip getTripForId(VersionedId id) {
    return getEntityForId(MetroKCTrip.class, id);
  }

  @Override
  public void removeTrip(MetroKCTrip trip) {
    removeEntity(trip);
  }

  @Override
  public Collection<MetroKCBlockTrip> getAllBlockTrips() {
    return getAllEntitiesForType(MetroKCBlockTrip.class);
  }

  @Override
  public Collection<MetroKCStopTime> getAllStopTimes() {
    return getAllEntitiesForType(MetroKCStopTime.class);
  }

  @Override
  public Collection<MetroKCPatternPair> getAllPatternPairs() {
    return getAllEntitiesForType(MetroKCPatternPair.class);
  }

}
