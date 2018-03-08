package com.emnify.cluster;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.emnify.cluster.frontend.ProfileSupervisor;
import com.emnify.cluster.messages.ClusterManagement.QueryById;
import com.emnify.cluster.messages.ClusterManagement.QueryByIp;
import com.emnify.cluster.messages.ClusterManagement.QueryResult;

import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging.Warning;
import akka.testkit.javadsl.EventFilter;
import akka.testkit.javadsl.TestKit;
import data.Endpoint;
import org.junit.Test;

import java.util.Arrays;

public class ProfileITCase extends ITCaseBase {

  @Override
  protected void setupActors() {
    profileSup = system.actorOf(Props.create(ProfileSupervisor.class), "profiles");
    checkScheduledMessages();
  }

  @Test
  public void testProfileActor() {
    new TestKit(system) {
      {
        assertEquals("ClusterSystem", system.name());
        Long id = 999L;
        Endpoint endpoint =
            new Endpoint(id, "epName" + id, "imsi", "msisdn", "ip", id % 10, id % 12);

        profileSup.tell(new QueryResult(endpoint), getRef());
        assertTrue("ProfileActor not created",
            waitLog("ProfileActor initialised with " + endpoint.toString(), 1000));

        // the profileActor responds to QueryById with the cached Endpoint object
        ActorSelection profileActorSel = system.actorSelection("/user/profiles/$a");
        profileActorSel.tell(new QueryById(id), getRef());
        QueryResult queryResult = expectMsgClass(QueryResult.class);
        assertEquals("ProfileActor did not return the original endpoint object", endpoint,
            queryResult.getEp());

        final String result = new EventFilter(Warning.class, system)
            .from("akka://ClusterSystem/user/profiles/$a").occurrences(1).intercept(() -> {
              // sending a QueryByIp message should result in logging a Warning
              profileActorSel.tell(new QueryByIp("127.0.0.1"), getRef());
              return "warning ok";
            });
        assertEquals("warning ok", result);
      }
    };
  }

  public void checkScheduledMessages() {
    for (String i : Arrays.asList("1", "2")) {
      assertTrue("did not route QueryById for id 255" + i,
          waitLog("backendRoutingActor   - QueryById for id 255" + i, 2000));
      assertTrue("did not route EntityEnvelope for id 254" + i,
          waitLog("backendRoutingActor   - EntityEnvelope for id 254" + i, 2000));
    }
  }


}
