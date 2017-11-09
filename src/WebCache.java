import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;


public class WebCache<K, T> {
	
	private long timeToLive;//Life span of cached object
    private HashMap cacheMap;//A map that will hold the cached objects
    private int maxNumItems;
    private ArrayList evictedObject;
    private T evictedByFIFO;
   
    //private static int counter = 1;
    
    //Inner class describing the value of cached Map
    protected class CachedObject{
    	
    	public long lastAccessed = System.currentTimeMillis(); //time cached-object was accessed
    	public T value;
    	//public int index;
    	
    	public CachedObject(T value){
    		this.value = value;
    		//this.index = index;
    	}
    }//End of inner class representing a CachedObject
    
    public WebCache(long timeToLive, int maxNumItems){
    	
    	this.timeToLive = timeToLive * 1000;
    	this.maxNumItems = maxNumItems;
    	cacheMap = new HashMap();
    }
    
    @SuppressWarnings("unchecked")
	public void put(K key, T value){
    	synchronized(cacheMap){
    		int mapSize = cacheMap.size();
    		int maxSize = maxNumItems;
    		String cachAlgorithm = ProxyCache.getCacheAlgorith();
    		
    		System.out.println(mapSize);
    		System.out.println(maxNumItems);
    		
    		if(cacheMap.containsKey(key)){
				CachedObject c = (CachedObject) cacheMap.get(key);//get the old value mapped to key
				c = (CachedObject) value;
				c.lastAccessed = System.currentTimeMillis();
			}else{
				if(mapSize >= maxSize){
					
					if(cachAlgorithm.equals("LRU")){
						cleanupLRU();
					}
					
					if(cachAlgorithm.equals("FIFO")){
						cleanFIFO();
					}
					
	    		}
			}
    		
    		cacheMap.put(key, new CachedObject(value));
    		//++counter;
    		System.out.println(mapSize);
    		System.out.println(maxNumItems);
    	}
    }
    
	@SuppressWarnings("unchecked")
    public T get(K key){
    	synchronized(cacheMap){
    		CachedObject c = (CachedObject) cacheMap.get(key);
    		
    		if(c==null){
    			return null;
    		}else{
    			c.lastAccessed = System.currentTimeMillis();
    			return c.value;
    		}
    	}
    	
    }
    
    public void remove(K key) {
        synchronized (cacheMap) {
        	cacheMap.remove(key);
        }
    }
    
    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }
    
    @SuppressWarnings("unchecked")
    public void cleanupLRU() {

        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;       

        synchronized (cacheMap) {
        	Set cacheSet = cacheMap.entrySet();
           Iterator itr = cacheSet.iterator();

            deleteKey = new ArrayList<K>();
            evictedObject = new ArrayList<CachedObject>();
            
            K key = null;
            CachedObject c = null;

            while (itr.hasNext()) {
            	Map.Entry mentry = (Map.Entry)itr.next();
                key = (K) mentry.getKey();
                c = (CachedObject) mentry.getValue();

                if (c != null && (now > (timeToLive + c.lastAccessed))) {//Get the Least Recently Accessed object
                    deleteKey.add(key);
                    evictedObject.add(c.value);
                }
            }
        }
        //Remove cached objects if life span expires
        if(!deleteKey.isEmpty()){
            for (K key : deleteKey) {
                synchronized (cacheMap) {
                    cacheMap.remove(key);
                }
            }
        }
    
    }
    
    @SuppressWarnings("unchecked")
	public void cleanFIFO(){
    	
    	ArrayList<K> cacheObjectList = null;
    	
    	synchronized(cacheMap){
    		Set cacheSet = cacheMap.entrySet();//Return all cached items as a set
            Iterator itr = cacheSet.iterator(); //create an iterator to loop through the set
            cacheObjectList = new ArrayList<K>();
            
            K key = null;
            CachedObject c = null;
            
            while (itr.hasNext()) {
            	Map.Entry mentry = (Map.Entry)itr.next();
                key = (K) mentry.getKey();
              
                	//Add item key to the list
                	cacheObjectList.add(key); 
                }
            
            	//Removed the first object in cache (FIFO)
            	c = (CachedObject)cacheMap.get(cacheObjectList.get(0)); //Object evicted
            	evictedByFIFO =  c.value; //Returns objected deleted by FIFO
                cacheMap.remove(cacheObjectList.get(0)); 
    	}//End of synchronized block
    }
    
@SuppressWarnings("unchecked")
public ArrayList<T> getEvictedObjects(){
	return evictedObject;
	}

public T getEvictedObject(){
	return evictedByFIFO;
}

}//End of class
