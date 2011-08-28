package org.onebusaway.king_county_metro.legacy_avl_to_siri;

public class Packet {

  public enum EStatus {
    NORMAL, EARLY, LATE, EMERGENCY, GROUP, BAD_ODOMETER
  }

  private short vehicleId;
  private int operatorId;
  private short route;
  private short run;
  private short serviceRoute;

  private boolean express;
  private EStatus status;

  private int servicePattern;
  private int servicePatternDistance;
  private int tripDistance;

  private short scheduleDeviation;

  private String pattern;

  private int trip;
  private int time;

  private boolean noTrip;

  private String unknown;

  public short getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(short vehicleId) {
    this.vehicleId = vehicleId;
  }

  public int getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(int operatorId) {
    this.operatorId = operatorId;
  }

  public short getRoute() {
    return route;
  }

  public void setRoute(short route) {
    this.route = route;
  }

  public short getRun() {
    return run;
  }

  public void setRun(short run) {
    this.run = run;
  }

  public short getServiceRoute() {
    return serviceRoute;
  }

  public void setServiceRoute(short serviceRoute) {
    this.serviceRoute = serviceRoute;
  }

  public boolean isExpress() {
    return express;
  }

  public void setExpress(boolean express) {
    this.express = express;
  }

  public EStatus getStatus() {
    return status;
  }

  public void setStatus(EStatus status) {
    this.status = status;
  }

  public int getServicePattern() {
    return servicePattern;
  }

  public void setServicePattern(int servicePattern) {
    this.servicePattern = servicePattern;
  }

  public int getServicePatternDistance() {
    return servicePatternDistance;
  }

  public void setServicePatternDistance(int servicePatternDistance) {
    this.servicePatternDistance = servicePatternDistance;
  }

  public int getTripDistance() {
    return tripDistance;
  }

  public void setTripDistance(int tripDistance) {
    this.tripDistance = tripDistance;
  }

  public short getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(short scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public int getTrip() {
    return trip;
  }

  public void setTrip(int trip) {
    this.trip = trip;
  }

  public int getTime() {
    return time;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public boolean isNoTrip() {
    return noTrip;
  }

  public void setNoTrip(boolean noTrip) {
    this.noTrip = noTrip;
  }

  public String getUnknown() {
    return unknown;
  }

  public void setUnknown(String unknown) {
    this.unknown = unknown;
  }
}
