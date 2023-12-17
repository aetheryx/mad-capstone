#[macro_use]
extern crate lazy_static;

use anyhow::Result;
use listenfd::ListenFd;
use tokio::net::TcpListener;

mod db;
mod router;
mod util;

#[tokio::main]
async fn main() -> Result<()> {
  dotenvy::dotenv()?;

  let mut listenfd = ListenFd::from_env();

  let listener = match listenfd.take_tcp_listener(0).unwrap() {
    Some(listener) => TcpListener::from_std(listener).unwrap(),
    None => TcpListener::bind("127.0.0.1:3000").await.unwrap(),
  };

  println!("listening on {}", listener.local_addr().unwrap());

  let db = db::init_db::init_db().await?;
  let app = router::get_router().with_state(db);
  axum::serve(listener, app).await.unwrap();

  Ok(())
}
