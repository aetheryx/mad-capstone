use anyhow::Context;
use axum::{extract::State, Json};
use sea_orm::{ActiveValue, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter, Condition, QuerySelect, QueryOrder};
use serde::{Deserialize, Serialize};
use typeshare::typeshare;

use crate::db::entities::{conversation, participant, user, conversation_message};
use crate::util::{app_error::HttpResult, authed_user::AuthedUser};

pub async fn get_conversations(
  AuthedUser(user): AuthedUser,
  State(db): State<DatabaseConnection>,
) -> HttpResult<Vec<FullConversation>> {
  let conversation_ids = participant::Entity::find()
    .filter(participant::Column::UserId.eq(user.id))
    .all(&db)
    .await?;

  let conversations = conversation_ids.iter().map(|p| async {
    let conversation = conversation::Entity::find_by_id(p.conversation_id)
      .one(&db)
      .await?
      .context("conversation not found")?;

    let other_participant = participant::Entity::find()
      .filter(
        Condition::all()
          .add(participant::Column::ConversationId.eq(conversation.id))
          .add(participant::Column::UserId.ne(user.id))
      )
      .find_also_related(user::Entity)
      .one(&db)
      .await?
      .unwrap().1.unwrap(); // TODO: remove unwrap

    let last_message = conversation_message::Entity::find()
      .filter(conversation_message::Column::ConversationId.eq(conversation.id))
      .order_by_desc(conversation_message::Column::CreatedAt)
      .limit(1)
      .one(&db)
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

pub async fn create_conversation(
  AuthedUser(user): AuthedUser,
  State(db): State<DatabaseConnection>,
  Json(input): Json<CreateConversation>,
) -> HttpResult<conversation::Model> {
  let new_conversation = conversation::Entity::insert(conversation::ActiveModel::default())
    .exec_with_returning(&db)
    .await?;

  let participants = vec![user.id, input.other_user]
    .into_iter()
    .map(|id| {
      let db = db.clone();

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

#[derive(Serialize)]
#[typeshare]
pub struct FullConversation {
  conversation: conversation::Model,
  other_participant: user::Model,
  last_message: Option<conversation_message::Model>
}
