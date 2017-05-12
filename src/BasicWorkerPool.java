import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;


public class BasicWorkerPool {
	
	private final WorkerThread[] workerPool;
	private final LinkedBlockingQueue workQueue = new LinkedBlockingQueue();
	private boolean workersRunning = false;
	
	public BasicWorkerPool(int workers, ArrayList<ScreenHash> screenHashes, List<Video> videoHashes) {
		this.workerPool = new WorkerThread[workers];
		for(int i = 0; i < workers; i++) {
			this.workerPool[i] = new WorkerThread(videoHashes, screenHashes, this.workQueue);
		}
	}
	
	public void stopWorkers() {
		for(int i = 0; i  < this.workerPool.length; i++) {
			this.workerPool[i].stopWorker();
		}
	}
	
	public boolean workersStopped() {
		for(int i = 0; i < this.workerPool.length; i++) {
			if(!this.workerPool[i].isCompleted())
				return false;
		}
		return true;
	}

}
