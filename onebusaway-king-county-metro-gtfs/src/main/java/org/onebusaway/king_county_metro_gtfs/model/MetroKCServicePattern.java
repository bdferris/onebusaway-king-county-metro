/**
 * 
 */
package org.onebusaway.king_county_metro_gtfs.model;

import org.onebusaway.csv_entities.schema.FlattenFieldMappingFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs_transformer.csv.ServiceTypeFieldMappingFactory;

@CsvFields(filename = "service_patterns.csv")
public class MetroKCServicePattern extends IdentityBean<VersionedId> {

  private static final long serialVersionUID = 1L;

  @CsvField(mapping=FlattenFieldMappingFactory.class)
  private VersionedId id;

  @CsvField(name = "service_type", mapping = ServiceTypeFieldMappingFactory.class)
  private boolean express;

  private String route;

  private int schedulePatternId;

  private String direction;

  public VersionedId getId() {
    return id;
  }

  public void setId(VersionedId id) {
    this.id = id;
  }

  public boolean isExpress() {
    return express;
  }

  public void setExpress(boolean express) {
    this.express = express;
  }

  public String getRoute() {
    return route;
  }

  public void setRoute(String route) {
    this.route = route;
  }

  public int getSchedulePatternId() {
    return schedulePatternId;
  }

  public void setSchedulePatternId(int schedulePatternId) {
    this.schedulePatternId = schedulePatternId;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  @Override
  public String toString() {
    return Integer.toString(this.id.getId());
  }
}