use axum::{extract::State, Json};
use sea_orm::*;
use serde::Deserialize;
use typeshare::typeshare;

use crate::SharedState;
use crate::db::entities::*;
use crate::util::{
  authed_user::*, 
  app_error::*
};

pub async fn create_conversation(
  AuthedUser(user): AuthedUser,
  State(state): State<SharedState>,
  Json(input): Json<CreateConversation>,
) -> HttpResult<conversation::Model> {
  let new_conversation = conversation::Entity::insert(conversation::ActiveModel {
    ..Default::default()
  })
    .exec_with_returning(&state.db)
    .await?;

  let participants = vec![user.id, input.other_user]
    .into_iter()
    .map(|id| {
      let db = state.db.clone();

      async move {
        let new_participant = participant::Entity::insert(participant::ActiveModel {
          conversation_id: ActiveValue::Set(new_conversation.id),
          user_id: ActiveValue::Set(id),
        })
        .exec_with_returning(&db)
        .await?;
  
        anyhow::Ok(new_participant)
      }
    });

  futures::future::join_all(participants).await;

  Ok(Json(new_conversation))
}

#[derive(Deserialize)]
#[typeshare]
pub struct CreateConversation {
  other_user: i32,
}
