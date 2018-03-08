package com.emnify.cluster.frontend;

import com.emnify.cluster.messages.ClusterManagement.QueryById;
import com.emnify.cluster.messages.ClusterManagement.QueryResult;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import data.Endpoint;

/**
 * handle endpoint profile in the frontend node
 *
 */
public class ProfileActor extends AbstractActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private Endpoint endpoint;

  public ProfileActor(Endpoint ep) {
    this.endpoint = ep;
    log.info("ProfileActor initialised with {} under path {}", ep, self().path());
  }


  @Override
  public Receive createReceive() {
    return receiveBuilder().match(QueryById.class, message -> {
      log.info("QueryById for id {}", message.getEndpointId());
      getSender().tell(new QueryResult(endpoint), getSelf());
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

  public static Props props(Endpoint ep) {
    return Props.create(ProfileActor.class, () -> new ProfileActor(ep));
  }
}
