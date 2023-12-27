#[macro_use]
extern crate lazy_static;

use anyhow::Result;
use axum::extract::ws::{Message, WebSocket};
use futures::{lock::Mutex, stream::SplitSink};
use listenfd::ListenFd;
use sea_orm::DatabaseConnection;
use std::{collections::HashMap, sync::Arc};
use tokio::net::TcpListener;

mod db;
mod router;
mod util;
mod ws;

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
  let clients = Mutex::default();

  let app_state = AppState { db, clients };
  let shared_state = Arc::new(app_state);

  let app = router::get_router().with_state(shared_state);
  axum::serve(listener, app).await.unwrap();

  Ok(())
}

struct AppState {
  db: DatabaseConnection,
  clients: Mutex<HashMap<i32, Vec<SplitSink<WebSocket, Message>>>>,
}

type SharedState = Arc<AppState>;
