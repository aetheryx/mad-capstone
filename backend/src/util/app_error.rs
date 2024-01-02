use axum::{
  http::StatusCode,
  response::{IntoResponse, Response},
  Json,
};
use serde_json::json;

pub enum HttpError {
  InternalServerError(anyhow::Error),
  Status(StatusCode),
}

// implement `?` operator
impl<E> From<E> for HttpError
where
  E: Into<anyhow::Error>,
{
  fn from(err: E) -> Self {
    HttpError::InternalServerError(err.into())
  }
}

// map HttpError to axum Response
impl IntoResponse for HttpError {
  fn into_response(self) -> Response {
    let (code, message) = match self {
      Self::InternalServerError(e) => {
        println!("error: {e:?}");
        
        (
          StatusCode::INTERNAL_SERVER_ERROR,
          format!("Something went wrong: {}", e),
        )
      },
      Self::Status(code) => (
        code,
        code.canonical_reason().unwrap_or("Unknown").into()
      ),
    };

    let payload = json!({
      "status": code.as_u16(),
      "message": message
    });

    (code, Json(payload)).into_response()
  }
}


pub type HttpResult<T> = anyhow::Result<Json<T>, HttpError>;
