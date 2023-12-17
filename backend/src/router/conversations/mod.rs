use axum::{routing, Json, Router};
use sea_orm::{ActiveValue, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter, Condition};

use self::conversations::{get_conversations, create_conversation};
use self::messages::{create_message};

mod conversations;
mod messages;

pub fn conversations_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/", routing::get(get_conversations))
    .route("/", routing::post(create_conversation))
    .route("/:conversation_id/messages", routing::post(create_message))
}