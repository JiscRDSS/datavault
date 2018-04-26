#!/bin/bash

echo "Waiting for RabbitMq"

while [ `curl -s -o /dev/null -w "%{http_code}" http://${RABBITMQ_HOST}:15672/` -ne 200 ]; do
  echo "."
  sleep 1
done

echo "RabbitMq is ready!"
