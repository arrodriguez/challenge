# Taurus Load Test Analysis

## Test Configuration

The test was executed using Taurus with the following configuration:

- **Concurrency:** 10 users
- **Ramp-up:** 10 seconds
- **Hold Duration:** 5 minutes
- **Scenario:** csv-based-test
- **Data Source:** ./request_path-fragment.csv
- **Request Method:** GET
- **URL:** `http://localhost:8080/blocklist/ips/${path}`
- **Modules:** Console with throughput metric

The server was started using the following Java command:

```java
taskset -c 0 java -Xmx512m -jar target/blocklist-1.0-SNAPSHOT.jar server ./dev-config.yml
```

The Taurus command was the following:

`bzt ./test-plan.json` 

The java resources comsumption were extracted using:

`jconsole $process_id`

## Results

### Throughput

The test executed a total of 724,630 requests with 0.00% failures. This gives an average throughput of:


Throughput = Total Requests / Test Duration  
= 724,630 / (5 x 60 + 44)  
≈ 2,127 requests/sec

### Response Times

The response time percentiles are as follows:

- **50th Percentile (Median):** 0.001 seconds
- **90th Percentile:** 0.001 seconds
- **95th Percentile:** 0.001 seconds
- **99th Percentile:** 0.006 seconds
- **99.9th Percentile:** 0.016 seconds
- **Max Response Time:** 0.088 seconds

### Memory Usage

The memory usage fluctuated during the test, starting from around 79 MB and reaching a peak of approximately 192 MB.

### Threads

The number of live threads started at 41 and peaked at 203 before settling back down to 200 by the end of the test.

### CPU Usage

The CPU usage started low but quickly ramped up to around 95% for the majority of the test. This indicates that the server was CPU-bound during the test, which could be a potential bottleneck. Take into account that the process was force to run in only one core by the `taskset` command.  

## Observations

- **High Throughput:** The server was able to handle over 2,000 requests per second, indicating good performance.
- **Low Response Times:** The majority of the requests were served within 1 millisecond, which is excellent.
- **Memory Usage:** There was a noticeable increase in memory usage during the test, but it didn't reach the maximum allocated memory (512 MB).

## Conclusion

The server demonstrated good performance under the given load.