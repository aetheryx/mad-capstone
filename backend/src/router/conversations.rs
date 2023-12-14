use anyhow::Context;
use axum::extract::State;
use axum::{routing, Json, Router};
use sea_orm::{ActiveValue, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter, Condition};
use serde::{Deserialize, Serialize};
use typeshare::typeshare;

use crate::db::entities::{conversation, participant, user};
use crate::util::{app_error::HttpResult, authed_user::AuthedUser};

pub fn conversations_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/", routing::get(get_conversations))
    .route("/", routing::post(create_conversation))
}

async fn get_conversations(
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

    anyhow::Ok(FullConversation {
      conversation,
      other_participant,
    })
  });

  let conversations = futures::future::join_all(conversations)
    .await
    .into_iter()
    .filter_map(|s| s.ok())
    .collect::<Vec<_>>();

  Ok(Json(conversations))
}

async fn create_conversation(
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
struct CreateConversation {
  other_user: i32,
}

#[derive(Serialize)]
#[typeshare]
struct FullConversation {
  conversation: conversation::Model,
  other_participant: user::Model,
}
