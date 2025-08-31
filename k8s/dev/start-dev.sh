#!/bin/bash

# --- 스크립트 시작 ---

echo "🚀 Starting MSA development environment on Kubernetes..."

# 1. 모든 Kubernetes 설정 파일 적용
#    -f . : 현재 폴더(k8s)를 기준으로
#    -R   : 하위 폴더(monitoring, gateway 등)까지 모두 포함하여 적용
echo "Applying all Kubernetes configurations from subdirectories..."
kubectl apply -f . -R

# 잠시 기다려서 Pod들이 생성될 시간을 줍니다.
echo "Waiting for resources to be created..."
sleep 5

# 2. Port Forwarding을 위한 함수 정의
start_port_forward() {
    # $1: 네임스페이스, $2: 서비스 이름, $3: 포트 매핑
    echo "  -> Starting port-forward for $2 on port $3"
    kubectl port-forward -n "$1" "svc/$2" "$3" > /dev/null 2>&1 &
    # 백그라운드에서 실행된 프로세스의 ID를 저장
    PIDS+=($!)
}

# 3. Port Forwarding 종료를 위한 함수 정의
cleanup() {
    echo -e "\n\n🛑 Stopping all port-forwarding processes..."
    for PID in "${PIDS[@]}"; do
        kill "$PID"
    done
    echo "✅ Cleanup complete."
}

# 스크립트가 Ctrl+C로 종료될 때 cleanup 함수를 실행하도록 설정
trap cleanup SIGINT SIGTERM

# 4. Port Forwarding 실행
echo -e "\n🔥 Starting all port-forwards in the background..."
PIDS=() # 프로세스 ID를 저장할 배열 초기화

# --- 여기에 포트 포워딩할 서비스들을 추가/수정하면 됩니다 ---
start_port_forward "default" "mysql-service" "3306:3306"
start_port_forward "default" "gateway-service" "8000:8000"
start_port_forward "default" "user-service" "8090:8090"
start_port_forward "default" "order-service" "8085:8085"
start_port_forward "default" "catalog-service" "8095:8095"
start_port_forward "default" "prometheus-service" "9090:9090"
start_port_forward "default" "grafana-service" "3000:3000"
start_port_forward "default" "zipkin-service" "9411:9411"
start_port_forward "default" "kafka-connect-service" "8083:8083"
start_port_forward "default" "kafka-ui-service" "8080:8080"
start_port_forward "kubernetes-dashboard" "kubernetes-dashboard" "8443:443"

echo -e "\n🎉 All services are now accessible from localhost!"
echo "   - Gateway: http://localhost:8000/actuator/info"
echo "   - User Service (Direct): http://localhost:8090/actuator/info"
echo "   - Order Service (Direct): http://localhost:8085/actuator/info"
echo "   - Catalog Service (Direct): http://localhost:8095/actuator/info"
echo "   - Prometheus: http://localhost:9090"
echo "   - Grafana: http://localhost:3000"
echo "   - Zipkin: http://localhost:9411"
echo "   - Kafka-ui: http://localhost:8080"
echo "   - Dashboard: https://localhost:8443"
echo -e "\n(Press Ctrl+C to stop all port-forwarding processes)"

# 백그라운드 프로세스들이 종료될 때까지 대기
wait