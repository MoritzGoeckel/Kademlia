package com.moritzgoeckel.kademlia;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/** Quick and dirty networking statistics */
class NodeStatistics {

    private HashMap<String, Integer> stats = new HashMap<>();
    private boolean printedHeader = false;

    void recordEvent(String name){
        if(!stats.containsKey(name))
            stats.put(name, 0);

        stats.put(name, stats.get(name) + 1);
    }

    /*void printSummery(int iterations, int nodes){
        System.out.println("---------Stats---------");
        System.out.println("Operation" + "\t" + "ops/iteration" + "\t" + "ops/iteration/node");
        for(Map.Entry<String, Integer> e : stats.entrySet())
            System.out.println(e.getKey() + "\t" + (e.getValue() / (double)iterations) + "\t" + (e.getValue() / (double)iterations / nodes));
        System.out.println("-----------------------");
    }

    void printHeader(){
        for(String key : stats.keySet())
            System.out.print(key + "\t");
        System.out.print("\r\n");
    }

    void printValues(){
        for(String key : stats.keySet())
            System.out.print(stats.getOrDefault(key, 0) + "\t");
    }*/

    int getSum(){
        int sum = 0;
        for(String key : stats.keySet())
            sum = stats.getOrDefault(key, 0) + sum;

        return sum;
    }
}
