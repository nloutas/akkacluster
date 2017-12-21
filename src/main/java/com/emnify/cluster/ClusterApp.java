package com.emnify.cluster;

import com.emnify.cluster.backend.BackendMain;
import com.emnify.cluster.frontend.FrontendMain;

public class ClusterApp {

  public static void main(String[] args) {
    // start multiple frontend and backend nodes
    BackendMain.main(new String[] {"2551"});
    BackendMain.main(new String[] {"2552"});
    // BackendMain.main(new String[0]);
    FrontendMain.main(new String[] {"2500"});
  }
}
