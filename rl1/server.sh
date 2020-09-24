#!/bin/bash
mvn clean compile
mvn exec:java -Dexec.mainClass="gash.grpc.route.server.RouteServerImpl" -Dexec.args="conf/server.conf"
