use axum::{routing, Router};

use crate::SharedState;

mod connect;

pub fn ws_router() -> Router<SharedState> {
  Router::new()
    .route("/connect", routing::get(connect::connect))
}
