use axum::{Router, routing};

use crate::SharedState;

mod get_me;
mod find_user;

pub fn users_router() -> Router<SharedState> {
  Router::new()
    .route("/@me", routing::get(get_me::get_me))
    .route("/find", routing::get(find_user::find_user))
}
