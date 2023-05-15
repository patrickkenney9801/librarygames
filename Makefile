SHELL := /bin/bash

VERSION := $(shell sed -n -e 's:.*<version>\(.*\)</version>.*:\1:p' pom.xml | head -1)

IMAGE_NAME := librarygames
PROFILE := librarygames
NAMESPACE := librarygames
RELEASE_NAME := librarygames

define node_ip
$(shell kubectl --context ${PROFILE} get nodes --namespace ${NAMESPACE} -o jsonpath="{.items[0].status.addresses[0].address}")
endef

define node_port
$(shell kubectl --context ${PROFILE} get --namespace ${NAMESPACE} -o jsonpath="{.spec.ports[0].nodePort}" services ${RELEASE_NAME})
endef

image:
	@docker build --build-arg VERSION=${VERSION} -t ${IMAGE_NAME}:${VERSION} .

run:
	@docker run --rm --net=host -e LIBRARY_GAMES_SERVER_ADDRESS=$(call node_ip) -e LIBRARY_GAMES_SERVER_PORT=$(call node_port) ${IMAGE_NAME}:${VERSION}

run-wsl:
	@docker run --rm -v /tmp/.X11-unix:/tmp/.X11-unix -v /mnt/wslg:/mnt/wslg --net=host -e LIBRARY_GAMES_SERVER_ADDRESS=$(call node_ip) -e LIBRARY_GAMES_SERVER_PORT=$(call node_port) ${IMAGE_NAME}:${VERSION}

clean:
	@docker image prune

start-minikube:
	@minikube -p ${PROFILE} start

stop-minikube:
	@minikube -p ${PROFILE} stop

delete-minikube:
	@minikube -p ${PROFILE} delete

load:
	@minikube -p ${PROFILE} image load ${IMAGE_NAME}:${VERSION}

deploy:
	@helm --kube-context ${PROFILE} -n ${NAMESPACE} install --create-namespace ${RELEASE_NAME} charts/librarygames

uninstall:
	@helm --kube-context ${PROFILE} -n ${NAMESPACE} delete ${RELEASE_NAME}

dependencies: dependencies-asdf

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

hooks:
	@pre-commit install --hook-type pre-commit
	@pre-commit install-hooks

pre-commit:
	@pre-commit run -a
