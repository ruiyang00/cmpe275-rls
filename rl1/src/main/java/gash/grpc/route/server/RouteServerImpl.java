package gash.grpc.route.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


import gash.grpc.route.qos.QoSperformance;
import gash.grpc.route.queue.JobQueue;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import route.RouteServiceGrpc.RouteServiceImplBase;


public class RouteServerImpl extends RouteServiceImplBase {
	private static final int Q_THRESHOLD = 200;
	private static final int Q_FLOOR = 50;

	private Server svr;
	private boolean verbose = false;
	private JobQueue<ServerTask> queue;

	private static Properties getConfiguration(final File path) throws IOException {
		if (!path.exists())
			throw new IOException("missing file");

		var rtn = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
			rtn.load(fis);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

		return rtn;
	}

	public RouteServerImpl() {
		System.out.println("--> new RouteServerImpl instance");
	}

	private void start() throws Exception {

		/*
		 * server queue + worker - the Impl will not directly respond to requests. The
		 * queue (worker) will process requests according to the QoS instance.
		 * 
		 */
		queue = new JobQueue<ServerTask>();
		
		//enable the queue output queue's detials, cpu load, computing time, meomry usage when verbose set to true
		verbose = true;
		queue.setVerbose(verbose);

		var qos = new QoSperformance<ServerTask>(10);
		qos.registerJobQueue(queue);
		queue.setQoS(qos);
		queue.start();

		// TODO How to expand on this concept?
		for (int i = 0; i < 1; i++) {
			ServerWorker<ServerTask> w = new ServerWorker<ServerTask>();
			w.setWorkerID("S" + i);
			// w.setVerbose(true);
			System.out.println("--> starting worker " + w.getWorkerID());
			queue.registerWorker(w);
			w.start();
		}

		System.out.println("--> starting server");
		svr = ServerBuilder.forPort(RouteServer.getInstance().getServerPort()).addService(this).build();
		svr.start();

		System.out.flush();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				RouteServerImpl.this.stop();
			}
		});
	}

	protected void stop() {
		// TODO how to gracefully quit?
		queue.shutdown(true);

		svr.shutdown();
	}

	private void blockUntilShutdown() throws Exception {
		svr.awaitTermination();
	}

	@Override
	public void request(route.Route request, StreamObserver<route.Route> responseObserver) {
		if (queue == null)
			throw new RuntimeException("worker not initalized");

		if (queue.size() > Q_THRESHOLD) {
			// TODO increase the number of workers to max allowed
		} else if (queue.size() < Q_FLOOR) {
			// TODO reduce the number of threads to default minimum
		}

		queue.enqueue(new ServerTask(request, responseObserver));
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO check args!


		var path = args[0];
		try {
			var conf = RouteServerImpl.getConfiguration(new File(path));
			RouteServer.configure(conf);

			final RouteServerImpl impl = new RouteServerImpl();
			impl.start();
			impl.blockUntilShutdown();
			
			System.exit(0);

		} catch (IOException e) {
			// TODO better error message
			e.printStackTrace();
		}
	}

}
