use axum::{extract::State, Json};
use sea_orm::*;
use serde::Deserialize;
use typeshare::typeshare;

use crate::SharedState;
use crate::db::entities::user;
use crate::util::{app_error::*, jwt};

use super::AuthResponse;

pub async fn signup(
  State(state): State<SharedState>,
  Json(input): Json<SignupInput>,
) -> HttpResult<AuthResponse> {
  let hashed_password = bcrypt::hash(&input.password, bcrypt::DEFAULT_COST)?;

  let new_user = user::Entity::insert(user::ActiveModel {
    username: ActiveValue::Set(input.username),
    password: ActiveValue::Set(hashed_password.clone()),
    avatar: ActiveValue::Set(input.avatar),
    ..Default::default()
  })
  .exec_with_returning(&state.db)
  .await?;

  let jwt = jwt::encode_jwt(&new_user)?;

  Ok(Json(AuthResponse { token: jwt }))
}

#[derive(Deserialize)]
#[typeshare]
pub struct SignupInput {
  username: String,
  password: String,
  avatar: String,
}
