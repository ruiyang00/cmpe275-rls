package gash.grpc.route.qos;

import gash.grpc.route.queue.JobQueue;

/**
 * Quality of Service (QoS) implementation using a simple time delay for every n
 * (interval) tasks.
 * 
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
 */
public class QoSdelay<T> implements QoS<T> {
	private int count;
	private int interval = 100;
	private long timeDelay = 100;
	private JobQueue queue;

	public QoSdelay(int interval, long timeDelay) {
		this.count = 0;
		this.interval = interval;
		this.timeDelay = timeDelay;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gash.grpc.route.queue.QoS#evaluate(route.Route)
	 */
	@Override
	public boolean evaluate(T task) {
		count++;
		if (count == Long.MAX_VALUE - 1)
			count = 0;

		if (count % interval == 0) {
			try {
				System.out.println("--> QoS pausing");
				Thread.sleep(timeDelay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return true;
	}

	@Override
	public void registerJobQueue(JobQueue q) {
		this.queue = q;
	}
}
