# Travel AI Application Validation Checklist

## Prerequisites ✅
- [ ] Java 21 installed
- [ ] Maven 3.6+ installed
- [ ] OPENAI_API_KEY environment variable set
- [ ] Ports 8080, 8081, 8082, 8083, 9000 available

## Build Validation ✅
```bash
mvn clean install
```
- [ ] All modules compile successfully
- [ ] No test failures
- [ ] All JAR files created in target/ directories

## Service Startup Validation ✅

### 1. MCP Flight Server (Port 8081)
```bash
cd mcp-flight-server && mvn spring-boot:run
```
- [ ] Server starts without errors
- [ ] H2 console accessible at http://localhost:8081/h2-console
- [ ] MCP capabilities endpoint responds: http://localhost:8081/mcp/capabilities

### 2. MCP Hotel Server (Port 8083)
```bash
cd mcp-hotel-server && mvn spring-boot:run
```
- [ ] Server starts without errors
- [ ] H2 console accessible at http://localhost:8083/h2-console
- [ ] MCP capabilities endpoint responds: http://localhost:8083/mcp/capabilities

### 3. Flight Agent (Port 8080)
```bash
cd flight-agent && mvn spring-boot:run
```
- [ ] Agent starts without errors
- [ ] Can connect to Flight MCP server
- [ ] Spring AI initialization successful

### 4. Hotel Agent (Port 8082)
```bash
cd hotel-agent && mvn spring-boot:run
```
- [ ] Agent starts without errors
- [ ] Can connect to Hotel MCP server
- [ ] Spring AI initialization successful

### 5. Travel Orchestrator (Port 9000)
```bash
cd travel-orchestrator && mvn spring-boot:run
```
- [ ] Orchestrator starts without errors
- [ ] Can connect to both agents
- [ ] Spring AI initialization successful

## Functional Testing ✅

### MCP Server Testing
```bash
# Test flight search
curl -X POST http://localhost:8081/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc": "2.0", "method": "tools/call", "id": "1", "params": {"name": "search_flights", "arguments": {"from": "NYC", "to": "LON", "departureDate": "2024-12-15", "passengers": 2}}}'
```
- [ ] Returns flight results from multiple providers (Joyair, AeroGo, DracAir)

```bash
# Test hotel search  
curl -X POST http://localhost:8083/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc": "2.0", "method": "tools/call", "id": "2", "params": {"name": "search_hotels", "arguments": {"destination": "London", "checkIn": "2024-12-15", "checkOut": "2024-12-20", "guests": 2}}}'
```
- [ ] Returns hotel results from multiple providers (Marriott, Holiday Inn, Accor)

### Agent Communication Testing
```bash
# Test travel planning
curl -X POST http://localhost:9000/api/travel/plan \
  -H "Content-Type: application/json" \
  -d '{"from": "New York", "to": "London", "departureDate": "2024-12-15", "returnDate": "2024-12-22", "passengers": 2, "preferences": "business class, luxury hotel"}'
```
- [ ] Orchestrator coordinates with both agents
- [ ] Receives responses from flight and hotel agents
- [ ] Returns comprehensive travel plan

### Chat Interface Testing
```bash
# Test chat functionality
curl -X POST http://localhost:9000/api/travel/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Find me flights from NYC to London for December"}'
```
- [ ] AI understands travel intent
- [ ] Routes request to appropriate agent
- [ ] Returns intelligent response

## Agent-to-Agent Communication Testing ✅
- [ ] Flight agent can communicate with hotel agent
- [ ] Hotel agent can communicate with flight agent
- [ ] A2A protocol messages are properly formatted
- [ ] Task IDs are generated and tracked

## Error Handling Validation ✅
- [ ] Invalid MCP tool calls return proper errors
- [ ] Missing environment variables are handled gracefully
- [ ] Network failures between services are handled
- [ ] OpenAI API failures are handled properly

## Performance Validation ✅
- [ ] Services start within reasonable time (< 30 seconds each)
- [ ] API responses return within 5 seconds
- [ ] Concurrent requests are handled properly
- [ ] Memory usage remains stable

## Security Validation ✅
- [ ] OpenAI API key is not logged
- [ ] No sensitive data in error messages
- [ ] CORS properly configured
- [ ] No hardcoded credentials in code

## Docker Validation ✅
```bash
docker-compose build
docker-compose up
```
- [ ] All services build successfully
- [ ] All services start in containers
- [ ] Inter-service communication works
- [ ] External API access works

## Integration Testing Scenarios ✅

### Scenario 1: Complete Trip Planning
1. User requests trip planning
2. Orchestrator analyzes request
3. Flight agent searches via MCP flight server
4. Hotel agent searches via MCP hotel server
5. Results are coordinated and returned

### Scenario 2: Agent Coordination
1. Flight agent finds flights to destination
2. Flight agent requests hotel coordination
3. Hotel agent finds hotels near destination
4. Coordinated response provided

### Scenario 3: Multi-Provider Results
1. Request generates calls to multiple providers
2. Results are aggregated and sorted
3. Best options from each provider included
4. Provider attribution maintained

## Known Issues & Troubleshooting ✅

### Common Issues:
1. **Port conflicts**: Ensure all ports are available
2. **OpenAI API quota**: Check API usage limits
3. **Memory issues**: Ensure sufficient heap size for Java 21
4. **A2A SDK**: May need to build locally if not in Maven Central

### Debug Commands:
```bash
# Check service logs
tail -f logs/mcp-flight.log
tail -f logs/mcp-hotel.log
tail -f logs/flight-agent.log
tail -f logs/hotel-agent.log
tail -f logs/orchestrator.log

# Check port usage
lsof -i :8080
lsof -i :8081
lsof -i :8082
lsof -i :8083
lsof -i :9000

# Test individual services
curl http://localhost:8081/mcp/capabilities
curl http://localhost:8083/mcp/capabilities
curl http://localhost:9000/api/travel/agents/status
```

## Success Criteria ✅
- [ ] All 5 services start successfully
- [ ] MCP servers respond to tool calls
- [ ] Agents can communicate with each other
- [ ] Orchestrator coordinates multi-agent workflows
- [ ] End-to-end travel planning works
- [ ] Chat interface responds intelligently
- [ ] Multiple providers return results
- [ ] Error handling works properly