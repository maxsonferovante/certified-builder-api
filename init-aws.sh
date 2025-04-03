#!bin/sh

echo "Init LocalStack"


awslocal sqs create-queue --queue-name builder.fifo --attributes FifoQueue=true
awslocal sqs create-queue --queue-name notification_generation.fifo --attributes FifoQueue=true
awslocal s3api create-bucket --bucket maal-upload-dev