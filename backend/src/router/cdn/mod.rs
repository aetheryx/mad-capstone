use std::{collections::HashMap, sync::Arc};

use axum::{body::Bytes, routing, Router};
use tokio::sync::{Mutex, OnceCell};

use crate::SharedState;

mod proxy;

lazy_static! {
  static ref FIREBASE_BUCKET: String =
    std::env::var("FIREBASE_BUCKET").expect("FIREBASE_BUCKET must be set");

  static ref CACHE: Mutex<HashMap<String, Arc<OnceCell<Bytes>>>> = Mutex::default();
}

pub fn cdn_router() -> Router<SharedState> {
  Router::new()
    .route("/proxy/:path", routing::get(proxy::proxy_image))
}
