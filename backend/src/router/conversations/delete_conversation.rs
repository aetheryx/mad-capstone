use axum::Json;
use axum::extract::{State, Path};
use sea_orm::{EntityTrait, QueryFilter, ColumnTrait};

use crate::util::app_error::HttpResult;
use crate::ws::ServerEvent;
use crate::{util::authed_user::AuthedUser, SharedState};
use crate::db::entities::*;

pub async fn delete_conversation(
  AuthedUser(_user): AuthedUser,
  Path(conversation_id): Path<i32>,
  State(state): State<SharedState>
) -> HttpResult<()> {
  let participants = participant::Entity::find()
    .filter(participant::Column::ConversationId.eq(conversation_id))
    .all(&state.db)
    .await?;

  conversation::Entity::delete_by_id(conversation_id)
    .exec(&state.db)
    .await?;

  for participant in participants {
    let event = ServerEvent::ConversationDelete(conversation_id);
    println!("sending {event:?} to {}", participant.user_id);
    event.send_to(&state, participant.user_id).await?;
  }

  Ok(Json(()))
}