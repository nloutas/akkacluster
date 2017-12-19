package com.emnify.cluster.simple;

import com.emnify.cluster.messages.ClusterManagement.QueryById;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import data.Endpoint;

/**
 * @author A team
 *
 */
public class EndpointActor extends AbstractActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private Endpoint ep;

  public EndpointActor(Long id) {
    ep = new Endpoint(id, "epName" + id, "112201234567890" + id, "111111" + id, "10.0.0.1", id + 1L,
        id + 2L);
  }


  @Override
  public Receive createReceive() {
    return receiveBuilder().match(QueryById.class, message -> {
      log.info("QueryById for id {}", message.getEndpointId());
      getSender().tell(ep, getSelf());
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

  public static Props props(Long id) {
    return Props.create(EndpointActor.class, () -> new EndpointActor(id));
  }
}
