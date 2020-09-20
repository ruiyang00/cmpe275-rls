package gash.grpc.route.client;

import gash.grpc.route.queue.JobWorker;
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
public class ClientWorker<T> extends JobWorker<T> {
	private RouteServiceGrpc.RouteServiceBlockingStub stub;
	private boolean verbose = false;

	public ClientWorker(RouteServiceGrpc.RouteServiceBlockingStub stub) {
		this.stub = stub;
	}

	public void setVerbose(boolean yes) {
		this.verbose = yes;
	}

	protected void process(T task2) {
		var task = (route.Route) task2;

		// blocking!
		var r = stub.request(task);
		
		// TODO response handling
		@SuppressWarnings("unused")
		var payload = new String(r.getPayload().toByteArray());

		if (verbose)
			System.out.println("reply: " + task);
	}
}
