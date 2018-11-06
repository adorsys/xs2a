[<--- Back to table of contents](README.md)

Introduction and Goals
======================
This implementation provides a REST-interface and corresponding services, that could be operated as a proxy to 
underlying ASPSP-Systems,
providing capabilities to interoperate with TPP by defined XS2A Standard Interface of Berlin Group.


[//]: # (Requirements Overview)
[//]: # (---------------------)

Quality Goals
-------------

| ID    | Prio | Goal                   | Description                                                                                                                                                                                                                                                                     |
|-------|------|------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| QG-01 |   1  | Functional Compliance  | The system should comply (in interface and behaviour) with XS2A Specifications designed and published by Berlin Group. |
| QG-02 |   1  | Adaptability           | The system should be able to perform in the technical environment of ASPSP, regardless of its topology and HW/SW used. This especially concerns database solutions.|
| QG-03 |   2  | Security               | The system should be functional under strict security restrictions provided by ASPSP to environment, deployment and data storage.                                                                                                                                      |
| QG-04 |   3  | Scalability            | Depending on size and number of customers of ASPSP the system environment should be able to scale deployment to satisfy quantitive metric of number of requests (here meant complete use-case, which may contain several technical requests) per user per hour                  |

[//]: # (Stakeholders)
[//]: # (------------)

[//]: # (| Role/Name        | Contact                   | Expectations              |)
[//]: # (|------------------|---------------------------|---------------------------|)
[//]: # (| *&lt;Role-1&gt;* | *&lt;Contact-1&gt;*       | *&lt;Expectation-1&gt;*   |)
[//]: # (| *&lt;Role-2&gt;* | *&lt;Contact-2&gt;*       | *&lt;Expectation-2&gt;*   |)


[//]: # (Architecture Constraints)
[//]: # (========================)

[<--- Back to table of contents](README.md)
