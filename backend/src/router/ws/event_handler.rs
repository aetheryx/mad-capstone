use axum::extract::ws::{Message, WebSocket};
use futures::{stream::SplitStream, StreamExt, SinkExt};

use super::call_handler::*;
use crate::{ws::ClientEvent, SharedState};

pub fn register_event_handler(
  user_id: i32,
  connection_id: u64,
  mut receiver: SplitStream<WebSocket>,
  state: SharedState
) {
  tokio::spawn(async move {
    while let Some(result) = receiver.next().await {
      match result {
        Ok(Message::Text(text)) =>
          handle_event(user_id, text, state.clone()).await?,

        Ok(Message::Close(_)) | Err(_) =>
          handle_close(user_id, connection_id, state.clone()).await?,

        Ok(_) => (),
      }
    }

    anyhow::Ok(())
  });
}

async fn handle_close(
  user_id: i32,
  connection_id: u64,
  state: SharedState
) -> anyhow::Result<()> {
  println!("handling close {user_id} {connection_id}");

  let mut users = state.users.lock().await;
  let Some(user_connections) = users.get_mut(&user_id) else {
    return Ok(());
  };

  let Some(connection) = user_connections.get_mut(&connection_id) else {
    return Ok(());
  };

  connection.close().await?;
  user_connections.remove(&connection_id);

  Ok(())
}

async fn handle_event(
  user_id: i32,
  text: String,
  state: SharedState,
) -> anyhow::Result<()> {
  let event: ClientEvent = serde_json::from_str(text.as_str())?;
  println!("got websocket event from: {user_id} {event:?}");

  match event {
    ClientEvent::CallOffer(offer) =>
      handle_call_offer(user_id, offer, state).await?,

    ClientEvent::CallResponse(resp) =>
      handle_call_response(resp, state).await?,

    ClientEvent::WebRTCPayload(payload) =>
      handle_webrtc_payload(user_id, payload, state).await?,

    ClientEvent::CallHangUp(call_id) =>
      handle_call_hangup(user_id, call_id, state).await?,
  };

  Ok(())
}
