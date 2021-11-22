FROM openjdk:11

RUN sh -c 'apt-get -y update && apt-get -y upgrade'
COPY err0agent-1.0.0-SNAPSHOT-fat.jar /root/
COPY err0.sh /usr/local/bin