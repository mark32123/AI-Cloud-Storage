#!/bin/bash

# 创建日志目录
mkdir -p logs

# 构建镜像
docker build -t nginx-proxy:1.24.0 .

# 停止并删除旧容器（如果存在）
docker stop nginx-proxy 2>/dev/null || true
docker rm nginx-proxy 2>/dev/null || true

# 运行容器
docker run -d \
    --name nginx-proxy \
    -p 80:80 \
    -v $(pwd)/nginx.conf:/etc/nginx/nginx.conf:ro \
    -v $(pwd)/logs:/var/log/nginx \
    --restart unless-stopped \
    nginx-proxy:1.24.0

# 显示容器日志
docker logs -f nginx-proxy 