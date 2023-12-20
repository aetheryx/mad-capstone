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

pub async fn connect(
  ws: WebSocketUpgrade,
  State(state): State<SharedState>,
  Query(query): Query<ConnectQuery>,
) -> impl IntoResponse {
  ws.on_upgrade(move |socket| {
    websocket(socket, state, query.user_id)
  })
}

async fn websocket(socket: WebSocket, state: SharedState, id: i32) {
  let (sender, _) = socket.split();

  // TODO: auth, keepalive, handle close

  let mut clients = state.clients.lock().await;
  clients.insert(id, sender);
}


#[derive(Deserialize)]
pub struct ConnectQuery {
  user_id: i32,
}