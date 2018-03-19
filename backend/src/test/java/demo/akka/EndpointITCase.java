package demo.akka;

import static org.junit.Assert.assertNotNull;

import demo.akka.backend.EndpointActor;
import demo.akka.backend.EndpointSupervisor;
import demo.akka.messages.ClusterManagement;
import demo.akka.messages.ClusterManagement.EntityEnvelope;
import demo.akka.messages.ClusterManagement.QueryById;
import demo.akka.messages.ClusterManagement.QueryByImsi;
import demo.akka.messages.ClusterManagement.QueryResult;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.testkit.javadsl.TestKit;
import org.junit.Test;

public class EndpointITCase extends ITCaseBase {

  @Override
  protected void setupActors() {
    // register Endpoint type in ClusterSharding Region
    final ActorRef epShardingRegion =
        ClusterSharding.get(system).start("Endpoint", Props.create(EndpointActor.class),
            ClusterShardingSettings.create(system), ClusterManagement.MESSAGE_EXTRACTOR);
    // start EndpointSupervisor
    endpointSupervisor =
        system.actorOf(Props.create(EndpointSupervisor.class, epShardingRegion), "endpoints");
  }

  @Test
  public void testEndpointActor() {
    new TestKit(system) {
      {
        int epTotal = 10;
        for (long id = 1L; id <= epTotal; id++) {
          if ((id % 2) == 0) {
            endpointSupervisor.tell(new QueryById(id), getRef());
          } else {
            endpointSupervisor
                .tell(new EntityEnvelope(id - 10L, new QueryByImsi("01234567890" + id)), getRef());
          }
        }

        // verify results
        for (int i = 1; i <= epTotal; i++) {
          QueryResult ceResult = expectMsgClass(TIMEOUT, QueryResult.class);

          assertNotNull("Endpoint " + i + " is null", ceResult.getEp());
          if (ceResult.getEp().getId() == null) {
            assertNotNull("Endpoint " + i + " Imsi is null", ceResult.getEp().getImsi());
          } else {
            assertNotNull("Endpoint " + i + " Id is null", ceResult.getEp().getId());
          }
        }

      }
    };
  }

}
