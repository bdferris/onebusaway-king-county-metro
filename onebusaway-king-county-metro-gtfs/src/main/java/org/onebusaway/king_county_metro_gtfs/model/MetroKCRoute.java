/**
 * 
 */
package org.onebusaway.king_county_metro_gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;

@CsvFields(filename = "routes.csv")
public class MetroKCRoute extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private Integer id;

  private int number;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }
}