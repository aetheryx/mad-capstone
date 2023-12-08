use axum::{
  http::StatusCode,
  response::{IntoResponse, Response}, Json,
};
use serde_json::json;

pub enum AppErrors {
  AppError(AppError),
  ApiError(ApiError)
}

pub struct AppError(pub anyhow::Error);

impl IntoResponse for AppErrors {
  fn into_response(self) -> Response {
    match self {
      AppErrors::AppError(e) => (
        StatusCode::INTERNAL_SERVER_ERROR,
        format!("Something went wrong: {}", e.0),
      )
        .into_response(),

      AppErrors::ApiError(e) => (
        e.status,
        e.message
      ).into_response()
    }
  }
}

impl<E> From<E> for AppErrors
where
  E: Into<anyhow::Error>,
{
  fn from(err: E) -> Self {
    AppErrors::AppError(AppError(err.into()))
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
