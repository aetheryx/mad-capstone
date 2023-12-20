use axum::{routing, Router};
use serde::Serialize;
use typeshare::typeshare;

use crate::{db::entities::*, SharedState};

mod create_conversation;
mod get_conversations;
mod create_message;
mod get_messages;

pub fn conversations_router() -> Router<SharedState> {
  Router::new()
    .route("/", routing::get(get_conversations::get_conversations))
    .route("/", routing::post(create_conversation::create_conversation))
    .route("/:conversation_id/messages", routing::post(create_message::create_message))
    .route("/:conversation_id/messages", routing::get(get_messages::get_messages))
}

#[derive(Serialize)]
#[typeshare]
struct FullConversation {
  conversation: conversation::Model,
  other_participant: user::Model,
  last_message: Option<conversation_message::Model>,
}
