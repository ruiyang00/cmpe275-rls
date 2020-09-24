package gash.grpc.route.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.google.protobuf.ByteString;
import org.apache.log4j.BasicConfigurator;

import gash.grpc.route.qos.QoSperformance;
import gash.grpc.route.queue.JobQueue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import route.Route;
import route.RouteServiceGrpc;

/**
 * copyright 2018, gash
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
 */

public class RouteClient {
	// TODO this is a bad idea to hard code configuration values
	private static long clientID = 501;
	private static int port = 2345;

	private static byte[] generateData() {
		var fn = new File("./lib/objenesis-1.0.jar");
		var blen = (int) fn.length();
		byte[] raw = new byte[blen];
		FileInputStream fis = null;
		try {
			// 28k file
			fis = new FileInputStream(fn);
			fis.read(raw, 0, blen);
		} catch (Exception ex) {
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		return raw;
	}

	public static void main(String[] args) {
		ManagedChannel ch = ManagedChannelBuilder.forAddress("localhost", RouteClient.port).usePlaintext().build();

		RouteServiceGrpc.RouteServiceBlockingStub stub = RouteServiceGrpc.newBlockingStub(ch);

		// TODO How to govern how much memory the client will use to store
		// tasks?
		
		var queue = new JobQueue<route.Route>();
		queue.setVerbose(false);

		var qos = new QoSperformance<route.Route>(10);
		queue.setQoS(qos);
		queue.start();

		System.out.println("--> starting worker");
		var w = new ClientWorker<route.Route>(stub);
		w.setMaxQueuedTasks(1);
		// w.setVerbose(true);
		w.setWorkerID("C1");
		queue.registerWorker(w);
		w.start();

		// where we are before starting
		// Runtime.getRuntime().gc();
		qos.snapshot();
		System.out.flush();

		int I = 20;
		for (int i = 0; i < I; i++) {
			// build a sizable payload
			byte[] raw = RouteClient.generateData();

			Route.Builder bld = Route.newBuilder();
			bld.setId(i);
			bld.setOrigin(RouteClient.clientID);
			bld.setPath("/to/somewhere");
			bld.setPayload(ByteString.copyFrom(raw));
			bld.setDestination(RouteClient.clientID);

			// l.append(task)
			queue.enqueue(bld.build());
		}

		System.out.println("-- done creating requests --");
		System.out.flush();

		// why is this bad?
		// ch.shutdown();
		//Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately cancelled.
	}
}
