package org.onebusaway.king_county_metro_gtfs.transformations;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs_transformer.GtfsTransformer;
import org.onebusaway.gtfs_transformer.GtfsTransformerLibrary;
import org.onebusaway.gtfs_transformer.factory.TransformFactory;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategyFactory;
import org.onebusaway.gtfs_transformer.updates.EnsureStopTimesIncreaseUpdateStrategy;
import org.onebusaway.gtfs_transformer.updates.LocalVsExpressUpdateStrategy;
import org.onebusaway.gtfs_transformer.updates.RemoveEmptyBlockTripsStrategy;
import org.onebusaway.gtfs_transformer.updates.RemoveRepeatedStopTimesStrategy;
import org.onebusaway.king_county_metro_gtfs.model.PatternPair;

/**
 * Default transforms to apply to a King County Metro default GTFS feed
 * 
 * @author bdferris
 * 
 */
public class KingCountyMetroStrategy implements GtfsTransformStrategyFactory {

  private String _path;

  public void setPath(String path) {
    _path = path;
  }

  @Override
  public void createTransforms(GtfsTransformer transformer) {

    transformer.setAgencyId("KCM");

    TransformFactory factory = transformer.getTransformFactory();
    factory.addEntityPackage("org.onebusaway.king_county_metro_gtfs.model");

    DaoStrategy daoStrategy = new DaoStrategy();
    daoStrategy.setPath(_path);
    transformer.addTransform(daoStrategy);

    transformer.addTransform(new RemoveMergedTripsStrategy());
    transformer.addTransform(new DeduplicateStopsStrategy());
    transformer.addTransform(new DeduplicateRoutesStrategy());
    transformer.addTransform(new RemoveRepeatedStopTimesStrategy());
    transformer.addTransform(new RemoveEmptyBlockTripsStrategy());
    transformer.addTransform(new EnsureStopTimesIncreaseUpdateStrategy());

    configureCalendarUpdates(
        transformer,
        "http://onebusaway.googlecode.com/svn/wiki/KingCountyMetroCalendarModifications.wiki");

    configureStopNameUpdates(
        transformer,
        "http://onebusaway.googlecode.com/svn/wiki/KingCountyMetroStopNameModifications.wiki");

    GtfsTransformerLibrary.configureTransformation(transformer,
        "http://onebusaway.googlecode.com/svn/wiki/KingCountyMetroModifications.wiki");

    configureInterlinedRoutesUpdates(transformer);
    transformer.addTransform(new LocalVsExpressUpdateStrategy());
    
    transformer.addTransform(new RemoveNonFrequencyTrips());
  }

  private void configureStopNameUpdates(GtfsTransformer transformer, String path) {

    if (path == null)
      return;

    try {
      StopNameUpdateStrategyFactory factory = new StopNameUpdateStrategyFactory();

      if (path.startsWith("http")) {
        GtfsTransformStrategy strategy = factory.createFromUrl(new URL(path));
        transformer.addTransform(strategy);
      } else {
        GtfsTransformStrategy strategy = factory.createFromFile(new File(path));
        transformer.addTransform(strategy);
      }
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private void configureCalendarUpdates(GtfsTransformer transformer, String path) {

    if (path == null)
      return;

    try {
      CalendarUpdateStrategy updateStrategy = new CalendarUpdateStrategy();

      TripScheduleModificationFactoryBean factory = new TripScheduleModificationFactoryBean();
      factory.setPath(path);

      TripScheduleModificationStrategy modification = factory.createModificationStrategy();
      updateStrategy.addModificationStrategy(modification);

      transformer.addTransform(updateStrategy);

    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private void configureInterlinedRoutesUpdates(GtfsTransformer updater) {
    GtfsReader reader = updater.getReader();
    reader.getEntityClasses().add(PatternPair.class);
    updater.addTransform(new PatternPairUpdateStrategy());
  }

}
