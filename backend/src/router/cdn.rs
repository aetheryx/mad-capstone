use axum::{
  body::Bytes,
  extract::Path,
  http::{header, StatusCode},
  response::IntoResponse,
  routing, Router,
};
use sea_orm::DatabaseConnection;
use std::{collections::HashMap, sync::Arc};
use tokio::sync::{Mutex, OnceCell};

use crate::util::app_error::HttpError;

lazy_static! {
  static ref FIREBASE_BUCKET: String =
    std::env::var("FIREBASE_BUCKET").expect("FIREBASE_BUCKET must be set");

  static ref CACHE: Mutex<HashMap<String, Arc<OnceCell<Bytes>>>> = Mutex::default();
}

pub fn cdn_router() -> Router<DatabaseConnection> {
  Router::new().route("/proxy/:path", routing::get(get_proxy_image))
}

async fn get_proxy_image(Path(path): Path<String>) -> Result<impl IntoResponse, HttpError> {
  let cell = {
    let mut cache = CACHE.lock().await;
    cache.entry(path.clone()).or_default().clone()
  };

  let v = cell
    .get_or_try_init(|| async {
      let url = format!(
        "https://firebasestorage.googleapis.com/v0/b/{}/o/{}",
        FIREBASE_BUCKET.as_str(),
        path
      );
      let image = reqwest::get(url).await?.bytes().await?;
      anyhow::Ok(image)
    })
    .await?;

  Ok((
    StatusCode::OK,
    [(header::CONTENT_TYPE, "image/png")],
    v.clone(),
  ))
}
