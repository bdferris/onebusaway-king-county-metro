package org.onebusaway.king_county_metro.service_alerts.model;

public class UnresolvedAlert extends AbstractAlert {

  private AlertDescription fullDescription;

  public AlertDescription getFullDescription() {
    return fullDescription;
  }

  public void setFullDescription(AlertDescription fullDescription) {
    this.fullDescription = fullDescription;
  }
}
