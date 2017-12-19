package com.emnify.cluster.backend;

import com.emnify.cluster.messages.ClusterManagement.QueryById;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.ORMap;
import akka.cluster.ddata.ORMapKey;
import akka.cluster.sharding.ClusterSharding;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * @author A team
 *
 */
public class EndpointSupervisor extends AbstractActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private final ActorRef epShardingRegion;
  // final Key<ORMap<Long, Long>> imsi2epid = ORMapKey.create("imsi2epid");

  public EndpointSupervisor(ActorRef epShardingRegion) {
    this.epShardingRegion = epShardingRegion;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(QueryById.class, message -> {
      log.info("QueryById for id {}", message.getEndpointId());
      epRegion().forward(message, getContext());
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }


  private ActorRef epRegion() {
    return ClusterSharding.get(getContext().system()).shardRegion("Endpoint");
  }

  public static Props props(ActorRef epShardingRegion) {
    return Props.create(EndpointSupervisor.class, () -> new EndpointSupervisor(epShardingRegion));
  }
}
