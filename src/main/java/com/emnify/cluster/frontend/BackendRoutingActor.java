package com.emnify.cluster.frontend;

import com.emnify.cluster.messages.ClusterManagement;

import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;

/**
 * BackendRoutingActor actor is responsible for forwarding messages to the backend.
 *
 */

public class BackendRoutingActor extends AbstractActor {
  final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  ActorRef backend = getContext().actorOf(FromConfig.getInstance().props(), "backend");


  @Override
  public Receive createReceive() {
    return receiveBuilder().match(ClusterManagement.class, message -> {
      backend.forward(message, getContext());
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

}