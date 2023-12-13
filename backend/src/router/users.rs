use axum::extract::State;
use axum::http::StatusCode;
use axum::{Router, routing, Json, extract::Query};
use sea_orm::{DatabaseConnection, EntityTrait, QueryFilter, ColumnTrait};
use serde::Deserialize;

use crate::util::app_error::HttpError;
use crate::util::{authed_user::AuthedUser, app_error::HttpResult};
use crate::db::entities::user;

pub fn users_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/@me", routing::get(get_me))
    .route("/find", routing::get(find_user))
}

async fn get_me(
  AuthedUser(user): AuthedUser
) -> HttpResult<user::Model> {
  Ok(Json(user))
}

async fn find_user(
  State(db): State<DatabaseConnection>,
  query: Query<FindUserQuery>
) -> HttpResult<user::Model> {
  let user = user::Entity::find()
    .filter(user::Column::Username.eq(&query.username))
    .one(&db)
    .await?;

  let Some(user) = user else {
    return Err(HttpError::Status(StatusCode::NOT_FOUND));
  };

  Ok(Json(user))
}

#[derive(Deserialize)]
struct FindUserQuery {
  username: String
}