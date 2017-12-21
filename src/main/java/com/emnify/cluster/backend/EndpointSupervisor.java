package com.emnify.cluster.backend;

import com.emnify.cluster.messages.ClusterManagement.EntityEnvelope;
import com.emnify.cluster.messages.ClusterManagement.QueryById;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.ORMap;
import akka.cluster.ddata.ORMapKey;
import akka.cluster.sharding.ClusterSharding;
import akka.event.Logging;
import akka.event.LoggingAdapter;


/**
 * Endpoint Supervisor
 *
 */
public class EndpointSupervisor extends AbstractActor {
  private final ActorSystem system = getContext().system();
  private final LoggingAdapter log = Logging.getLogger(system, this);
  private final ActorRef epShardingRegion;
  // final Key<ORMap<Long, Long>> imsi2epid = ORMapKey.create("imsi2epid");
  private final Long port =
      system.settings().config().getLong("akka.remote.netty.tcp.port");

  public EndpointSupervisor(ActorRef epShardingRegion) {
    this.epShardingRegion = epShardingRegion;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(QueryById.class, message -> {
      log.info("BE {}: QueryById for id {}", port, message.getEndpointId());
      epRegion().forward(message, getContext());
    }).match(EntityEnvelope.class, message -> {
      log.info("BE {}: EntityEnvelope for id {}", port, message.id);
      epRegion().forward(message, getContext());
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }


  private ActorRef epRegion() {
    return ClusterSharding.get(system).shardRegion("Endpoint");
  }


  public static Props props(ActorRef epShardingRegion) {
    return Props.create(EndpointSupervisor.class, () -> new EndpointSupervisor(epShardingRegion));
  }
}
