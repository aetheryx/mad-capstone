use axum::Json;
use axum::extract::{State, Path, Query};
use sea_orm::*;
use serde::Deserialize;

use crate::SharedState;
use crate::util::{authed_user::*, app_error::*};
use crate::db::entities::*;

pub async fn get_messages(
  AuthedUser(_user): AuthedUser, // TODO: assert user is participant in conversation
  State(state): State<SharedState>,
  Path(conversation_id): Path<i32>,
  Query(query): Query<GetMessagesQuery>
) -> HttpResult<Vec<conversation_message::Model>> {
  let messages = conversation_message::Entity::find()
    .filter(conversation_message::Column::ConversationId.eq(conversation_id))
    .order_by_desc(conversation_message::Column::CreatedAt)
    .limit(query.limit.unwrap_or(50))
    .offset(query.offset.unwrap_or(0))
    .all(&state.db)
    .await?;

  Ok(Json(messages))
}

#[derive(Deserialize)]
pub struct GetMessagesQuery {
  limit: Option<u64>,
  offset: Option<u64>,
}