use axum::{
  extract::{Path, State},
  Json,
};
use sea_orm::{DatabaseConnection, EntityTrait, ActiveValue};
use serde::Deserialize;
use typeshare::typeshare;

use crate::db::entities::{conversation_message};
use crate::util::app_error::HttpResult;
use crate::util::authed_user::AuthedUser;

pub async fn create_message(
  AuthedUser(user): AuthedUser,
  State(db): State<DatabaseConnection>,
  Path(conversation_id): Path<i32>,
  Json(input): Json<CreateMessage>,
) -> HttpResult<conversation_message::Model> {
  let message = conversation_message::Entity::insert(conversation_message::ActiveModel {
    author_id: ActiveValue::Set(user.id),
    conversation_id: ActiveValue::Set(conversation_id),
    content: ActiveValue::Set(input.content),
    ..Default::default()
  })
    .exec_with_returning(&db)
    .await?;

  Ok(Json(message))
}

#[derive(Deserialize)]
#[typeshare]
pub struct CreateMessage {
  content: String,
}
