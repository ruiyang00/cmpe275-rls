package gash.grpc.route.qos;

import gash.grpc.route.queue.JobQueue;

public interface QoS<T> {

	/**
	 * Provide processing evaluation for a ServerQueue.
	 * 
	 * @param task
	 * @return
	 */
	boolean evaluate(T task);

	/**
	 * registration of a job queue allows the QoS to make decisions based on
	 * awaiting tasks.
	 * 
	 * @param q
	 */
	void registerJobQueue(JobQueue q);
}