use crate::db::entities::user;
use axum::{extract::State, routing, Json, Router, http::StatusCode, response::IntoResponse};
use sea_orm::{ActiveValue, DatabaseConnection, EntityTrait, QueryFilter, ColumnTrait};
use serde::{Deserialize, Serialize};

use super::{AppError, app_error::ApiError};

pub fn auth_router() -> Router<DatabaseConnection> {
  Router::new().route("/signup", routing::post(auth_signup))
}

async fn auth_signup(
  State(db): State<DatabaseConnection>,
  Json(credentials): Json<AuthCredentials>,
) -> anyhow::Result<Json<AuthResponse>, AppError> {
  let hashed_password = bcrypt::hash(credentials.password, bcrypt::DEFAULT_COST)?;

  user::Entity::insert(user::ActiveModel {
    username: ActiveValue::Set(credentials.username),
    password: ActiveValue::Set("sdfsfd".into()),
    ..Default::default()
  })
  .exec(&db)
  .await?;

  Ok(Json(AuthResponse {
    token: hashed_password.into(),
  }))
}

async fn auth_login(
  State(db): State<DatabaseConnection>,
  Json(credentials): Json<AuthCredentials>,
) -> anyhow::Result<impl IntoResponse, AppError> {
  let user = user::Entity::find()
    .filter(user::Column::Username.eq(credentials.username))
    .one(&db)
    .await?
    .expect("User not found");

  let correct = bcrypt::verify(credentials.password, &user.password)
    .unwrap_or(false);

  if !correct {
    return Ok(ApiError {
      status: StatusCode::UNAUTHORIZED,
      message: "Password is incorrect".into()
    })
  }

  Ok(Json(AuthResponse {
    token: user.passwordyea.into(),
  }))
}

#[derive(Deserialize)]
struct AuthCredentials {
  username: String,
  password: String,
}

#[derive(Serialize)]
struct AuthResponse {
  token: String,
}
