package org.onebusaway.king_county_metro_gtfs;

import java.util.Collection;

import org.onebusaway.gtfs.services.GenericMutableDao;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCBlockTrip;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCChangeDate;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCPatternPair;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCStop;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCStopTime;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCTrip;
import org.onebusaway.king_county_metro_gtfs.model.VersionedId;

public interface MetroKCDao extends GenericMutableDao {

  public Collection<MetroKCChangeDate> getAllChangeDates();

  public MetroKCChangeDate getChangeDateForId(String changeDateId);

  public MetroKCStop getStopForId(int id);

  public Collection<MetroKCTrip> getAllTrips();

  public MetroKCTrip getTripForId(VersionedId id);

  public void removeTrip(MetroKCTrip trip);

  public Collection<MetroKCBlockTrip> getAllBlockTrips();

  public Collection<MetroKCStopTime> getAllStopTimes();

  public Collection<MetroKCPatternPair> getAllPatternPairs();
}
