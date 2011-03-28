package org.onebusaway.king_county_metro_gtfs.transformations;

import java.util.Collection;

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class FixMergedIds implements GtfsTransformStrategy {

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    fix(dao.getAllStops(), "id");
    fix(dao.getAllTrips(), "id");

    fix(dao.getAllTrips(), "serviceId");
    fix(dao.getAllCalendars(), "serviceId");
    fix(dao.getAllCalendarDates(), "serviceId");

    fix(dao.getAllTrips(), "shapeId");
    fix(dao.getAllShapePoints(), "shapeId");
  }

  private void fix(Collection<?> entities, String propertyName) {

    for (Object entity : entities) {

      BeanWrapper w = BeanWrapperFactory.wrap(entity);
      Object v = w.getPropertyValue(propertyName);
      if (v != null && v instanceof AgencyAndId) {
        AgencyAndId id = (AgencyAndId) v;
        id = fixId(id);
        w.setPropertyValue(propertyName, id);
      }
    }
  }

  private AgencyAndId fixId(AgencyAndId aid) {
    String agencyId = aid.getAgencyId();
    String id = aid.getId();
    id = id.replaceAll("_merged_.*", "");
    return new AgencyAndId(agencyId, id);
  }
}
