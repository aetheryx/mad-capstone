use axum::{routing, Router};
use sea_orm::DatabaseConnection;
use serde::Serialize;
use typeshare::typeshare;

mod login;
mod signup;

pub fn auth_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/signup", routing::post(signup::signup))
    .route("/login", routing::post(login::login))
}

#[derive(Serialize)]
#[typeshare]
struct AuthResponse {
  token: String,
}
