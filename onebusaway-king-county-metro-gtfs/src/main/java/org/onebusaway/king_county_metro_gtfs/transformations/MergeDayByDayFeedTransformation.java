package org.onebusaway.king_county_metro_gtfs.transformations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.collections.Counter;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.impl.RemoveEntityLibrary;
import org.onebusaway.gtfs_transformer.services.GtfsEntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.UpdateLibrary;

public class MergeDayByDayFeedTransformation implements
    GtfsEntityTransformStrategy, GtfsTransformStrategy {

  private RemoveEntityLibrary _removeEntityLibrary = new RemoveEntityLibrary();

  /**
   * Mapping of trips based on their "common" id.
   */
  private Map<AgencyAndId, List<Trip>> _tripsByCommonTripId = new FactoryMap<AgencyAndId, List<Trip>>(
      new ArrayList<Trip>());

  private Map<Set<AgencyAndId>, AgencyAndId> _serviceIdMapping = new HashMap<Set<AgencyAndId>, AgencyAndId>();

  private double _minNumberOfWeeksForCalendarEntry = 3;

  private double _dayOfTheWeekInclusionRatio = 0.5;

  private Set<AgencyAndId> _tripsToKeep = new HashSet<AgencyAndId>();

  public void setDayOfTheWeekInclusionRatio(double dayOfTheWeekInclusionRatio) {
    _dayOfTheWeekInclusionRatio = dayOfTheWeekInclusionRatio;
  }

  @Override
  public Object transformEntity(TransformContext context,
      GtfsMutableRelationalDao dao, Object entity) {

    if (entity instanceof Trip) {
      return transformTrip(context, dao, (Trip) entity);
    } else if (entity instanceof StopTime) {
      return transformStopTime(context, dao, (StopTime) entity);
    }

    return entity;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    for (Map.Entry<AgencyAndId, List<Trip>> entry : _tripsByCommonTripId.entrySet()) {

      AgencyAndId commonTripId = entry.getKey();
      List<Trip> trips = entry.getValue();
      List<AgencyAndId> serviceIds = MappingLibrary.map(trips, "serviceId");

      AgencyAndId serviceId = getMergedServiceIdForOriginalServiceIds(serviceIds);

      /**
       * Remove the old trips
       */
      for (int i = 1; i < trips.size(); i++) {
        Trip trip = trips.get(i);
        _removeEntityLibrary.removeTrip(dao, trip);
      }

      Trip firstTrip = trips.get(0);
      firstTrip.setId(commonTripId);
      firstTrip.setServiceId(serviceId);
    }

    CalendarService calendarService = createCalendarService(dao);
    Calendar c = Calendar.getInstance();
    TimeZone tz = TimeZone.getDefault();

    List<ServiceCalendar> calendarsToAdd = new ArrayList<ServiceCalendar>();
    List<ServiceCalendarDate> calendarDatesToAdd = new ArrayList<ServiceCalendarDate>();

    for (Map.Entry<Set<AgencyAndId>, AgencyAndId> entry : _serviceIdMapping.entrySet()) {

      Set<AgencyAndId> serviceIds = entry.getKey();
      AgencyAndId updatedServiceId = entry.getValue();

      Set<ServiceDate> allServiceDates = new HashSet<ServiceDate>();
      for (AgencyAndId serviceId : serviceIds) {
        Set<ServiceDate> serviceDates = calendarService.getServiceDatesForServiceId(serviceId);
        allServiceDates.addAll(serviceDates);
      }

      List<ServiceDate> serviceDatesInOrder = new ArrayList<ServiceDate>(
          allServiceDates);
      Collections.sort(serviceDatesInOrder);

      Counter<Integer> daysOfTheWeekCounts = new Counter<Integer>();
      for (ServiceDate serviceDate : allServiceDates) {
        c.setTime(serviceDate.getAsDate());
        int dayOfTheWeek = c.get(Calendar.DAY_OF_WEEK);
        daysOfTheWeekCounts.increment(dayOfTheWeek);
      }

      Set<Integer> daysOfTheWeekToUse = new HashSet<Integer>();
      Integer maxKey = daysOfTheWeekCounts.getMax();
      int maxCount = daysOfTheWeekCounts.getCount(maxKey);

      for (Integer dayOfTheWeek : daysOfTheWeekCounts.getKeys()) {
        int count = daysOfTheWeekCounts.getCount(dayOfTheWeek);
        if (count < maxCount * _dayOfTheWeekInclusionRatio)
          continue;
        daysOfTheWeekToUse.add(dayOfTheWeek);
      }

      ServiceDate fromDate = serviceDatesInOrder.get(0);
      ServiceDate toDate = serviceDatesInOrder.get(serviceDatesInOrder.size() - 1);

      boolean useDateRange = maxCount >= _minNumberOfWeeksForCalendarEntry;

      if (useDateRange) {
        ServiceCalendar sc = createServiceCalendar(updatedServiceId,
            daysOfTheWeekToUse, fromDate, toDate);
        calendarsToAdd.add(sc);
      }

      for (ServiceDate serviceDate = fromDate; serviceDate.compareTo(toDate) <= 0; serviceDate = serviceDate.next(tz)) {

        boolean isActive = allServiceDates.contains(serviceDate);

        Calendar serviceDateAsCalendar = serviceDate.getAsCalendar(tz);
        if (useDateRange) {
          int dayOfWeek = serviceDateAsCalendar.get(Calendar.DAY_OF_WEEK);
          boolean dateRangeIncludesServiceDate = daysOfTheWeekToUse.contains(dayOfWeek);
          if (isActive && !dateRangeIncludesServiceDate) {
            ServiceCalendarDate scd = new ServiceCalendarDate();
            scd.setDate(serviceDate);
            scd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_ADD);
            scd.setServiceId(updatedServiceId);
            calendarDatesToAdd.add(scd);
          }
          if (!isActive && dateRangeIncludesServiceDate) {
            ServiceCalendarDate scd = new ServiceCalendarDate();
            scd.setDate(serviceDate);
            scd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_REMOVE);
            scd.setServiceId(updatedServiceId);
            calendarDatesToAdd.add(scd);
          }
        } else {
          ServiceCalendarDate scd = new ServiceCalendarDate();
          scd.setDate(serviceDate);
          scd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_ADD);
          scd.setServiceId(updatedServiceId);
          calendarDatesToAdd.add(scd);
        }
      }
    }

    dao.clearAllEntitiesForType(ServiceCalendar.class);
    dao.clearAllEntitiesForType(ServiceCalendarDate.class);

    for (ServiceCalendar sc : calendarsToAdd)
      dao.saveEntity(sc);
    for (ServiceCalendarDate scd : calendarDatesToAdd)
      dao.saveEntity(scd);

    UpdateLibrary.clearDaoCache(dao);
  }

  private Object transformTrip(TransformContext context,
      GtfsMutableRelationalDao dao, Trip trip) {

    AgencyAndId tripId = trip.getId();
    String rawTripId = tripId.getId();

    AgencyAndId serviceId = trip.getServiceId();
    String rawServiceId = serviceId.getId();

    if (!rawTripId.endsWith(rawServiceId))
      throw new IllegalStateException(
          "expected trip id to take form COMMON_TRIP_ID + SERVICE_ID - tripId="
              + rawTripId + " serviceId=" + rawServiceId);

    String rawCommonTripId = rawTripId.substring(0, rawTripId.length()
        - rawServiceId.length());

    AgencyAndId commonTripId = new AgencyAndId(tripId.getAgencyId(),
        rawCommonTripId);
    List<Trip> tripsWithCommonTripId = _tripsByCommonTripId.get(commonTripId);
    tripsWithCommonTripId.add(trip);

    /**
     * We only keep the very first trip in the set of trips with the same common
     * trip id. Note that we can't actually preemptively delete a secondary trip
     * yet, because we need it around for loading stop time references. We'll
     * delete the trip later.
     */
    if (tripsWithCommonTripId.size() == 1) {
      _tripsToKeep.add(trip.getId());
    }

    return trip;
  }

  private Object transformStopTime(TransformContext context,
      GtfsMutableRelationalDao dao, StopTime stopTime) {

    /**
     * Preemptively delete the stop time if its parent trip shouldn't be kept
     * around
     */
    Trip trip = stopTime.getTrip();
    if (!_tripsToKeep.contains(trip.getId()))
      return null;

    return stopTime;
  }

  private AgencyAndId getMergedServiceIdForOriginalServiceIds(
      List<AgencyAndId> serviceIds) {

    if (serviceIds.isEmpty())
      throw new IllegalStateException();

    Set<AgencyAndId> serviceIdsSet = new HashSet<AgencyAndId>(serviceIds);
    AgencyAndId serviceId = _serviceIdMapping.get(serviceIdsSet);

    if (serviceId == null) {

      AgencyAndId firstId = serviceIds.get(0);
      String mergedId = Integer.toString(_serviceIdMapping.size());
      serviceId = new AgencyAndId(firstId.getAgencyId(), mergedId);

      _serviceIdMapping.put(serviceIdsSet, serviceId);
    }

    return serviceId;
  }

  private CalendarServiceImpl createCalendarService(GtfsMutableRelationalDao dao) {
    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
    factory.setGtfsDao(dao);

    CalendarServiceImpl calendarService = new CalendarServiceImpl();
    calendarService.setDataFactory(factory);
    return calendarService;
  }

  private ServiceCalendar createServiceCalendar(AgencyAndId updatedServiceId,
      Set<Integer> daysOfTheWeekToUse, ServiceDate fromDate, ServiceDate toDate) {

    ServiceCalendar sc = new ServiceCalendar();

    sc.setServiceId(updatedServiceId);

    sc.setStartDate(fromDate);
    sc.setEndDate(toDate);

    if (daysOfTheWeekToUse.contains(Calendar.MONDAY))
      sc.setMonday(1);
    if (daysOfTheWeekToUse.contains(Calendar.TUESDAY))
      sc.setTuesday(1);
    if (daysOfTheWeekToUse.contains(Calendar.WEDNESDAY))
      sc.setWednesday(1);
    if (daysOfTheWeekToUse.contains(Calendar.THURSDAY))
      sc.setThursday(1);
    if (daysOfTheWeekToUse.contains(Calendar.FRIDAY))
      sc.setFriday(1);
    if (daysOfTheWeekToUse.contains(Calendar.SATURDAY))
      sc.setSaturday(1);
    if (daysOfTheWeekToUse.contains(Calendar.SUNDAY))
      sc.setSunday(1);
    return sc;
  }

}
