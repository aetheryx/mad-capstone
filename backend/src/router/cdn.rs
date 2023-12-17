use anyhow::Context;
use once_cell::sync::Lazy;
use std::collections::HashMap;

use axum::{
  body::Bytes,
  extract::Path,
  http::{header, StatusCode},
  response::IntoResponse,
  routing, Router,
};
use sea_orm::DatabaseConnection;
use tokio::sync::Mutex;

use crate::util::app_error::HttpError;

const FIREBASE_URL: &str =
  "https://firebasestorage.googleapis.com/v0/b/capstone-386f7.appspot.com/o";

static CACHE: Lazy<Mutex<HashMap<String, Bytes>>> = Lazy::new(|| Mutex::new(HashMap::new()));

pub fn cdn_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/proxy/:path", routing::get(get_proxy_image))
}

async fn get_proxy_image(Path(path): Path<String>) -> Result<impl IntoResponse, HttpError> {
  let mut cache = CACHE.lock().await;

  if !cache.contains_key(&path) {
    let url = format!("{FIREBASE_URL}/{path}");
    let resp = reqwest::get(url).await?.bytes().await?;

    cache.insert(path.clone(), resp);
  }

  let entry = cache.get(&path).context("Entry not found")?;

  Ok((
    StatusCode::OK,
    [(header::CONTENT_TYPE, "image/png")],
    entry.clone(),
  ))
}
