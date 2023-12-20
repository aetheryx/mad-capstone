use axum::{extract::State, http::StatusCode, Json};
use sea_orm::*;
use serde::Deserialize;
use typeshare::typeshare;

use crate::SharedState;
use crate::db::entities::*;
use crate::util::{app_error::*, jwt};

use super::AuthResponse;

pub async fn login(
  State(state): State<SharedState>,
  Json(credentials): Json<LoginInput>,
) -> HttpResult<AuthResponse> {
  let user = user::Entity::find()
    .filter(user::Column::Username.eq(credentials.username))
    .one(&state.db)
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
pub struct LoginInput {
  username: String,
  password: String,
}
