use axum::extract::State;
use axum::{Router, routing, Json};
use sea_orm::{DatabaseConnection, EntityTrait, ActiveValue, QueryFilter, ColumnTrait, Condition};
use serde::Deserialize;
use typeshare::typeshare;

use crate::db::entities::conversation;
use crate::util::{authed_user::AuthedUser, app_error::HttpResult};

pub fn conversations_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/", routing::get(get_conversations))
    .route("/", routing::post(create_conversation))
}

async fn get_conversations(
  AuthedUser(user): AuthedUser,
  State(db): State<DatabaseConnection>
) -> HttpResult<Vec<conversation::Model>> {
  let conversations = conversation::Entity::find()
    .filter(
      Condition::any()
        .add(conversation::Column::ParticipantA.eq(user.id))
        .add(conversation::Column::ParticipantB.eq(user.id))
    )
    .all(&db)
    .await?;

  Ok(Json(conversations))
}

async fn create_conversation(
  AuthedUser(user): AuthedUser,
  State(db): State<DatabaseConnection>,
  Json(input): Json<CreateConversation>,
) -> HttpResult<conversation::Model> {
  let new_conversation = conversation::Entity::insert(conversation::ActiveModel {
    participant_a: ActiveValue::Set(user.id),
    participant_b: ActiveValue::Set(input.other_user),
    ..Default::default()
  })
    .exec_with_returning(&db)
    .await?;

  Ok(Json(new_conversation))
}

#[derive(Deserialize)]
#[typeshare]
struct CreateConversation {
  other_user: i32
}