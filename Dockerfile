FROM centos:centos7
MAINTAINER Bishal Paul <bishal.paul@go-mmt.com>
RUN  yum install -y epel-release
RUN  yum update -y && yum install -y \
  git \
  unzip \
  tar \
  wget \
  python \
  python-devel \
  python-setuptools \
  crontabs
RUN easy_install supervisor
ENV SCALA_VERSION 2.11.7
ENV SBT_VERSION 0.13.9
COPY sbt.sh /usr/local/bin/sbt
#install Java
RUN yum update -y && \
yum install -y wget && \
yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel && \
yum clean all
# Install Scala
RUN set -x \
    && mkdir -p /usr/src/scala \
    && curl -SL "http://downloads.typesafe.com/scala/2.11.7/scala-${SCALA_VERSION%%[a-z]*}.tgz" -o scala.tgz \
    # && curl -SL "https://github.com/scala/scala/archive/v${SCALA_VERSION%%[a-z]*}.tar.gz" -o scala.tar.gz \
    && tar -xC /usr/src/scala --strip-components=1 -f scala.tgz \
    && rm scala.tgz* \
    && mv /usr/src/scala /usr/lib/
# make symlinks that are expected
RUN ln -s /usr/lib/scala/bin/fsc /usr/local/bin \
    && ln -s /usr/lib/scala/bin/scala /usr/local/bin \
    && ln -s /usr/lib/scala/bin/scalac /usr/local/bin \
    && ln -s /usr/lib/scala/bin/scaladoc /usr/local/bin \
    && ln -s /usr/lib/scala/bin/scalap /usr/local/bin
# install SBT
RUN mkdir -p /usr/src/scala-sbt \
    && curl -SL "https://dl.bintray.com/sbt/native-packages/sbt/$SBT_VERSION/sbt-$SBT_VERSION.tgz" -o scala-sbt.tgz \
    && tar -xC /usr/src/scala-sbt --strip-components=1 -f scala-sbt.tgz \
    && rm scala-sbt.tgz* \
    && chmod u+x /usr/local/bin/sbt \
    && ln -s /usr/src/scala-sbt/bin/sbt-launch.jar /usr/local/bin
RUN mkdir -p /logs \
             /virtualSegments \
             /var/log/supervisor \
             /etc/supervisord.d

RUN touch /logs/DynamoDBStreams.log
RUN touch /logs/supervisord.log
RUN touch /logs/metrics.log


# Supervidord services
COPY services/supervisord /etc/rc.d/init.d/supervisord
ADD services/configs/* /etc/supervisord.d/
RUN chmod 755 /etc/rc.d/init.d/supervisord

COPY ./ /DynamoDBStreams/
WORKDIR /DynamoDBStreams/
RUN sbt assembly