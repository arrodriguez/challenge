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
    local versionID="$1"
    local versionCmd=""

    if [ -n "$versionID" ]; then
       versionCmd="--version-id $versionID"
    fi

    command="java -jar $FAT_JAR monitor $CONFIG $versionCmd"

    result=$($command)

    updated=$(echo "$result" | jq -r '.updated')
    versionID=$(echo "$result" | jq -r '.versionId')

    if [ "$updated" = "true" ]; then
        curl -XPUT 'http://localhost:8080/blocklist/ips:reload'
    fi

    echo "$versionID"
}

versionID="$(load_ipblocklist_dataset)"

start_blocklist_service
trap "kill $TARGET_PID 2>/dev/null" EXIT

while true; do
    # Sleep every 5minutes, this guarantee to NOT reach Github rate-limit
    sleep 5m
    versionID="$(load_ipblocklist_dataset $versionID)"
done