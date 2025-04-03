#!bin/sh

echo "Init LocalStack"


awslocal sqs create-queue --queue-name builder.fifo
awslocal sqs create-queue --queue-name notification_generation.fifo
awslocal s3api create-bucket --bucket maal-upload-dev