#!/bin/bash

# 设置镜像名称和版本
IMAGE_NAME="dcloud-aipan"
IMAGE_VERSION="latest"
CONTAINER_NAME="dcloud-aipan"

# 打印开始信息
echo "开始部署 ${IMAGE_NAME}:${IMAGE_VERSION}..."

# 停止并删除旧容器
echo "停止并删除旧容器..."
docker stop ${CONTAINER_NAME} 2>/dev/null || true
docker rm ${CONTAINER_NAME} 2>/dev/null || true

# 删除旧镜像
echo "删除旧镜像..."
docker rmi ${IMAGE_NAME}:${IMAGE_VERSION} 2>/dev/null || true

# 构建新镜像
echo "构建新镜像..."
docker build -t ${IMAGE_NAME}:${IMAGE_VERSION} .

# 运行新容器
echo "运行新容器..."
docker run -d \
    --name ${CONTAINER_NAME} \
    -p 8080:8080 \
    -v /etc/localtime:/etc/localtime:ro \
    --restart unless-stopped \
    ${IMAGE_NAME}:${IMAGE_VERSION}

# 等待容器启动
echo "等待容器启动..."
sleep 5

# 检查容器状态
if docker ps | grep -q ${CONTAINER_NAME}; then
    echo "容器已成功启动！"
    echo "容器ID: $(docker ps -q --filter name=${CONTAINER_NAME})"
    echo "容器日志:"
    docker logs --tail=20 ${CONTAINER_NAME}
else
    echo "容器启动失败，请检查日志！"
    docker logs ${CONTAINER_NAME}
    exit 1
fi

echo "部署完成！" 