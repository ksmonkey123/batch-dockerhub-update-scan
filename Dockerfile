FROM openjdk:17-alpine
COPY target/batch_dockerhub_update_scan.jar batch_dockerhub_update_scan.jar
ENTRYPOINT ["java", "-jar", "batch_dockerhub_update_scan.jar"]