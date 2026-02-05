#!/bin/bash

# Garage 초기화 스크립트
# Docker Compose 실행 후 한 번만 실행하면 됩니다.

echo "Waiting for Garage to start..."
sleep 5

# 노드 상태 확인
echo "Checking Garage status..."
docker exec meezy-garage /garage status

# 노드 ID 가져오기
NODE_ID=$(docker exec meezy-garage /garage node id -q 2>/dev/null | head -1)
echo "Node ID: $NODE_ID"

# 클러스터 레이아웃 설정
echo "Configuring cluster layout..."
docker exec meezy-garage /garage layout assign -z dc1 -c 1G "$NODE_ID"

# 레이아웃 적용
echo "Applying layout..."
docker exec meezy-garage /garage layout apply --version 1

# 버킷 생성
echo "Creating bucket..."
docker exec meezy-garage /garage bucket create meezy-bucket

# API 키 생성
echo "Creating API key..."
docker exec meezy-garage /garage key create meezy-app-key

# 키 정보 가져오기
echo ""
echo "=========================================="
echo "API Key Information (save these values!):"
echo "=========================================="
docker exec meezy-garage /garage key info meezy-app-key

# 버킷에 키 권한 부여
echo ""
echo "Granting permissions..."
docker exec meezy-garage /garage bucket allow --read --write --owner meezy-bucket --key meezy-app-key

echo ""
echo "=========================================="
echo "Garage setup complete!"
echo "=========================================="
echo ""
echo "Update your .env file with the following:"
echo "  GARAGE_ACCESS_KEY=<GaragekeyId from above>"
echo "  GARAGE_SECRET_KEY=<GarageSecretKey from above>"
echo "  BUCKET_NAME=meezy-bucket"
