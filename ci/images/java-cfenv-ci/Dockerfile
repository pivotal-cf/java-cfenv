FROM harbor-repo.vmware.com/dockerhub-proxy-cache/library/ubuntu:bionic

RUN apt-get update && apt-get install --no-install-recommends -y ca-certificates net-tools git curl
RUN rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /opt/openjdk
ENV PATH $JAVA_HOME/bin:$PATH
RUN mkdir -p /opt/openjdk && \
    cd /opt/openjdk && \
    curl -L https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u292-b10/OpenJDK8U-jdk_x64_linux_hotspot_8u292b10.tar.gz | tar xz --strip-components=1

ADD https://raw.githubusercontent.com/spring-io/concourse-java-scripts/v0.0.4/concourse-java.sh /opt/
ADD https://repo.spring.io/libs-release/io/spring/concourse/releasescripts/concourse-release-scripts/0.3.3/concourse-release-scripts-0.3.3.jar /opt/

