package demo.akka;

import com.typesafe.config.ConfigFactory;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.Before;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public abstract class ITCaseBase {
  // protected final static Long timefactor = Tools.timefactor;
  protected static ActorSystem system;
  protected static ActorRef backendSup; 
  protected static ActorRef endpointSupervisor;
  protected static ActorRef profileSup;

  protected String systemConfigName = "application-test.conf";

  protected static final FiniteDuration TIMEOUT = FiniteDuration.create(5L, TimeUnit.SECONDS);
  private static final Duration SHUTDOWN_TIMEOUT = Duration.apply(15, TimeUnit.SECONDS);
  static {
    TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
  }

  @Before
  public void setup() throws Exception {
    if (system == null) {
      system = ActorSystem.create("ClusterSystem",
          ConfigFactory.parseResources(systemConfigName).resolve());
      setupActors();
    }

  }

  @AfterClass
  public static void shutdownSystem() {
    if (system != null) {
      TestKit.shutdownActorSystem(system, SHUTDOWN_TIMEOUT);
      system = null;
      backendSup = null;
      endpointSupervisor = null;
      profileSup = null;
    }
  }

  protected void restartActorSystem(String configFileName) throws Exception {
    shutdownSystem();
    systemConfigName = configFileName;
    setup();
  }

  protected abstract void setupActors();

}
