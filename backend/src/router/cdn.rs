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

const FIREBASE_URL: &str =
  "https://firebasestorage.googleapis.com/v0/b/capstone-386f7.appspot.com/o";

lazy_static! {
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
      let url = format!("{FIREBASE_URL}/{path}");
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
