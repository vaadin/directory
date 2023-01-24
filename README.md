# Vaadin Directory project

Visit at [vaadin.com/directory-beta/](https://vaadin.com/directory-beta/)

This project is a public beta version and the subsequent development iteration of the [Vaadin Directory](https://vaadin.com/directory). It is still missing many features, but the project shares the data with the production system, and you can use it to search for and install add-ons. It is here for your feedback and ideas -  please report issues, create pull requests, or propose new features at https://github.com/vaadin/directory/issues. Also, positive feedback is welcome :)

Vaadin Directory is the place to share Vaadin add-ons: Sophisticated client-server Java and Web Components compatible with [Vaadin Flow](https://github.com/vaadin/flow) and [Vaadin Framework](https://github.com/vaadin/framework). 

Read more about Vaadin at [vaadin.com/developers](https://vaadin.com/developers) and how to publish your own Vaadin Add-ons at [vaadin.com/directory/help](https://vaadin.com/directory/help).

## Building and running

_(Note: This project depends on separate backend project, and before byilding you should have installed that.)_

Vaadin Directory itself is built with [Hilla](https://hill.dev/) (previously known as Vaadin Fusion) with Java 17 and Lit. 

Build and run development mode using Maven:
```
mvn install
```

Build Docker image:
```
export DOCKER_BUILDKIT=0
docker build --progress plain -t vaadin/directory . > docker.build.log
```

Run Docker image:
```
docker run -p 8080:8080 vaadin/directory
```

Application is running at [localhost:8080](http://localhost:8080).
