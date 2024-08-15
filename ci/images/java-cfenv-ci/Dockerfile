FROM public.ecr.aws/docker/library/ubuntu:bionic

RUN apt-get update && apt-get install --no-install-recommends -y ca-certificates net-tools git curl jq
RUN rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/opt/openjdk
ENV PATH=$JAVAHOME/bin:$PATH
RUN mkdir -p /opt/openjdk && \
    cd /opt/openjdk && \
    curl -L https://download.bell-sw.com/java/17.0.12+10/bellsoft-jdk17.0.12+10-linux-amd64.tar.gz | tar xz --strip-components=1

ADD https://raw.githubusercontent.com/spring-io/concourse-java-scripts/v0.0.4/concourse-java.sh /opt/

