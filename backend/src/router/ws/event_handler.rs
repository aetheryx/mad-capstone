use axum::extract::ws::{Message, WebSocket};
use futures::{stream::SplitStream, StreamExt};

use super::call_handler::*;
use crate::{ws::IncomingWebsocketEvent, SharedState};

pub fn register_event_handler(id: i32, mut receiver: SplitStream<WebSocket>, state: SharedState) {
  tokio::spawn(async move {
    while let Some(Ok(Message::Text(text))) = receiver.next().await {
      let event: IncomingWebsocketEvent = serde_json::from_str(text.as_str())?;
      handle_event(id, event, state.clone()).await?;
    }

    anyhow::Ok(())
  });
}

async fn handle_event(
  id: i32,
  event: IncomingWebsocketEvent,
  state: SharedState,
) -> anyhow::Result<()> {
  println!("got websocket event from: {id} {event:?}");

  match event {
    IncomingWebsocketEvent::CallOffer(offer) => handle_call_offer(offer, state).await?,
    IncomingWebsocketEvent::CallResponse(resp) => handle_call_response(id, resp, state).await?,
    IncomingWebsocketEvent::WebRTCPayload(payload) => handle_webrtc_payload(id, payload, state).await?,
  }

  Ok(())
}
