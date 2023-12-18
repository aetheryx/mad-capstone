use axum::{routing, Router};
use sea_orm::*;
use serde::Serialize;
use typeshare::typeshare;

use crate::db::entities::*;

mod create_conversation;
mod get_conversations;
mod create_message;

pub fn conversations_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/", routing::get(get_conversations::get_conversations))
    .route("/", routing::post(create_conversation::create_conversation))
    .route("/:conversation_id/messages", routing::post(create_message::create_message))
}

#[derive(Serialize)]
#[typeshare]
struct FullConversation {
  conversation: conversation::Model,
  other_participant: user::Model,
  last_message: Option<conversation_message::Model>,
}
