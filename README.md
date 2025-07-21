A Spring Boot application implementing distributed rate limiting using Redis and Lua scripts with a token bucket algorithm.
Features

🚀 Token Bucket Algorithm: Smooth rate limiting with token refill over time
🔄 Distributed: Works across multiple application instances using Redis
📊 Rate Limit Headers: Standard HTTP headers showing remaining tokens
🛡️ Graceful Degradation: Fails open when Redis is unavailable
👤 User-based: Rate limiting by User ID
🐳 Docker Ready: Complete Docker Compose setup included


Prerequisites

Java 21+
Maven 3.6+
Docker and Docker Compose (for containerized setup)
