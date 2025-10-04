package org.itmo;

import java.util.*;

public class Graph {
    private final int size;
    private final ArrayList<Integer>[] adjList;

    Graph(int vertices) {
        this.size = vertices;
        adjList = new ArrayList[vertices];
        for (int i = 0; i < vertices; ++i) {
            adjList[i] = new ArrayList<>();
        }
    }

    void addEdge(int src, int dest) {
        if (!adjList[src].contains(dest)) {
            adjList[src].add(dest);
        }
    }

    public int getSize() {
        return size;
    }

    // not safe, but today I don't care
    public ArrayList<Integer>[] getAdjList() {
        return adjList;
    }
}
