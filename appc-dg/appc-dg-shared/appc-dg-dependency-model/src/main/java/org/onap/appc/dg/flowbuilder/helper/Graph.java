/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.dg.flowbuilder.helper;

import java.util.*;


public class Graph<T> {
    private int size;
    private List<T> vertexList;

    private int[][] dependencyMatrix;

    public Graph(int size){
        this.size =size;
        vertexList = new ArrayList<>();
        dependencyMatrix = new int[size][size];
    }

    public void addVertex(T vertex){
        vertexList.add(vertex);
    }

    public int getIndex(T vertex){
        return vertexList.indexOf(vertex);
    }

    public void addEdge(T vertex1,T vertex2){
        dependencyMatrix[vertexList.indexOf(vertex1)][vertexList.indexOf(vertex2)] = 1;
    }

    public int[][] getDependencyMatrix() {
        return dependencyMatrix;
    }

    public int getSize() {
        return size;
    }

    public List<T> getVertexList() {
        return vertexList;
    }
}
