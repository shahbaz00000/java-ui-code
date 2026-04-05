FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY main.java .

RUN javac main.java

EXPOSE 8080

CMD ["java","main"]
