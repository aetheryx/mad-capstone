use axum::Json;

use crate::db::entities::user;
use crate::util::{app_error::*, authed_user::*};

pub async fn get_me(
  AuthedUser(user): AuthedUser
) -> HttpResult<user::Model> {
  Ok(Json(user))
}
