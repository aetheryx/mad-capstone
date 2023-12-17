use crate::db::entities::user;
use axum::{extract::State, http::StatusCode, routing, Json, Router};
use sea_orm::{ActiveValue, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter};
use serde::{Deserialize, Serialize};
use typeshare::typeshare;

use crate::util::app_error::{HttpError, HttpResult};
use crate::util::jwt;

pub fn auth_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/signup", routing::post(auth_signup))
    .route("/login", routing::post(auth_login))
}

async fn auth_signup(
  State(db): State<DatabaseConnection>,
  Json(input): Json<SignupInput>,
) -> HttpResult<AuthResponse> {
  let hashed_password = bcrypt::hash(&input.password, bcrypt::DEFAULT_COST)?;

  let new_user = user::Entity::insert(user::ActiveModel {
    username: ActiveValue::Set(input.username),
    password: ActiveValue::Set(hashed_password.clone()),
    avatar: ActiveValue::Set(input.avatar),
    ..Default::default()
  })
  .exec_with_returning(&db)
  .await?;

  let jwt = jwt::encode_jwt(&new_user)?;
  Ok(Json(AuthResponse { token: jwt }))
}

async fn auth_login(
  State(db): State<DatabaseConnection>,
  Json(credentials): Json<LoginInput>,
) -> HttpResult<AuthResponse> {
  let user = user::Entity::find()
    .filter(user::Column::Username.eq(credentials.username))
    .one(&db)
    .await?;

  let Some(user) = user else {
    return Err(HttpError::Status(StatusCode::NOT_FOUND));
  };

  let correct = bcrypt::verify(&credentials.password, &user.password).unwrap_or(false);
  if !correct {
    return Err(HttpError::Status(StatusCode::BAD_REQUEST));
  }

  let jwt = jwt::encode_jwt(&user)?;
  Ok(Json(AuthResponse { token: jwt }))
}

#[derive(Deserialize)]
#[typeshare]
struct LoginInput {
  username: String,
  password: String,
}

#[derive(Deserialize)]
#[typeshare]
struct SignupInput {
  username: String,
  password: String,
  avatar: String,
}

#[derive(Serialize)]
#[typeshare]
struct AuthResponse {
  token: String,
}
