use axum::{routing, Router};
use serde::Serialize;
use typeshare::typeshare;

use crate::SharedState;

mod login;
mod signup;

pub fn auth_router() -> Router<SharedState> {
  Router::new()
    .route("/signup", routing::post(signup::signup))
    .route("/login", routing::post(login::login))
}

#[derive(Serialize)]
#[typeshare]
struct AuthResponse {
  token: String,
}
