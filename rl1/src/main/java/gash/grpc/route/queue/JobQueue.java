package gash.grpc.route.queue;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

import gash.grpc.route.qos.QoS;
import gash.grpc.route.qos.QoSnoop;

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
public class JobQueue<T> extends Thread {
	private boolean forever = true;
	private QoS<T> qos = new QoSnoop<T>();
	private boolean verbose = false;
	private JobWorker<T> lastWorker = null;

	// jobs (tasks)
	private LinkedBlockingDeque<T> queue;

	private ArrayList<JobWorker<T>> workers = new ArrayList<JobWorker<T>>();

	private static final int sFullDelay = 100;

	public JobQueue() {
		queue = new LinkedBlockingDeque<T>();
	}

	public void setVerbose(boolean yes) {
		this.verbose = yes;
	}

	public void registerWorker(JobWorker<T> w) {
		workers.add(w);
	}

	/**
	 * provide processing implementation
	 * 
	 * @param task
	 */
	private boolean process(T task) {
		if (workers.size() > 1) {
			// determine which thread to give the work to
			for (JobWorker<T> w : workers) {
				// TODO How to implement a round-robin scheduler?
				if ((lastWorker == null || !w.getWorkerID().equals(lastWorker.getWorkerID())) && w.isAcceptingTasks()) {
					w.addTask(task);
					lastWorker = w;
					return true;
				}
			}
		} else {
			workers.get(0).addTask(task);
			return true;
		}

		// unable to find a thread to do the work
		lastWorker = null;
		System.out.println("--> JQ no workers available");
		queue.push(task);

		// TODO a boolean return may not be descriptive
		return false;
	}

	public void enqueue(T task) {
		queue.add(task);
	}

	private void clearQueue() {
		queue.clear();
	}

	public void setQoS(QoS<T> qos) {
		this.qos = qos;
	}

	/**
	 * is the worker busy with tasks. This is different than accepting tasks.
	 * 
	 * @return
	 */
	public boolean isBusy() {
		return queue.size() > 0;
	}

	public int size() {
		return queue.size();
	}

	public void shutdown(boolean hard) {
		if (hard)
			clearQueue();

		// TODO if not hard allow the workers to finish

		// while not technically correct since this thread does not own the
		// worker threads, it works
		for (JobWorker<T> w : workers) {
			try {
				w.stopWorker();
			} catch (Exception e) {
				; // ignore
			}
		}

		forever = false;
	}

	/**
	 * monitor the job queue to deligate jobs to workers
	 */
	@Override
	public void run() {
		if (verbose)
			System.out.println("--> JQ ready to receive tasks");

		while (true) {
			if (!forever && queue.size() == 0)
				break;

			try {
				// this QoS is overly simplistic. What can (will) go wrong? How
				// would you recode the QoS to queue relationship?
				T task = queue.take();
				if (qos == null || qos.evaluate(task)) {
					if (!process(task)) {
						// need to delay retries as this will spin
						try {
							Thread.sleep(sFullDelay);
						} catch (InterruptedException e) {
						}
					}
				} else
					queue.push(task);
			} catch (InterruptedException e) {
				// TODO report error
			}
		}
	}
}
