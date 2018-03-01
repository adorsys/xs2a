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
* Javadocs for public methods are written (including parameter description)

### Contributing
Any person are free to join us by implementing some parts of code or fixing some bugs and making a merge requests for them.
The conditions listed in the Definition of Done are required to be fulfilled in any case.
