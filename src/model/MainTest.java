package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MainTest {
	static int deep = 1;
	
	public static void main(String[] args) {
		Dependency dependency = new Dependency("org.apache.maven",  "maven-model", "3.9.6");
//		Dependency dependency1 = new Dependency("org.openjfx",  "javafx-controls", "21.0.1");
//		explore(dependency);
		
		Map<Dependency, Integer> myMap = new HashMap<>();

        // Thêm các cặp key-value vào HashMap
        myMap.put(dependency, 0);
		List<Dependency> visited = new ArrayList<>();
		Queue<Dependency> queue = new LinkedList<>();

        // Thêm phần tử vào hàng đợi
        queue.offer(dependency);
        visited.add(dependency);
        
        while(queue.isEmpty() == false) {
        	Dependency curr = queue.poll();
        	System.out.println(myMap.get(curr) + ": "+ curr.toString());
        	for( Dependency dpdc: curr.getDependency()) {
        		if( visited.contains(dpdc) == false) {
        			myMap.put(dpdc, myMap.get(curr) + 1);
        			queue.offer(dpdc);
        	        visited.add(dpdc);
        		}
        	}
        }
	}
	
	public static void explore(Dependency dpdc) {
		for( int i = 0; i<deep; i++) {
			System.out.print("  ");
		}
		System.out.println(dpdc.toString());
		deep += 1;
		if( deep < 5)
			for(Dependency d: dpdc.getDependency()) {
				explore(d);
			}
		deep -= 1;
	}
}
