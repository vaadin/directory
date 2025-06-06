# Vaadin Directory project

Visit at [vaadin.com/directory/](https://vaadin.com/directory/)

The Vaadin Directory is the place to share Vaadin add-ons: Sophisticated client-server Java and Web Components compatible with [Vaadin Flow](https://github.com/vaadin/flow) and [Vaadin Framework](https://github.com/vaadin/framework). 

Read more about Vaadin at [vaadin.com/flow](https://vaadin.com/flow) and how to publish your own Vaadin Add-ons at [vaadin.com/directory-help](https://vaadin.com/directory-help).

## Building and running

While the admin UI based on [Vaadin](https://vaadin.com/flow), the search is built with [Hilla](https://vaadin.com/hilla/) (previously known as Vaadin Fusion) with Java 17 and Lit. 

_(Note: This project depends on a separate backend project, and before building you should have installed that.)_

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
