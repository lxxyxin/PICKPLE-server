FROM amd64/amazoncorretto:17
WORKDIR /app
COPY ./build/libs/PickpleServer-0.0.1-SNAPSHOT.jar /app/PickpleServer.jar
CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=dev", "PickpleServer.jar"]
