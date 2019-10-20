#!/bin/bash
set -e
printhelp() {
    echo "Usage:"
    echo "  efk up [service]    - to init and start elasticsearch, kibana and filebeat or fluentbit containers"
    echo "  efk down            - to destroy elasticsearch, kibana, filebeat, fluentbit containers and all resources attached to theirs"
    echo "  efk stop [service]  - to stop elasticsearch, kibana, filebeat, fluentbit containers and all resources attached to theirs"
    echo "  efk ps              - to list containers"
    echo "  efk logs            - to view output from containers"
}

APPDIR="$(cd "$(dirname "$0")"; "pwd")"
SERVICE="$2"

case $1 in
    up)
        if [[ -z "$SERVICE" ]]; then
            SERVICE="kibana"
        fi
        pushd $APPDIR || exit
        docker-compose -f "$APPDIR"/docker/docker-compose.yml up -d $SERVICE
        popd || exit
    ;;
    down)
        pushd $APPDIR || exit
        docker-compose -f "$APPDIR"/docker/docker-compose.yml down -v
        popd || exit
    ;;
    stop)
        pushd $APPDIR || exit
        docker-compose -f "$APPDIR"/docker/docker-compose.yml stop $SERVICE
        popd || exit
    ;;
    ps)
        pushd $APPDIR || exit
        docker-compose -f "$APPDIR"/docker/docker-compose.yml ps
        popd || exit
    ;;
    top)
        pushd $APPDIR || exit
        docker-compose -f "$APPDIR"/docker/docker-compose.yml top
        popd || exit
    ;;
    log|logs)
        pushd $APPDIR || exit
        docker-compose -f "$APPDIR"/docker/docker-compose.yml logs -f
        popd || exit
    ;;
    -h|--help)
        printhelp
    ;;
    *)
        echo "Unknown argument: $1"
        echo ""
        printhelp
        exit 1
    ;;
esac
