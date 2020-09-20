package gash.grpc.route.server;

import io.grpc.stub.StreamObserver;

public class ServerTask {
	private int tries = 0;
	route.Route request;
	StreamObserver<route.Route> responseObserver;

	public ServerTask(route.Route request, StreamObserver<route.Route> responseObserver) {
		this.request = request;
		this.responseObserver = responseObserver;
	}

	public void incAttempt() {
		tries++;
	}

	public int getAttempts() {
		return tries;
	}
}