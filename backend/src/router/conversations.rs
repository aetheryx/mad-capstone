use anyhow::Context;
use axum::extract::State;
use axum::{routing, Json, Router};
use sea_orm::{ActiveValue, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter};
use serde::{Deserialize, Serialize};
use typeshare::typeshare;

use crate::db::entities::{conversation, participant};
use crate::util::{app_error::HttpResult, authed_user::AuthedUser};

pub fn conversations_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/", routing::get(get_conversations))
    .route("/", routing::post(create_conversation))
}

async fn get_conversations(
  AuthedUser(user): AuthedUser,
  State(db): State<DatabaseConnection>,
) -> HttpResult<Vec<Conversation>> {
  let conversation_ids = participant::Entity::find()
    .filter(participant::Column::UserId.eq(user.id))
    .all(&db)
    .await?
    .iter()
    .map(|participant| participant.conversation_id)
    .collect::<Vec<_>>();

  let mut conversations: Vec<Conversation> = vec![];

  for id in conversation_ids {
    let conversation = conversation::Entity::find_by_id(id)
      .one(&db)
      .await?
      .context("conversation not found")?;

    let participants = participant::Entity::find()
      .filter(participant::Column::ConversationId.eq(conversation.id))
      .all(&db)
      .await?;

    conversations.push(Conversation {
      conversation,
      participants,
    })
  }

  Ok(Json(conversations))
}

async fn create_conversation(
  AuthedUser(user): AuthedUser,
  State(db): State<DatabaseConnection>,
  Json(input): Json<CreateConversation>,
) -> HttpResult<Conversation> {
  let new_conversation = conversation::Entity::insert(conversation::ActiveModel::default())
    .exec_with_returning(&db)
    .await?;

  let p1 = participant::Entity::insert(participant::ActiveModel {
    conversation_id: ActiveValue::Set(new_conversation.id),
    user_id: ActiveValue::Set(user.id),
  })
  .exec_with_returning(&db)
  .await?;

  let p2 = participant::Entity::insert(participant::ActiveModel {
    conversation_id: ActiveValue::Set(new_conversation.id),
    user_id: ActiveValue::Set(input.other_user),
  })
  .exec_with_returning(&db)
  .await?;

  Ok(Json(Conversation {
    conversation: new_conversation,
    participants: vec![p1, p2]
  }))
}

#[derive(Deserialize)]
#[typeshare]
struct CreateConversation {
  other_user: i32,
}

#[derive(Serialize)]
#[typeshare]
struct Conversation {
  conversation: conversation::Model,
  participants: Vec<participant::Model>,
}
