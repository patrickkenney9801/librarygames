LibraryGames is an online platform for various board games that could be found in a library.

Created by Patrick Kenney and Syed Quadri

Build `docker` image via `docker build -t librarygames:<version> .`

On WSL2 run the `docker` client via `docker run --rm -v /tmp/.X11-unix:/tmp/.X11-unix -v /mnt/wslg:/mnt/wslg [--net=host] -e LIBRARY_GAMES_SERVER_ADDRESS=$MINIKUBE_IP librarygames:<version>`.

For `minikube`'s ingress addon `kubectl patch configmap udp-services -n ingress-nginx --patch '{"data":{"19602":"librarygames/librarygames:19602"}}'`
And `kubectl patch deployment ingress-nginx-controller --patch "$(cat nginx-patch.yaml)" -n ingress-nginx`.
