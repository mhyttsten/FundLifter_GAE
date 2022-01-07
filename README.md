


# App Engine

### GCP Deployment
From pom.xml folder: $ mvn clean package appengine:deploy
From WEB-INF folder: $ gcloud app deploy cron.yaml

### Local Deployment
See: https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/appengine-java11/helloworld-servlet
cd appengine-simple-jetty-main
mvn install

### app.xml vs app-engine.xml
app.xml is used for flexible environment
app-engine.xml is used for standard
However, app.xml states how to run local so maybe relevant
It seems like both can coexist, i.e. I can specify the instance class in app-engine.xml that works

### Other things

