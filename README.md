# Vaadin Directory
The upcoming open source version of Vaadin Directory

## Building and running

Build and run development mode using Maven:
```
mvn
```

Build Docker image:
```
docker build -t vaadin/directory .
```

Run Docker image:
```
docker run -p 8080:8080 vaadin/directory
```

Application is running at [localhost:8080](http://localhost:8080).