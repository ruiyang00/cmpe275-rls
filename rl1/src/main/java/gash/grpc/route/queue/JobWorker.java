package gash.grpc.route.queue;

import java.util.concurrent.LinkedBlockingDeque;

public abstract class JobWorker<T> extends Thread {
	private boolean forever = true;
	private boolean verbose = true;
	private int maxQueuedTasks = 1;
	private int tasksDone;
	private long lastTask;
	private String workerID = "JW";

	// each worker can have a set of tasks that it is assigned
	private LinkedBlockingDeque<T> queue = new LinkedBlockingDeque<T>();

	protected abstract void process(T task);

	public void addTask(T task) {
		this.queue.add(task);
	}

	public void stopWorker() {
		forever = false;
		queue.clear();

		try {
			this.notify();
		} catch (Exception e) {
			;
		}
	}

	public boolean isAcceptingTasks() {
		return (queue.size() <= maxQueuedTasks);
	}

	public boolean isBusy() {
		return (queue.size() > 0);
	}

	@Override
	public void run() {
		if (verbose)
			System.out.println("--> " + workerID + " ready to accept tasks");
		while (forever) {
			try {
				System.out.println("--> " + workerID + " waiting for work");
				var task = queue.take();
				tasksDone++;
				lastTask = System.currentTimeMillis();
				process(task);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	public String getWorkerID() {
		return workerID;
	}

	public void setWorkerID(String nameID) {
		this.workerID = nameID;
	}

	/**
	 * number of tasks completed
	 * 
	 * @return
	 */
	public int getTasksDone() {
		return tasksDone;
	}

	/**
	 * timestamp of last task processed
	 * 
	 * @return
	 */
	public long getLastTask() {
		return lastTask;
	}

	public int getMaxQueuedTasks() {
		return maxQueuedTasks;
	}

	/**
	 * number of allowed queued tasks in a worker
	 * 
	 * @param maxQueuedTasks must be greater than zero
	 */
	public void setMaxQueuedTasks(int maxQueuedTasks) {
		if (maxQueuedTasks > 0)
			this.maxQueuedTasks = maxQueuedTasks;
		else
			this.maxQueuedTasks = 1;
	}
}
