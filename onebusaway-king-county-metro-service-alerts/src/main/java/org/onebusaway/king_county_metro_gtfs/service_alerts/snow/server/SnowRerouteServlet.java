package org.onebusaway.king_county_metro_gtfs.service_alerts.snow.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.king_county_metro_gtfs.service_alerts.snow.model.RerouteBean;
import org.onebusaway.siri.ConditionDetails;
import org.onebusaway.siri.core.SiriServer;

import uk.org.siri.siri.AffectedCallStructure;
import uk.org.siri.siri.AffectedVehicleJourneyStructure;
import uk.org.siri.siri.AffectedVehicleJourneyStructure.Calls;
import uk.org.siri.siri.AffectsScopeStructure;
import uk.org.siri.siri.AffectsScopeStructure.VehicleJourneys;
import uk.org.siri.siri.DefaultedTextStructure;
import uk.org.siri.siri.DirectionRefStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.ParticipantRefStructure;
import uk.org.siri.siri.PtConsequenceStructure;
import uk.org.siri.siri.PtConsequencesStructure;
import uk.org.siri.siri.PtSituationElementStructure;
import uk.org.siri.siri.ServiceConditionEnumeration;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.ServiceRequest;
import uk.org.siri.siri.SituationExchangeDeliveryStructure;
import uk.org.siri.siri.SituationExchangeDeliveryStructure.Situations;
import uk.org.siri.siri.StopPointRefStructure;

public class SnowRerouteServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private SnowRerouteController _controller;

  private SiriServer _siriServer;

  public void setController(SnowRerouteController controller) {
    _controller = controller;
  }

  public void setSiriServer(SiriServer siriServer) {
    _siriServer = siriServer;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    ServiceRequest serviceRequest = _siriServer.unmarshall(req.getReader());
    Map<String, RerouteBean> reroutes = _controller.getActiveReroutes();

    ServiceDelivery delivery = new ServiceDelivery();
    addReroutesToDelivery(reroutes, delivery);

    _siriServer.deliverResponse(serviceRequest, delivery, resp.getWriter());
  }

  private void addReroutesToDelivery(Map<String, RerouteBean> reroutes,
      ServiceDelivery delivery) {

    SituationExchangeDeliveryStructure situationDelivery = new SituationExchangeDeliveryStructure();
    delivery.getSituationExchangeDelivery().add(situationDelivery);

    Situations situations = new Situations();
    situationDelivery.setSituations(situations);

    for (Map.Entry<String, RerouteBean> entry : reroutes.entrySet()) {

      String situationId = entry.getKey();
      RerouteBean reroute = entry.getValue();

      PtSituationElementStructure ptSituation = new PtSituationElementStructure();
      situations.getPtSituationElement().add(ptSituation);

      ParticipantRefStructure participantRef = new ParticipantRefStructure();
      participantRef.setValue(situationId);
      ptSituation.setParticipantRef(participantRef);

      DefaultedTextStructure summary = new DefaultedTextStructure();
      summary.setValue("Route " + reroute.getRouteShortName()
          + " - Snow Reroute");
      ptSituation.setSummary(summary);

      DefaultedTextStructure description = new DefaultedTextStructure();
      description.setValue(reroute.getDescription());
      ptSituation.setDescription(description);

      PtConsequencesStructure consequencesContainer = new PtConsequencesStructure();
      ptSituation.setConsequences(consequencesContainer);

      List<PtConsequenceStructure> consequences = consequencesContainer.getConsequence();

      PtConsequenceStructure consequence = new PtConsequenceStructure();
      consequences.add(consequence);

      consequence.setCondition(ServiceConditionEnumeration.DIVERTED);

      ConditionDetails conditionDetails = new ConditionDetails();
      conditionDetails.setDiversionPath(reroute.getReroteAsPolylineString());

      ExtensionsStructure extensions = new ExtensionsStructure();
      consequence.setExtensions(extensions);

      extensions.setAny(conditionDetails);

      AffectsScopeStructure affects = new AffectsScopeStructure();
      ptSituation.setAffects(affects);

      VehicleJourneys journeys = new VehicleJourneys();
      affects.setVehicleJourneys(journeys);
      List<AffectedVehicleJourneyStructure> vehicleJourneys = journeys.getAffectedVehicleJourney();

      AffectedVehicleJourneyStructure affectedVehicleJourney = new AffectedVehicleJourneyStructure();
      vehicleJourneys.add(affectedVehicleJourney);

      LineRefStructure lineRef = new LineRefStructure();
      lineRef.setValue(reroute.getRouteId());
      affectedVehicleJourney.setLineRef(lineRef);

      if (reroute.getDirectionId() != null) {
        DirectionRefStructure directionRef = new DirectionRefStructure();
        directionRef.setValue(reroute.getDirectionId());
        affectedVehicleJourney.setDirectionRef(directionRef);
      }

      List<String> stopIds = reroute.getStopIds();
      if (!CollectionsLibrary.isEmpty(stopIds)) {
        Calls callsContainer = new Calls();
        List<AffectedCallStructure> calls = callsContainer.getCall();
        for (String stopId : stopIds) {
          AffectedCallStructure call = new AffectedCallStructure();
          StopPointRefStructure stopPointRef = new StopPointRefStructure();
          stopPointRef.setValue(stopId);
          call.setStopPointRef(stopPointRef);
          calls.add(call);
        }
        affectedVehicleJourney.setCalls(callsContainer);
      }

    }

  }
}
