# base image
FROM java:7

# that's me!
MAINTAINER zzg, zhange.zzg@gmail.com

WORKDIR /

# run the (java) server as the daemon user
USER daemon

# Here the stuff that we're going to place into the image
ADD target/scala-2.11/aggregated_search-assembly-1.0.jar /aggregated_search/server.jar

# entry jar to be run in a container
ENTRYPOINT ["java", "-jar", "/aggregated_search/server.jar"]

# HTTP port
EXPOSE 8080
# b681197cf2ac