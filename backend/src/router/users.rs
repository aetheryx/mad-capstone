use axum::{Router, routing, Json};
use sea_orm::DatabaseConnection;

use crate::util::{authed_user::AuthedUser, app_error::HttpResult};
use crate::db::entities::user;

pub fn users_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/@me", routing::get(get_me))
}

async fn get_me(
  AuthedUser(user): AuthedUser
) -> HttpResult<user::Model> {
  Ok(Json(user))
}