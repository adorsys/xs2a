# Project Title

Implementation of PSD2 XS2A Interface of Berlin Group 

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

```
- Java JDK version 1.8.x
- Docker
- IDP-Server (we recommend Keycloak)
```

### Installing

A step by step series of examples that tell you have to get a development env running

Say what the step will be

```
Give the example
```

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Java, version 1.8](http://java.oracle.com) - The main language of implementation
* [Maven, version 3.0](https://maven.apache.org/) - Dependency Management
* [Spring Boot](https://projects.spring.io/spring-boot/) - Spring boot as core Java framework

## Development and contributing

Please read [CONTRIBUTING](doc/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning and Releasing

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags).

Release is being done using the [release scripts](https://github.com/borisskert/release-scripts) ( (C) by [Boris Skert](https://github.com/borisskert) ) located under scripts/release-scripts.
For detailed info see [README for release-scripts](scripts/release-scripts/README.md).

### Steps to make a release

**Release is made from local copy! Ensure that you have enough rights to push to master and develop branches**
```bash
$ git submodule update
$ scripts/release-scripts/release.sh <release-version> <next-develop-version>
``` 
Example
```bash
$ scripts/release-scripts/release.sh 1.0 1.1
```
 

## Authors

* **[Francis Pouchata](mailto:fpo@adorsys.de)** - *Initial work* - [adorsys](https://www.adorsys.de)

See also the list of [contributors](doc/contributors.md) who participated in this project.

## License

This project is licensed under the Apache License version 2.0 - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone who's code was used
* Inspiration
* etc
