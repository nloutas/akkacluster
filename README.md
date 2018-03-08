# akkacluster
sample java app implementing Akka cluster sharding

## installation 
- compile and generate the tarball

`mvn clean install`

- make a suitable directory and extract the  tarball 

`mkdir -p ~/akkacluster; cd ~/akkacluster; tar xzvf $PATH_TO_REPO/akkacluster/target/akkacluster-$APP_VERSION.tar.gz`


## Startup 
- executing **ClusterApp** will start 2 backend nodes and 1 frontend node in a single jvm

`java  -cp "config:lib/*" com.emnify.cluster.ClusterApp`
