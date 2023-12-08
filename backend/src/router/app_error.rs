use axum::{
  http::StatusCode,
  response::{IntoResponse, Response}, Json,
};
use serde_json::json;

pub struct AppError(pub anyhow::Error);

impl IntoResponse for AppError {
  fn into_response(self) -> Response {
    (
      StatusCode::INTERNAL_SERVER_ERROR,
      format!("Something went wrong: {}", self.0),
    )
      .into_response()
  }
}

impl<E> From<E> for AppError
where
  E: Into<anyhow::Error>,
{
  fn from(err: E) -> Self {
    Self(err.into())
  }
}

// For application-level http errors (e.g. 404, 401)
#[derive(Debug)]
pub struct ApiError {
  pub status: StatusCode,
  pub message: String,
}

impl IntoResponse for ApiError {
  fn into_response(self) -> Response {
    let payload = json!({
      "status": self.status.as_u16(),
      "message": self.message
    });

    (self.status, Json(payload)).into_response()
  }
}
