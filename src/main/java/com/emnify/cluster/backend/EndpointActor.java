package com.emnify.cluster.backend;

import com.emnify.cluster.messages.ClusterManagement.QueryById;
import com.emnify.cluster.messages.ClusterManagement.QueryResult;

import akka.actor.AbstractActor;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import data.Endpoint;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * @author A team
 *
 */
public class EndpointActor extends AbstractActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private final Duration INACTIVITY_TIMEOUT = Duration.create(120, TimeUnit.SECONDS);
  private Endpoint ep;

  public EndpointActor(Long id) {
    ep = new Endpoint(id, "epName" + id, "112201234567890" + id, "111111" + id, "10.0.0.1", id + 1L,
        id + 2L);
  }

  @Override
  public void preStart() throws Exception {
    super.preStart();
    getContext().setReceiveTimeout(INACTIVITY_TIMEOUT);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(QueryById.class, message -> {
      log.info("QueryById for id {}", message.getEndpointId());
      getSender().tell(new QueryResult(ep), getSelf());
    }).matchEquals(ReceiveTimeout.getInstance(), msg -> passivate())
        .matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

  private void passivate() {
    getContext().getParent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
  }

  public static Props props(Long id) {
    return Props.create(EndpointActor.class, () -> new EndpointActor(id));
  }
}
