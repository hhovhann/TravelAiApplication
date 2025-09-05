#!/bin/bash

echo "Stopping Travel AI Application services..."

if [ -f logs/pids.txt ]; then
    while read pid; do
        if ps -p $pid > /dev/null; then
            echo "Stopping process $pid"
            kill $pid
        fi
    done < logs/pids.txt
    rm logs/pids.txt
else
    echo "No PID file found. Trying to stop by port..."

    # Kill processes by port
    lsof -ti:8080 | xargs kill -9 2>/dev/null || true
    lsof -ti:8081 | xargs kill -9 2>/dev/null || true
    lsof -ti:8082 | xargs kill -9 2>/dev/null || true
    lsof -ti:8083 | xargs kill -9 2>/dev/null || true
    lsof -ti:9000 | xargs kill -9 2>/dev/null || true
fi

echo "All services stopped."
