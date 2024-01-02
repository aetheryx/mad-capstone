use axum::{routing, Router};

use crate::SharedState;

mod create_conversation;
mod get_conversations;
mod create_message;
mod get_messages;
mod delete_conversation;

pub fn conversations_router() -> Router<SharedState> {
  Router::new()
    .route("/", routing::get(get_conversations::get_conversations))
    .route("/", routing::post(create_conversation::create_conversation))
    .route("/:conversation_id", routing::delete(delete_conversation::delete_conversation))
    .route("/:conversation_id/messages", routing::post(create_message::create_message))
    .route("/:conversation_id/messages", routing::get(get_messages::get_messages))
}

