import java.util.*;
import java.io.File;


public class ThreadPool extends ThreadGroup{
	boolean isThreadPoolStarted = false;
	boolean ifShutDown;
	int sizeOfPool;
	List<Task> requestList = new LinkedList<Task>();
	File rootDirectory;
	int port;
	
	//constructor
	public ThreadPool(String poolName,int sizeOfPool){
		super(poolName);
		this.sizeOfPool = sizeOfPool;
		setDaemon(true);
	}
	
	//start threadPool, generate and active workerThreads
	public synchronized void startThreadPool(){
		if (isThreadPoolStarted || sizeOfPool == 0){
			try{
				throw new Exception();
			}catch(Exception e){
				e.printStackTrace();
			}
			return;
		}
		if (requestList == null){
			try{
				throw new Exception();
			}catch(Exception e){
				e.printStackTrace();
			}
			return;
		}
		for (int i=0; i < sizeOfPool; i++){
			new workerThread(i).start(); 
		}
		isThreadPoolStarted = true;
	}
	
	//Stop thread pool
	public synchronized void stopThreadPool(){
		if (!isThreadPoolStarted || sizeOfPool == 0){
			try{
				throw new Exception();
			}catch(Exception e){
				System.out.println("The pool is not started yet or already be stopped");
			}
			return;
		}
		if (requestList == null){
			try{
				throw new Exception();
			}catch(Exception e){
				System.out.println("No reqeust in waiting list");
			}
			return;
		}
		requestList.clear();
		isThreadPoolStarted = false;
		sizeOfPool = 0;
		interrupt();
	}
	
	//adding request to requestList
	public synchronized void addRequest(Task newRequest){
		if (requestList == null){
			try{
				throw new Exception();
			}catch(Exception e){
				e.printStackTrace();
			}
			return;
		}
		//give the requestList a limit size of 50 as the blocking queue does
		while(requestList.size()==50){
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		requestList.add(newRequest);
		notify();
	}
	
	//get request from requestList
	public synchronized Task getRequest(){
		if (requestList == null){
			try{
				throw new Exception();
			}catch(Exception e){
			}
			return null;
		}
		
		while(requestList.size() == 0){
			try{
				wait();
			}catch(InterruptedException e){

			}
		}
		
		//while the requestList is full, workerThread take a request and notify the dispatcher thread to continue to receive and add request
		if(requestList.size()==50){
			notifyAll();
		}
		return requestList.remove(0);
	}
	
	//subclass:workerThread
	public class workerThread extends Thread{
		public workerThread(int threadID){
			super(ThreadPool.this,""+threadID);
		}
		
		
		public void run(){
			while(!interrupted()){
				Task task = getRequest();
				if(task==null){
					break;
				}
				task.run();
			}
			return;
		}
	}
}

