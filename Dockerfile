FROM        adoptopenjdk/openjdk12:x86_64-alpine-jdk-12.0.2_10-slim

RUN         apk add --no-cache bash git nodejs

# Install sbt
RUN         apk add --no-cache --virtual=build-dependencies
RUN         wget -O /usr/local/bin/sbt https://git.io/sbt && chmod 0755 /usr/local/bin/sbt
RUN         apk del build-dependencies

# Cache sbt
RUN         mkdir -p \
              ./cache/project/ \
              ./cache/src/main/scala/ \
              ./cache/src/test/scala/
ADD         ./project/build.properties ./cache/project/
RUN         cd ./cache/ && sbt -v exit

# Cache scala
ADD         ./scalaVersion.sbt ./cache/
RUN         echo "class App" > ./cache/src/main/scala/App.scala
RUN         cd ./cache/ && sbt -v +compile

# Cache plugins
ADD         ./project/plugins.sbt ./cache/project/
RUN         cd ./cache/ && sbt -v +compile

# Cache dependencies
ADD         ./project ./cache/project/
ADD         ./build.sbt ./cache/
RUN         echo "class Test" > ./cache/src/test/scala/Test.scala
RUN         cd ./cache/ && sbt -v +compile

# Clean cache
RUN         rm -r ./cache/

WORKDIR     /home/flog/
