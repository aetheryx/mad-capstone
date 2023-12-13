use axum::{
  async_trait,
  extract::{FromRef, FromRequestParts},
  http::{request::Parts, StatusCode},
  RequestPartsExt,
};
use axum_extra::{
  headers::{authorization::Bearer, Authorization},
  TypedHeader,
};
use sea_orm::{DatabaseConnection, EntityTrait};

use super::{app_error::HttpError, jwt::decode_jwt};
use crate::db::entities::user;

pub struct AuthedUser(pub user::Model);

#[async_trait]
impl<S> FromRequestParts<S> for AuthedUser
where
  DatabaseConnection: FromRef<S>,
  S: Send + Sync,
{
  type Rejection = super::app_error::HttpError;

  async fn from_request_parts(parts: &mut Parts, state: &S) -> Result<Self, Self::Rejection> {
    let db = DatabaseConnection::from_ref(state);

    let TypedHeader(Authorization(bearer)) = parts
      .extract::<TypedHeader<Authorization<Bearer>>>()
      .await?;

    let user = match decode_jwt(bearer.token()) {
      Err(_) => return Err(HttpError::Status(StatusCode::UNAUTHORIZED)),
      Ok(user_id) => user::Entity::find_by_id(user_id).one(&db).await?,
    };

    match user {
      Some(u) => Ok(AuthedUser(u)),
      None => Err(HttpError::Status(StatusCode::UNAUTHORIZED)),
    }
  }
}
