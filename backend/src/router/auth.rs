use crate::db::entities::user;
use axum::{extract::State, http::StatusCode, routing, Json, Router};
use sea_orm::{ActiveValue, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter};
use serde::{Deserialize, Serialize};

use super::app_error::{HttpError, HttpResult};

pub fn auth_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/signup", routing::post(auth_signup))
    .route("/login", routing::post(auth_login))
}

async fn auth_signup(
  State(db): State<DatabaseConnection>,
  Json(credentials): Json<AuthCredentials>,
) -> HttpResult<AuthResponse> {
  let hashed_password = bcrypt::hash(&credentials.password, bcrypt::DEFAULT_COST)?;

  user::Entity::insert(user::ActiveModel {
    username: ActiveValue::Set(credentials.username),
    password: ActiveValue::Set(hashed_password.clone()),
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
) -> HttpResult<AuthResponse> {
  let user = user::Entity::find()
    .filter(user::Column::Username.eq(credentials.username))
    .one(&db)
    .await?;

  let user = match user {
    Some(u) => u,
    None => {
      return Err(HttpError::Status(StatusCode::NOT_FOUND));
    }
  };

  let correct = bcrypt::verify(&credentials.password, &user.password).unwrap_or(false);

  if !correct {
    return Err(HttpError::Status(StatusCode::UNAUTHORIZED));
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
