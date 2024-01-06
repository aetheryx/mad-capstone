use axum::{
  extract::{Path, State},
  Json,
};
use sea_orm::*;
use serde::Deserialize;
use typeshare::typeshare;

use crate::ws::ServerEvent;
use crate::db::entities::*;
use crate::{
  util::{app_error::*, authed_user::*},
  SharedState,
};

pub async fn create_message(
  AuthedUser(user): AuthedUser,
  State(state): State<SharedState>,
  Path(conversation_id): Path<i32>,
  Json(input): Json<CreateMessageInput>,
) -> HttpResult<conversation_message::Model> {
  let message = conversation_message::Entity::insert(conversation_message::ActiveModel {
    author_id: ActiveValue::Set(user.id),
    conversation_id: ActiveValue::Set(conversation_id),
    content: ActiveValue::Set(input.content),
    reply_to_id: match input.reply_to_id {
      None => ActiveValue::NotSet,
      Some(id) => ActiveValue::Set(Some(id))
    },
    ..Default::default()
  })
  .exec_with_returning(&state.db)
  .await?;

  let other_user = participant::Entity::find()
    .filter(
      Condition::all()
        .add(participant::Column::ConversationId.eq(conversation_id))
        .add(participant::Column::UserId.ne(user.id)),
    )
    .one(&state.db)
    .await?
    .unwrap(); // TODO unwrap

  let event = ServerEvent::MessageCreate(&message);
  event.send_to(&state, other_user.user_id).await?;

  Ok(Json(message))
}

#[derive(Deserialize)]
#[typeshare]
pub struct CreateMessageInput {
  content: String,
  reply_to_id: Option<i32>
}
