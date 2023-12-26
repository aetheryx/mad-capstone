use axum::{routing, Router};

use crate::SharedState;

mod connect;
mod event_handler;
mod call_handler;

pub fn ws_router() -> Router<SharedState> {
  Router::new()
    .route("/connect", routing::get(connect::connect))
}
