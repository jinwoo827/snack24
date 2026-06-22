# build

# APP[catalog, identity, order, billing, notification-worker]
./gradlew :service:catalog:bootJar
docker build -t snack24/catalog:latest service/identity


./gradlew :service:identity:bootJar
docker build -t snack24/identity:v1.0.1 service/identity
k3d image import snack24/identity:v1.0.1 -c snack24
kubectl set image -n snack24 deploy/identity identity=snack24/identity:v1.0.1

./gradlew :service:order:bootJar
docker build -t snack24/order:v1.0.1 service/order
k3d image import snack24/order:v1.0.1 -c snack24
kubectl set image -n snack24 deploy/order order=snack24/order:v1.0.1

./gradlew :service:billing:bootJar
docker build -t snack24/billing:v1.0.1 service/billing
k3d image import snack24/billing:v1.0.1 -c snack24
kubectl set image -n snack24 deploy/billing billing=snack24/billing:v1.0.1

./gradlew :service:notification-worker:bootJar
docker build -t snack24/notification-worker:v1.0.1 service/notification-worker
k3d image import snack24/notification-worker:v1.0.1 -c snack24
kubectl set image -n snack24 deploy/notification-worker notification-worker=snack24/notification-worker:v1.0.1