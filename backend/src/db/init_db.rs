use sea_orm::{Database, DatabaseConnection};
use std::env;

pub async fn init_db() -> anyhow::Result<DatabaseConnection> {
  let database_url = env::var("DATABASE_URL")
    .expect("DATABASE_URL not set");

  let db = Database::connect(database_url).await?;
  Ok(db)
}

