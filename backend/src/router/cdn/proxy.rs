use axum::{
  extract::Path,
  http::{header, StatusCode},
  response::IntoResponse,
};

use crate::util::app_error::*;

use super::{CACHE, FIREBASE_BUCKET};

pub async fn proxy_image(
  Path(path): Path<String>
) -> Result<impl IntoResponse, HttpError> {
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
