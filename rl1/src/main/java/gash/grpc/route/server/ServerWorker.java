package gash.grpc.route.server;

import com.google.protobuf.ByteString;

import gash.grpc.route.queue.JobWorker;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import route.Route;
import route.RouteServiceGrpc;

/**
 * copyright 2019, gash
 *
 * Gash licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @author gash
 *
 */
public class ServerWorker<T> extends JobWorker<T> {
	private boolean verbose = false;
	private static int port = 2345;


	public void setVerbose(boolean yes) {

		this.verbose = yes;
	}

	protected void process(T task2) {
		ServerTask task = (ServerTask) task2;

		if (verbose)
			System.out.println("--> " + this.getWorkerID() + " got: " + task.request.getOrigin() + ", path: "
					+ task.request.getPath());

		var builder = route.Route.newBuilder();

		// routing/header information
		builder.setId(RouteServer.getInstance().getNextMessageID());
		builder.setOrigin(RouteServer.getInstance().getServerID());
		builder.setDestination(task.request.getOrigin());
		builder.setPath(task.request.getPath());

		// -------------------------------------------------------------------
		// TODO a placeholder for doing work
		ManagedChannel ch = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build();
		

		

		

		final String blank = "blank";
		var workResults = ByteString.copyFrom(blank.getBytes());
		builder.setPayload(workResults);

		// questions: How does a work express retries or failures?

		// -------------------------------------------------------------------

		var rtn = builder.build();
		task.responseObserver.onNext(rtn);
		task.responseObserver.onCompleted();
	}
}
