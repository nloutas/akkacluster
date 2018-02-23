package com.emnify.cluster;


import static org.junit.Assert.assertTrue;

import com.emnify.cluster.frontend.ProfileSupervisor;
import com.emnify.cluster.messages.ClusterManagement.QueryResult;

import akka.actor.Props;
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
  public void testProfileCreation() {
    new TestKit(system) {
      {
        Long id = 999L;
        Endpoint endpoint =
            new Endpoint(id, "epName" + id, "imsi", "msisdn", "ip", id % 10, id % 12);

        profileSup.tell(new QueryResult(endpoint), getRef());
        assertTrue("ProfileActor not created",
            waitLog("ProfileActor initialised with " + endpoint.toString(), 1000));
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
