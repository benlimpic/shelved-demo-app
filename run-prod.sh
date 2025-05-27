#!/bin/bash
echo "Running in PROD profile..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# ./run-prod.sh