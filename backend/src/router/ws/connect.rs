use std::sync::atomic::{AtomicU64, Ordering};

use serde::Deserialize;
use axum::{
  extract::{
    ws::{WebSocket, WebSocketUpgrade},
    State, Query,
  },
  response::IntoResponse,
};
use futures::StreamExt;

use crate::SharedState;
use super::event_handler::register_event_handler;

static CONNECTION_ID_SEQ: AtomicU64 = AtomicU64::new(0);

pub async fn connect(
  ws: WebSocketUpgrade,
  State(state): State<SharedState>,
  Query(query): Query<ConnectQuery>,
) -> impl IntoResponse {
  ws.on_upgrade(move |socket| {
    websocket(socket, state, query.user_id)
  })
}

async fn websocket(socket: WebSocket, state: SharedState, user_id: i32) {
  let connection_id = CONNECTION_ID_SEQ.fetch_add(1, Ordering::Relaxed);
  let (sender, receiver) = socket.split();

  println!("got connection from {user_id} (id: {connection_id})");

  register_event_handler(user_id, connection_id, receiver, state.clone());

  // TODO: auth, keepalive

  let mut users = state.users.lock().await;
  let user = users.entry(user_id).or_default();
  user.insert(connection_id, sender);
  println!("users: {users:?}");
}


#[derive(Deserialize)]
pub struct ConnectQuery {
  user_id: i32,
}