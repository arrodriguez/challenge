# IP blocklist Service  
This service is intended to be used to prevent abuse in different applications to ban IPs that are known to be used for malicious purposes.  
We can found [here](DESIGNDOC.md) a complete description of the design choices, compromises or trade-off made in this assignment.  

How to start the service in your development environment
---  
1. Run `mvn clean install` to build your application
2. Make sure dev-config.yml configuration has the right values for your setup.
3. IP blocklist application uses an external dataset that can be download using one of the following options:
    - You can follow the steps described [here](https://github.com/stamparm/ipsum),
    - Or you can run the folowing command `java -jar target/blocklist-1.0-SNAPSHOT.jar monitor dev-config.yml`, the
       file will be place here from `dev-config.yml`: 

       ```yaml 
       blockListPath: $WHATEVERPATH/ipsum.txt`

    - If you choose option 1, make sure that `blockListPath: $WHATEVERPATH/ipsum.txt` variable from `dev-config.yml` is
      consistent with where the file was located.
4. Start the application with the following command:  
   `java -jar target/blocklist-1.0-SNAPSHOT.jar server dev-config.yml`
5. To check that your application is running enter url `http://localhost:8081` and navigate the operation menu.  
 
General Code structure
---

| Directory/File               | Description                                                                 |
|------------------------------|-----------------------------------------------------------------------------|
| `api/`                       | Representations. Request and response bodies.                               |
| `cli/`                       | Commands.                                                                   |
| `core/`                      | Domain implementation; where objects not used in the API such as POJOs, validations, converters, etc, . |
| `configuration/`             | Custom configuration.                                                       |
| `db/`                        | Database access classes.                                                    |
| `infrastructure/`            | Houses Docker configurations, orchestration setups, and container initialization scripts.               |
| `health/`                    | Health Checks.                                                              |
| `resources/`                 | API Resources.                                                              |
| `IPBlocklistApplication`     | The application class.                                                      |
| `IPBlocklistConfiguration`   | Configuration class.                                                        |

Health Check
---  
To see application health enter url `http://localhost:8081/healthcheck`

Metrics
---  
To see application metrics enter url `http://localhost:8081/metrics`  

### There are 4 importants metrics to follow:

```json
{
  "version" : "4.0.0",
  "gauges" : {
    "blockListVersion.lastUpdate" : {
      "value" : 1696547179401
    },
    "blockListVersion.sha256" : {
      "value" : "1bbfd5925b81bfb2eb4357912c7c3b0a11d5115b8c116b210f7f2baf934e8729"
    },
    ...
  },
  "timers" : {
    "com.muun.resources.BlocklistResource.blockList" : {
      "count" : 4362999,
      "max" : 3.3672E-5,
      "mean" : 6.871140900100589E-6,
      "min" : 1.8880000000000002E-6,
      "p50" : 6.428E-6,
      "p75" : 8.141E-6,
      "p95" : 9.648E-6,
      "p98" : 1.0972000000000001E-5,
      "p99" : 1.2838000000000001E-5,
      "p999" : 2.2293E-5,
      "stddev" : 1.968768413914828E-6,
      "m15_rate" : 3868.7331444372585,
      "m1_rate" : 14839.978602770883,
      "m5_rate" : 8757.65484379387,
      "mean_rate" : 1936.7678309594337,
      "duration_units" : "seconds",
      "rate_units" : "calls/second"
    },
    ...
  },
  ...
}
```  
### Block List Version Metrics

**`blockListVersion.lastUpdate`**
- Description: Timestamp of the last time the `/blocklist/ips:reload` endpoint was called.

**`blockListVersion.sha256`**
- Description: SHA256 hash of the blocklist dataset file. This value is recalculated every time the dataset is updated—either by a user or a script—and the `/blocklist/ips:reload` endpoint is triggered.

### Resource Execution Metrics

**`com.muun.resources.BlocklistResource.blockList.total`**
- Description: Histogram metric representing the total time taken for the execution of the resource method.

Running the tests
---

Run `mvn test` to run and verify all tests.

How to simulate a production like environment using `docker compose`
---

As was mention, `infrastructure/` folder houses all files required for simulating a productive environment.  
Under that folder run `docker compose build`.  

**`docker-compose.yml`**: 
- **Purpose**: Orchestration setup.
- **Details**: Facilitates the creation of multiple services and integrates an NGINX load-balancer.

**`start.sh`**: 
- **Purpose**: Service entry point.
- **Details**: 
  - Initiates the service.
  - Contains mechanisms for periodic IP blocklist dataset updates.
  - Polls the repository every 5 minutes to detect changes.
  - ⚠️ Caution: Reducing this interval may trigger GitHub API rate limits.

**`nginx.conf`**: 
- **Purpose**: Load balancing configuration.
- **Details**: Manages traffic on port `:8000`, distributing it among all containers spawned by `docker-compose`.

Scaling the service up/down could be trigger executing the following: `docker compose up --scale blocklist=n -d` where indicates
the number of containers in the scaling group.  

The recommended order of commands to try are:

1. `docker compose up -d` starts blocklist service and the nginx with 1 container instance.  
2. `docker compose up -d --scale blocklist=4` scales up with 3 more blocklist container service instances.  
3. `docker compose up -d --scale blocklist=2` scales down reducing in 2 the numbe of blocklist container service instances.  

If you're interested on how the polling ipblocklist dataset mechanism works, you can [continue here](BLOCKLISTMONITOR.md)  

Load testing using [Taurus](https://gettaurus.org/)
---
You can read the entire analisis load test [here](LOADTESTING.md) 

