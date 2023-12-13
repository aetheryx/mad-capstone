use axum::{error_handling::HandleErrorLayer, http::StatusCode, Router};
use sea_orm::DatabaseConnection;
use std::time::Duration;
use tower::{BoxError, ServiceBuilder};

mod auth;
mod users;
mod conversations;

pub fn get_router() -> Router<DatabaseConnection> {
  let timeout_svc = ServiceBuilder::new()
    .layer(HandleErrorLayer::new(|error: BoxError| async move {
      if error.is::<tower::timeout::error::Elapsed>() {
        Ok(StatusCode::REQUEST_TIMEOUT)
      } else {
        Err((
          StatusCode::INTERNAL_SERVER_ERROR,
          format!("Unhandled internal error: {error}"),
        ))
      }
    }))
    .timeout(Duration::from_secs(10));

  Router::new()
    .nest("/auth", auth::auth_router())
    .nest("/users", users::users_router())
    .nest("/conversations", conversations::conversations_router())
    .layer(timeout_svc.into_inner())
}
