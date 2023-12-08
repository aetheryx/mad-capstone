use axum::{Router, response::Html, extract::State, routing};
use sea_orm::DatabaseConnection;

pub fn auth_router() -> Router<DatabaseConnection> {
  Router::new()
    .route("/signup", routing::post(auth_signup))
}

async fn auth_signup(
  State(db): State<DatabaseConnection>
) -> Html<&'static str> {
  Html("ah")
}
