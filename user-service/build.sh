#!/bin/bash

set -e

SERVICE_NAME="user-service"
VERSION="v1"
DOCKERFILE_PATH="user-service/Dockerfile"
IMAGE_FULL_NAME="${SERVICE_NAME}:${VERSION}"

echo "Starting build for ${IMAGE_FULL_NAME}..."

docker build \
  --progress=plain \
  -t "${IMAGE_FULL_NAME}" \
  -f "${DOCKERFILE_PATH}" \
  .

echo "Build complete: ${IMAGE_FULL_NAME}"

# add kind integration after tests
kind load docker-image "${IMAGE_FULL_NAME}" --name kind