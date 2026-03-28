# Inbox Service Agent Rules

## Service Overview
Conversation orchestrator for the Business Agent platform. Receives inbound messages from channel-adapter, manages conversations and contacts, notifies ai-engine, processes AI replies, and sends outbound messages via Redis queue + outbound worker.

## Message Flow
```
Inbound:  channel-adapter → POST /api/v1/inbound → save contact/conversation/message → notify ai-engine
AI Reply: ai-engine → POST /api/v1/reply → save message → enqueue to Redis
Outbound: OutboundWorker polls Redis → calls channel-adapter /api/v1/messages/send
History:  ai-engine → GET /api/v1/conversations/{id}/messages
Typing:   ai-engine → POST /api/v1/typing
```

## Project Structure
```
com.businessagent.inbox/
├── config/          AppProperties, SecurityConfig, RequestIdFilter, RestClientConfig, RedisConfig
├── controller/      InboundController, ConversationController, ReplyController, HealthController
├── dto/
│   ├── request/     InboundMessageRequest, ReplyRequest, TypingRequest
│   └── response/    ConversationResponse, MessageResponse, InboundResult, ReplyResult, ErrorResponse
├── model/           BaseEntity, Contact, Conversation, Message
├── model/enums/     ConversationStatus, AssigneeType, MessageDirection, MessageActorType, MessageContentType, MessageStatus
├── repository/      ContactRepository, ConversationRepository, MessageRepository
├── service/
│   ├── impl/        InboundServiceImpl, ConversationServiceImpl, ReplyServiceImpl
│   └── OutboundQueueService (Redis queue operations)
├── client/          ChannelAdapterClient, AiEngineClient, AuthServiceClient
├── security/        ApiKeyAuthFilter
├── worker/          OutboundWorker (scheduled Redis queue poller)
└── exception/       Custom exceptions + GlobalExceptionHandler
```

## Key Conventions
- **Inbound flow**: Contact upsert → conversation upsert (or reuse open) → save message → async notify AI + typing
- **Outbound flow**: Save message (PENDING) → enqueue to Redis → OutboundWorker polls → send via channel-adapter → update status (SENT/FAILED)
- **Redis queues**: Key pattern `outbound:{channelId}`, FIFO, configurable batch size and retry count
- **Messages are immutable**: No `updatedAt` — only `status` changes (PENDING → SENT → DELIVERED → READ or FAILED)
- **API key auth**: All `/api/v1/**` endpoints require `X-API-Key` validated against auth-service
- **Async operations**: Mark-as-read, typing indicators, and AI engine notifications use CompletableFuture (fire-and-forget)

## Database
- Three tables: `contacts`, `conversations`, `messages`
- Flyway migrations V1-V3
- Conversations are reused when OPEN for the same contact+channel
- Unique constraint on `(business_id, external_id, channel_provider)` for contacts

## Adding Features
- **Escalation to human**: Change `assignee_type` from AI_BOT to USER, skip ai-engine notification
- **Conversation close**: Update status to CLOSED, new messages create a new conversation
- **BigQuery export**: Add a scheduled job that exports messages to BigQuery
- **WebSocket**: Add Pusher/SSE for real-time frontend updates
