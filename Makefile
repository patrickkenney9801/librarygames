SHELL := /bin/bash

VERSION := $(shell sed -n -e 's:.*<version>\(.*\)</version>.*:\1:p' pom.xml | head -1)

IMAGE_NAME := librarygames
SERVER_IMAGE_NAME := librarygames-server
PROFILE := librarygames
NAMESPACE := librarygames
RELEASE_NAME := librarygames

define node_ip
$(shell kubectl --context ${PROFILE} get nodes --namespace ${NAMESPACE} -o jsonpath="{.items[0].status.addresses[0].address}")
endef

define node_port
$(shell kubectl --context ${PROFILE} get --namespace ${NAMESPACE} -o jsonpath="{.spec.ports[0].nodePort}" services ${RELEASE_NAME})
endef

define grafana_ip
$(shell kubectl --context ${PROFILE} get nodes --namespace observability -o jsonpath="{.items[0].status.addresses[0].address}")
endef

define grafana_port
$(shell kubectl --context ${PROFILE} get --namespace grafana -o jsonpath="{.spec.ports[0].nodePort}" services grafana)
endef

define sonar_ip
$(shell kubectl --context ${PROFILE} get nodes --namespace sonarqube -o jsonpath="{.items[0].status.addresses[0].address}")
endef

define sonar_port
$(shell kubectl --context ${PROFILE} get --namespace sonarqube -o jsonpath="{.spec.ports[0].nodePort}" services sonarqube-sonarqube)
endef

image-client:
	@docker build --build-arg VERSION=${VERSION} -t ${IMAGE_NAME}:${VERSION} --file client.Dockerfile .

image-server:
	@docker build --build-arg VERSION=${VERSION} -t ${SERVER_IMAGE_NAME}:${VERSION} --file server.Dockerfile .

image-test-server:
	@docker build --build-arg VERSION=${VERSION} --build-arg SONAR_ADDRESS=http://$(call sonar_ip):$(call sonar_port) -t ${SERVER_IMAGE_NAME}-test:${VERSION} --file server.Dockerfile --target go-test .
	@docker run --rm --net=host ${SERVER_IMAGE_NAME}-test:${VERSION}

image-client-debug:
	@docker build --build-arg VERSION=${VERSION} -t ${IMAGE_NAME}-debug:${VERSION} --file client.Dockerfile --target mvn-build .

test-server:
	@cd server; \
	go test -v ./...

proto:
	@protoc --proto_path=pbs pbs/*.proto --go_out=server/internal/pbs --go-grpc_out=server/internal/pbs

run:
	@docker run --rm --net=host -e LIBRARY_GAMES_SERVER_ADDRESS=$(call node_ip):$(call node_port) ${IMAGE_NAME}:${VERSION}

run-wsl:
	@docker run --rm -v /tmp/.X11-unix:/tmp/.X11-unix -v /mnt/wslg:/mnt/wslg --net=host -e LIBRARY_GAMES_SERVER_ADDRESS=$(call node_ip):$(call node_port) ${IMAGE_NAME}:${VERSION}

run-offline:
	@docker run --rm -v /tmp/.X11-unix:/tmp/.X11-unix -v /mnt/wslg:/mnt/wslg --net=host ${IMAGE_NAME}:${VERSION}

clean:
	@docker image prune

start-minikube:
	@minikube -p ${PROFILE} start

stop-minikube:
	@minikube -p ${PROFILE} stop

delete-minikube:
	@minikube -p ${PROFILE} delete

observability-minikube:
	@helm --kube-context ${PROFILE} -n elasticsearch install --create-namespace elasticsearch elastic/elasticsearch --version=8.5.1 -f observability/elasticsearch_values.yaml --wait || true
	@helm --kube-context ${PROFILE} -n fluent install --create-namespace fluent-bit fluent/fluent-bit --version=0.29.0 -f observability/fluentbit_values.yaml --wait || true
	@helm --kube-context ${PROFILE} -n tempo install --create-namespace tempo grafana/tempo --version=1.0.0 -f observability/tempo_values.yaml --wait || true
	@helm --kube-context ${PROFILE} -n monitoring install --create-namespace kube-prometheus-stack prometheus/kube-prometheus-stack --version=45.23.0 -f observability/prometheus_values.yaml --wait || true
	@helm --kube-context ${PROFILE} -n grafana install --create-namespace grafana grafana/grafana --version=6.50.7 -f observability/grafana_values.yaml --wait || true
	@helm --kube-context ${PROFILE} -n sonarqube install --create-namespace sonarqube sonarqube/sonarqube --version=10.0.0 -f observability/sonarqube_values.yaml --wait || true

clean-observability-minikube:
	@helm --kube-context ${PROFILE} -n elasticsearch delete elasticsearch --wait
	@helm --kube-context ${PROFILE} -n fluent delete fluent-bit --wait
	@helm --kube-context ${PROFILE} -n tempo delete tempo grafana/tempo --wait
	@helm --kube-context ${PROFILE} -n monitoring delete kube-prometheus-stack --wait
	@helm --kube-context ${PROFILE} -n grafana delete grafana --wait

clean-fluent:
	@helm --kube-context ${PROFILE} -n fluent delete fluent-bit --wait

clean-grafana:
	@helm --kube-context ${PROFILE} -n grafana delete grafana --wait

clean-sonar:
	@helm --kube-context ${PROFILE} -n sonarqube delete sonarqube --wait

grafana:
	@echo http://$(call grafana_ip):$(call grafana_port)
	@python -mwebbrowser http://$(call grafana_ip):$(call grafana_port)

sonar:
	@echo http://$(call sonar_ip):$(call sonar_port)
	@python -mwebbrowser http://$(call sonar_ip):$(call sonar_port)

load:
	@minikube -p ${PROFILE} image load ${SERVER_IMAGE_NAME}:${VERSION}

metrics:
	@echo $(call node_ip):$(call node_port)
	@curl -s $(call node_ip):$(call node_port)/metrics

deploy:
	@helm --kube-context ${PROFILE} -n ${NAMESPACE} install --create-namespace ${RELEASE_NAME} charts/librarygames -f observability/librarygames_values.yaml --set=librarygames.image.tag=${VERSION}

uninstall:
	@helm --kube-context ${PROFILE} -n ${NAMESPACE} delete ${RELEASE_NAME}

dependencies: dependencies-asdf dependencies-helm

dependencies-asdf:
	@echo "Updating asdf plugins..."
	@asdf plugin update --all >/dev/null 2>&1 || true
	@echo "Adding new asdf plugins..."
	@cut -d" " -f1 ./.tool-versions | xargs -I % asdf plugin-add % >/dev/null 2>&1 || true
	@echo "Installing asdf tools..."
	@cat ./.tool-versions | xargs -I{} bash -c 'asdf install {}'
	@echo "Updating local environment to use proper tool versions..."
	@cat ./.tool-versions | xargs -I{} bash -c 'asdf local {}'
	@asdf reshim
	@echo "Done!"

dependencies-helm:
	@helm repo add bitnami https://charts.bitnami.com/bitnami
	@helm repo add elastic https://helm.elastic.co
	@helm repo add fluent https://fluent.github.io/helm-charts
	@helm repo add grafana https://grafana.github.io/helm-charts
	@helm repo add prometheus https://prometheus-community.github.io/helm-charts
	@helm repo add sonarqube https://SonarSource.github.io/helm-chart-sonarqube
	@cd charts/librarygames; \
	helm dependencies update; \
	helm dependencies build; \

hooks:
	@pre-commit install --hook-type pre-commit
	@pre-commit install-hooks

pre-commit:
	@pre-commit run -a

dive:
	@dive ${IMAGE_NAME}:${VERSION} --ci
