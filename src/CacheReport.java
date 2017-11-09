import java.io.*;

public class CacheReport{

public static int countLines(String filename) throws IOException {
    InputStream is = new BufferedInputStream(new FileInputStream(filename));
    try {
        byte[] c = new byte[1024];
        int count = 0;
        int readChars = 0;
        boolean empty = true;
        while ((readChars = is.read(c)) != -1) {
            empty = false;
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n') {
                    ++count;
                }
            }
        }
        return (count == 0 && !empty) ? 1 : count;
    } finally {
        is.close();
    }
}

public static void main(String[] args) throws Exception{
	
	int cacheMiss = countLines("/Users/nwachukwuc1/Downloads/ProxyServer/src/logs/cacheMissCount.txt");
	
	int cachehit = countLines("/Users/nwachukwuc1/Downloads/ProxyServer/src/logs/cacheHitCount.txt");
	
	int requestCount = countLines("/Users/nwachukwuc1/Downloads/ProxyServer/src/logs/requestCount.txt");
	
	double divisor = (cacheMiss * 1.0) + (cachehit * 1.0);
	
	double hitrate = (cachehit / divisor) * 100;
	
	System.out.println("Total Request: " + requestCount); 
	System.out.println("Total Cache Miss: " +cacheMiss);
	System.out.println("Total Cache Hit: " +cachehit);
	
	String hitRate = String.format("Hit rate: %.2f percent", hitrate);
	System.out.println(hitRate);
}

}