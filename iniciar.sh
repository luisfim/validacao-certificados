#!/bin/sh
set -eu
cd "$(dirname "$0")"
if [ -f target/validacao-certificados.jar ]; then
  exec java -jar target/validacao-certificados.jar
elif [ -f bin/validacao-certificados.jar ]; then
  exec java -jar bin/validacao-certificados.jar
else
  sh ./mvnw -q -DskipTests package
  exec java -jar target/validacao-certificados.jar
fi
