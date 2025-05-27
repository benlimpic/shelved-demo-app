#!/bin/bash
echo "Running in DEV profile..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# ./run-dev.sh