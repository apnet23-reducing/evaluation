## Dependencies

- Java. We use JDK 17, but lower versions may also work.
- Gurobi. Download and install Gurobi from [Gurobi Software - Gurobi Optimization](https://www.gurobi.com/downloads/gurobi-software/). The version we use is 10.0.0.
- Maven (optional). This project uses Maven to manage dependencies. If you don't have Maven installed, we provide a pre-compiled jar (`lib/paper.jar`).

## Data Preprocessing (Optional)

Before conducting experiments, the original data provided by Facebook needs to be preprocessed, which may take a considerable amount of time. Therefore, we have stored the processed results in the `data` directory. If you need to reproduce this process, first download the raw data `fb_clusterA_full.csv` and `fb_clusterB_full.csv` from [this site](https://trace-collection.net/dc-traces/), and move them to the `data` directory. Then, execute the following commands:

```shell
java -cp lib/* ProcessRawData data/fb_clusterA_full.csv data/facebook_cluster_a.csv data/facebook_cluster_a_metadata.json
java -cp lib/* ProcessRawData data/fb_clusterB_full.csv data/facebook_cluster_b.csv data/facebook_cluster_b_metadata.json
java -cp lib/* AggregateData data/facebook_cluster_a.csv data/facebook_cluster_a_metadata.json data/facebook_cluster_a_10min.json rack 600
java -cp lib/* AggregateData data/facebook_cluster_b.csv data/facebook_cluster_b_metadata.json data/facebook_cluster_b_10min.json rack 600
```

## Running Experiments

Run the following commands to obtain experimental results:

```shell
java -cp lib/* Benchmark data/facebook_cluster_a_10min.json data/facebook_cluster_a_results.json
java -cp lib/* Benchmark data/facebook_cluster_b_10min.json data/facebook_cluster_b_results.json
```
