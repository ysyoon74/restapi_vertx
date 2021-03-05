FROM ysyoon74/ubuntu1804_openjdk8

LABEL maintainer="ysyun@saltlux.com"

USER root

RUN mkdir /app

COPY ./jvm.options /app/
COPY ./logback.xml /app/
COPY ./service.sh /app/

WORKDIR /app/

RUN chmod a+x ./service.sh

EXPOSE 8080

CMD [ "./service.sh", "console" ]