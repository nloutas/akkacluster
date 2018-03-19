# akkacluster
demo java app implementing Akka cluster sharding

## installation 
- compile and generate the tarballs

`mvn clean install`

- extract the  tarballs in suitable directories 

`mkdir -p ~/backend; cd ~/backend; tar xzvf $PATH_TO_REPO/akkacluster/backend/target/backend/target/akkacluster-$APP_VERSION.tar.gz`
`mkdir -p ~/frontend; cd ~/frontend; tar xzvf $PATH_TO_REPO/akkacluster/frontend/target/frontend/target/akkacluster-$APP_VERSION.tar.gz`


## Startup 
- start 2 backend nodes and 1 frontend node as separate JVM processes

* First backend to run on default port 2551 (this is also the cluster seed, so it must run first)
`cd ~/backend; java -cp "config:lib/*" demo.akka.backend.BackendApp`

* Second backend to run on port 2552
`java -cp "config:lib/*" -Dakka.remote.netty.tcp.port=2552 demo.akka.backend.BackendApp `

* Frontend to run on default port 2550 
`cd ~/frontend; java -cp "config:lib/*" demo.akka.frontend.FrontendApp`
