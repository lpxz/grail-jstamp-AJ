package SSCA2;

public class Graph {
    /*atomicset(I)*/
    public int numVertices;

    public int numEdges;

    public int numDirectedEdges;

    public int numUndirectedEdges;

    public int numIntEdges;

    public int numStrEdges;

    public int[] outDegree;

    public int[] outVertexIndex;

    public int[] outVertexList;

    public int[] paralEdgeIndex;

   /*atomic(I)*/ public int[] inDegree /*this.I[]=this.I*/;

    public int[] inVertexIndex;

    public int[] inVertexList;

    public int[] intWeight;

    public byte[] strWeight;

    public Graph() {
    }
}
