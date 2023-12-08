use crate::db::entities::user;
use axum::{extract::State, routing, Json, Router, http::StatusCode};
use sea_orm::{ActiveValue, DatabaseConnection, EntityTrait, QueryFilter, ColumnTrait};
use serde::{Deserialize, Serialize};

use super::app_error::{ApiError, AppErrors};

pub fn auth_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/signup", routing::post(auth_signup))
    .route("/login", routing::post(auth_login))
}

async fn auth_signup(
  State(db): State<DatabaseConnection>,
  Json(credentials): Json<AuthCredentials>,
) -> anyhow::Result<Json<AuthResponse>, AppErrors> {
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
) -> anyhow::Result<Json<AuthResponse>, AppErrors> {
  let user = user::Entity::find()
    .filter(user::Column::Username.eq(credentials.username))
    .one(&db)
    .await?
    .expect("User not found");

  let correct = bcrypt::verify(credentials.password, &user.password)
    .unwrap_or(false);

  if !correct {
    return Err(AppErrors::ApiError(ApiError {
      status: StatusCode::UNAUTHORIZED,
      message: "Password is incorrect".into()
    }))
  }

  let resp = AuthResponse {
    token: user.password.into(),
  };

  Ok(Json(resp))
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
