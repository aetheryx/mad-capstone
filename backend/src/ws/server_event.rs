use crate::{db::entities::*, SharedState};
use axum::extract::ws::Message;
use futures::SinkExt;
use serde::Serialize;

use super::call_state::{CallResponse, WebRTCPayload, OutgoingCallOffer};

#[derive(Serialize, Debug)]
#[serde(tag = "event", content = "data")]
#[typeshare::typeshare]
pub enum ServerEvent<'a> {
  MessageCreate(&'a conversation_message::Model),
  CallOffer(OutgoingCallOffer),
  CallResponse(CallResponse),
  WebRTCPayload(WebRTCPayload)
}

impl<'a> ServerEvent<'a> {
  pub async fn send_to(&self, state: &SharedState, id: i32) -> anyhow::Result<()> {
    let msg = Message::Binary(serde_json::to_vec(&self)?);

    let mut clients = state.clients.lock().await;
    if let Some(client) = clients.get_mut(&id) {
      client.send(msg).await?;
    }

    Ok(())
  }
}
