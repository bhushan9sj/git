/**
 *  TEAM E
 */

import java.io.*;
import java.net.*;
//the main class that creates a SimpleJobManager object,
// opens one ServerSocket to accept incoming connections from clients,
//creates one thread for each client connection, and waits until all jobs are finished.
public class SimpleJobServer {
    public static void main(String args[]) {
        // Obtain port number
        int portNumber = 9090;
        if (args.length >= 1)
            portNumber = Integer.parseInt(args[0]);
//SimpleJobManager-this class maintains 
//and updates the video encoding job table.
        // Declaration
        SimpleJobManager jobManager = null;
        SimpleJobServerThread jobThread = null;
        ServerSocket serverSocket = null;

// Create JobManager
        jobManager = new SimpleJobManager();
        jobManager.display();

 // Open ServerSocket
        try {
            serverSocket = new ServerSocket(portNumber);
            serverSocket.setSoTimeout(1000);
        } catch (IOException e) {
            System.err.println(e);
        }
// Handle client's connection
        try {
            boolean listening = true;
            while (listening) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    jobThread = new SimpleJobServerThread(clientSocket, jobManager);
                    jobThread.start();
                } catch (SocketTimeoutException e) {
                    if (jobManager.done())
                        listening = false;
                }
            }
            serverSocket.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}

final class SimpleJobServerThread extends Thread {
    private Socket socket;
    private SimpleJobManager manager;

    public SimpleJobServerThread(Socket socket, SimpleJobManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    public void run() {
        try {
            process();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void process() throws Exception {
        // Display connection information
        String clientInfo = socket.getInetAddress() + ":" + socket.getPort();
        String serverInfo = socket.getLocalAddress() + ":" + socket.getLocalPort();
        System.out.println("Server " + serverInfo + " received connection from " + clientInfo);

        // Open input and output streams
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Main process loop
        double acceptMargin = 0.3;
        boolean clientDone = false;
        while (!clientDone) {
            String inputLine;
            String outputLine=null;
            // Display job summary
            manager.display();
            // Read client's request
            inputLine = in.readLine();
            System.out.println("server <- " + clientInfo + "  " + inputLine);
            // Process client's request
            String token[] = inputLine.split(" ");
            // Task3: add proper response from server
           switch(token[0]){
           	case "requestJob": 
           		
           		int jobId=manager.assignJob();
           		if(jobId!=-1){
           			outputLine="assignJob"+" "+jobId+" "+manager.getJobEntry(jobId);
           			System.out.println("server -> " + clientInfo +" "+ outputLine);
           			out.println(outputLine);
           		}else{
           			outputLine="No Jobs Available for Now";
           			System.out.println("server -> " + clientInfo +" "+ outputLine);
           			out.println(outputLine);
           		}
           		break;
           	
           	case "submitJob":if(manager.rejectJob(Integer.parseInt(token[1]),Double.parseDouble(token[2]))==true){
           				outputLine="rejectJob "+token[1];
           				System.out.println("server -> " + clientInfo +" "+ outputLine);
               			out.println(outputLine);
           		}else{
           			outputLine="acceptJob "+token[1];
       				System.out.println("server -> " + clientInfo +" "+ outputLine);
           			out.println(outputLine);
           		}
           		break;
           
           }
           
        }
 // Close streams and sockets
        out.close();
        in.close();
        socket.close();
    }
}

final class SimpleJobManager {
    public enum JobState {
        JS_READY, JS_ASSIGNED, JS_FINISHED
    }

    private int totalJobs;
    private int assignedJobs;
    private int finishedJobs;
    private String[] jobEntry;
    private JobState[] jobState;

    public SimpleJobManager() {
        totalJobs = 10;
        assignedJobs = 0;
        finishedJobs = 0;
        jobEntry = new String[totalJobs];
        jobState = new JobState[totalJobs];
        jobEntry[0] = "ch01.yuv  ch01.265  01:00:00:00 240 21.1";
        jobEntry[1] = "ch02.yuv  ch02.265  01:00:10:00 360 26.2";
        jobEntry[2] = "ch03.yuv  ch03.265  01:00:25:00 240 24.3";
        jobEntry[3] = "ch04.yuv  ch04.265  01:00:35:00 480 25.9";
        jobEntry[4] = "ch05a.yuv ch05a.265 01:00:55:00 600 25.0";
        jobEntry[5] = "ch05b.yuv ch05b.265 01:01:20:00 360 25.7";
        jobEntry[6] = "ch06.yuv  ch06.265  01:01:35:00 720 22.3";
        jobEntry[7] = "ch07a.yuv ch07a.265 01:02:05:00 360 24.8";
        jobEntry[8] = "ch07b.yuv ch07b.265 01:02:20:00 480 27.4";
        jobEntry[9] = "ch08.yuv  ch08.265  01:02:40:00 360 20.7";
        for (int i = 0; i < totalJobs; i++) {
            jobState[i] = JobState.JS_READY;
        }
    }

    public int assignJob() {
        // Task4: codes for assigning jobs
    	 for(int i=0;i<jobState.length;i++){
    		 if(jobState[i].equals(JobState.JS_READY)){
    			 jobState[i]=JobState.JS_ASSIGNED;
    			 assignedJobs++;
    			 return i;
    		 }
    	 }
    	 return -1;
       
    }

    public boolean rejectJob(int jobIndex, double result) {
        // Task5: codes for rejecting jobs
    	String token[]=jobEntry[jobIndex].split(" ");
    	double required=Double.parseDouble(token[token.length-1]);
    	if((required-result)<=1.0&&(required-result)>=-1.0){
    		finishJob(jobIndex);
    		return false;
    	}else{
    		jobState[jobIndex]=JobState.JS_READY;
    		assignedJobs--;
    		return true;
    	}
    }

    public boolean finishJob(int jobIndex) {
        // Task6:  codes for finished jobs
    	jobState[jobIndex]=JobState.JS_FINISHED;
    	finishedJobs++;
    	return true;
    }

    public String getJobEntry(int jobIndex) {
        if (jobIndex < totalJobs) {
            return jobEntry[jobIndex];
        }
        return null;
    }

    public int total() {
        return totalJobs;
    }

    public int remaining() {
        return Math.max(0, totalJobs - assignedJobs);
    }

    public int finished() {
        return finishedJobs;
    }

    public boolean done() {
        return finishedJobs >= totalJobs;
    }

    public void display() {
        System.out.println("JobSummary   total = " + total() + "  remaining = " + remaining() + "  finished = "
                + finished());
    }
}
