# LibraryGames

LibraryGames is an online platform for various board games
that could be found in a library.

## Authors

Created by Patrick Kenney and Syed Quadri

## Tools

### [asdf](https://asdf-vm.com)

Provides a declarative set of tools pinned to
specific versions for environmental consistency.

These tools are defined in `.tool-versions`.
Run `make dependencies` to initialize a new environment.

### [pre-commit](https://pre-commit.com)

A left shifting tool to consistently run a set of checks on the code repo.
Our checks enforce syntax validations and formatting.
We encourage contributors to use pre-commit hooks.

```shell
# install all pre-commit hooks
make hooks

# run pre-commit on repo once
make pre-commit
```

### [helm](https://helm.sh/)

Tool for deploying on `kubernetes`.
Run `make helm-dependencies` to initialize a new environment.

## Usage

### Building

To build the latest docker image run `make image`.

### Server

To launch the server on `minikube` run:

```shell
# if minikube is not already running
make start-minikube
# if a new image was created
make load
# actually deploy the server
make deploy
# uninstall the deployment
make uninstall
# stop the minikube profile
make stop-minikube
# delete the minikube profile
make delete-minikube
```

### Client

To run a client locally run:

```shell
# if on linux
make run
# if on WSL2 with Windows 11
make run-wsl
```
