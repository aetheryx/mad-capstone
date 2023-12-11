use std::time::Duration;

use sea_orm::DatabaseConnection;

use axum::{error_handling::HandleErrorLayer, http::StatusCode, Router};
use tower::{BoxError, ServiceBuilder};

use self::auth::auth_router;

mod app_error;
mod auth;

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
    .nest("/auth", auth_router())
    .layer(timeout_svc.into_inner())
}
