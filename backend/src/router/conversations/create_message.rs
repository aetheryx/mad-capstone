use axum::{
  extract::{Path, State},
  Json,
};
use sea_orm::*;
use serde::Deserialize;
use typeshare::typeshare;

use crate::{util::{app_error::*, authed_user::*}, SharedState};
use crate::db::entities::*;

pub async fn create_message(
  AuthedUser(user): AuthedUser,
  State(state): State<SharedState>,
  Path(conversation_id): Path<i32>,
  Json(input): Json<CreateMessage>,
) -> HttpResult<conversation_message::Model> {
  let message = conversation_message::Entity::insert(conversation_message::ActiveModel {
    author_id: ActiveValue::Set(user.id),
    conversation_id: ActiveValue::Set(conversation_id),
    content: ActiveValue::Set(input.content),
    ..Default::default()
  })
  .exec_with_returning(&state.db)
  .await?;

  Ok(Json(message))
}

#[derive(Deserialize)]
#[typeshare]
pub struct CreateMessage {
  content: String,
}
