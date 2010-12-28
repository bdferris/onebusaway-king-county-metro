package org.onebusaway.king_county_metro.service_alerts.snow.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.king_county_metro.service_alerts.snow.model.ActiveReroute;
import org.onebusaway.king_county_metro.service_alerts.snow.model.ActiveRerouteRef;
import org.onebusaway.king_county_metro.service_alerts.snow.model.RerouteBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnowRerouteController {

  private static Logger _log = LoggerFactory.getLogger(SnowRerouteController.class);

  private Set<String> _regions = new HashSet<String>();

  private Map<ActiveRerouteRef, List<RerouteBean>> _reroutesByActivation = new FactoryMap<ActiveRerouteRef, List<RerouteBean>>(
      new ArrayList<RerouteBean>());

  private Map<RerouteBean, String> _activeReroutesWithIds = new HashMap<RerouteBean, String>();

  public void setReroutes(Collection<RerouteBean> reroutes) {

    for (RerouteBean bean : reroutes) {

      String region = bean.getRegionId();
      String routeId = bean.getRouteId();
      String routeShortName = routeId.substring(routeId.indexOf('_') + 1);

      ActiveRerouteRef ref = new ActiveRerouteRef(routeShortName, region);

      _reroutesByActivation.get(ref).add(bean);

      _regions.add(region);
    }
  }

  public synchronized Map<String, RerouteBean> getActiveReroutes() {
    Map<String, RerouteBean> beansById = new HashMap<String, RerouteBean>();
    for (Map.Entry<RerouteBean, String> entry : _activeReroutesWithIds.entrySet())
      beansById.put(entry.getValue(), entry.getKey());
    return beansById;
  }

  public synchronized void setActiveReroutes(List<ActiveReroute> activeReroutes) {

    Set<ActiveRerouteRef> activeRefs = new HashSet<ActiveRerouteRef>();
    for (ActiveReroute reroute : activeReroutes) {
      activeRefs.add(reroute.getRef());
      if (!_regions.contains(reroute.getRegion()))
        _log.info("unknown region: " + reroute.getRegion());
    }

    Set<RerouteBean> activeRerouteBeans = new HashSet<RerouteBean>();
    for (ActiveRerouteRef ref : activeRefs) {
      activeRerouteBeans.addAll(_reroutesByActivation.get(ref));
    }

    _activeReroutesWithIds.keySet().retainAll(activeRerouteBeans);

    int index = 0;
    for (RerouteBean bean : activeRerouteBeans) {
      if (!_activeReroutesWithIds.containsKey(bean)) {
        String id = "1_" + System.currentTimeMillis() + "." + (index++);
        _activeReroutesWithIds.put(bean, id);
      }
    }
  }
}
