import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class BasicWorkerPool {
	
	private final WorkerThread[] workerPool;
	private final LinkedBlockingQueue<Video> workQueue = new LinkedBlockingQueue<Video>();
	private boolean workersRunning = false;
	
	public BasicWorkerPool(int workers, ArrayList<ScreenHash> screenHashes) {
		this.workerPool = new WorkerThread[workers];
		for(int i = 0; i < workers; i++) {
			this.workerPool[i] = new WorkerThread(screenHashes, this.workQueue);
		}
	}
	
	//Adds a new video to the work queue
	//Returns number of elements currently in queue
	public int addVideo(Video video) {
		workQueue.add(video);
		
		return workQueue.size(); 
	}
	
	public void stopWorkers() {
		for(int i = 0; i  < this.workerPool.length; i++) {
			this.workerPool[i].stopWorker();
		}
	}
	
	public void startWorkers() {
		for(int i = 0; i  < this.workerPool.length; i++) {
			this.workerPool[i].start();
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
