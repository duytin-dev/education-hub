-- Chat tư vấn khóa học

CREATE TABLE IF NOT EXISTS conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    guest_name VARCHAR(255),
    guest_email VARCHAR(255),
    guest_token VARCHAR(64) NOT NULL UNIQUE,
    course_id BIGINT REFERENCES courses(id),
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    last_message_at TIMESTAMP,
    unread_for_staff INTEGER DEFAULT 0,
    unread_for_visitor INTEGER DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations(status);
CREATE INDEX IF NOT EXISTS idx_conversations_last_message_at ON conversations(last_message_at DESC);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_type VARCHAR(32) NOT NULL,
    staff_id BIGINT REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_conversation_id ON chat_messages(conversation_id);
