use axum::Router;
use sea_orm::DatabaseConnection;

use self::auth::auth_router;

mod auth;

pub fn get_router() -> Router<DatabaseConnection> {
  Router::new()
    .nest("/auth", auth_router())
}