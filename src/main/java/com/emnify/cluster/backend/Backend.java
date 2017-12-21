package com.emnify.cluster.backend;

import static com.emnify.cluster.messages.TransformationMessages.BACKEND_REGISTRATION;

import com.emnify.cluster.messages.TransformationMessages.TransformationJob;
import com.emnify.cluster.messages.TransformationMessages.TransformationResult;

import akka.actor.AbstractActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberUp;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.cluster.Member;
import akka.cluster.MemberStatus;


public class Backend extends AbstractActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  Cluster cluster = Cluster.get(getContext().system());

  //subscribe to cluster changes, MemberUp
  @Override
  public void preStart() {
    cluster.subscribe(getSelf(), MemberUp.class);
  }

  //re-subscribe when restart
  @Override
  public void postStop() {
    cluster.unsubscribe(getSelf());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(TransformationJob.class, message -> {
      getSender().tell(new TransformationResult(message.getText().toUpperCase()), getSelf());
    }).match(CurrentClusterState.class, message -> {
      for (Member member : message.getMembers()) {
        if (member.status().equals(MemberStatus.up())) {
          register(member);
        }
      }
    }).match(MemberUp.class, message -> {
      register(message.member());
    }).matchAny(o -> log.warning("received unknown message: {}", o)).build();
  }

  void register(Member member) {
    if (member.hasRole("frontend")) {
      getContext().actorSelection(member.address() + "/user/frontend").tell(
          BACKEND_REGISTRATION, getSelf());
    }
  }

}

