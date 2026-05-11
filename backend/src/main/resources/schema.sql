DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS conversations;
DROP TABLE IF EXISTS blocks;
DROP TABLE IF EXISTS post_views;
DROP TABLE IF EXISTS ai_messages;
DROP TABLE IF EXISTS ai_conversations;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS hidden_posts;
DROP TABLE IF EXISTS bookmarks;
DROP TABLE IF EXISTS reposts;
DROP TABLE IF EXISTS post_likes;
DROP TABLE IF EXISTS follows;
DROP TABLE IF EXISTS comment_images;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS post_images;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(255) NOT NULL,
    cover_url VARCHAR(255),
    bio VARCHAR(255),
    role VARCHAR(16) NOT NULL DEFAULT 'USER',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    mute_status VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    likes_count INT NOT NULL DEFAULT 0,
    reposts_count INT NOT NULL DEFAULT 0,
    comments_count INT NOT NULL DEFAULT 0,
    views_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE post_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    CONSTRAINT fk_post_images_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    likes_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE comment_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    CONSTRAINT fk_comment_images_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
);

CREATE TABLE follows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_follows_pair UNIQUE (follower_id, following_id),
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_following FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE post_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_post_likes_pair UNIQUE (post_id, user_id),
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE bookmarks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_bookmarks_pair UNIQUE (post_id, user_id),
    CONSTRAINT fk_bookmarks_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmarks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE hidden_posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_hidden_posts_pair UNIQUE (post_id, user_id),
    CONSTRAINT fk_hidden_posts_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_hidden_posts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE post_views (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    viewer_id BIGINT NOT NULL,
    viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_views_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_views_viewer FOREIGN KEY (viewer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    category VARCHAR(64) NOT NULL,
    details VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    resolved_by BIGINT,
    resolved_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_reports_pair UNIQUE (post_id, reporter_id),
    CONSTRAINT fk_reports_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_resolver FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE reposts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_reposts_pair UNIQUE (post_id, user_id),
    CONSTRAINT fk_reposts_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_reposts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE blocks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_blocks_pair UNIQUE (blocker_id, blocked_id),
    CONSTRAINT fk_blocks_blocker FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_blocks_blocked FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_a_id BIGINT NOT NULL,
    user_b_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_conversations_pair UNIQUE (user_a_id, user_b_id),
    CONSTRAINT fk_conversations_user_a FOREIGN KEY (user_a_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_user_b FOREIGN KEY (user_b_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT,
    message_type VARCHAR(16) NOT NULL DEFAULT 'TEXT',
    file_url VARCHAR(255),
    file_name VARCHAR(255),
    mime_type VARCHAR(128),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME,
    recalled BOOLEAN NOT NULL DEFAULT FALSE,
    recalled_at DATETIME,
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(32) NOT NULL,
    recipient_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    post_id BIGINT,
    message VARCHAR(500),
    action_label VARCHAR(64),
    action_url VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE ai_conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_conversations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE ai_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    model VARCHAR(64) NOT NULL DEFAULT 'gemini-2.5-flash-lite',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_messages_conversation FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE
);
