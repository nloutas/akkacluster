package com.emnify.cluster;

import com.emnify.cluster.backend.BackendMain;
import com.emnify.cluster.frontend.FrontendMain;

public class ClusterApp {

  public static void main(String[] args) {
    // start multiple frontend and backend nodes
    for (int i = 0; i < 5; i++) {
      BackendMain.main(new String[] {"255" + i});
    }
    FrontendMain.main(new String[] {"2500"});
  }
}
