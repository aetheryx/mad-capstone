use crate::db::entities::user;
use chrono::{Duration, Utc};
use jsonwebtoken::{decode, encode, DecodingKey, EncodingKey, Header, Validation};
use once_cell::sync::Lazy;
use serde::{Deserialize, Serialize};
use std::ops::Add;

static JWT_SECRET: Lazy<String> =
  Lazy::new(|| std::env::var("JWT_SECRET").expect("JWT_SECRET must be set"));

static ENCODING_KEY: Lazy<EncodingKey> =
  Lazy::new(|| EncodingKey::from_secret(JWT_SECRET.as_bytes()));
static DECODING_KEY: Lazy<DecodingKey> =
  Lazy::new(|| DecodingKey::from_secret(JWT_SECRET.as_bytes()));

pub fn encode_jwt(user: &user::Model) -> anyhow::Result<String> {
  let expiry = Utc::now().add(Duration::days(31)).timestamp();

  let claims = Claims {
    sub: user.id.to_string(),
    name: user.username.clone(),
    exp: expiry,
  };

  let token = encode(&Header::default(), &claims, &ENCODING_KEY)?;
  Ok(token)
}

pub fn decode_jwt(jwt: &str) -> anyhow::Result<i32> {
  let valid = decode::<Claims>(jwt, &DECODING_KEY, &Validation::default())?;
  let id = valid.claims.sub.parse()?;
  Ok(id)
}

#[derive(Debug, Deserialize, Serialize)]
pub struct Claims {
  pub sub: String,
  name: String,
  exp: i64,
}
