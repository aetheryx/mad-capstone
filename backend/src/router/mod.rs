use axum::Router;
use sea_orm::DatabaseConnection;

use self::auth::auth_router;

mod auth;
mod app_error;
pub use app_error::AppError;

pub fn get_router() -> Router<DatabaseConnection> {
  Router::new().nest("/auth", auth_router())
}
