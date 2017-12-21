package com.emnify.cluster.frontend;

import static com.emnify.cluster.messages.TransformationMessages.BACKEND_REGISTRATION;

import com.emnify.cluster.messages.TransformationMessages.JobFailed;
import com.emnify.cluster.messages.TransformationMessages.TransformationJob;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.ArrayList;
import java.util.List;

// frontend
public class Frontend extends AbstractActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  List<ActorRef> backends = new ArrayList<ActorRef>();
  int jobCounter = 0;

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(TransformationJob.class, message -> {
      if (backends.isEmpty()) { 
        getSender().tell(new JobFailed("Service unavailable, try again later", message),
            getSender());
      } else {
        jobCounter++;
        backends.get(jobCounter % backends.size()).forward(message, getContext());
      }
    }).match(String.class, message -> {
      if (message.equals(BACKEND_REGISTRATION)) {
        getContext().watch(getSender());
        backends.add(getSender());
      }
    }).match(Terminated.class, message -> {
      backends.remove(message.getActor());
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

}

