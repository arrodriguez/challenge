#!/bin/bash

FAT_JAR='/app/blocklist-1.0-SNAPSHOT.jar'
CONFIG='/app/config.yml'

if [ -n "$1" ] && [ -n "$2" ]; then
    FAT_JAR="$1"
    CONFIG="$2"
fi

start_blocklist_service() {
    local fatjar="$FAT_JAR"
    local config="$CONFIG"

    java -jar "$fatjar" server "$config" &

    TARGET_PID=$!
}

load_ipblocklist_dataset() {
    local eventId="$1"
    local eventcmd=""

    if [ -n "$eventId" ]; then 
       eventcmd="--event-id $eventId"
    fi

    command="java -jar $FAT_JAR monitor $CONFIG $eventcmd"

    result=$($command)

    updated=$(echo "$result" | jq -r '.updated')
    eventId=$(echo "$result" | jq -r '.eventId')

    if [ "$updated" = "true" ]; then
        curl -XPUT 'http://localhost:8080/blocklist/ips:reload'
    fi

    echo "$eventId"
}

eventId="$(load_ipblocklist_dataset)"

start_blocklist_service
trap "kill $TARGET_PID 2>/dev/null" EXIT

while true; do 
    sleep 5m
    eventId="$(load_ipblocklist_dataset $eventId)"
done