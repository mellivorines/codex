FROM azul/zulu-openjdk-alpine:17-jre-headless-latest

MAINTAINER mellivorines<lilinxi015@163.com>

COPY build/libs/codex-0.0.1.jar /codex-0.0.1.jar
ENTRYPOINT ["java", "-jar", "/codex-0.0.1.jar"]

EXPOSE 8888
EXPOSE 3306