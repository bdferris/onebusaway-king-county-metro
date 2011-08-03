package org.onebusaway.king_county_metro_gtfs.transformations;

import java.io.File;
import java.util.List;

import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.king_county_metro_gtfs.MetroKCDao;
import org.onebusaway.king_county_metro_gtfs.MetroKCDaoImpl;
import org.onebusaway.king_county_metro_gtfs.MetroKCDataReader;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCChangeDate;
import org.onebusaway.king_county_metro_gtfs.model.MetroKCTrip;

/**
 * Plugin for loading legacy King County Metro Schedule data.
 * 
 * @author bdferris
 * 
 */
public class DaoStrategy implements GtfsTransformStrategy {

  private String _path;

  public void setPath(String path) {
    _path = path;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    MetroKCDao metroKCDao = new MetroKCDaoImpl();

    if (_path != null) {

      File path = new File(_path);

      try {
        MetroKCDataReader reader = new MetroKCDataReader();
        reader.setInputLocation(path);
        reader.setDao(metroKCDao);

        List<Class<?>> entityClasses = reader.getEntityClasses();
        entityClasses.clear();
        entityClasses.add(MetroKCChangeDate.class);
        entityClasses.add(MetroKCTrip.class);
        //entityClasses.add(MetroKCStop.class);
        //entityClasses.add(MetroKCPatternPair.class);
        //entityClasses.add(MetroKCStopTime.class);
        //entityClasses.add(MetroKCBlockTrip.class);

        reader.run();
      } catch (Exception ex) {
        throw new IllegalStateException(ex);
      }
    }

    context.putParameter(MetroKCDao.class.getName(), metroKCDao);
  }

  public static MetroKCDao getDao(TransformContext context) {
    MetroKCDao metrokcDao = context.getParameter(MetroKCDao.class.getName());
    if (metrokcDao == null)
      metrokcDao = new MetroKCDaoImpl();
    return metrokcDao;
  }
}
