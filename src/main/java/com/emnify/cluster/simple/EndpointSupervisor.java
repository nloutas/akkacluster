package com.emnify.cluster.simple;

import com.emnify.cluster.messages.ClusterManagement;
import com.emnify.cluster.messages.ClusterManagement.QueryById;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * @author A team
 *
 */
public class EndpointSupervisor extends AbstractActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private final ActorRef startedCounterRegion;

  public EndpointSupervisor() {
    ClusterShardingSettings settings = ClusterShardingSettings.create(getContext().system());
    this.startedCounterRegion = ClusterSharding.get(getContext().system()).start("Endpoint",
        Props.create(EndpointActor.class), settings, ClusterManagement.MESSAGE_EXTRACTOR);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(QueryById.class, message -> {
      log.info("QueryById for id {}", message.getEndpointId());
      getEndpointActor(message.getEndpointId()).forward(message, getContext());
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }


  private ActorRef getEndpointActor(Long id) {
    return getContext().actorOf(EndpointActor.props(id));
  }

  public static Props props() {
    return Props.create(EndpointSupervisor.class, () -> new EndpointSupervisor());
  }
}
