FROM docker.lezhin.com/java:8u121b13
HEALTHCHECK --interval=10s CMD wget http://127.0.0.1:8080/_/status -qO /dev/null

COPY panther.jar /opt/webapps/
CMD java -server $JAVA_OPTS -jar /opt/webapps/panther.jar
