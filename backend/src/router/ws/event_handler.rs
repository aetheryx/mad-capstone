use axum::extract::ws::{Message, WebSocket};
use futures::{stream::SplitStream, StreamExt};

use super::call_handler::*;
use crate::{ws::ClientEvent, SharedState};

pub fn register_event_handler(id: i32, mut receiver: SplitStream<WebSocket>, state: SharedState) {
  tokio::spawn(async move {
    while let Some(Ok(Message::Text(text))) = receiver.next().await {
      let event: ClientEvent = serde_json::from_str(text.as_str())?;
      handle_event(id, event, state.clone()).await?;
    }

    anyhow::Ok(())
  });
}

async fn handle_event(
  id: i32,
  event: ClientEvent,
  state: SharedState,
) -> anyhow::Result<()> {
  println!("got websocket event from: {id} {event:?}");

  match event {
    ClientEvent::CallOffer(offer) => handle_call_offer(id, offer, state).await?,
    ClientEvent::CallResponse(resp) => handle_call_response(id, resp, state).await?,
    ClientEvent::WebRTCPayload(payload) => handle_webrtc_payload(id, payload, state).await?,
  }

  Ok(())
}
