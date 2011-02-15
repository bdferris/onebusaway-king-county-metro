package org.onebusaway.king_county_metro.mybus_siri;

import java.io.Serializable;

public class TimepointPrediction implements Serializable {

  private static final long serialVersionUID = 1L;

  private String agencyId;

  private String blockId;

  private String tripId;

  private String timepointId;

  private int timepointScheduledTime;

  private int timepointPredictedTime;

  private int scheduleDeviation;

  private String vehicleId;

  private String predictorType;

  private int timeOfPrediction;

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getTimepointId() {
    return timepointId;
  }

  public void setTimepointId(String timepointId) {
    this.timepointId = timepointId;
  }

  public int getTimepointScheduledTime() {
    return timepointScheduledTime;
  }

  public void setTimepointScheduledTime(int timepointScheduledTime) {
    this.timepointScheduledTime = timepointScheduledTime;
  }

  public int getTimepointPredictedTime() {
    return timepointPredictedTime;
  }

  public void setTimepointPredictedTime(int timepointPredictedTime) {
    this.timepointPredictedTime = timepointPredictedTime;
  }

  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(int scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public String getPredictorType() {
    return predictorType;
  }

  public void setPredictorType(String predictorType) {
    this.predictorType = predictorType;
  }

  public int getTimeOfPrediction() {
    return timeOfPrediction;
  }

  public void setTimeOfPrediction(int timeOfPrediction) {
    this.timeOfPrediction = timeOfPrediction;
  }
}
