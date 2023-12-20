use axum::{error_handling::HandleErrorLayer, http::StatusCode, Router};
use std::time::Duration;
use tower::{BoxError, ServiceBuilder};

use crate::SharedState;

mod auth;
mod cdn;
mod conversations;
mod users;
mod ws;

pub fn get_router() -> Router<SharedState> {
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
    .nest("/ws", ws::ws_router())
    .nest("/cdn", cdn::cdn_router())
    .layer(timeout_svc.into_inner())
}
