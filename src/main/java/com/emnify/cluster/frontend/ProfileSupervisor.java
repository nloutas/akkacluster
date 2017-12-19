package com.emnify.cluster.frontend;

import com.emnify.cluster.messages.ClusterManagement.QueryById;
import com.emnify.cluster.messages.ClusterManagement.QueryResult;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import data.Endpoint;

/**
 * Supervisor for endpoint profiles
 *
 */
public class ProfileSupervisor extends AbstractActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private final ActorRef epShardRegionProxy;

  public ProfileSupervisor(ActorRef epShardRegionProxy) {
    this.epShardRegionProxy = epShardRegionProxy;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(QueryById.class, message -> {
      log.info("QueryById for id {}", message.getEndpointId());
      epShardRegionProxy.forward(message, getContext());

    }).match(QueryResult.class, message -> {
      getProfileActor(message.getEp());
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }


  private ActorRef getProfileActor(Endpoint ep) {
    return getContext().actorOf(ProfileActor.props(ep));
  }

  public static Props props(ActorRef epShardRegionProxy) {
    return Props.create(ProfileSupervisor.class, () -> new ProfileSupervisor(epShardRegionProxy));
  }
}
