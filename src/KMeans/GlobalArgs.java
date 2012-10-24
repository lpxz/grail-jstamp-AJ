package KMeans;

public class GlobalArgs {
   /*atomicset(G)*/
	/*atomicset(I)*/
	/*atomicset(D)*/
    public GlobalArgs() {
    }

    /**
   * Number of threads
   **/
    public int nthreads;

    /**
   * List of attributes
   **/
  /*atomic(G)*/  public float[][] feature/*this.G[]=this.G*/;

    /**
   * Number of attributes per Object
   **/
    public int nfeatures;

    /**
   * Number of Objects
   **/
    public int npoints;

    /**
   * Iteration id between min_nclusters to max_nclusters 
   **/
    public int nclusters;

    /**
   * Array that holds change index of cluster center per thread 
   **/
    public int[] membership;

    /**
   *
   **/
    public float[][] clusters;

    /**
   * Number of points in each cluster [nclusters]
   **/
    /*atomic(G)*/   public int[] new_centers_len  /*this.G[]=this.G*/;

    /**
   * New centers of the clusters [nclusters][nfeatures]
   **/
    /*atomic(G)*/   public float[][] new_centers /*this.G[]=this.G*/;

    /**
    *
  **/
 /*atomic(I)*/   public int global_i;

 /*atomic(D)*/   public float global_delta;

    long global_time;
}
