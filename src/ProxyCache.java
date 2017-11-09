
import java.net.*;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;
import java.io.*;

public class ProxyCache {
	
	private static int port;//Port for the proxy server
	private static ServerSocket socket;//Socket for client connection
	private static WebCache<String, String> cache;//The cache object
	private static String cacheAlgorithm;
	
	public synchronized static void caching(HttpRequest request, HttpResponse response) throws IOException{
		
		File file;
		String hashFile;
		DataOutputStream output;
		
		if((hashFile = cache.get(request.URI)) != null){
			file = new File(hashFile);
			output = new DataOutputStream(new FileOutputStream(file, false));//Overwrite the file
			output.writeBytes(response.toString());//Writes headers
			output.write(response.body);//Write body
			output.close();
			System.out.println("Overwritten existing cached File : " + request.URI+ " stored at " + file.getAbsolutePath());
		}else{
			file = new File("cache/", "cached_"+System.currentTimeMillis());
			output = new DataOutputStream(new FileOutputStream(file));
			output.writeBytes(response.toString());//Writes headers
			output.write(response.body);//Write body
			//output.flush();
			output.close();
			cache.put(request.URI, file.getAbsolutePath());
		
			System.out.println("Response file to: " + request.URI+ " stored at " + file.getAbsolutePath());
		}
	}
	
	public synchronized static byte[] uncaching(String uriRequest) throws IOException{
		File cachedFile;
		FileInputStream outputFile;
		String hashFile;
		byte[] bytescached;
		
		if((hashFile = cache.get(uriRequest)) != null){
			cachedFile = new File(hashFile);
			outputFile = new FileInputStream(cachedFile);
			bytescached = new byte[(int)cachedFile.length()];
			outputFile.read(bytescached);
			System.out.println("Caching: Hit on "+uriRequest+" :returning cache to user");
			return bytescached;
		}else{
			System.out.println("Caching: No hit on "+uriRequest);
			return bytescached = new byte[0];
		}
	}
	
	public synchronized static void deleteCachedFile(String uriRequest){
		File cachedFile;
		String hashFile;
		
		if((hashFile = cache.get(uriRequest)) != null){
			cachedFile = new File(hashFile);
			boolean deleted = cachedFile.delete();
			System.out.println("File: " + hashFile + " deleted: "+ deleted);
		}
		
	}
		
	public static void init(int p){
		port = p;
		try{
			socket = new ServerSocket(port);
		} catch(IOException e){
			System.err.println("Error creating socket: " + e);
			System.exit(-1);
		}
	}
	
	public static ArrayList<String> getRemovedObjects(){
		return cache.getEvictedObjects();
	}
	
	public static String removedObject(){
		
			return cache.getEvictedObject();
		
	}
	
	public static String getCacheAlgorith(){
		
		return cacheAlgorithm;
	}

	//Read command line arguments and start proxy
	public static void main(String[] args) {
		int proxyPort = 0;
		int maxNumOfCachedItem = 0;
		long timeToLive = 100; //Life span of cached objects in seconds
		
		File cachedir = new File("cache/");
		
		try{
			proxyPort = Integer.parseInt(args[0]);
		} catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Port number not provided...");
			System.exit(-1);
		} catch(NumberFormatException e){
			System.out.println("Port number must be an integer...");
			System.exit(-1);
		}
		
		try{
			//create the cache
			maxNumOfCachedItem = Integer.parseInt(args[1]);
			cache = new WebCache<String, String>(timeToLive, maxNumOfCachedItem);
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Maximum number of cache objects not specified...");
			System.exit(-1);
		}catch(NumberFormatException n){
			System.out.println("Maximum number of cache objects should be a number...");
			System.exit(-1);
		}
			
		if(!cachedir.exists()){
			cachedir.mkdir();
		}
		
		try{
			
			if(args[2] != null && !args[2].isEmpty()){
				cacheAlgorithm = args[2];
			}
			
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Cache management algorithm not provided...");
			System.exit(-1);
		}catch(Exception e){
			System.out.println("Cache management algorithm ..." + e);
			System.exit(-1);
		}
		
		
		
		init(proxyPort);
		
		//Listen for connection
		Socket client = null;
		
		System.out.println("Proxy Server is up \n");
		
		while(true){
			try{
				client = socket.accept(); //Accept client connection
				(new Thread(new Threads(client))).start();//Spawn thread for every client connection
			}
			catch(IOException e){
				System.err.println("Error reading request from client: " + e);
				
				continue; //Skip to next iteration of the loop
			}
		}

	}//End of main method...

}
