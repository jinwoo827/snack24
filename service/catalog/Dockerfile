FROM amazoncorretto:21-alpine
WORKDIR /app
COPY build/libs/app.jar app.jar
EXPOSE 8002
ENTRYPOINT ["java", "-jar", "/app/app.jar"]