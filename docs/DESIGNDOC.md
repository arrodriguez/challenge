# IP Block List Service Design Document

## Problem Statement

A microservice is required to manage an IP blocklist. The primary purpose of this service is to prevent abuse across various applications and to blacklist IPs known for malicious activities. Recognizing the potential complexity of such a problem, the design's scope will be confined to the following essential features:

- Enable consumers to verify if a specific IPv4 address is present in 30 or more publicly accessible lists of suspicious and/or malicious IP addresses. This functionality will be made available through a distributed service adhering to REST API standards.
- Ensure the service's high availability, emphasizing swift restart times and minimal downtime during blocklist updates.
- Maintain the service's operational efficiency under substantial load, guaranteeing prompt responses.

## Summary

- A hashing structure will be employed by the service, utilizing either perfect hash or cuckoo hash for implementation.
- The service will undergo updates every 24 hours, leveraging a lockless approach supported by atomic references.
- Driven by its load, the service will possess the capability to scale both upwards and downwards as necessary.
- A load balancer will be integrated, complementing the aforementioned autoscaling feature.

## Core Feature Design Choices

### IPV4 Blocklist Test Feature

The advantage of [this public list](https://github.com/stamparm/ipsum), which is updated every 24 hours, will be utilized. This list is accepted as part of the requirements. For further details, refer to the [Blocklist Dataset Decision](#blocklist-dataset-decision) section.

Upon examining the IP list data source [ipsum.txt](https://github.com/stamparm/ipsum/ipsum.txt), three key characteristics were identified that influence the internal data structure design:
* The dataset remains fixed daily, implying no changes throughout the day.
* Its size is relatively small (2 or 3 orders of magnitude) compared to the lower-tier hardware available from today's cloud providers.
* The growth rate of the dataset size is uncertain.

Before proposing an implementation, the last point warrants further analysis:

No method was found to measure the dataset's growth rate, as the repository owner updates it with a force push daily, disrupting any commit diff or [IPSum history](https://github.com/stamparm/ipsum/activity). Despite the uncertainty regarding daily dataset growth, it was observed that the repository has been updated daily since March 2023. Given the dataset's consistent size of around 8MB since its inception, it can be reasonably inferred that its growth rate is manageable.

For the design of an individual computing unit, the data structure has the following specifications:

* An in-memory data structure tied to the service will store blocked IPv4 address information. Given that the minimum memory hardware from most cloud providers starts at 1GB, storing several MBs in memory should not compromise the system's stability, scalability, or resilience.
* A hashing structure optimal for data access patterns will be employed. The emphasis is on high READ ACCESS throughput and minimal WRITE ACCESS, allowing the dataset to be treated as immutable. This shifts the challenge to efficiently swapping small, immutable structures in memory.

#### Why a Hashing Structure?

While the IPV4 string representation from the API call could be used, computations are generally more efficient with INTEGER or FLOAT arithmetic. Converting the IPV4 address's string representation to a 32-bit integer facilitates efficient storage and quick comparisons. Furthermore, hashing structures offer constant-time average complexity for lookups. 

By applying this technique, the dataset, which contained 238,459 dot decimal format IPV4 addresses (1 per line), was reduced from approximately 4MB to roughly 0.9 MB.

In conclusion, this feature was adapted to fit well-established patterns and tools known to be effective in similar contexts and use cases.

### The Service: Achieving High Availability

Several characteristics from the previous analysis were identified with high availability in mind:

* Immutable dataset structures are swapped during dataset changes, minimizing downtime or degradation during updates.
* No single point of failure exists since the entire dataset is stored in memory, eliminating reliance on external storage services.

The latter point is self-explanatory, but the former requires a deeper dive:

A solution capable of monitoring repository changes (on a daily basis) and notifying all service instances of these changes can achieve zero downtime during dataset deployments.

#### How So?

The following implementation is proposed once the service receives an update signal:
* A REST-style method will be introduced, which some process will invoke every time the dataset repository is updated.
* Internally, given the hashing structure, a mechanism must be developed to manage concurrency between readers (service clients) and the dataset updating method.

#### Concurrency Handling

Given the non-transactional nature of the dataset, an approach focused on high performance is feasible. The processor's guarantees can be harnessed to swap references of the immutable dataset. Here's a step-by-step breakdown:

1. Create a minival perfect hash function reference that encompasses all IPV4 entries in the dataset. This reference is then encapsulated within a CPU atomic reference.
2. Ensure all lookups utilize this atomic reference.
3. When a reload signal is activated, instantiate a new Minimal Hash function reference.
4. Subsequently, perform an atomic swap between the old and new references.
5. In the event an in-flight thread accesses the old reference, it remains valid. The garbage collector (`gc()`) will only dispose of it once no references to the object exist.

### The Service: Operational excelence guarantess

In the pursuit of operational excellence, several strategic decisions regarding the architecture and scaling of the service have been made. Here's a breakdown of these choices and the rationale behind them:

#### Horizontal Scaling under a Load Balancer

Horizontal scaling under a load balancer has been chosen. This approach allows incoming network traffic to be efficiently distributed across multiple servers, ensuring high availability and redundancy. In the event of a server failure or heavy traffic to one server, traffic is redirected by the load balancer to other servers in the pool, maintaining a seamless user experience.

#### Stateless Service

The service has been designed to be stateless, meaning each request is processed without relying on any stored session or user-related data from previous requests. This design choice simplifies scalability since any request can be handled by any server at any time.

#### Dataset Independence

The dataset upon which the service relies is independent, with updates occurring once a day. This predictable update frequency simplifies the scaling strategy and ensures that all servers have access to the most recent data without frequent synchronization needs.

#### Local Data Storage Trade-offs

A challenge that was encountered was data fragmentation and duplication across all servers. However, after careful consideration, it was concluded that storing the dataset locally on each server was the best trade-off. Local storage ensures rapid data access, reduces latency, and eliminates the need for a centralized database that could become a bottleneck or single point of failure.

#### Scaling Triggers: CPU, Memory, or Both

The exact triggers for scaling—whether based on CPU exhaustion, memory exhaustion, or a combination of both—haven't been definitively determined. This aspect requires further analysis and testing to ascertain the most effective and efficient scaling criteria.

In summary, the design choices aim to guarantee operational excellence by ensuring high availability, redundancy, and efficient data access, even as traffic scales.

### Implementation Notes

The base code for the project was written entirely in Java. While the author hasn't extensively worked with Java frameworks in recent years, they possess adequate proficiency with the Java Standard implementation up to version 1.6. To align with current market standards, a modern REST/HTTP framework was selected. Dropwizard was the framework of choice due to its simplicity. A [comprehensive quickstart guide](https://www.dropwizard.io/en/stable/getting-started.html) provided the foundation for the project skeleton and facilitated a swift implementation process for the author.

#### Why a minimal perfect hash algorithm as method for handling collisions?   

In the context of our software, we have the unique advantage of knowing all possible hash keys beforehand. Additionally, our problem doesn't necessitate storing any data associated with these keys. Given these specifics, a minimal perfect hash function emerges as an ideal solution.

However, there isn't a standardized implementation of a perfect hash function. As a result, we turned to open-source projects to find a robust implementation. Our search led us to [this project](https://github.com/vigna/Sux4J/), which stood out for several reasons. Not only does it have artifacts available in the Maven repository, but its substantial number of stars, comprehensive tests, and detailed documentation underscore its reliability and robustness.

## Trade-offs Due to Time Constraints

### Blocklist Dataset Decision

Due to time constraints, a more optimal solution for the blocklist dataset couldn't be explored. As a result, the solution proposed in the challenge exercise was relied upon.

### Service Observability: Telemetry and Alarming

The service is equipped with a default admin endpoint, courtesy of **Dropwizard**. However, the foundational code does not incorporate a comprehensive observability system that can harness these metrics effectively. Such observability is paramount for production-grade deployments. A notable concern is the inability to monitor the sidecar's functionality in updating the `ipsum.txt` dataset.

For optimal monitoring, one should access the timestamp of the last blocklist reload via the relevant endpoint metric. Subsequently, an alarm should be established to track any discrepancies or failures in this metric. Typically, such monitoring tasks are the purview of the observability system.

Another area that requires attention is the lack of a centralized metrics storage system. This omission complicates autoscaling based on service load. The current setup would require polling each server behind the load balancer, aggregating the data, and then determining the scaling direction (either scaling up or down). It's worth noting that many cloud providers offer integrated metric and observability solutions to streamline these processes.

### Benchmarks

It was not possible due to time-constraint limitations to explore and Benchmarks with other similar solutions.
Nevertheless, a load testing was implemented in order to analyse the performance of the service.

### Decision to Focus on IPv4

While IPv6 is becoming more prevalent, the decision was made to focus primarily on IPv4 for this project. Here are the reasons for this choice:

1. **Simplicity of IPv4:** IPv4 is straightforward and well-understood, making it easier to work with, especially within the constraints of this project.
2. **IPv6 Complexity:** A comprehensive understanding of how IPv6 operates would necessitate significant research and time investment. While the overall solution might not differ drastically, the nuances of IPv6 could introduce complexities not present with IPv4.
3. **Blocklist Source Compatibility:** The blocklist sources available for this project are not optimized for IPv6, further justifying the decision to focus on IPv4.

In summary, while IPv6 is essential for modern networking, the decision to prioritize IPv4 was based on simplicity, time constraints, and available resources.

