package gash.grpc.route.qos;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Timestamp;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import gash.grpc.route.queue.JobQueue;

/**
 * Quality of Service (QoS) implementation to monitor memory and performance
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
public class QoSperformance<T> implements QoS<T> {
	private int count;
	private int interval;
	private JobQueue queue;

	private MemoryMXBean memoryMXBean;
	private OperatingSystemMXBean systemMXBean;
	private ThreadMXBean threadMXBean;
	private FileWriter log;


	@SuppressWarnings("unused")
	private static final long sKB = 1024L;

	public QoSperformance(int interval) {
		this.count = 0;
		this.interval = interval;

		threadMXBean = ManagementFactory.getThreadMXBean();
		memoryMXBean = ManagementFactory.getMemoryMXBean();
		systemMXBean = ManagementFactory.getOperatingSystemMXBean();
		
		try {	
			log = new FileWriter("logs.txt");

		} catch (Exception e) {
			//TODO: handle exception
			System.out.println("An error occurred.");
      		e.printStackTrace();
		}
	}

	public void snapshot() {
		try {
			var tt = threadMXBean.getThreadCpuTime(Thread.currentThread().getId()) / 1_000_000_000.0;
			var la = systemMXBean.getSystemLoadAverage();
			var hm = (double) memoryMXBean.getHeapMemoryUsage().getUsed() / 1024.0; // kb
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());

			// snapshot can be called w/o queue set
			if (queue != null)
				log.write(count+ "    --> " +"queue size: " + queue.size() + " || thead computing time: " + tt
						+ " || system average load: " + la + "  || memory usage: " + hm + "kb\n");
			else
				log.write(count + ", " + "-1" + ", " + tt + ", " + la + ", " + hm);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		if (count % interval == 0)
			snapshot();

		return true;
	}

	@Override
	public void registerJobQueue(JobQueue q) {
		this.queue = q;
	}
}
