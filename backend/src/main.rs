use anyhow::Result;

mod db;
mod router;

#[tokio::main]
async fn main() -> Result<()> {
  dotenvy::dotenv()?;

  let db = db::init_db::init_db().await?;

  let app = router::get_router()
    .with_state(db);

  let listener = tokio::net::TcpListener::bind("127.0.0.1:3000")
    .await
    .unwrap();

  println!("listening on {}", listener.local_addr().unwrap());

  axum::serve(listener, app).await.unwrap();

  Ok(())
}
