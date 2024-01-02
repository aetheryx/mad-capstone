use anyhow::Context;
use axum::{extract::State, Json};
use sea_orm::*;

use crate::SharedState;
use crate::db::entities::*;
use crate::util::{
  authed_user::*, 
  app_error::*
};
use crate::ws::conversation::FullConversation;

pub async fn get_conversations(
  AuthedUser(user): AuthedUser,
  State(state): State<SharedState>,
) -> HttpResult<Vec<FullConversation>> {
  let conversation_ids = participant::Entity::find()
    .filter(participant::Column::UserId.eq(user.id))
    .all(&state.db)
    .await?;

  let conversations = conversation_ids.iter().map(|p| async {
    let conversation = conversation::Entity::find_by_id(p.conversation_id)
      .one(&state.db)
      .await?
      .context("conversation not found")?;

    let other_participant = participant::Entity::find()
      .filter(
        Condition::all()
          .add(participant::Column::ConversationId.eq(conversation.id))
          .add(participant::Column::UserId.ne(user.id))
      )
      .find_also_related(user::Entity)
      .one(&state.db)
      .await?
      .unwrap().1.unwrap(); // TODO: remove unwrap

    let last_message = conversation_message::Entity::find()
      .filter(conversation_message::Column::ConversationId.eq(conversation.id))
      .order_by_desc(conversation_message::Column::CreatedAt)
      .limit(1)
      .one(&state.db)
      .await?;

    anyhow::Ok(FullConversation {
      conversation,
      other_participant,
      last_message
    })
  });

  let conversations = futures::future::join_all(conversations)
    .await
    .into_iter()
    .filter_map(|s| s.ok())
    .collect::<Vec<_>>();

  Ok(Json(conversations))
}
