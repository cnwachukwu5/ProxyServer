
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;

public class Threads implements Runnable {
	
	private final Socket client;
	private String statusCode;
	private long requestTime;
	private long responseTime;
	private long duration;
	private String remoteClient;
	private String cacheStatus;
	private String status;
	private String requestLine;
	private String validated;
	private String host;
	
	
	public Threads(Socket client){
		this.client = client;
	}
	
	public void run(){
		Socket server = null;
		HttpRequest request = null;;
		HttpResponse response = null; 
		
		//Create client streams
		try{
			DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
			BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			remoteClient = getRemoteClient(client);
			//get client request
			//synchronized(this){
			
				request = new HttpRequest(fromClient);
				requestLine = request.URI;
				
			//}
			if(request.method != null && !request.method.isEmpty()){
				
			if(request.method.equals("GET")){ //Handling only GET Method
				//Sending request to server
				//Open socket and write request to web-server socket
				//First confirm that the response is not in the proxy cache
				
				System.out.println("");
				System.out.println("URI is: " + request.URI);
				System.out.println("Host to contact is: " + request.getHost() + " at port " + request.getPort());
				System.out.println(request.getHost());
				System.out.println("");
				
				//For uc.edu, host variable contains www.uc.eduhttp. remove that and assign www.uc.edu
				
				
				byte[] cache = ProxyCache.uncaching(request.URI);
				if(cache.length == 0){
					//Get cache Miss
					cacheStatus = "Cache miss";
					
					//send request to server
					server = new Socket(request.getHost(), request.getPort());
					DataOutputStream toServer = new DataOutputStream(server.getOutputStream());	
					//Get time of request
					requestTime = System.currentTimeMillis();
					toServer.writeBytes(request.toString());
					
					//Get server response and send to client
					DataInputStream fromServer = new DataInputStream(server.getInputStream());
					response = new HttpResponse(fromServer);//Create response object
					status = processStatusLine(response.statusLine);			
										
					ProxyCache.caching(request, response); //Cache the server response.
					
					//Get the time of response
					responseTime = System.currentTimeMillis();
					duration = responseTime - requestTime;
					proxyLog();	getRequestCount();
					getCacheMissCount();
							
					//Send response to client
					toClient.writeBytes(response.toString());//Response headers
					toClient.write(response.body);//Response body
					toClient.flush();
					
				}else{
					//Get cache Hit
					cacheStatus = "Cache hit";
					
					//send request to server
					
					server = new Socket(request.getHost(), request.getPort());
					DataOutputStream toServer = new DataOutputStream(server.getOutputStream());	
					//Get time of request
					requestTime = System.currentTimeMillis(); 
					toServer.writeBytes(request.toString());
					
					//Get server response and send to client
					DataInputStream fromServer = new DataInputStream(server.getInputStream());
					response = new HttpResponse(fromServer);//Create response object
					status = processStatusLine(response.statusLine);
					
					if(response.statusLine.contains("HTTP/1.1 304 Not Modified")){
						//get the URI of validated cachedObject
						validated = "Validated";
						ProxyCache.caching(request, response); //Update Cache the server response.
						cache = ProxyCache.uncaching(request.URI);
					}
					
					responseTime = System.currentTimeMillis();
					duration = responseTime - requestTime;
					
					proxyLog(); getRequestCount();
					getCacheHitCount();
					toClient = new DataOutputStream(client.getOutputStream());
					toClient.write(cache);
					toClient.flush();
					
					//Get the time of response
					
					//Cache Hit
					//toClient.flush();
				}
			}//End of if statement for GET
		}
			
		}//End try.
		catch(UnknownHostException e){
			System.out.println("Unknown host: "+ request.getHost());
			System.out.println(e);
			return;
		}
		catch(IOException e){
			return;
		}
	}//End of run method
	
	public synchronized String processStatusLine(String statusLine){
		String statusLineHeader[] = statusLine.split(" ");
		statusCode = statusLineHeader[1];
		return statusCode;
	}
	
	public String getRemoteClient(Socket client){
		String remoteAddress = client.getRemoteSocketAddress().toString();
		remoteAddress = remoteAddress.substring(1, 13);
		return remoteAddress;
	}
	
	public synchronized void proxyLog(){
		//Create proxylog.log file if not exists
		File logDir = new File("logs/proxylog.txt");
		String messageLRU = "";
		String messageFIFO = "";
		String removedFiles = "";
		String reqTime = "";
		String period = "";
		
		
		if(!logDir.exists()){
			try{
				logDir.getParentFile().mkdir();
				logDir.createNewFile();
			}catch(IOException e){
				System.err.println("Cannot create file" + e);
			}
			
		}
		period = getDuration(duration);
		reqTime = dateFormatter (requestTime);
		
		//Get the values to be logged to the file.
		ArrayList<String> removedObjectList;
		removedObjectList = ProxyCache.getRemovedObjects();
		if(removedObjectList != null && !removedObjectList.isEmpty()){
			for(int i = 0; i <= removedObjectList.size() - 1; i++ ){
				removedFiles += removedObjectList.get(i) + " ";
			}
			removedObjectList.clear();
			messageLRU = "Evicted by LRU";
		}
		
		String removedObject = ProxyCache.removedObject();
		
		if(removedObject != null && !removedObject.isEmpty()){
			messageFIFO = "Evicted by FIFO";
		}
		
		//Log the info to the log file.
		String logInfo = "[" + reqTime + "] " + period + " " + cacheStatus + " ";
		if(removedFiles != null && !removedFiles.isEmpty()){
			logInfo += removedFiles  +  " " +  messageLRU + " ";
		}
		
		if(removedObject != null && !removedObject.isEmpty()){
			logInfo += removedObject  + " " + messageFIFO + " ";
		}
		
		if(validated != null && !validated.isEmpty()){
			logInfo += validated + " " ;
		}
		
		logInfo +=  remoteClient + " " + requestLine + " " + status + "\r\n" ;
		
		BufferedWriter bw = null;
		 FileWriter fw = null;
		 
		 try{	
			 	fw = new FileWriter(logDir.getAbsoluteFile(), true);
			 	bw = new BufferedWriter(fw);
			 	bw.write(logInfo);
			 	bw.close();
				fw.close();
		 }catch(IOException e){
			 System.err.println("Cannot open file for writing" + e);
		 }
	}
	
	public static String dateFormatter (long timeAsLong){
		Date date = new Date(timeAsLong);
		DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
		String dateFormatted = formatter.format(date);
		
		return dateFormatted;
			
	}
	
	public static String getDuration(long duration){
		long second = (duration/1000) % 60;
		long minute = (duration/(1000*60)) % 60;
		long hour = (duration/(1000*60*60)) % 24;
		String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, duration);
		return time;
	}
	
	public synchronized void getRequestCount() throws IOException{
		File requestCount = new File("logs/", "requestCount.txt");
		
		if(!requestCount.exists()){
			requestCount.createNewFile();
		}
		
		BufferedWriter bw = null;
		 FileWriter fw = null;
		 
		 try{	
			 	fw = new FileWriter(requestCount.getAbsoluteFile(), true);
			 	bw = new BufferedWriter(fw);
			 	bw.write("Requested \r\n");
			 	bw.close();
				fw.close();
		 }catch(IOException e){
			 System.err.println("Cannot open file for writing" + e);
		 }
	}
	
	public synchronized void getCacheMissCount() throws IOException{
		File cacheMissCount = new File("logs/", "cacheMissCount.txt");
		
		if(!cacheMissCount.exists()){
			cacheMissCount.createNewFile();
		}
		
		BufferedWriter bw = null;
		FileWriter fw = null;
		
		try{	
		 	fw = new FileWriter(cacheMissCount.getAbsoluteFile(), true);
		 	bw = new BufferedWriter(fw);
		 	bw.write("CacheMiss \r\n");
		 	bw.close();
			fw.close();
	 }catch(IOException e){
		 System.err.println("Cannot open file for writing" + e);
	 }
		
}
	public String getLocationHeader(String statusCode, HttpResponse response){
		String location = "";
		if(statusCode != null && !statusCode.isEmpty()){
			if(status.contains("302") || status.contains("301")){
				String responseHeaders[] = response.headers.split("\n");
				ArrayList<String> responseHeaderList = new ArrayList<String>(Arrays.asList(responseHeaders));
				for(int i = 0; i < responseHeaderList.size(); i++){
					if(responseHeaderList.get(i).contains("Location")){
						location = responseHeaderList.get(i);
					}
				}
			}
		}
		return location;
	}
	public synchronized void getCacheHitCount() throws IOException{
		File cacheHitCount = new File("logs/", "cacheHitCount.txt");
		
		if(!cacheHitCount.exists()){
			cacheHitCount.createNewFile();
		}
		
		BufferedWriter bw = null;
		FileWriter fw = null;
		
		try{	
		 	fw = new FileWriter(cacheHitCount.getAbsoluteFile(), true);
		 	bw = new BufferedWriter(fw);
		 	bw.write("CacheHit \r\n");
		 	bw.close();
			fw.close();
	 }catch(IOException e){
		 System.err.println("Cannot open file for writing" + e);
	 }
}

}//End of class
