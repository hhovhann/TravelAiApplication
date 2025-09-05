#!/bin/bash

echo "Starting Travel AI Application..."
echo "Make sure you have set OPENAI_API_KEY environment variable"

if [ -z "$OPENAI_API_KEY" ]; then
    echo "ERROR: OPENAI_API_KEY environment variable is not set"
    echo "Please set it with: export OPENAI_API_KEY=your_api_key_here"
    exit 1
fi

# Build the project
echo "Building the project..."
mvn clean install

# Start services in background
echo "Starting MCP Flight Server..."
cd mcp-flight-server
nohup mvn spring-boot:run > ../logs/mcp-flight.log 2>&1 &
MCP_FLIGHT_PID=$!
cd ..

echo "Starting MCP Hotel Server..."
cd mcp-hotel-server
nohup mvn spring-boot:run > ../logs/mcp-hotel.log 2>&1 &
MCP_HOTEL_PID=$!
cd ..

# Wait for MCP servers to start
echo "Waiting for MCP servers to start..."
sleep 10

echo "Starting Flight Agent..."
cd flight-agent
nohup mvn spring-boot:run > ../logs/flight-agent.log 2>&1 &
FLIGHT_AGENT_PID=$!
cd ..

echo "Starting Hotel Agent..."
cd hotel-agent
nohup mvn spring-boot:run > ../logs/hotel-agent.log 2>&1 &
HOTEL_AGENT_PID=$!
cd ..

# Wait for agents to start
echo "Waiting for agents to start..."
sleep 10

echo "Starting Travel Orchestrator..."
cd travel-orchestrator
nohup mvn spring-boot:run > ../logs/orchestrator.log 2>&1 &
ORCHESTRATOR_PID=$!
cd ..

# Save PIDs to file for easy cleanup
mkdir -p logs
echo $MCP_FLIGHT_PID > logs/pids.txt
echo $MCP_HOTEL_PID >> logs/pids.txt
echo $FLIGHT_AGENT_PID >> logs/pids.txt
echo $HOTEL_AGENT_PID >> logs/pids.txt
echo $ORCHESTRATOR_PID >> logs/pids.txt

echo ""
echo "All services started!"
echo "Services running on:"
echo "  - MCP Flight Server: http://localhost:8081"
echo "  - MCP Hotel Server: http://localhost:8083"
echo "  - Flight Agent: http://localhost:8080"
echo "  - Hotel Agent: http://localhost:8082"
echo "  - Travel Orchestrator: http://localhost:9000"
echo ""
echo "To stop all services, run: ./stop-dev.sh"
echo "Logs are available in the logs/ directory"
