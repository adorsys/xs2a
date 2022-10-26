## Development and contributing

### Internal development process
Development is performed by 2-weeks sprints (We use kind of Scrum framework)
We use [Git flow](http://nvie.com/posts/a-successful-git-branching-model/) for development. 
Only repository masters (currently: [Denys Golubiev](https://github.com/DG0lden) and 
[Alexander Geist](https://github.com/tadschik))
are allowed to accept merge request to develop branch and make releases.

### Definition of Ready
The task is ready to be put into a sprint when all following conditions are met:
* All dependencies are clear and the work to work with them are clarified
* Use-case is defined in the task
* Acceptance criteria are defined

### Definition of Done
The Task could be accepted only when following requirements are met:
* Code is reviewed (and approved) by another developer
* API documentation in Swagger UI corresponds to acceptance criteria
* At least one automated test for every Use-case exists
* Project documentation (Markdown files) contains the information how to run the demo of use case
* Javadocs for public methods are written (including parameter description). 
  For REST interfaces Swagger-annotations are sufficient.

### Contributing
Any person are free to join us by implementing some parts of code or fixing some bugs and making a merge requests for them.
The conditions listed in the Definition of Done are required to be fulfilled in any case.

### Technical conditions for the implementations

#### Testing API with Postman json collections
 
 For testing API of XS2A [Postman](https://www.getpostman.com/) tool can be used.
 Environment jsons with global parameter’s sets and Collections of jsons for imitation of processes flows are stored in /scripts/tests/postman folder.
 To import Postman collections and environments follow next steps:
 1.     Download Postman jsons with collections and environments to your local machine.
 2.     Open Postman, press button “Import”.
 3.     Choose “Import file” to import one json or “Import folder” to import all jsons within the folder, then press button “Choose Files” or “Choose Folders” and open necessary files/folders.
 4.     To change settings of environments - go to “Manage Environments”, press the environment name and change variables.
 
 To start testing with Postman collections it is necessary to have all services running.
 
#### Code styling
If you are using Intellij IDEs, like we do, please consider importing our code-style settings.
General settings are also documented in .editorconfig file.
You may find more information and a plugin for your editor/IDE [here](http://editorconfig.org/))

#### Java
* Please use Optionals and corresponding streams instead of null-checks where possible
* We prefer using Mockito over EasyMock for unit-tests.
* We prefer SpringBoot autoconfiguration over manual Configuration where possible

### How to Release

Release is being done using the [release scripts](https://github.com/borisskert/release-scripts) ( (C) by [Boris Skert](https://github.com/borisskert) ) located under scripts/release-scripts.
For detailed info see [README for release-scripts](../scripts/release-scripts/README.md).

#### Steps to make a release

**Release is made from local copy! Ensure that you have enough rights to push to master and develop branches**
```bash
$ git submodule update --init --remote
$ scripts/release-scripts/release.sh <release-version> <next-develop-version>
``` 
Example
```bash
$ scripts/release-scripts/release.sh 1.0 1.1
```
