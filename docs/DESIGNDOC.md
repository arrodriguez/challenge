# IP Block List Service Design Document
## Problem Statement
We need to create a microservice that manages a blocklist of IPs. This service will be used to prevent abuse in different applications and to ban IPs known to be used for malicious purposes.

Given that this type of problem can be as complex as we want, we will limit the scope of the design to the following core features:

* Consumers will be able to test if a determined IPv4 address appears in 30+ different publicly available lists of suspicious and/or malicious IP addresses. Expose this functionality with a distributed service using REST API-like standards.
* This service should be highly available, minimizing the time it takes to restart it and the downtime when updating the blocklist.
* The service should remain operational under heavy load and be able to respond in a reasonably short time. We will run benchmarks comparing a set of different competitors services.

## Summary
* The service will implement a hashing structure with perfect hash or cuckoo hash as implementation,
* The service will update every 24 hs using a lockless approach with atomic references,
* The service will scale up/down as much as needed driven by the load of the service,
* The service will use a loadbalancer in top of previous autoscaling characteristic.

## Core feature design choices

### IPV4 blocklist test feature
We are going to take advantage of [this public list](https://github.com/stamparm/ipsum), which gets updated every 24 hours. We will take this list for granted as part of the requirements. You can look [here](#tradeoffs) for more information.

Looking at the IP list data source [ipsum.txt](https://github.com/stamparm/ipsum/ipsum.txt) 2 important characteristic was found that will drive the internal implementation of the data structure:
* It is daily fixed meaning that the dataset is not going to change for the day,
* It is relatively low in size ( 2 or 3 orders of magnitude ) comparing today's lower tier of available hardware from cloud providers.
* Dataset size growth rate is unknown.

Before continuing with an implementation proposal, let's take a moment to analyse the last point:

We couldn´t find an approach to measure the growth rate of the dataset because the owner of the repository updates with a force push every day breaking any kind of commit diff or [IPSum history](https://github.com/stamparm/ipsum/activity). Although we don't know how the dataset will growth day by day we do know that the repository has ben daily updated since March 2023 and if we assume that this dataset should have a daily positive growth ( could shrink but looks rare ) and we also know that the dataset size is around 8MB since beginning, we can say relativelly safe that the growth rate is managable.     

Assuming for now that we are designing an individual unit of computing power, the datastructure will have the following definitions:

* We will use an in memory data structure bounded to the service for storing blocked IPv4 information. In most cloud providers the minimal memory hardware available starts from 1GB, storing dozens of MB in memory won't impact system ability to be stable, scalable and failure resilient.
* We will use a hashing structure that will be optiminal for the access pattern to the data. High throughput for READ ACCESS and almost none WRITE ACCESS during a day meaning that we can assume our dataset to be inmutable, and deal with a different problem of swaping inmutables low size structures in memory.  

#### Why a hashing structure ? 
Well, we can use the IPV4 string representation that came from the API call, but we know that computation is more efficient when has to deal with INTEGER OR FLOAT arithmetic. So, first of all converting the string representation of the IPV4 address to a 32bit integer allows efficient storage and quick comparisons. More over, hashing structures has constant-time average complexity for lookups ( also for addition and deletion but in this context insertion, deletion and updates are not as relevant in the critical path ). 

Applying the technique, the dataset holding 238459 dot decimal format ipv4 addresses ( 1 per line ) went from around 4MB in size to (238459 lines * 4bytes) ~= 0.9 MB  

In conclusion, we managed to make this feature fit well into known patterns and tooling that are proven to work in similar scopes and use-cases. 

### The Service: Achieve high availability
There are several characteristics from the previous analysis that's was made with high availability in mind,

* Inmutable dataset swaping structures on dataset changes thus minimizing downtime or degradation while updating the dataset,
* There is NO single point of failure given that we decided to have the entire dataset in memory ( we are not using any external service for storing the dataset ),

The last point is self explanatory, but let's zoom in the first one:

Having a solution that can watch repository changes ( on daily basis ) notifying all instaces of the service with the changes, and also manage to swap the old dataset with the new will achieve a zero downtime of dataset deployment system. 

#### How so? 

Once the service is signaled, we proposed the following implementation:
* Create a REST style method that some process will call every time the dataset repository is updated,
* Internally, assuming a hashing structure, we have to implement a mechanism that will handle the concurrency between readers ( client of the service ) and the method for updating the dataset.

#### Concurrency handling

Again using the data ability to NOT be updated regularly or transactionally, we can be even more eager in the search of a high performant implementation thus, we can use processor guarantess to swap the references of the inmutable dataset. Let's try to visualize this:

* We create a hash map reference with all the IPV4 entries in the dataset, we wrap the reference 

### The Service: Operational excelence guarantess

### Implementation notes
The basecode of the project was entirely written in Java. The author does not have much experience working with Java frameworks in the last years, but has sufficient profiency in Java Standard implementation up to 1.6. Nevertheless, a modern REST/HTTP framework was chosen in order to be at pair with market standards. The framework choose was Dropwizard given its simplicity and a [complete quickstart guide](https://www.dropwizard.io/en/stable/getting-started.html) that was used to create the skeleton and helped the author for moving fast with the implementation.

#### Why a minimal perfect hash algorithm as method for handling collisions? 
Given that the entire universe of keys for hash are given in advance, the idea was to try to mimic a perfect hashing without 



#### Drop wizard

## Trade-offs Due to Time Constraints

### Service Observability: Telemetry and Alarming

The service is equipped with a default admin endpoint, courtesy of **Dropwizard**. However, the foundational code does not incorporate a comprehensive observability system that can harness these metrics effectively. Such observability is paramount for production-grade deployments. A notable concern is the inability to monitor the sidecar's functionality in updating the `ipsum.txt` dataset.

For optimal monitoring, one should access the timestamp of the last blocklist reload via the relevant endpoint metric. Subsequently, an alarm should be established to track any discrepancies or failures in this metric. Typically, such monitoring tasks are the purview of the observability system.

Another area that requires attention is the lack of a centralized metrics storage system. This omission complicates autoscaling based on service load. The current setup would require polling each server behind the load balancer, aggregating the data, and then determining the scaling direction (either scaling up or down). It's worth noting that many cloud providers offer integrated metric and observability solutions to streamline these processes.