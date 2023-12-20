use axum::{extract::{State, Query}, http::StatusCode, Json};
use sea_orm::*;
use serde::Deserialize;

use crate::{
  util::app_error::*,
  db::entities::user, SharedState
};

pub async fn find_user(
  State(state): State<SharedState>,
  query: Query<FindUserQuery>
) -> HttpResult<user::Model> {
  let user = user::Entity::find()
    .filter(user::Column::Username.eq(&query.username))
    .one(&state.db)
    .await?;

  let Some(user) = user else {
    return Err(HttpError::Status(StatusCode::NOT_FOUND));
  };

  Ok(Json(user))
}

#[derive(Deserialize)]
pub struct FindUserQuery {
  username: String
}